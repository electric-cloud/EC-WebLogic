import spock.lang.*

class StopAppSuite extends WebLogicHelper {
    /**
     * Environments Variables
     */
    static String wlstPath = System.getenv('WEBLOGIC_WLST_PATH')

    /**
     * Dsl Parameters
     */

    @Shared
    def procedureName = 'StopApp'
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
        empty          : '',
        file_not_exists: 'File  doesn\'t exist'

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
    def configName
    def wlstAbsPath
    def appName

    // Optional parameters
    def additionalOptions
    def versionIdentifier

    // expected results
    def expectedOutcome
    def expectedSummaryMessage
    def expectedJobDetailedResult

    /**
     * Preparation actions
     */

    def doSetupSpec() {
        assert wlstPath

        setupResource()
        createConfig(CONFIG_NAME)

        def deployed = deployApplication(projectName,
            [
                configname : CONFIG_NAME,
                wlstabspath: wlstPath,
                appname    : APPLICATION_NAME,
                apppath    : '', // Will be filled inside
                targets    : 'AdminServer',
                is_library : ""
            ],
            ARTIFACT_NAME,
            FILENAME
        )

        assert deployed.outcome == 'success'

        dslFile "dsl/procedures.dsl", [
            projectName  : projectName,
            resourceName : getResourceName(),
            procedureName: procedureName,
            params       : [
                configname        : CONFIG_NAME,
                wlstabspath       : '',
                appname           : '',

                additional_options: '',
                version_identifier: ''
            ]
        ]

        dslFile("dsl/Application/StopApp.dsl", [
            resourceName   : getResourceName()
        ])
    }

    /**
     * Clean Up actions after test will finished
     */

    def doCleanupSpec() {
        undeployApplication(projectName,
            [
                configname : CONFIG_NAME,
                wlstabspath: wlstPath,
                appname    : APPLICATION_NAME
            ]
        )
    }

    /**
     * Positive Scenarios
     */

    @Unroll
    //Positive Scenarios for delete should be first
    def "Stop Application. application '#appName', wlstPath : '#wlstAbsPath' - procedure "() {
        setup: 'Define the parameters for Procedure running'
        def runParams = [
            wlstabspath       : wlstAbsPath,
            appname           : appName,

            additional_options: additionalOptions,
            version_identifier: versionIdentifier
        ]

        startApplication(projectName, [
            configname        : CONFIG_NAME,
            appname           : appName,
            wlstabspath       : wlstAbsPath,

            additional_options: "",
            version_identifier: ""
        ])

        when: 'Procedure runs: '

        def result = runTestedProcedure(projectName, procedureName, runParams, getResourceName())

        then: 'Wait until job run is completed: '

        def debugLog = result.logs

        println "Procedure log:\n$debugLog\n"

        def upperStepSummary = getJobUpperStepSummary(result.jobId)
        logger.info("[SUMMARY]" + upperStepSummary)

        expect: 'Outcome and Upper Summary verification'
        assert result.outcome == expectedOutcome
        if (expectedOutcome == expectedOutcomes.success && result.outcome == expectedOutcomes.success) {
            def pageAfterDeploy = checkUrl(APPLICATION_PAGE_URL)
            assert pageAfterDeploy.code == NOT_FOUND_RESPONSE
        }
        if (expectedSummaryMessage) {
            assert upperStepSummary.contains(expectedSummaryMessage)
        }

        where: 'The following params will be: '
        caseId    | wlstAbsPath | appName          | additionalOptions | versionIdentifier | expectedOutcome          | expectedSummaryMessage
        'C325210' | wlstPath    | APPLICATION_NAME | ''                | ''                | expectedOutcomes.success | ''

        // Empty wlst path should return "File  doesn't exist"
        'C325223' | ''          | APPLICATION_NAME | ''                | ''                | expectedOutcomes.error   | expectedSummaryMessages.file_not_exists
    }

    @Unroll
    //Positive Scenarios for delete should be first
    def "Stop Application. application '#appName' - application "() {
        setup: 'Define the parameters for Procedure running'
        def paramsStr = stringifyArray([
            wlstabspath       : wlstAbsPath,
            appname           : appName,

            additional_options: additionalOptions,
            version_identifier: versionIdentifier
        ])

        startApplication(projectName, [
            configname        : CONFIG_NAME,
            appname           : appName,
            wlstabspath       : wlstAbsPath,

            additional_options: "",
            version_identifier: ""
        ])

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

        assert jobStatus(result.jobId).outcome == expectedOutcome

        if (expectedOutcome == expectedOutcomes.success && result.outcome == expectedOutcomes.success) {
            def pageAfterDeploy = checkUrl(APPLICATION_PAGE_URL)
            assert pageAfterDeploy.code == NOT_FOUND_RESPONSE
        }

        where: 'The following params will be: '
        caseId    | wlstAbsPath | appName          | additionalOptions | versionIdentifier | expectedOutcome          | expectedSummaryMessage
        'C325211' | wlstPath    | APPLICATION_NAME | ''                | ''                | expectedOutcomes.success | ''
    }
}
