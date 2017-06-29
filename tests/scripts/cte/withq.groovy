
[
        $DSL.cte {
            WITH ("Film_Table", ["id", "title", "year"]) {
                TARGET (Film.alias('f'))
                WHERE {
                    GTE (f.year, 2016)
                }
            }

            SELECT {
                TARGET (Film_Table.alias('ft'))
            }
        },
        [
                mysql: 'WITH `Film_Table` (id, title, year) AS (SELECT * FROM `Film` f WHERE f.year >= 2016) ' +
                        'SELECT * FROM `Film_Table` ft'
        ],

        $DSL.cte {
            WITH ("Film_Table", ["id", "title", "year"]) {
                TARGET (Film.alias('f'))
                WHERE {
                    GTE (f.year, PARAM('theYear'))
                }
            }

            SELECT {
                TARGET (Film_Table.alias('ft'))
            }
        },
        [
                mysql: ['WITH `Film_Table` (id, title, year) AS (SELECT * FROM `Film` f WHERE f.year >= ?) ' +
                        'SELECT * FROM `Film_Table` ft', ['theYear']]
        ],

        $DSL.cte {
            WITH ("Film_Table", ["id", "title", "year"]) {
                TARGET (Film.alias('f'))
                WHERE {
                    GTE (f.year, PARAM('theYear'))
                }
            }
            WITH ('Actor_Table') {
                TARGET (Actor.alias('ac'))
                WHERE {
                    EQ (ac.died, false)
                }
            }

            SELECT {
                TARGET (Film_Table.alias('ft'))
            }
        },
        [
                mysql: ['WITH `Film_Table` (id, title, year) AS (SELECT * FROM `Film` f WHERE f.year >= ?), ' +
                                '`Actor_Table` AS (SELECT * FROM `Actor` ac WHERE ac.died = 0) ' +
                                'SELECT * FROM `Film_Table` ft', ['theYear']]
        ],

        $DSL.cte {
            WITH ("Film_Table", ["id", "title", "year"]) {
                TARGET (Film.alias('f'))
                WHERE {
                    GTE (f.year, PARAM('theYear'))
                }
            }
            WITH ('Actor_Table') {
                TARGET (Actor.alias('ac'))
                WHERE {
                    EQ (ac.died, PARAM('isDied'))
                }
            }

            SELECT {
                TARGET (Film_Table.alias('ft'))
                JOIN {
                    INNER_JOIN (Actor_Table.alias('act')) ON act.film_id, ft.id
                }
            }
        },
        [
                mysql: ['WITH `Film_Table` (id, title, year) AS (SELECT * FROM `Film` f WHERE f.year >= ?), ' +
                                '`Actor_Table` AS (SELECT * FROM `Actor` ac WHERE ac.died = ?) ' +
                                'SELECT * FROM `Film_Table` ft INNER JOIN `Actor_Table` act ON act.film_id = ft.id',
                        ['theYear', 'isDied']]
        ],
]