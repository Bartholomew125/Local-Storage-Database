import sqlite3
from typing import Any
import threading

class Database:

    db_path: str
    connection: sqlite3.Connection
    _local: threading.local

    def __init__(self, db_path: str) -> None:
        """
        Initialize a new database. By default it is not connected. Call connect
        to connect to the database.
        """
        self.db_path = db_path
        self._local = threading.local()

    @property
    def connection(self):
        """
        Establish a connection.
        """
        if not hasattr(self._local, "connection"):
            self._local.connection = sqlite3.connect(self.db_path)
        return self._local.connection

    def query(self, query: str, params: tuple[Any,...]=()) -> sqlite3.Cursor:
        """
        Execute the given query with the optional parameters, and return a
        cursor of the result.
        """
        return self.connection.cursor().execute(query, params)
    
    def commit(self) -> None:
        """
        Commit the transaction to the database.
        """
        self.connection.commit()

    def close(self) -> None:
        """
        Close the connection to the database.
        """
        if hasattr(self._local, "connection"):
            self._local.connection.close()
            del self._local.connection

    def __enter__(self):
        return self

    def __exit__(self, *args) -> None:
        self.close()       
