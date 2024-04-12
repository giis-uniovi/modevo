FOR $1,$2 = SELECT user, external FROM scim_external_ids
$3 = SELECT team FROM user_team WHERE user=$1 ALLOW FILTERING
INSERT INTO scim_external(user, external, team) VALUES ($1, $2, $3);
