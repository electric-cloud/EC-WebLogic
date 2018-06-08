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
    // Procedure params
    def wlstabspath
    def appname

    //optional parameters
    def retire_gracefully
    def version_identifier
    def give_up
    def additional_options

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

        def artifactName = 'test:sample'
        def version = '1.0'

        setupResource()
        createConfig(CONFIG_NAME)

        publishArtifact(artifactName, version, FILENAME)
        apppath = downloadArtifact(artifactName, getResourceName())

        dslFile "dsl/procedures.dsl", [
            projectName  : projectName,
            resourceName : getResourceName(),
            procedureName: procedureName,
            params       : [
                configname        : CONFIG_NAME,
                wlstabspath       : wlstabspath,
                appname           : appname,
                additional_options: additional_options,
                retire_gracefully : retire_gracefully,
                version_identifier: version_identifier,
                give_up           : give_up
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
    def "Undeploy Application. with server '#targets'. Expected : #expectedOutcome : #expectedSummaryMessage"() {
        setup: 'Define the parameters for Procedure running'
        def runParams = [
            wlstabspath       : wlstabspath,
            appname           : appname,
            additional_options: additional_options,
            retire_gracefully : retire_gracefully,
            version_identifier: version_identifier,
            give_up           : give_up
        ]

        // Check that application is not installed and running already
        def pageBeforeUndeploy = checkUrl(APPLICATION_PAGE_URL)
        if (pageBeforeUndeploy.code == NOT_FOUND_RESPONSE) {
            def deploy = deployApplication(projectName, [
                configname : CONFIG_NAME,
                wlstabspath: getWlstPath(),
                appname    : appname,
                apppath    : apppath,
                targets    : 'AdminServer',
                is_library : ""
            ])

            assert (deploy.outcome == 'success')
        }

        when: 'Procedure runs: '

        def result = runTestedProcedure(projectName, procedureName, runParams, getResourceName())

        then: 'Wait until job run is completed: '

        def debugLog = result.logs

        println "Procedure log:\n$debugLog\n"
//        def upperStepSummary = getJobUpperStepSummary(result.jobId)

        expect: 'Outcome and Upper Summary verification'
        assert result.outcome == expectedOutcome

        if (expectedOutcome == expectedOutcomes.success && result.outcome == expectedOutcomes.success) {
            def pageAfterUndeploy = checkUrl(APPLICATION_PAGE_URL)
            assert pageAfterUndeploy.code == NOT_FOUND_RESPONSE
        }

        where: 'The following params will be: '
        wlstabspath | appname          | retire_gracefully        | version_identifier | give_up | additional_options | expectedOutcome
        wlstPath    | APPLICATION_NAME | checkBoxValues.unchecked | ''                 | ''      | ''                 | expectedOutcomes.success
        wlstPath    | APPLICATION_NAME | checkBoxValues.checked   | ''                 | ''      | ''                 | expectedOutcomes.success

    }
}