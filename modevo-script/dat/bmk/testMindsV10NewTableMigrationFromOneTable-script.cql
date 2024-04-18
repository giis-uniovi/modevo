FOR $1,$2 = SELECT hashtag, user_guid FROM user_hashtags
INSERT INTO hidden_hashtags(hashtag, admin_guid) VALUES ($1, $2);
