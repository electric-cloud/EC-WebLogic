import spock.lang.*

class StartAppSuite extends WebLogicHelper {
    /**
     * Environments Variables
     */
    static String wlstPath = System.getenv('WEBLOGIC_WLST_PATH')

    /**
     * Dsl Parameters
     */

    @Shared
    def procedureName = 'StartApp'
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
    // Procedure params
    def configname
    def wlstabspath
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

        def artifactName = 'test:sample'
        def version = '1.0'

        setupResource()
        createConfig(CONFIG_NAME)

        def deploy = deployApplication(projectName,
            [
                configname : CONFIG_NAME,
                wlstabspath: getWlstPath(),
                appname    : APPLICATION_NAME,
                apppath    : '', // This will be filled by downloadArtifact()
                targets    : 'AdminServer',
                is_library : "false"
            ],
            artifactName,
            FILENAME
        )

        assert deploy.outcome == 'success'

        def stop = stopApplication(projectName,
            [
                configname        : CONFIG_NAME,
                wlstabspath       : getWlstPath(),
                appname           : APPLICATION_NAME,

                additional_options: "",
                version_identifier: ""
            ]
        )
        assert stop.outcome == 'success'

        dslFile "dsl/procedures.dsl", [
            projectName  : projectName,
            resourceName : getResourceName(),
            procedureName: procedureName,
            params       : [
                configname        : CONFIG_NAME,
                wlstabspath       : '',
                appname           : '',

                additional_options: '',
                version_identifier: '',
            ]
        ]
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
    //Positive Scenarios for delete should be first
    def "Start Application. appname '#appName' - Expected: #expectedOutcome : #expectedSummaryMessage"() {
        setup: 'Define the parameters for Procedure running'
        def runParams = [
            wlstabspath       : wlstabspath,
            appname           : appName,

            additional_options: additionalOptions,
            version_identifier: versionIdentifier
        ]

        when: 'Procedure runs: '

        def result = runTestedProcedure(projectName, procedureName, runParams, getResourceName())

        then: 'Wait until job run is completed: '
        def debugLog = result.logs
        println "Procedure log:\n$debugLog\n"

        expect: 'Outcome and Upper Summary verification'
        assert result.outcome == expectedOutcome
        if (expectedOutcome == expectedOutcomes.success && result.outcome == expectedOutcomes.success) {
            def pageAfterStart = checkUrl(APPLICATION_PAGE_URL)
            assert pageAfterStart.code == SUCCESS_RESPONSE
        }

        if (expectedSummaryMessage) {
            def upperStepSummary = getJobUpperStepSummary(result.jobId)
            assert upperStepSummary.contains(expectedSummaryMessage)
        }

        cleanup: 'Stop application if start was successful'
        if (expectedOutcome == expectedOutcomes.success && result.outcome == expectedOutcomes.success) {
            stopApplication(projectName, [
                configname        : CONFIG_NAME,
                appname           : appName,
                wlstabspath       : wlstabspath,

                additional_options: "",
                version_identifier: ""
            ])
        }

        where: 'The following params will be: '
        wlstabspath | appName          | additionalOptions | versionIdentifier | expectedOutcome          | expectedSummaryMessage
        wlstPath    | APPLICATION_NAME | ''                | ''                | expectedOutcomes.success | ''
    }
}
