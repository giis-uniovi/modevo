FOR $1 = SELECT key FROM user
INSERT INTO sendwyre_accounts(user_guid) VALUES ($1);
