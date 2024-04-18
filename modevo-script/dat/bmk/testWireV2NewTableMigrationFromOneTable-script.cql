FOR $1,$2 = SELECT team, user FROM user_team
INSERT INTO billing_team_member(team, user) VALUES ($1, $2);
