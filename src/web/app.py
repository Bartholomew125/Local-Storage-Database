import time
from flask import Flask, Response, jsonify, make_response, redirect, request, render_template, send_from_directory, url_for
import os

from src.database import Database
from src.table import ImagesTable
from config import config 

app = Flask(__name__)

db = Database(config.database_path)
images = ImagesTable(db)

@app.route("/")
def index():
    return render_template("gallery.html")

@app.route("/api/images")
def api_images():
    page = request.args.get("page", 0, type=int)
    limit = 20
    offset = page * limit
    sort_by = "taken_at"
    rows = images.get_n_rows_with_offset(n=limit, offset=offset, sort_by=sort_by)
    return jsonify(
        [
            {
                "id": r[0],
                "title": r[1],
                "taken_at": r[2],
                "width": r[4],
                "height": r[5]
            }
            for r in rows
        ]
    )

@app.route("/api/images/<image_id>")
def api_image_blob(image_id):
    res = images.get_image_blob_and_type(id=image_id)
    if res is None:
        return "Not found", 404
    blob, mimetype = res
    return Response(blob, mimetype=mimetype)

@app.route("/api/images/<image_id>/thumbnail")
def api_thumbnail(image_id):
    thumbnail = images.get_thumbnail_blob(id=image_id)
    if thumbnail is None:
        return "Not found", 404
    return Response(thumbnail[0], mimetype="image/jpeg")

@app.route("/images/<filename>")
def get_image(filename):
    return send_from_directory(config.input_path, filename)

if __name__ == "__main__":
    app.run(debug=True, threaded=True, host='0.0.0.0')
