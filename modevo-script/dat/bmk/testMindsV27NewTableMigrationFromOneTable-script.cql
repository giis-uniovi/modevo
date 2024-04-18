FOR $1,$2 = SELECT wallet_address, user_guid FROM withholdings
INSERT INTO wire_support_tiers(address, user_guid) VALUES ($1, $2);
