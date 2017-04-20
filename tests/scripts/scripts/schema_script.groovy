/**
 * @author IWEERARATHNA
 */
$DSL.script {

    RUN("@ddls/temp_create")
    RUN("@./ddls/temp_create")
    RUN("scripts/ddls/temp_create")

    RUN("scripts/ddls/temp_drop")
}