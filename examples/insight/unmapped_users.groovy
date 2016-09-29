/**
 * @author IWEERARATHNA
 */
def innQ = $DSL.select {

    TARGET (TABLE("Release_Module_Dev_Metric").alias("rmdm"))

    JOIN {
        TARGET() INNER_JOIN TABLE("scm_user").alias("su") ON rmdm.scm_user_id, su.scm_user_id \
            INNER_JOIN TABLE("Scm_User_Team").alias("sut") ON rmdm.scm_user_id, sut.scm_user_id \
            INNER_JOIN TABLE("Module").alias("m") ON rmdm.module_id, m.module_id
    }

    FETCH ($IMPORT("partials/selectprojection"))

    WHERE {
        ALL {
            EQ(m.code_branch_id, 1)
            EQ(rmdm.release_id, PARAM("releaseId"))
            EQ(rmdm.org_unit_id, 1)
            GTE(sut.date, 1474684200000)
            LTE(sut.date, 1474684201000)

            if ($SESSION.hello?.abc != null) {
                EQ(m.is_removed, PARAM("memem"))
            }
            //EQ(sut.team_id, 1)

        }
    }

    GROUP_BY (su.scm_user_id)

    TOP 1
}

$DSL.select {

    TARGET (TABLE("Devs").alias("dev"))

//    JOIN {
//        TARGET() INNER_JOIN TABLE("Hello").alias("he") ON (dev.all, he.id)
//    }
    JOINING {
        INNER_JOIN (TABLE("Hello").alias("he")) ON (dev.all, he.id)
        if ($SESSION.hello) {
            INNER_JOIN(TABLE("User").alias("us")) ON(he.id, us.id)
        }
    }

    FETCH (dev.all.alias("xxx"),
            IFNULL(dev.a, PARAM("threshold")),
            COUNT(CASE {WHEN {NOTNULL (dev.scmUserId) } THEN {1} ELSE {0}} ).alias("mappedUserCount"),
            SUM(CASE {WHEN {ISNULL (dev.scmUserId) } THEN {1} ELSE {0}} ).alias("unmappedUserCount"))


    WHERE {
        //IN (dev.users, innQ)
        EQ (dev.all, 1)
        AND (EQ (1,1))
    }
}
