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
            TARGET (Film.alias("f"))
            DISTINCT_FETCH (f.title, f.description)
        },
        [
            mysql: "SELECT DISTINCT f.title, f.description FROM `Film` f"
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