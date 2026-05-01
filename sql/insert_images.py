# import pdb; pdb.set_trace()
import os
import sqlite3
import PIL
from tqdm import tqdm

from src.image import Image
from src.database import Database
from src.table import ImagesTable

db = Database("db/mydatabase.db")
images = ImagesTable(db)
images.buffer(1000)

dirpath, dirnames, filenames = list(os.walk("input/"))[0]
for file in tqdm(filenames):
    image_path = "input/"+file
    try:
        image = Image(image_path)
        try:
            result = images.insert_image(image)
            if result is not None:
                tqdm.write(result)
        except sqlite3.IntegrityError as e:
            tqdm.write(str(e))
    except PIL.UnidentifiedImageError as e:
        tqdm.write(str(e))

print("Flushed:", images.flush_buffer())
