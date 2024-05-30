import os
import time
import boto3
import random
import logging
import pandas as pd
import pyarrow as pa
from google.cloud import bigquery
from google.cloud import bigquery_storage
from google.oauth2 import service_account

from utils import manager_kinesis, manager_bq

logging.basicConfig(level=logging.INFO)

credentials = service_account.Credentials.from_service_account_file(
    '<service_account_keys_file>.json'
)

bq_client = bigquery.Client(credentials=credentials)
bq_storage_client = bigquery_storage.BigQueryReadClient(credentials=credentials)

project_id = '<project_name>'
dataset_id = '<dataset_id_bq>'
table_id = 'transactionsv2'
table = f"projects/{project_id}/datasets/{dataset_id}/tables/{table_id}"

# AWS Kinesis Stream to send data

session_aws = boto3.session.Session(profile_name="streaming_dev")

kinesis_client = session_aws.client('kinesis', region_name='us-east-1')

aws_kinesis_stream_source = "example-stream"


# Function to convert rows to DataFrame
def rows_to_dataframe(rows):
    df = pd.DataFrame([dict(row) for row in rows])
    return df


page_size = 250
offset = 0
timeout = 600 # seconds

if __name__ == "__main__":

    while True:

        start_time = time.time()

        read_session = manager_bq.create_read_session(bq_storage_client, project_id, table)
        rows = manager_bq.read_rows_with_timeout(bq_storage_client, read_session)

        if not rows:
            logging.info("No more rows to read or an error occurred.")
            break

        if time.time() - start_time > timeout:
            logging.error("Reading rows finished with timeout.")
            break

        # chunk list of rows
        for i in range(0, len(rows), page_size):

            rows_chunk = rows[i:i + page_size]
                
            serialized_df = rows_to_dataframe(rows_chunk)

            print(serialized_df.shape[0])

            messages_to_kinesis = manager_kinesis.convert_dataframe_to_messages(serialized_df, 'block_timestamp')
            manager_kinesis.send_messages_to_kinesis(kinesis_client, aws_kinesis_stream_source, messages_to_kinesis)
            
            logging.info(serialized_df)

            print(f"Data sent to Kinesis stream in chunks of {page_size} rows.")
            
            time.sleep(random.uniform(1, 2))
            
            if len(rows_chunk) < page_size:
                break
        
        time.sleep(random.uniform(1, 1.5))

    logging.info("Script finished successfully.")
