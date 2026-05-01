DROP TABLE IF EXISTS images;

CREATE TABLE images (
    id          CHAR(40)        NOT NULL,
    title       VARCHAR(256),
    taken_at    DATETIME,
    image_data  BLOB            NOT NULL,
    width       INTEGER         NOT NULL,
    height      INTEGER         NOT NULL,
    thumbnail   BLOB            NOT NULL,
    mimetype    VARCHAR(64),
    PRIMARY KEY(id)
);

CREATE INDEX idx_images_id       ON images(id);
CREATE INDEX idx_images_taken_at ON images(taken_at);
