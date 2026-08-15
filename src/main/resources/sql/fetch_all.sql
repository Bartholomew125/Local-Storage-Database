SELECT *
FROM (
    SELECT
        v.id,
        v.title,
        v.taken_at,
        v.path,
        v.width,
        v.height,
        v.duration,
        v.mimetype,
        v.views,
        v.latitude,
        v.latitudeSpan,
        v.longitude,
        v.longitudeSpan,
        v.altitude
    FROM videos AS v
    JOIN user_videos AS uv
    ON v.id = uv.video_id
    WHERE uv.user_id = ?
        AND v.deleted_at IS NULL
    UNION
    SELECT
        i.id,
        i.title,
        i.taken_at,
        i.path,
        i.width,
        i.height,
        NULL,
        i.mimetype,
        i.views,
        i.latitude,
        i.latitudeSpan,
        i.longitude,
        i.longitudeSpan,
        i.altitude
    FROM images AS i
    JOIN user_images AS ui
    ON i.id = ui.image_id
    WHERE ui.user_id = ?
        AND i.deleted_at IS NULL
)
ORDER BY %s %s NULLS LAST
LIMIT ? 
OFFSET ?
