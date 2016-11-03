import com.virtusa.gto.nyql.exceptions.NyException

/**
 * @author IWEERARATHNA
 */
$DSL.script {
    throw new NyException("A sample exception thrown by script! Don't worry about this.")
}