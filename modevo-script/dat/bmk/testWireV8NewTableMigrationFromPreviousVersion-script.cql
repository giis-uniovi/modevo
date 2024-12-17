FOR $1 = SELECT id FROM scim_user
INSERT INTO scim_user_times(uid) VALUES ($1);
