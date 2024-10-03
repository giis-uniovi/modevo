FOR $1,$2 = SELECT parent_guid, id FROM comments WHERE parent_guid>'f' ALLOW FILTERING
FOR $3,$4 = SELECT parent_guid, id FROM comments WHERE parent_guid>'p' ALLOW FILTERING
FOR $5,$6 = SELECT parent_guid, id FROM comments WHERE parent_guid<'p' ALLOW FILTERING
INSERT INTO comments(parent_guid_c1, id) VALUES ($1, $2);
INSERT INTO comments(parent_guid_c2, id) VALUES ($3, $4);
INSERT INTO comments(parent_guid_c3, id) VALUES ($5, $6);
