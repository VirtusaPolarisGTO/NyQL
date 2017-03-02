/**
 * (c) 2017 Virtusa Corporation.
 * All Rights Reserved
 * @author MLAKJEEWA
 * @name QualityDashboardJobService/populateTimelineReleaseMetricsForToday
 *
 */

def openIssueCountQuery = $DSL.select {

    TARGET (TABLE("work_item").alias("workItem"))

    FETCH (
            COUNT().alias("openIssueCount"),
            workItem.release_sid
    )

    WHERE {
        ALL {
            EQ (workItem.type, STR("ISU"))
            IN (workItem.status, [STR("New"), STR("Reviewed to Fix"), STR("Fixing")])
        }
    }

    GROUP_BY (workItem.release_sid)
}

def createdIssueCountQuery = $DSL.select {

    TARGET (TABLE("work_item").alias("workItem"))

    FETCH (
            COUNT().alias("createdIssueCount"),
            workItem.release_sid
    )

    WHERE {
        EQ (CAST_DATE (workItem.created_date), CAST_DATE (PARAM("dateTime")))
    }

    GROUP_BY (workItem.release_sid)
}


def timelineData = $DSL.select {

    TARGET (TABLE("release").alias("release"))

    JOIN (TARGET()) {
        LEFT_JOIN (TABLE(openIssueCountQuery).alias("openIssueCountTbl")) ON openIssueCountTbl.release_sid, release.sid
        LEFT_JOIN (TABLE(createdIssueCountQuery).alias("createdIssueCountTbl")) ON createdIssueCountTbl.release_sid, release.sid
    }

    FETCH (
            CASE({
                WHEN {
                    EQ(openIssueCountTbl.openIssueCount, null)
                } THEN { NUM(0) }
                ELSE {
                    openIssueCountTbl.openIssueCount
                }
            }),
            CASE({
                WHEN {
                    EQ(createdIssueCountTbl.createdIssueCount, null)
                } THEN { NUM(0) }
                ELSE {
                    createdIssueCountTbl.createdIssueCount
                }
            }),
            release.sid,
            PARAM("dateTime")
    )

    WHERE {
        LTE (CAST_DATE (release.end_date), CAST_DATE (PARAM("dateTime")))
    }
}


$DSL.insert {

    TARGET (TABLE(timelineData).alias("timelineMetricsData"))

    INTO (
            TABLE("timeline_release_metrics").alias("timelineMetrics"),
            timelineMetrics.open_issue_count,
            timelineMetrics.detected_issue_count,
            timelineMetrics.release_sid,
            timelineMetrics.date
    )

}
