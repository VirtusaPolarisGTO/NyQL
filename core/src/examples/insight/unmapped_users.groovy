/**
 * @author IWEERARATHNA
 */
def innQ = $DSL.select {

    TARGET (TABLE("Release_Module_Dev_Metric").alias("rmdm"))

    JOIN {
        TARGET() INNER_JOIN TABLE("scm_user").alias("su") ON rmdm.scm_user_id, su.scm_user_id INNER_JOIN TABLE("Scm_User_Team").alias("sut") ON rmdm.scm_user_id, sut.scm_user_id INNER_JOIN TABLE("Module").alias("m") ON rmdm.module_id, m.module_id
    }

    FETCH (su.scm_user_id.alias("scmUserId"), MAX(sut.date).alias("date"), MAX(su.user_id).alias("user"))

    WHERE {
        EQ (m.code_branch_id, 1)
        AND
        EQ (rmdm.release_id, PARAM("releaseId"))
        AND
        EQ (rmdm.org_unit_id, 1)
        AND
        GTE (sut.date, 1474684200000)
        AND
        LTE (sut.date, 1474684201000)
        AND
        EQ (sut.team_id, 1)
        if ($SESSION.hello.abc == null) {
            AND
            EQ(m.is_removed, PARAM("memem"))
        }
    }

    GROUP_BY (su.scm_user_id)

}

$DSL.select {

    TARGET (TABLE("Devs").alias("dev"))

    FETCH (COUNT(CASE {WHEN {NOTNULL (dev.scmUserId) } THEN {1} ELSE {0}} ).alias("mappedUserCount"),
            SUM(CASE {WHEN {ISNULL (dev.scmUserId) } THEN {1} ELSE {0}} ).alias("unmappedUserCount"))

    WHERE {
        IN (dev.users, innQ)
    }
}