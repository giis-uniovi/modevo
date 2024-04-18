FOR $1 = SELECT idBook FROM table2
$2 = SELECT title FROM table1 WHERE idBook=$1 ALLOW FILTERING
INSERT INTO table2(idBook, title) VALUES ($1, $2);
