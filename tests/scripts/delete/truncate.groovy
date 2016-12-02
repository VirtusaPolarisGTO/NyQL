/**
 * @author IWEERARATHNA
 */
[
    $DSL.truncate ("Film"),
    [
            mysql:  "TRUNCATE TABLE `Film`",
            pg:     'TRUNCATE TABLE "Film"'
    ]

]