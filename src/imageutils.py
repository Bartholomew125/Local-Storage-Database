import mimetypes
import os
from typing import override
from PIL.Image import Exif
from PIL.ExifTags import TAGS
import hashlib

from src.image import Image

class ImageUtils:

    @staticmethod
    def hash(image: Image) -> str:
        return hashlib.sha1(image.tobytes()).hexdigest()

    @staticmethod
    def make_psudo_id(image: Image) -> str:
        stat = os.stat(image.path)
        return f"{stat.st_size}-{stat.st_ctime}"


    class Meta:
        """
        Class for extracting metadata from images.
        """

        image: Image
        exifdata: Exif

        def __init__(self, image: Image) -> None:
            self.image = image
            self.exifdata = image.getexif()

        def get_datetime(self) -> str:
            """
            Get the datetime of the image.
            """
            # Check basic tags first
            for tag_id in self.exifdata:
                tag = TAGS.get(tag_id, tag_id)
                if tag in ("DateTimeOriginal", "DateTimeDigitized", "DateTime"):
                    data = self.exifdata.get(tag_id)
                    if data:
                        return data.decode() if isinstance(data, bytes) else str(data)

            # Fall back to EXIF sub-IFD (where DateTimeOriginal often lives)
            exif_ifd = self.exifdata.get_ifd(0x8769)
            for tag_id, data in exif_ifd.items():
                tag = TAGS.get(tag_id, tag_id)
                if tag in ("DateTimeOriginal", "DateTimeDigitized"):
                    if data:
                        return data.decode() if isinstance(data, bytes) else str(data)

            return ""

        def get_mimetype(self) -> str | None:
            """
            Return the guessed mimetype of the image.
            """
            path = str(self.image.filename)
            mimetype, _ = mimetypes.guess_type(path)
            return mimetype
        
        def get_size(self) -> tuple[int, int]:
            """
            Return the size of the image as (width,height).
            """
            return (self.image.width, self.image.height)
            
        @override
        def __repr__(self) -> str:
            string: str = ""
            for tag_id in self.exifdata:
                tag = TAGS.get(tag_id, tag_id)
                data = self.exifdata.get(tag_id)
                if isinstance(data, bytes):
                    data = data.decode()
                string += f"{tag:25}: {data}\n"
            string += f"{'mimetype':25}: {self.get_mimetype()}"
            return string
