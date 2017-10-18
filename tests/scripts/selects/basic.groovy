/**
 * @author IWEERARATHNA
 */
[
        $DSL.select {
            TARGET (Film.alias("f"))
            DISTINCT_FETCH (f.title)
        },
        [
            mysql: "SELECT DISTINCT f.title FROM `Film` f"
        ],

        $DSL.select {
            TARGET (TABLE("MovieManager", "Film").alias("f"))
            DISTINCT_FETCH (f.title)
        },
        [
                mysql: "SELECT DISTINCT f.title FROM `MovieManager`.`Film` f"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            DISTINCT_FETCH (f.title, f.description)
        },
        [
            mysql: "SELECT DISTINCT f.title, f.description FROM `Film` f"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            FETCH (f.title.alias("title1"), f.title.alias("title2"))
        },
        [
                mysql: "SELECT f.title AS title1, f.title AS title2 FROM `Film` f"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            FETCH (RAW("SUBSTRING_INDEX('Isuru', 'u', -1)"))
        },
        [
                mysql: "SELECT SUBSTRING_INDEX('Isuru', 'u', -1) FROM `Film` f"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            FETCH (RAW("SUBSTRING_INDEX(?, ?)", PARAM("p1"), PARAM("p2")) )
        },
        [
                mysql: ["SELECT SUBSTRING_INDEX(?, ?) FROM `Film` f", ["p1", "p2"]]
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            FETCH (f.title.alias("title"), f.title.alias("title2"))
        },
        [
                mysql: "SELECT f.title AS title, f.title AS title2 FROM `Film` f"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            JOIN {
                INNER_JOIN (Actor.alias("a")) ON a.film_id, f.film_id
            }
            FETCH (f.name.alias("title1"), a.name.alias("title1"))
        },
        [
                mysql: "SELECT f.name AS title1, a.name AS title1 FROM `Film` f INNER JOIN `Actor` a ON a.film_id = f.film_id"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            FETCH (f.rental_duration, COUNT())
            GROUP_BY (f.rental_duration)
            HAVING {
                GT (COUNT(), 200)
            }
        },
        [
            mysql: "SELECT f.rental_duration, COUNT(*) FROM `Film` f GROUP BY f.rental_duration HAVING COUNT(*) > 200"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            FETCH (f.rental_duration.alias("rental duration"), COUNT())
            GROUP_BY (f.rental_duration)
            HAVING {
                GT (COUNT(), 200)
            }
        },
        [
                mysql: "SELECT f.rental_duration AS `rental duration`, COUNT(*) " +
                        "FROM `Film` f GROUP BY `rental duration` HAVING COUNT(*) > 200"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            FETCH (f.rental_duration.alias("rental duration"), COUNT())
            GROUP_BY (ALIAS_REF("rental duration"))
            HAVING {
                GT (COUNT(), 200)
            }
        },
        [
                mysql: "SELECT f.rental_duration AS `rental duration`, COUNT(*) " +
                        "FROM `Film` f GROUP BY `rental duration` HAVING COUNT(*) > 200"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            FETCH (f.rental_duration, COUNT().alias("total"))
            GROUP_BY (f.rental_duration)
            HAVING {
                GT (total, 200)
            }
        },
        [
            mysql: "SELECT f.rental_duration, COUNT(*) AS total FROM `Film` f GROUP BY f.rental_duration HAVING total > 200"
        ],

        $DSL.select {
            TARGET (FilmIncome.alias("f"))
            HAVING {
                GT (AVG(f.gross), 200)
            }
        },
        [
                mysql: "SELECT * FROM `FilmIncome` f HAVING AVG(f.gross) > 200"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            FETCH (COLUMN("rental_duration"), COUNT().alias("total"))
            GROUP_BY (f.rental_duration)
            HAVING {
                GT (total, 200)
            }
        },
        [
            mysql: "SELECT f.rental_duration, COUNT(*) AS total FROM `Film` f GROUP BY f.rental_duration HAVING total > 200"
        ],

        $DSL.select {
            TARGET (Film.alias("f"))
            FETCH (CASE {WHEN { EQ (f.count, PARAM("total")) }
                THEN { BOOLEAN(true) }
                ELSE { BOOLEAN(false) }})
            WHERE {
                EQ (f.count, PARAM("total"))
            }
        },
        [
                mysql:  ["SELECT CASE WHEN f.count = ? THEN 1 ELSE 0 END FROM `Film` f WHERE f.count = ?", ["total", "total"]]
        ],

        $DSL.select {
            TARGET (Sales.alias("s"))
            FETCH (s.year, SUM(s.profit).alias("Profit"))
            GROUP_BY (s.year) ROLLUP()
        },
        [
                mysql: "SELECT s.year, SUM(s.profit) AS Profit FROM `Sales` s GROUP BY s.year WITH ROLLUP"
        ],

        $DSL.select {
            TARGET (Sales.alias("s"))
            FETCH (s.year.alias("theYear"), SUM(s.profit).alias("Profit"))
            GROUP_BY (theYear) ROLLUP()
        },
        [
                mysql: "SELECT s.year AS theYear, SUM(s.profit) AS Profit FROM `Sales` s GROUP BY theYear WITH ROLLUP"
        ],

//        SELECT year, country, product, SUM(profit) AS profit
//        FROM sales
//        GROUP BY year ASC, country ASC, product ASC WITH ROLLUP;

        $DSL.select {
            TARGET (Sales.alias("s"))
            FETCH (s.year.alias("theYear"), SUM(s.profit).alias("Profit"))
            GROUP_BY (ASC(theYear)) ROLLUP()
        },
        [
                mysql: "SELECT s.year AS theYear, SUM(s.profit) AS Profit FROM `Sales` s GROUP BY theYear ASC WITH ROLLUP"
        ],

        $DSL.select {
            TARGET (Sales.alias("s"))
            FETCH (s.year.alias("theYear"), s.country, COUNT().alias("total"))
            GROUP_BY (ASC(theYear), DESC(s.country)) ROLLUP()
        },
        [
                mysql: "SELECT s.year AS theYear, s.country, COUNT(*) AS total FROM `Sales` s " +
                        "GROUP BY theYear ASC, s.country DESC WITH ROLLUP"
        ],
]