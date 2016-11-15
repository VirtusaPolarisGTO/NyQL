/**
 * @author IWEERARATHNA
 */
$DSL.$q {
    EXPECT(Film.alias("f"))

    DATA (
            "importedCol1" : PARAM("iparam1"),
            "importedCol2" : STR("const"),
            "importedCol3" : PARAM("iparam2")
    )
}