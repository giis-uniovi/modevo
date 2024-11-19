FOR $1,$2,$3,$4 = SELECT title, subtitle, idAuthor, idBook FROM table1
INSERT INTO table1(completetitle, idAuthor, idBook) VALUES ($1+$2, $3, $4);
