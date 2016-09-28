/**
 * Created by IWEERARATHNA on 9/28/2016.
 */
$DSL.$q {

    EXPECT (TABLE("scm_user").alias("su"))
    EXPECT (TABLE("Scm_User_Team").alias("sut"))

    FETCH (su.scm_user_id.alias("scmUserId"), MAX(sut.date).alias("date"), MAX(su.user_id).alias("user"))

}
