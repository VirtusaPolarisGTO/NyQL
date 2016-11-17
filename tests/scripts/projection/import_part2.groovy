/**
 * @author IWEERARATHNA
 */
$DSL.$q {

    EXPECT (OTHER().alias("ac"))

    FETCH (ac.id,
            IFNULL(ac.year, 2016).alias("thisYear"),
            ac.title.alias("nickName"))

}