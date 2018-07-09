import spock.lang.*

@Stepwise
class UndeployAppSuite extends WebLogicHelper {

    /**
     * Environments Variables
     */
    static String wlstPath = System.getenv('WEBLOGIC_WLST_PATH')

    /**
     * Dsl Parameters
     */

    @Shared
    def procedureName = 'UndeployApp'
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
    //* Optional Parameter
    def additionalOptions = [
        empty    : '',
        correct  : '-subscriptionDurability Durable',
        incorrect: 'incorrect Additional Options',
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
    def wlstAbsPath
    def appName

    //optional parameters
    def retireGracefully
    def versionIdentifier
    def giveUp
    def additionalOptionsIs

    @Shared def artifactName = 'test:sample'
    @Shared def version = '1.0'

    // This should be saved for deployApplication procedure
    def static apppath

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

        dslFile "dsl/procedures.dsl", [
            projectName  : projectName,
            resourceName : getResourceName(),
            procedureName: procedureName,
            params       : [
                configname        : CONFIG_NAME,
                wlstabspath       : '',
                appname           : '',
                additional_options: '',
                retire_gracefully : '',
                version_identifier: '',
                give_up           : ''
            ]
        ]

        dslFile("dsl/Application/UndeployApp.dsl", [
            resourceName   : getResourceName()
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
    def "Undeploy Application. application - '#appName' - procedure"() {
        setup: 'Define the parameters for Procedure running'
        def runParams = [
            wlstabspath       : wlstAbsPath,
            appname           : appName,
            additional_options: additionalOptionsIs,
            retire_gracefully : retireGracefully,
            version_identifier: versionIdentifier,
            give_up           : giveUp
        ]

        // Check that application is not installed and running already
        def pageBeforeUndeploy = checkUrl(APPLICATION_PAGE_URL)
        if (pageBeforeUndeploy.code == NOT_FOUND_RESPONSE) {
            def deploy = deployApplication(projectName, [
                configname : CONFIG_NAME,
                wlstabspath: getWlstPath(),
                appname    : appName,
                apppath    : '', // Will be filled inside
                targets    : 'AdminServer',
                is_library : ""
            ], artifactName, FILENAME)

            assert (deploy.outcome == 'success')
        }

        when: 'Procedure runs: '

        def result = runTestedProcedure(projectName, procedureName, runParams, getResourceName())

        then: 'Wait until job run is completed: '

        def debugLog = result.logs

        println "Procedure log:\n$debugLog\n"

        expect: 'Outcome and Upper Summary verification'
        assert result.outcome == expectedOutcome

        if (expectedOutcome == expectedOutcomes.success && result.outcome == expectedOutcomes.success) {
            def pageAfterUndeploy = checkUrl(APPLICATION_PAGE_URL)
            assert pageAfterUndeploy.code == NOT_FOUND_RESPONSE
        }

        where: 'The following params will be: '
        caseId    | wlstAbsPath | appName          | retireGracefully         | expectedOutcome
        'C325214' | wlstPath    | APPLICATION_NAME | checkBoxValues.unchecked | expectedOutcomes.success
    }

    @Unroll
    def "Undeploy Application. application - '#appName' - application"() {
        setup: 'Define the parameters for Procedure running'
        def paramsStr = stringifyArray([
            wlstabspath       : wlstAbsPath,
            appname           : appName,
            additional_options: additionalOptionsIs,
            retire_gracefully : retireGracefully,
            version_identifier: versionIdentifier,
            give_up           : giveUp
        ])

        // Check that application is not installed and running already
        def pageBeforeUndeploy = checkUrl(APPLICATION_PAGE_URL)
        if (pageBeforeUndeploy.code == NOT_FOUND_RESPONSE) {
            def deploy = deployApplication(projectName, [
                configname : CONFIG_NAME,
                wlstabspath: getWlstPath(),
                appname    : appName,
                apppath    : '', // Will be filled inside
                targets    : 'AdminServer',
                is_library : ""
            ], artifactName, FILENAME)

            assert (deploy.outcome == 'success')
        }

        when: 'process runs'
        def result = dsl("""
                runProcess(
                    projectName    : "$HELPER_PROJECT",
                    applicationName: "$TEST_APPLICATION",
                    environmentName: '$ENVIRONMENT_NAME',
                    processName    : '$procedureName',
                    actualParameter: $paramsStr
                )
            """, [resourceName : getResourceName()])

        then: 'wait until process finishes'
        waitUntil {
            jobCompleted result
        }

        assert jobStatus(result.jobId).outcome == expectedOutcome

        def logs = getJobLogs(result.jobId)
        logger.debug("Process logs: " + logs)

        if (expectedOutcome == expectedOutcomes.success && result.outcome == expectedOutcomes.success) {
            def pageAfterUndeploy = checkUrl(APPLICATION_PAGE_URL)
            assert pageAfterUndeploy.code == NOT_FOUND_RESPONSE
        }

        where: 'The following params will be: '
        caseId    | wlstAbsPath | appName          | retireGracefully         | expectedOutcome
        'C325215' | wlstPath    | APPLICATION_NAME | checkBoxValues.unchecked | expectedOutcomes.success
    }
}