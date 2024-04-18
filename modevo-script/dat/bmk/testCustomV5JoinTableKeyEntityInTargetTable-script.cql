FOR $1,$2 = SELECT idAuthor, idBook FROM table2
$3 = SELECT title FROM table1 WHERE idBook=$2 ALLOW FILTERING
INSERT INTO table2(idAuthor, idBook, title) VALUES ($1, $2, $3);
