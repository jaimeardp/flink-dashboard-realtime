import os
import time
import random
import logging
import pandas as pd
import pyarrow as pa
from datetime import datetime

from google.cloud.bigquery_storage_v1 import types
from google.api_core.exceptions import GoogleAPICallError, RetryError

from utils import manager_random


# Function to create a read session
def create_read_session(bq_storage_client, project_id, table, datetime_str='2024-05-19 03:21:00'): # Pending to use page_size and offset
    read_session = types.ReadSession(
        table=table,
        data_format=types.DataFormat.AVRO,
        read_options=types.ReadSession.TableReadOptions(
            selected_fields=['block_timestamp', 'from_address', 'to_address', 'value', 'transaction_type'], 
            row_restriction=f"block_timestamp > '{datetime_str}'",  # Replace with your actual filter
        )
    )

    parent = f"projects/{project_id}"
    read_session = bq_storage_client.create_read_session(
        parent=parent,
        read_session=read_session,
        max_stream_count=1
    )

    return read_session


def read_rows_with_timeout(bq_storage_client, read_session, timeout=30):
    rows = []
    for stream in read_session.streams:
        print(f"Reading stream: {stream.name} {len(rows)}")
        reader = bq_storage_client.read_rows(stream.name)
        try:
            start_time = time.time()
            for row in reader.rows(read_session):
                row['value'] = int(manager_random.generate_random_number())
                row['block_timestamp'] = datetime.now()  # Fix: Use datetime.now() instead of datetime.utcnow()
                row['asset'] = manager_random.generate_random_crypto_symbol()
                row['transaction_type'] = manager_random.select_transaction_type()
                rows.append(row)
                if time.time() - start_time > timeout:
                    raise TimeoutError("Reading rows took too long.")
            return rows
        except (GoogleAPICallError, RetryError, TimeoutError) as e:
            logging.error(f"Error reading rows: {e}")
            return []