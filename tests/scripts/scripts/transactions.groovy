/**
 * @author IWEERARATHNA
 */
$DSL.script {

    TRANSACTION {

        CHECKPOINT()
        RUN ("scripts/other_query")

        COMMIT()
    }

    TRANSACTION {
        AUTO_COMMIT()

        def snap = CHECKPOINT()
        try {
            RUN ("scripts/script_with_error")
        } catch (Exception ex) {
            ROLLBACK(snap)
        }
    }

    TRANSACTION {
        AUTO_COMMIT()

        RUN ("scripts/ddls/temp_create")
        RUN ("scripts/script_with_error")
    }

}