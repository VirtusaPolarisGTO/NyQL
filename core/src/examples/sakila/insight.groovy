/**
 * @author IWEERARATHNA
 */
def s = $SESSION

$DSL.select {

    TARGET (TABLE("release_module_summary"))

    FETCH (SUM(release_open_tech_s1).alias("techS1"), SUM(release_open_tech_s2).alias("techS2"), SUM(release_open_tech_s3).alias("techS3"), SUM(release_open_security_s1).alias("securityS1"), SUM(release_open_security_s2).alias("securityS2"), SUM(release_open_security_s3).alias("securityS3"))

    WHERE {
        EQ (org_unit_id, "123")
        AND
        EQ (release_id, "11739")
        AND
        IN (team_id, 1410, 1411)
        AND
        IN (module_id, 97389, 97390)
    }

}

/*
$DSL.select {
    TARGET (TABLE("release_module_summary").alias("rms"))

    FETCH (SUM(rms.release_open_tech_s1).alias("techS1"),
            SUM(rms.release_open_tech_s2).alias("techS2"),
            SUM(rms.release_open_tech_s3).alias("techS3"),
            SUM(rms.release_open_security_s1).alias("securityS1"),
            SUM(rms.release_open_security_s2).alias("securityS2"),
            SUM(rms.release_open_security_s3).alias("securityS3"))

    WHERE {
        EQ (rms.org_unit_id, "123")
        AND
        EQ (rms.release_id, "11739")
        AND
        IN (rms.team_id, PARAM("teamIDs", s.teamIDs.size()) )
        AND
        IN (rms.module_id, PARAM("moduleIDs", s.moduleIDs.size()))
    }
}
*/
