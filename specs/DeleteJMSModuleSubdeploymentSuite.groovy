import spock.lang.*

class DeleteJMSModuleSubdeploymentSuite extends WebLogicHelper {
    /**
     * Dsl Parameters
     */

    @Shared
    def procedureName = 'DeleteJMSModuleSubdeployment'
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
    def jmsSubdeploymentNames = [
        default    : 'sub1',
        nonexisting: 'NoSuchJMSSubdeployment'
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
    def jmsSubdeploymentName
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
        deleteProject(projectName)

        createConfig(CONFIG_NAME)
        discardChanges()

        dslFile "dsl/procedures.dsl", [
            projectName  : projectName,
            resourceName : getResourceName(),
            procedureName: 'CreateOrUpdateJMSModuleSubdeployment',
            params       : [
                configname                            : CONFIG_NAME,
                ecp_weblogic_jms_module_name          : '',
                ecp_weblogic_update_action            : 'do_nothing',
                ecp_weblogic_subdeployment_target_list: '',
                ecp_weblogic_subdeployment_name       : ''
            ]
        ]

        dslFile "dsl/procedures.dsl", [
            projectName  : projectName,
            resourceName : getResourceName(),
            procedureName: procedureName,
            params       : [
                configname                     : CONFIG_NAME,
                ecp_weblogic_jms_module_name   : '',
                ecp_weblogic_subdeployment_name: '',
            ]
        ]

        dslFile("dsl/Application/CreateOrUpdateJMSModuleSubdeployment.dsl", [
            resourceName: getResourceName()
        ])
    }

    /**
     * Clean Up actions after test will finished
     */

    def doCleanupSpec() {
//        deleteProject(projectName)
    }

    /**
     * Positive Scenarios
     */

    @Unroll
    def "Delete JMS Subdeployment. (Subdeployment : #jmsSubdeploymentName, Module : #jmsModuleName) - procedure"() {
        setup: 'Define the parameters for Procedure running'
        def runParams = [
            ecp_weblogic_jms_module_name   : jmsModuleName,
            ecp_weblogic_subdeployment_name: jmsSubdeploymentName,
        ]

        // Create JMS Module to delete unless it should not exist
        if (jmsSubdeploymentName != jmsSubdeploymentNames.nonexisting) {
            createJMSModule(jmsModuleName)
            def createResult = runProcedure """
        runProcedure(
            projectName: '$projectName',
            procedureName: 'CreateOrUpdateJMSModuleSubdeployment',
            actualParameter: [
                ecp_weblogic_jms_module_name: '$jmsModuleName',
                ecp_weblogic_subdeployment_target_list: 'AdminServer',
                ecp_weblogic_update_action: 'selective_update',
                ecp_weblogic_subdeployment_name: '$jmsSubdeploymentName'
            ]
        )
        """, getResourceName()
            assert createResult.outcome == 'success'
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
            assert !checkJMSSubdeploymentExists(jmsModuleName, jmsSubdeploymentName)
        }

        if (expectedJobDetailedResult) {
            assert debugLog.contains(expectedJobDetailedResult)
        }

        if (expectedSummaryMessage) {
            assert upperStepSummary.contains(expectedSummaryMessage)
        }
        where: 'The following params will be: '
        jmsSubdeploymentName              | jmsModuleName              | expectedOutcome          | expectedJobDetailedResult

        // delete JMS Module
        jmsSubdeploymentNames.default     | jmsModuleNames.default     | expectedOutcomes.success | "Subdeployment $jmsSubdeploymentName has been deleted from JMS Module $jmsModuleName"

        // delete unexisting JMS queue
        jmsSubdeploymentNames.nonexisting | jmsModuleNames.default     | expectedOutcomes.error   | "Subdeployment $jmsSubdeploymentName does not exist in the JMS Module $jmsModuleName"

        // delete non-existing jms queue from non-existing jms module
        jmsSubdeploymentNames.nonexisting | jmsModuleNames.nonexisting | expectedOutcomes.error   | "Subdeployment $jmsSubdeploymentName does not exist in the JMS Module $jmsModuleName"
    }

    @Unroll
    def "Delete JMS Subdeployment. (Subdeployment : #jmsSubdeploymentName, Module : #jmsModuleName) - application"() {
        setup: 'Define the parameters for Procedure running'
        def paramsStr = stringifyArray([
            ecp_weblogic_jms_module_name   : jmsModuleName,
            ecp_weblogic_subdeployment_name: jmsSubdeploymentName,
        ])

        // Create JMS Module to delete unless it should not exist
        if (jmsSubdeploymentName != jmsSubdeploymentNames.nonexisting) {
            createJMSModule(jmsModuleName)
            def createResult = runProcedure """
        runProcedure(
            projectName: '$projectName',
            procedureName: 'CreateOrUpdateJMSModuleSubdeployment',
            actualParameter: [
                ecp_weblogic_jms_module_name: '$jmsModuleName',
                ecp_weblogic_subdeployment_target_list: 'AdminServer',
                ecp_weblogic_update_action: 'selective_update',
                ecp_weblogic_subdeployment_name: '$jmsSubdeploymentName'
            ]
        )
        """, getResourceName()
            assert createResult.outcome == 'success'
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

        def outcome = jobStatus(result.jobId).outcome
        assert outcome == expectedOutcome

        if (expectedOutcome == expectedOutcomes.success && result.outcome == expectedOutcomes.success) {
            assert !checkJMSSubdeploymentExists(jmsModuleName, jmsSubdeploymentName)
        }

        where: 'The following params will be: '
        jmsSubdeploymentName              | jmsModuleName              | expectedOutcome          | expectedJobDetailedResult

        // delete JMS Module
        jmsSubdeploymentNames.default     | jmsModuleNames.default     | expectedOutcomes.success | "Subdeployment $jmsSubdeploymentName has been deleted from JMS Module $jmsModuleName"

        // delete unexisting JMS queue
        jmsSubdeploymentNames.nonexisting | jmsModuleNames.default     | expectedOutcomes.error   | "Subdeployment $jmsSubdeploymentName does not exist in the JMS Module $jmsModuleName"

        // delete non-existing jms queue from non-existing jms module
        jmsSubdeploymentNames.nonexisting | jmsModuleNames.nonexisting | expectedOutcomes.error   | "Subdeployment $jmsSubdeploymentName does not exist in the JMS Module $jmsModuleName"
    }

    def checkJMSSubdeploymentExists(jmsModuleName, jmsSubdeploymentName) {
        def code = """
jmsSubdeploymentName = '${jmsSubdeploymentName}'

def getJMSSubdeploymentPath(jmsModule, queue):
    return "/JMSSystemResources/%s/JMSResource/%s/Queues/%s" % (jmsModule, jmsModule, queue)

connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')
cd('/')

jmsModuleName = '$jmsModuleName'
jmsSubdeploymentName = '$jmsSubdeploymentName'

jmsSubdeployment = getMBean(getJMSSubdeploymentPath(jmsModuleName, jmsSubdeploymentName))
if jmsSubdeployment == None:
    print("JMS Queue %s does not exist" % jmsSubdeploymentName)
else:
    print("JMS Queue %s exists" % jmsSubdeploymentName)
"""
        def result = runWLST(code)
        assert result.outcome == 'success'
        return result.logs?.contains("JMS Queue $jmsSubdeploymentName exists")
    }

}
