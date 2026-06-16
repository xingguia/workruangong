import pymysql
from contextlib import contextmanager
from config import DB_CONFIG

def get_connection():
    return pymysql.connect(**DB_CONFIG)

@contextmanager
def get_db():
    conn = get_connection()
    try:
        yield conn
        conn.commit()
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()
