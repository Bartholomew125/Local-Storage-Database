from dataclasses import dataclass


class config:
    """
    Configuration class for the entire project.
    """
    project_path:  str = "/home/andreas/Documents/LocalStorage/"
    input_path:    str = project_path+"input"
    database_path: str = project_path+"db/mydatabase.db"

