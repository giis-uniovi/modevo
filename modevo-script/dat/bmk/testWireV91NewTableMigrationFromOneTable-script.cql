FOR $1 = SELECT user FROM user_team
INSERT INTO users_pending_activation(user) VALUES ($1);
