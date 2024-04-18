FOR $1,$2,$3,$4 = SELECT id, tenant_id, customer_id, device_id FROM entity_view_devpk
$5 = SELECT type FROM device WHERE id=$4 ALLOW FILTERING
INSERT INTO entity_view_devpk(id, tenant_id, customer_id, device_id, dev_type) VALUES ($1, $2, $3, $4, $5);
