from functools import partial
from typing import Any


def limit_str(string: str, n: int) -> str:
    """
    Limit the given string to n characters. If it is greater than n characters,
    shorten it to n-3 characters and add ... to symbolize its continuation.
    """
    if len(string) > n:
        return string[:n-3]+"..."
    else:
        return string

def stringify_cursor_result(res: list[Any]) -> str:
    limit = partial(limit_str, n=20)
    rows = [
        "(" + ", ".join(
            "<blob>" if isinstance(val, (bytes, memoryview)) else limit(str(val))
            for val in row
        ) + ")"
        for row in res
    ]
    return "\n".join(rows)
