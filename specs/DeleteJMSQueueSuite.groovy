import spock.lang.*

class DeleteJMSQueueSuite extends WebLogicHelper {
    /**
     * Dsl Parameters
     */

    @Shared
    def procedureName = 'DeleteJMSQueue'
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
    def jmsQueueNames = [
        default    : 'TestJMSQueue',
        nonexisting: 'NoSuchJMSQueue'
    ]

    @Shared
    def jmsModuleNames = [
        default    : 'TestJMSModule',
        nonexisting: 'NoSuchJMSModule'
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
    def jmsQueueName
    @Shared
    def jmsModuleName

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

        createJMSModule(jmsModuleNames.default, 'AdminServer')

        dslFile "dsl/procedures.dsl", [
            projectName  : projectName,
            resourceName : getResourceName(),
            procedureName: procedureName,
            params       : [
                configname                  : CONFIG_NAME,
                ecp_weblogic_jms_module_name: '',
                ecp_weblogic_jms_queue_name : '',
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
    def "Delete JMS Topic. (Topic : #jmsQueueName, Module : #jmsModuleName) - procedure"() {
        setup: 'Define the parameters for Procedure running'
        def runParams = [
            ecp_weblogic_jms_module_name: jmsModuleName,
            ecp_weblogic_jms_queue_name : jmsQueueName,
        ]

        // Create JMS Module to delete unless it should not exist
        if (jmsQueueName != jmsQueueNames.nonexisting) {
            createJMSQueue(jmsModuleName, jmsQueueName)
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
            assert !checkJMSQueueExists(jmsModuleName, jmsQueueName)
        }

        if (expectedJobDetailedResult) {
            assert debugLog.contains(expectedJobDetailedResult)
        }

        if (expectedSummaryMessage) {
            assert upperStepSummary.contains(expectedSummaryMessage)
        }
        where: 'The following params will be: '
        jmsQueueName              | jmsModuleName              | expectedOutcome          | expectedJobDetailedResult

        // delete JMS Module
        jmsQueueNames.default     | jmsModuleNames.default     | expectedOutcomes.success | "Removed JMS Queue $jmsQueueName from the module $jmsModuleName"

        // delete unexisting JMS queue
        jmsQueueNames.nonexisting | jmsModuleNames.default     | expectedOutcomes.error   | "JMS Queue $jmsQueueName does not exist"

        // delete non-existing jms queue from non-existing jms module
        jmsQueueNames.nonexisting | jmsModuleNames.nonexisting | expectedOutcomes.error   | "JMS Queue $jmsQueueName does not exist"
    }

    def checkJMSQueueExists(jmsModuleName, jmsQueueName) {
        def code = """
jmsQueueName = '${jmsQueueName}'

def getJMSQueuePath(jmsModule, queue):
    return "/JMSSystemResources/%s/JMSResource/%s/Queues/%s" % (jmsModule, jmsModule, queue)

connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')
cd('/')

jmsModuleName = '$jmsModuleName'
jmsQueueName = '$jmsQueueName'

jmsQueue = getMBean(getJMSQueuePath(jmsModuleName, jmsQueueName))
if jmsQueue == None:
    print("JMS Queue %s does not exist" % jmsQueueName)
else:
    print("JMS Queue %s exists" % jmsQueueName)
"""
        def result = runWLST(code)
        assert result.outcome == 'success'
        return result.logs?.contains("JMS Queue $jmsQueueName exists")
    }

}