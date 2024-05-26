import os
import json
import time
import base64
import boto3
import hashlib

from botocore.exceptions import ClientError

# create dynamodb resource
dynamodb = boto3.resource('dynamodb')

table = dynamodb.Table('websocket_connections')

endpoint_url = os.getenv('APIGATEWAY_URL')

api_client = boto3.client('apigatewaymanagementapi', endpoint_url = endpoint_url)

def send_to_websocket(connection_id, data):
    """
    Send data to a WebSocket connection.

    :param connection_id: The ID of the WebSocket connection.
    :param data: The data to send.
    """
    api_client.post_to_connection(
        ConnectionId=connection_id,
        Data=json.dumps(data)
    )

def ingest_messages_to_dynamodb(table_name, messages):
    """
    Ingest a batch of messages into the specified DynamoDB table.

    :param table_name: The name of the DynamoDB table.
    :param messages: A list of message dictionaries to insert.
    """
    table = dynamodb.Table(table_name)
    
    with table.batch_writer() as batch:
        for message in messages:
            try:
                batch.put_item(Item=message)
            except ClientError as e:
                print(f"Failed to insert item: {e.response['Error']['Message']}")

def generate_hash_key(data):
    
    if not isinstance(data, bytes):

        data_str = json.dumps(data, sort_keys=True)
        
        hash_key = hashlib.md5(data_str.encode()).hexdigest()

    else:
        hash_key = hashlib.md5(data).hexdigest()
    
    return hash_key

def lambda_handler(event, context):
    clean_records = []

    records = event['Records']

    response = table.scan()
    connection_ids = [item['connectionId'] for item in response['Items']]

    for record in records:
        print(record)
        
        message_str = base64.b64decode(record['kinesis']['data'])
        data = json.loads(message_str)

        data["hashkey_id"] = generate_hash_key(message_str)
        print(data)

        clean_records.append(data)
    
    ingest_messages_to_dynamodb('crypto_analysis', clean_records)

    print(f"Connection IDs: {connection_ids}")

    for record in clean_records:

        for connection_id in connection_ids:
            
            send_to_websocket(connection_id, record)
        
            time.sleep(0.5)

    return { 'statusCode': 200, 'body': json.dumps('Hello from Lambda!') , 'size_of_records': len(clean_records) }