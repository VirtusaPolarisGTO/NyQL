import com.virtusa.gto.insight.nyql.model.units.AParam

/**
 * @author IWEERARATHNA
 */
[
        $DSL.dbFunction ("MyProcedure",
                [
                        $DSL.PARAM("firstP", AParam.ParamScope.IN, "mapper1"),
                        $DSL.PARAM("secondP", AParam.ParamScope.IN, "mapper2"),
                        $DSL.PARAM("thirdP", AParam.ParamScope.OUT, "mapper3")
                ]
        ),
        ["{ CALL MyProcedure(?, ?, ?) }", ["firstP", "secondP", "thirdP"]]
]