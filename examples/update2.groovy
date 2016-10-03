/**
 * @author IWEERARATHNA
 */
$DSL.update {

    TARGET(TABLE("release_violation_summary").alias("rvs"))

    JOIN (TARGET()) {
        INNER_JOIN (TABLE("_tmp_introduced_violations").alias("intro")) ON {

            EQ(intro.release_id, rvs.release_id)
            AND
            EQ(intro.module_id, rvs.module_id)
            AND
            EQ(intro.scm_user_id, rvs.scm_user_id)
            AND
            EQ(intro.coding_rule_id, rvs.coding_rule_id)
            AND
            EQ(intro.source_id, rvs.source_id)

        }

        RIGHT_OUTER_JOIN(TABLE("_tmp_fixed_violations").alias("fixed")) ON {

            EQ(intro.release_id, fixed.release_id)
            AND
            EQ(intro.module_id, fixed.module_id)
            AND
            EQ(intro.scm_user_id, fixed.scm_user_id)
            AND
            EQ(intro.coding_rule_id, fixed.coding_rule_id)
            AND
            EQ(intro.source_id, fixed.source_id)


        }
    }

    SET {
        EQ(rvs.fixed_violations, fixed.fixed_violations)
        EQ(rvs.fixed_potential_qa_bugs, fixed.fixed_potential_qa_bugs)
        EQ(rvs.fixed_security_s1, fixed.fixed_security_s1)
        EQ(rvs.fixed_security_s2, fixed.fixed_security_s2)
        EQ(rvs.fixed_security_s3, fixed.fixed_security_s3)
        EQ(rvs.fixed_tech_s1, fixed.fixed_tech_s1)
        EQ(rvs.fixed_tech_s2, fixed.fixed_tech_s2)
        EQ(rvs.fixed_tech_s3, fixed.fixed_tech_s3)
        EQ(rvs.other_fixed_violations, fixed.other_fixed_violations)
        EQ(rvs.own_fixed_violations, fixed.own_fixed_violations)
    }

    WHERE {
        NOTNULL(intro.introduced_violations)
    }
}