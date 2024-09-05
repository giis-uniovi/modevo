FOR $1,$2,$3,$4,$5,$6 = SELECT id, tenant_id, customer_id, name, type, device_id FROM entity_view_devpk
$7 = SELECT type FROM device WHERE id=$6 ALLOW FILTERING
INSERT INTO entity_view_devpk(id, tenant_id, customer_id, name, type, device_id, dev_type) VALUES ($1, $2, $3, $4, $5, $6, $7);
