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
class TheFirst extends TestHelper {

    def projName = 'Test Weblogic'
    def pluginConfig = 'local'
    def pipeStage = 'Stage 1'
    // End of vars editing section

    def doSetupSpec() {
        
    }


    @Ignore
    def "Get Artifact"() {
        when: 'process runs'
        def flowRuntimeId = runPipelineFromDSL('dsl/getArtifact.dsl', [
                                                                    projName: projName,
                                                                    pluginConfig: pluginConfig,
                                                                    pipeName: 'Download and Publish Artifact',
                                                                    resName: 'Weblogic Demo',
                                                                    projectRemoval: true
                                                                    ]);
        assert flowRuntimeId
        then: 'wait untill process is completed'
        
        

        assert waitNCheckLog(flowRuntimeId, pipeStage, 'Published 1 file to the artifact repository')
    }


    def "Install App"() {
        when: 'process runs'
        def flowRuntimeId = runPipelineFromDSL('dsl/createApp.dsl', [
                                                                    projName: projName,
                                                                    pluginConfig: pluginConfig,
                                                                    pipeName: 'WebLogic - DemoApp - Undeploy',
                                                                    resName: 'Weblogic Demo',
                                                                    webLogicHost: '10.200.1.150'
                                                                    ]);
        assert flowRuntimeId
        then: 'wait untill process is completed'
        

        assert waitNCheckLog(flowRuntimeId, pipeStage, 'DONE')
    }

}