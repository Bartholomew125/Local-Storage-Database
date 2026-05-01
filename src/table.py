from datetime import datetime
import io
from typing import Any, override
from PIL.Image import Resampling
from PIL.ImageFile import Image as PILImage
import pyvips

from src.imageutils import ImageUtils
from src.database import Database
from src.utils import stringify_cursor_result
from src.image import Image

class Table:

    name: str
    labels: list[str]
    db: Database

    buffering: bool
    buffered_args: list[Any]
    buffer_size: int
    
    def __init__(self, name: str, labels: list[str], db: Database):
        """
        Initialize a new table.
        """
        self.name = name
        self.labels = labels
        self.db = db
        self.buffered_args = []
        self.buffering = False
        self.buffer_size = 0

    def buffer(self, size: int = 10) -> None:
        """
        Enable query buffering of the given size, so the database is only
        queried whenever size inserts have been made.
        """
        self.buffering = True
        self.buffer_size = size

    def flush_buffer(self) -> str:
        """
        Flush the contents of the argument buffer and query the database with
        them.
        """
        result = self.insert_all(*self.buffered_args)
        self.buffered_args = []
        return result

    def insert(self, *args: Any) -> str | None:
        """
        Insert the given values as a new row in the table. They must match with
        the table schema. Returns a string representation of the inserted row.
        If buffering is enabled, no insert will happen untill the buffer size
        is reached.
        """
        if self.buffering:
            self.buffered_args.append(args)
            if self.buffer_size == len(self.buffered_args):
                return self.flush_buffer()
            return None

        placeholders = ", ".join("?" * len(args))
        query = f"INSERT INTO {self.name} VALUES ({placeholders})"
        cur = self.db.query(query, args)
        self.db.commit()
        return stringify_cursor_result([args])

    def insert_all(self, *args: list[Any]) -> str:
        """
        Insert the given values as a new rows in the table. They must match
        with the table schema. Returns a string representation of the inserted
        rows.
        """
        placeholders = "(" + ", ".join("?" * len(args[0])) + ")"
        all_placeholders = ", ".join(placeholders for _ in args)
        query = f"INSERT INTO {self.name} VALUES {all_placeholders}"
        flat_args = [val for row in args for val in row]
        self.db.connection.execute(query, flat_args)
        self.db.commit()
        return stringify_cursor_result(list(args))

    def get_n_rows(self, n: int, sort_by: str | None = None) -> list[Any]:
        """
        Return all rows from this table.
        """
        return self.get_n_rows_with_offset(n, 0, sort_by)

    def get_n_rows_with_offset(self,
                               n: int,
                               offset: int, 
                               sort_by: str | None = None,
                               desending: bool = True
                               ) -> list[Any]:
        """
        Return all rows from this table, offset by the given offset.
        """
        sorting = f"ORDER BY {sort_by} {'DESC' if desending else 'ASC'} NULLS LAST"
        query = f"""
        SELECT {', '.join(self.labels)}
        FROM {self.name}
        {sorting if sort_by else ''}
        LIMIT {n}
        OFFSET {offset}
        """
        return self.db.query(query).fetchall()

    def get_all(self) -> list[Any]:
        """
        Return all rows from this table.
        """
        query = f"SELECT {', '.join(self.labels)} FROM {self.name}"
        return self.db.query(query).fetchall()

    @override
    def __repr__(self) -> str:
        query = f"SELECT * FROM {self.name}"
        cur = self.db.query(query)
        return stringify_cursor_result(cur.fetchall())

class ImagesTable(Table):

    def __init__(self, db: Database):
        super().__init__("images", [
            "id",
            "title",
            "taken_at",
            "image_data",
            "width",
            "height",
            "thumbnail",
            "mimetype"
            ], db)

    def insert_image(self, image: Image, title: str | None = None) -> str | None:
        """
        Insert a new image into the images table. Takes the image, and an
        optional title of the image.
        """
        meta = ImageUtils.Meta(image)
        id = ImageUtils.make_psudo_id(image)
        taken_at = meta.get_datetime()
        mimetype = meta.get_mimetype()
        width, heigth = meta.get_size()

        with open(image.path, "rb") as image_reader:
            image_data = image_reader.read();

        img = pyvips.Image.thumbnail(image.path, 200, height=200)
        thumbnail = img.write_to_buffer(".jpg[Q=80]")

        return self.insert(id, title, taken_at, image_data, width, heigth, thumbnail, mimetype)

    def get_image_blob_and_type(self, id: str = "") -> Any:
        query = f"""
        SELECT image_data, mimetype
        FROM {self.name}
        WHERE id = ?
        """
        return self.db.query(query, (id,)).fetchone()

    def get_thumbnail_blob(self, id: str = "") -> Any:
        query = f"""
        SELECT thumbnail
        FROM {self.name}
        WHERE id = ?
        """
        return self.db.query(query, (id,)).fetchone()


