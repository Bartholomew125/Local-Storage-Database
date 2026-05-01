from src.database import Database
import os

db_path = "db/mydatabase.db"

if os.path.exists(db_path):
    os.remove(db_path)

with open("sql/init.sql", "r") as sql:
    with Database(db_path) as db:
        db.connection.executescript(sql.read())
