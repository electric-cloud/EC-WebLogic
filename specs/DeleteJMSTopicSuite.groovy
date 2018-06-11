import spock.lang.*

class DeleteJMSTopicSuite extends WebLogicHelper {
    /**
     * Dsl Parameters
     */

    @Shared
    def procedureName = 'DeleteJMSTopic'
    @Shared
    def projectName = "EC-WebLogic ${procedureName}"

    /**
     * Common Maps: General Maps for different fields
     */

    @Shared
    def checkBoxValues = [
        unchecked: '0',
        checked  : '1',
    ]

    /**
     * Parameters for Test Setup
     */

    /**
     * Procedure Values: test parameters Procedure values
     */
    @Shared
    def jmsTopicNames = [
        default    : 'TestJMSTopic',
        nonexisting: 'NoSuchJMSTopic'
    ]

    /**
     * Verification Values: Assert values
     */

    @Shared
    def expectedOutcomes = [
        success: 'success',
        error  : 'error',
        warning: 'warning',
        running: 'running',
    ]

    @Shared
    def expectedSummaryMessages = [
        empty: "",

    ]

    @Shared
    def expectedJobDetailedResults = [
        empty: '',
    ]

    @Shared
    def expectedLogParts = [
        empty: '',
    ]

    /**
     * Test Parameters: for Where section
     */

    // Procedure params
    @Shared
    def jmsTopicName
    @Shared
    def jmsModuleName = 'TestJMSModule'

    // expected results
    def expectedOutcome
    def expectedSummaryMessage
    def expectedJobDetailedResult

    /**
     * Preparation actions
     */

    def doSetupSpec() {
        setupResource()
        createConfig(CONFIG_NAME)

        createJMSModule(jmsModuleName, 'AdminServer')

        dslFile "dsl/procedures.dsl", [
            projectName  : projectName,
            resourceName : getResourceName(),
            procedureName: procedureName,
            params       : [
                configname                  : CONFIG_NAME,
                ecp_weblogic_jms_module_name: '',
                ecp_weblogic_jms_topic_name : '',
            ]
        ]
    }

    /**
     * Clean Up actions after test will finished
     */

    def doCleanupSpec() {
        deleteProject(projectName)
    }

    /**
     * Positive Scenarios
     */

    @Unroll
    def "Delete JMS Topic. (Topic : #jmsTopicName) - procedure"() {
        setup: 'Define the parameters for Procedure running'
        def runParams = [
            ecp_weblogic_jms_module_name: jmsModuleName,
            ecp_weblogic_jms_topic_name : jmsTopicName,
        ]

        // Create JMS Module to delete unless it should not exist
        if (jmsTopicName != jmsTopicNames.nonexisting) {
            createJMSTopic(jmsModuleName, jmsTopicName)
        }

        when: 'Procedure runs: '

        def result = runTestedProcedure(projectName, procedureName, runParams, getResourceName())

        then: 'Wait until job run is completed: '

        def debugLog = result.logs

        println "Procedure log:\n$debugLog\n"

        def upperStepSummary = getJobUpperStepSummary(result.jobId)
        logger.info(upperStepSummary)

        expect: 'Outcome and Upper Summary verification'
        assert result.outcome == expectedOutcome
        if (expectedOutcome == expectedOutcomes.success && result.outcome == expectedOutcomes.success) {
            assert !checkJMSTopicExists(jmsTopicName)
        }

        if (expectedJobDetailedResult) {
            assert debugLog.contains(expectedJobDetailedResult)
        }

        if (expectedSummaryMessage) {
            assert upperStepSummary.contains(expectedSummaryMessage)
        }
        where: 'The following params will be: '
        jmsTopicName              | expectedOutcome          | expectedJobDetailedResult

        // delete JMS Module
        jmsTopicNames.default     | expectedOutcomes.success | "Removed JMS Topic $jmsTopicName"

        // delete non-existing jms module from non-existing connection factory
        jmsTopicNames.nonexisting | expectedOutcomes.error   | "JMS Topic $jmsTopicName does not exist"
    }

    def checkJMSTopicExists(jmsTopicName) {
        def code = """
jmsTopicName = '${jmsTopicName}'

def getJMSTopicPath(jms_server_name):
    return "/JMSTopics/%s" % (jms_server_name)
    
connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')
cd('/')

jmsTopic = getMBean(getJMSTopicPath(jmsTopicName))
if jmsTopic == None:
    print("JMS Topic %s does not exist" % jmsTopicName)
else:
    print("JMS Topic %s exists" % jmsTopicName)
"""
        def result = runWLST(code)
        assert result.outcome == 'success'
        return result.logs?.contains("JMS Topic $jmsTopicName exists")
    }

}
