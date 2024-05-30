import json
import boto3
import pandas as pd
from typing import List, Tuple


def convert_dataframe_to_messages(dataframe: pd.DataFrame, partition_key_column: str) -> List[Tuple[str, str]]:
    """
    Convert a DataFrame to a list of messages.

    :param dataframe: The DataFrame containing the data.
    :param partition_key_column: The column name to use as the partition key.
    :return: A list of tuples containing (partition_key, message).
    """
    messages = []
    for _, row in dataframe.iterrows():

        messages.append( (str(row[partition_key_column]), row.to_json()) )

    return messages

def send_messages_chunk(kinesis_client, messages_chunk: List[Tuple[str, str]], stream_name):
    records = [
        {'Data': message, 'PartitionKey': partition_key}
        for partition_key, message in messages_chunk
    ]
    try:
        response = kinesis_client.put_records(
            StreamName=stream_name,
            Records=records
        )
        failed_record_count = response['FailedRecordCount']
        if failed_record_count > 0:
            print(f"Failed to send {failed_record_count} records to Kinesis stream")
        else:
            print(f"Successfully sent {len(records)} records to Kinesis stream")
    except Exception as e:
        print(f"Failed to send messages to Kinesis stream: {e}")


def send_messages_to_kinesis(kinesis_client, stream_name: str, messages: List[Tuple[str, str]], chunk_size: int = 500):
    # Send messages in chunks
    for i in range(0, len(messages), chunk_size):
        chunk = messages[i:i + chunk_size]
        send_messages_chunk(kinesis_client, chunk, stream_name)
