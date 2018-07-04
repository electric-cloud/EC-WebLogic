import spock.lang.*

class DeleteJMSServerSuite extends WebLogicHelper {
    /**
     * Dsl Parameters
     */

    @Shared
    def procedureName = 'DeleteJMSServer'
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
    def jmsServerNames = [
        default    : 'TestJMSServer',
        nonexisting: 'NoSuchJMSServer'
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
    @Shared
    def caseId

    // Procedure params
    @Shared
    def jmsServerName

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

        dslFile "dsl/procedures.dsl", [
            projectName  : projectName,
            resourceName : getResourceName(),
            procedureName: procedureName,
            params       : [
                configname                  : CONFIG_NAME,
                ecp_weblogic_jms_server_name: ''
            ]
        ]

        dslFile("dsl/Application/DeleteJMSServer.dsl", [
            resourceName: getResourceName()
        ])
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
    def "Delete JMS Module. (Server : #jmsServerName) - procedure"() {
        setup: 'Define the parameters for Procedure running'
        def runParams = [
            ecp_weblogic_jms_server_name: jmsServerName,
        ]

        // Create JMS Module to delete unless it should not exist
        if (jmsServerName != jmsServerNames.nonexisting) {
            createJMSServer(jmsServerName)
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
            assert !checkJMSServerExists(jmsServerName)
        }

        if (expectedJobDetailedResult) {
            assert debugLog.contains(expectedJobDetailedResult)
        }

        if (expectedSummaryMessage) {
            assert upperStepSummary.contains(expectedSummaryMessage)
        }
        where: 'The following params will be: '
        caseId    | jmsServerName              | expectedOutcome          | expectedJobDetailedResult

        // delete JMS Module
        'C325200' | jmsServerNames.default     | expectedOutcomes.success | "Removed JMS Server $jmsServerName"

        // delete non-existing jms module from non-existing connection factory
        'C325201' | jmsServerNames.nonexisting | expectedOutcomes.error   | "JMS Server $jmsServerName does not exist"
    }
    @Unroll
    def "Delete JMS Module. (Server : #jmsServerName) - application"() {

        setup: 'Define the parameters for Procedure running'
        def paramsStr = stringifyArray([
            ecp_weblogic_jms_server_name: jmsServerName,
        ])

        // Create JMS Module to delete unless it should not exist
        if (jmsServerName != jmsServerNames.nonexisting) {
            createJMSServer(jmsServerName)
        }

        when: 'process runs'
        def result = dsl("""
                runProcess(
                    projectName    : "$HELPER_PROJECT",
                    applicationName: "$TEST_APPLICATION",
                    environmentName: '$ENVIRONMENT_NAME',
                    processName    : '$procedureName',
                    actualParameter:  $paramsStr
                )
            """, [resourceName: getResourceName()])

        then: 'wait until process finishes'
        waitUntil {
            jobCompleted result
        }

        def logs = getJobLogs(result.jobId)
        logger.debug("Process logs: " + logs)

        expect:
        assert jobStatus(result.jobId).outcome == expectedOutcome
        if (expectedOutcome == expectedOutcomes.success && result.outcome == expectedOutcomes.success) {
            assert !checkJMSServerExists(jmsServerName)
        }

        where: 'The following params will be: '
        caseId    | jmsServerName              | expectedOutcome          | expectedJobDetailedResult

        // delete JMS Module
        'C325202' | jmsServerNames.default     | expectedOutcomes.success | "Removed JMS Server $jmsServerName"

        // delete non-existing jms module from non-existing connection factory
        'C325203' | jmsServerNames.nonexisting | expectedOutcomes.error   | "JMS Server $jmsServerName does not exist"
    }

    def checkJMSServerExists(jmsServerName) {
        def code = """
jmsServerName = '${jmsServerName}'

def getJMSServerPath(jms_server_name):
    return "/JMSServers/%s" % (jms_server_name)
    
connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')
cd('/')

jmsServer = getMBean(getJMSServerPath(jmsServerName))
if jmsServer == None:
    print("JMS Server %s does not exist" % jmsServerName)
else:
    print("JMS Server %s exists" % jmsServerName)
"""
        def result = runWLST(code)
        assert result.outcome == 'success'
        return result.logs?.contains("JMS Server $jmsServerName exists")
    }

}
