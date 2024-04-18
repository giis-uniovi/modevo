FOR $1 = SELECT idBook FROM table2
$2 = SELECT publisher FROM table3 WHERE idBook=$1 ALLOW FILTERING
$3 = SELECT title FROM table1 WHERE idBook=$1 ALLOW FILTERING
INSERT INTO table2(idBook, publisher) VALUES ($1, $2);
INSERT INTO table2(idBook, title) VALUES ($1, $3);
