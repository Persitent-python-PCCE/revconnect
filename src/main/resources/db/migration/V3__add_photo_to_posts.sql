ALTER TABLE posts
    ADD COLUMN photo_url VARCHAR(500);

UPDATE posts
SET photo_url = '/uploads/default.jpg'
WHERE photo_url IS NULL;

ALTER TABLE posts
    MODIFY COLUMN photo_url VARCHAR(500) NOT NULL;

ALTER TABLE posts
    CHANGE COLUMN content caption TEXT NOT NULL;