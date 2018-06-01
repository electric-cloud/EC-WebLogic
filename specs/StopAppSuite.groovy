import spock.lang.Shared
import spock.lang.Unroll

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

    @Shared
    //* Required Parameter (need incorrect and empty value)
    def pluginConfigurationNames = [
            empty    : '',
            correct  : CONFIG_NAME,
            incorrect: 'incorrect config Name',
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
    def appname

    // Optional parameters
    def additional_options
    def version_identifier

    // expected results
    def expectedOutcome
    def expectedSummaryMessage
    def expectedJobDetailedResult

    /**
     * Preparation actions
     */

    def doSetupSpec() {
        assert wlstPath

        createConfig(pluginConfigurationNames.correct)

        def deployed = DeployApplication(projectName,
                [
                        configname : CONFIG_NAME,
                        wlstabspath: wlstPath,
                        appname    : APPLICATION_NAME,
                        apppath    : APPLICATION_PATH,
                        targets    : 'AdminServer',
                        is_library : ""
                ]
        )

        assert deployed.outcome == 'success'
    }

    /**
     * Clean Up actions after test will finished
     */

    def doCleanupSpec() {
        UndeployApplication(projectName,
                [
                        configname : CONFIG_NAME,
                        wlstabspath: wlstPath,
                        appname    : APPLICATION_NAME
                ]
        )
        deleteProject(projectName)
    }

    /**
     * Positive Scenarios
     */

    @Unroll
    //Positive Scenarios for delete should be first
    def "Stop Application. application '#appname' - #expectedOutcome : #expectedSummaryMessage"() {
        setup: 'Define the parameters for Procedure running'
        def runParams = [
                configname        : configname,
                wlstabspath       : wlstabspath,
                appname           : appname,

                additional_options: additional_options,
                version_identifier: version_identifier
        ]

        StartApplication(projectName, [
                configname : configname,
                appname    : appname,
                wlstabspath: wlstabspath,

                additional_options : "",
                version_identifier : ""
        ])

        when: 'Procedure runs: '

        def result = runTestedProcedure(projectName, procedureName, runParams, getResourceName())

        then: 'Wait until job run is completed: '

        def outcome = result.outcome
        def debugLog = result.logs

        println "Procedure log:\n$debugLog\n"

        def upperStepSummary = getJobUpperStepSummary(result.jobId)
        logger.info("[SUMMARY]" + upperStepSummary)

        expect: 'Outcome and Upper Summary verification'
        assert outcome == expectedOutcome
        if (expectedOutcome == expectedOutcomes.success && outcome == expectedOutcomes.success) {
            def pageAfterDeploy = checkUrl(APPLICATION_PAGE_URL)
            assert pageAfterDeploy.code == NOT_FOUND_RESPONSE
        }
        if (expectedSummaryMessage) {
            assert upperStepSummary.contains(expectedSummaryMessage)
        }

        where: 'The following params will be: '
        configname                       | wlstabspath | appname          | additional_options | version_identifier | expectedOutcome          | expectedSummaryMessage
        pluginConfigurationNames.correct | wlstPath    | APPLICATION_NAME | ''                 | ''                 | expectedOutcomes.success | ''

        //with TargetServerSpecified
        pluginConfigurationNames.correct | wlstPath    | APPLICATION_NAME | ''                 | ''                 | expectedOutcomes.success | ''

        // Empty wlst path should return "File  doesn't exist"
        pluginConfigurationNames.correct | ''          | APPLICATION_NAME | ''                 | ''                 | expectedOutcomes.error   | expectedSummaryMessages.file_not_exists
    }
}
