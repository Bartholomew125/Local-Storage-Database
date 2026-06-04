CREATE TABLE images (
    id              CHAR(40)        NOT NULL,
    title           VARCHAR(256),
    taken_at        DATETIME,
    path            VARCHAR(1024)   NOT NULL,
    width           INTEGER         NOT NULL,
    height          INTEGER         NOT NULL,
    mimetype        VARCHAR(64),
    views           INTEGER         NOT NULL,
    latitude        FLOAT,
    latitudeSpan    Float,
    longitude       FLOAT,
    longitudeSpan   FLOAT,
    altitude        FLOAT,
    PRIMARY KEY(id)
);

CREATE INDEX idx_images_id       ON images(id);
CREATE INDEX idx_images_taken_at ON images(taken_at);

CREATE TABLE videos (
    id              CHAR(40)        NOT NULL,
    title           VARCHAR(256),
    taken_at        DATETIME,
    path            VARCHAR(1024)   NOT NULL,
    width           INTEGER         NOT NULL,
    height          INTEGER         NOT NULL,
    duration        FLOAT           NOT NULL,
    mimetype        VARCHAR(64),
    views           INTEGER         NOT NULL,
    latitude        FLOAT,
    latitudeSpan    Float,
    longitude       FLOAT,
    longitudeSpan   FLOAT,
    altitude        FLOAT,
    PRIMARY KEY(id)
);

CREATE INDEX idx_videos_id       ON videos(id);
CREATE INDEX idx_videos_taken_at ON videos(taken_at);

