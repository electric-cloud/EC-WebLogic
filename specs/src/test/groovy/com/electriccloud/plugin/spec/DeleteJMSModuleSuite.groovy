package com.electriccloud.plugin.spec

import spock.lang.*
@Requires({WebLogicHelper.testJMS()})

class DeleteJMSModuleSuite extends WebLogicHelper {
    /**
     * Environments Variables
     */
    static String wlstPath = System.getenv('WEBLOGIC_WLST_PATH')

    /**
     * Dsl Parameters
     */

    @Shared
    def procedureName = 'DeleteJMSModule'
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
    @Shared
    def caseId

    // Procedure params
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

        createJMSModule(jmsModuleNames.default)

        dslFile "dsl/procedures.dsl", [
            projectName  : projectName,
            resourceName : getResourceName(),
            procedureName: procedureName,
            params       : [
                configname                  : CONFIG_NAME,
                ecp_weblogic_jms_module_name: ''
            ]
        ]

        dslFile("dsl/Application/DeleteJMSModule.dsl", [
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
    def "Delete JMS Module. (Module name : #jmsModuleName) - procedure"() {
        setup: 'Define the parameters for Procedure running'
        def runParams = [
            ecp_weblogic_jms_module_name: jmsModuleName,
        ]

        // Create JMS Module to delete unless it should not exist
        if (jmsModuleName != jmsModuleNames.nonexisting) {
            createJMSModule(jmsModuleName)
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
            assert !checkJMSModuleExists(jmsModuleName)
            checkServerRestartOutputParameter(result.jobId)
        }

        if (expectedJobDetailedResult) {
            assert debugLog.contains(expectedJobDetailedResult)
        }

        if (expectedSummaryMessage) {
            assert upperStepSummary.contains(expectedSummaryMessage)
        }

        where: 'The following params will be: '
        caseId | jmsModuleName              | expectedOutcome          | expectedJobDetailedResult                         | expectedSummaryMessage

        // delete JMS Module
        'C325182' | jmsModuleNames.default     | expectedOutcomes.success | "JMS Module $jmsModuleName has been deleted"      | "Deleted JMS System Module $jmsModuleName"

        // delete non-existing jms module from non-existing connection factory
        'C325183' | jmsModuleNames.nonexisting | expectedOutcomes.error   | "JMS System Module $jmsModuleName does not exist" | ''
    }

    @Unroll
    def "Delete JMS Module. (Module name : #jmsModuleName) - application"() {
        setup: 'Define the parameters for Procedure running'
        def paramsStr = stringifyArray([
            ecp_weblogic_jms_module_name: jmsModuleName,
        ])

        // Create JMS Module to delete unless it should not exist
        if (jmsModuleName != jmsModuleNames.nonexisting) {
            createJMSModule(jmsModuleName)
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
            assert logs.contains(expectedJobDetailedResult)
            assert !checkJMSModuleExists(jmsModuleName)
        }
        where: 'The following params will be: '
        caseId    | jmsModuleName              | expectedOutcome          | expectedJobDetailedResult                         | expectedSummaryMessage

        // delete JMS Module
        'C325186' | jmsModuleNames.default     | expectedOutcomes.success | "JMS Module $jmsModuleName has been deleted"      | "Deleted JMS System Module $jmsModuleName"

        // delete non-existing jms module from non-existing connection factory
        'C325187' | jmsModuleNames.nonexisting | expectedOutcomes.error   | "JMS System Module $jmsModuleName does not exist" | ''
    }

    def checkJMSModuleExists(jmsModuleName) {
        def code = """
jmsModuleName = '${jmsModuleName}'
target = 'AdminServer'

def getJMSResource(name):
    if (name == None or name == ''):
        raise Exception("No JMS Module Name is provided")
    mbean = getMBean('/JMSSystemResources/%s' % name)
    if mbean == None:
        return None
    else:
        print("Got JMS Bean %s" % mbean)
        return mbean.getJMSResource()

connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')
cd('/')

jmsResource = getJMSResource(jmsModuleName)
print("Found JMS Resource %s" % jmsModuleName)
if jmsResource == None:
    print("JMS Resource %s does not exist" % jmsModuleName)
else:
    print("JMS Resource %s exists" % jmsModuleName)
"""
        def result = runWLST(code)
        assert result.outcome == 'success'
        return result.logs?.contains("JMS Resource $jmsModuleName exists")
    }

}
