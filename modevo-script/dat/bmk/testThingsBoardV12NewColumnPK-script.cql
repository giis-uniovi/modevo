FOR $1,$2,$3,$4,$5 = SELECT id, tenant_id, customer_id, name, type FROM entity_view2_old
$6 = SELECT type FROM device WHERE tenant_id=$2 AND customer_id=$3 ALLOW FILTERING
INSERT INTO entity_view2(id, tenant_id, customer_id, name, type, dev_type) VALUES ($1, $2, $3, $4, $5, $6);
