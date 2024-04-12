FOR $1,$2,$3 = SELECT id, tenant_id, customer_id FROM entity_view
$4 = SELECT type FROM device WHERE tenant_id=$2 AND customer_id=$3 ALLOW FILTERING
INSERT INTO entity_view(id, tenant_id, customer_id, dev_type) VALUES ($1, $2, $3, $4);
