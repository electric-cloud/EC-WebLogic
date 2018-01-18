import spock.lang.*
import com.electriccloud.spec.SpockTestSupport
import TestHelper

/**
 * Data-driven testing example.
 * Runs the test with different inputs.
 */
// @Stepwise
//@Ignore
@Unroll
class StopAdmin extends TestHelper {

    def projName = 'Test Weblogic'
    def pluginConfig = 'local'
    def pipeStage = 'Stage 1'
    // End of vars editing section

    def doSetupSpec() {
        
    }

    def "Stop AdminServer"() {
        when: 'process runs'
        def flowRuntimeId = runNWaitPipeline(projName, 'WebLogic - DemoApp - StopAdminServer')

        assert flowRuntimeId
        then: 'wait untill process is completed'
        

        assert waitNCheckLog(flowRuntimeId, pipeStage, 'Criteria met')
    }

}