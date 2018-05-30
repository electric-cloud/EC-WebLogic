import spock.lang.*

@Stepwise
class DeployAppSuite extends WebLogicHelper {

    /**
     * Environments Variables
     */
    static String wlstPath = System.getenv('WEBLOGIC_WLST_PATH')

    /**
     * Dsl Parameters
     */

    @Shared
    def procedureName = 'DeployApp'
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

//    @Shared
//    //* Optional Parameter
//    def additionalOptions = [
//            empty    : '',
//            correct  : '-subscriptionDurability Durable',
//            incorrect: 'incorrect Additional Options',
//    ]
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
    def apppath
    def targets

    //optional parameters
    def is_library
    def stage_mode
    def plan_path
    def deployment_plan
    def overwrite_deployment_plan
    def additional_options
    def archive_version
    def retire_gracefully
    def retire_timeout
    def version_identifier
    def upload
    def remote

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

        createConfig(pluginConfigurationNames.correct)

        publishArtifact(artifactName, version, FILENAME)
        downloadArtifact(artifactName, REMOTE_DIRECTORY, getResourceName())
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
    //Positive Scenarios for delete should be first
    def "Deploy Application. Positive Scenarios and Extended Scenarios"() {
        setup: 'Define the parameters for Procedure running'
        def runParams = [
                configname               : configname,
                wlstabspath              : wlstabspath,
                appname                  : appname,
                apppath                  : apppath,
                targets                  : targets,

                is_library               : is_library,
                stage_mode               : stage_mode,
                plan_path                : plan_path,
                deployment_plan          : deployment_plan,
                overwrite_deployment_plan: overwrite_deployment_plan,
                additional_options       : additional_options,
                archive_version          : archive_version,
                retire_gracefully        : retire_gracefully,
                retire_timeout           : retire_timeout,
                version_identifier       : version_identifier,
                upload                   : upload,
                remote                   : remote,
        ]

        // Check that application is not installed already
        // Check application don't exists
        def pageBeforeDeploy = checkUrl(APPLICATION_PAGE_URL)
        if (pageBeforeDeploy.code == SUCCESS_RESPONSE) {
            def undeploy = UndeployApplication(projectName, [
                    configname : configname,
                    wlstabspath: wlstPath,
                    appname    : appname,
            ])
            assert (undeploy.outcome == 'success')

            // Delete to prevent [weird] caching in DSL params
            deleteProject(projectName)
        }

        when: 'Procedure runs: '

        def result = runTestedProcedure(projectName, procedureName, runParams, getResourceName())

        then: 'Wait until job run is completed: '

        def outcome = result.outcome
        def debugLog = result.logs

        println "Procedure log:\n$debugLog\n"
//        def upperStepSummary = getJobUpperStepSummary(result.jobId)

        def upperStepSummary = getJobUpperStepSummary(result.jobId)
        logger.info("[SUMMARY]" + upperStepSummary)

        expect: 'Outcome and Upper Summary verification'
        assert outcome == expectedOutcome
        if (expectedOutcome == expectedOutcomes.success && outcome == expectedOutcomes.success) {
            def pageAfterDeploy = checkUrl(APPLICATION_PAGE_URL)
            assert pageAfterDeploy.code == SUCCESS_RESPONSE
        }
        if (expectedSummaryMessage){
            assert upperStepSummary.contains(expectedSummaryMessage)
        }

        where: 'The following params will be: '
        configname                       | wlstabspath | appname          | apppath                       | targets       | is_library               | stage_mode | plan_path | deployment_plan | overwrite_deployment_plan | additional_options | archive_version | retire_gracefully | retire_timeout | version_identifier | upload | remote | expectedOutcome          | expectedSummaryMessage
        pluginConfigurationNames.correct | wlstPath    | APPLICATION_NAME | "$REMOTE_DIRECTORY/$FILENAME" | ''            | ''                       | ''         | ''        | ''              | ''                        | ''                 | ''              | ''                | ''             | ''                 | ''     | ''     | expectedOutcomes.success | ''
        //with TargetServerSpecified
        pluginConfigurationNames.correct | wlstPath    | APPLICATION_NAME | "$REMOTE_DIRECTORY/$FILENAME" | 'AdminServer' | checkBoxValues.unchecked | ''         | ''        | ''              | ''                        | ''                 | ''              | ''                | ''             | ''                 | ''     | ''     | expectedOutcomes.success | ''

        // Empty wlst path should return "File  doesn't exist"
        pluginConfigurationNames.correct | ''          | APPLICATION_NAME | "$REMOTE_DIRECTORY/$FILENAME" | ''            | checkBoxValues.unchecked | ''         | ''        | ''              | ''                        | ''                 | ''              | ''                | ''             | ''                 | ''     | ''     | expectedOutcomes.error   | expectedSummaryMessages.file_not_exists

    }



    def deployApp(def params, def resourceName) {

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
                projectName: '$projectName',
                procedureName: '$procedureName',
                actualParameter: $params_str_arr
                
            )
                """, resourceName
        )

        deleteProject(projectName)

        return result
    }
}