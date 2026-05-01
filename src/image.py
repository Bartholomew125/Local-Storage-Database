from PIL import Image as PILImage
from PIL.ImageFile import ImageFile

class Image:
    path: str
    image: ImageFile

    def __init__(self, path: str) -> None:
        self.path = path
        self.image = PILImage.open(path)

    def __getattr__(self, name):
        return getattr(self.image, name)
