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
    //* Required Parameter (need incorrect and empty value)
    def pluginConfigurationNames = [
            empty    : '',
            correct  : CONFIG_NAME,
            incorrect: 'incorrect config Name',
    ]


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
    def configname
    def wlstabspath
    def appname

    //optional parameters
    def retire_gracefully
    def version_identifier
    def give_up
    def additional_options

    // expected results
    def expectedOutcome
    def expectedSummaryMessage
    def expectedJobDetailedResult

    /**
     * Preparation actions
     */

    def doSetupSpec() {
        assert wlstPath

        def resourceName = getResourceName()
        def artifactName = 'test:sample'
        def version = '1.0'

        createWorkspace(resourceName)

//        createConfig(pluginConfigurationNames.empty)
        createConfig(pluginConfigurationNames.correct)
        createConfig(pluginConfigurationNames.incorrect)

        publishArtifact(artifactName, version, FILENAME)
        downloadArtifact(artifactName, REMOTE_DIRECTORY, getResourceName())
    }

    /**
     * Clean Up actions after test will finished
     */

    def doCleanupSpec() {
        deleteProject(projectName)

//        deleteConfiguration('EC-WebLogic', pluginConfigurationNames.empty)
        deleteConfiguration('EC-WebLogic', pluginConfigurationNames.correct)
        deleteConfiguration('EC-WebLogic', pluginConfigurationNames.incorrect)
    }

    /**
     * Positive Scenarios
     */

    @Unroll
    //Positive Scenarios for delete should be first
    def "Undeploy Application. Positive Scenarios and Extended Scenarios"() {
        setup: 'Define the parameters for Procedure running'
        def runParams = [
                configname        : configname,
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

            def deploy = DeployApplication(projectName, [
                    configname : pluginConfigurationNames.correct,
                    wlstabspath: getWlstPath(),
                    appname    : appname,
                    apppath    : "$REMOTE_DIRECTORY/$FILENAME",
                    targets    : 'AdminServer',
                    is_library : ""
            ])

            assert (deploy.outcome == 'success')

            // Delete to prevent [weird] caching in DSL params
            deleteProject(projectName)
        }

        when: 'Procedure runs: '

        def result = undeployApp(runParams, getResourceName())

        then: 'Wait until job run is completed: '

        def outcome = result.outcome
        def debugLog = result.logs

        println "Procedure log:\n$debugLog\n"
//        def upperStepSummary = getJobUpperStepSummary(result.jobId)

        expect: 'Outcome and Upper Summary verification'
        assert outcome == expectedOutcome
        if (expectedOutcome == expectedOutcomes.success && outcome == expectedOutcomes.success) {
            def pageAfterUndeploy = checkUrl(APPLICATION_PAGE_URL)
            assert pageAfterUndeploy.code == NOT_FOUND_RESPONSE
        }

        where: 'The following params will be: '
        configname                       | wlstabspath | appname          | retire_gracefully | version_identifier | give_up | additional_options | expectedOutcome
        pluginConfigurationNames.correct | wlstPath    | APPLICATION_NAME | ''                | ''                 | ''      | ''                 | expectedOutcomes.success

        // Empty wlst path should return "File  doesn't exist'"
        pluginConfigurationNames.correct | ''          | APPLICATION_NAME | ''                | ''                 | ''      | ''                 | expectedOutcomes.error

    }

    def undeployApp(def params, def resourceName) {

        dslFile('dsl/procedures.dsl', [
                projectName  : projectName,
                procedureName: procedureName,
                resourceName : resourceName,
                params       : params
        ])

        // Stringify map
        def params_str_arr = []
        params.each() { k, v ->
            params_str_arr.push(k + " : '" + (v ?: '') + "'")
        }
        logger.debug(params_str_arr.toString())

        def result = runProcedure("""
            runProcedure(
                projectName    : '$projectName',
                procedureName  : '$procedureName',
                actualParameter: $params_str_arr
                
            )
                """, resourceName
        )

        deleteProject(projectName)

        return result
    }

}