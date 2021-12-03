package com.electriccloud.plugin.spec

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
    def wlstabspath
    def appname
    def static apppath
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

        setupResource()
        createConfig(CONFIG_NAME)

        publishArtifact(artifactName, version, FILENAME)
        String path = downloadArtifact(artifactName, getResourceName())
        apppath = new File(path, FILENAME)

        dslFile "dsl/procedures.dsl", [
            projectName  : projectName,
            resourceName : getResourceName(),
            procedureName: procedureName,
            params       : [
                // Required
                configname               : CONFIG_NAME,
                wlstabspath              : '',
                appname                  : '',
                apppath                  : '',
                targets                  : '',

                // Optional
                is_library               : '',
                stage_mode               : '',
                plan_path                : '',
                deployment_plan          : '',
                overwrite_deployment_plan: '',
                additional_options       : '',
                archive_version          : '',
                retire_gracefully        : '',
                retire_timeout           : '',
                version_identifier       : '',
                upload                   : '',
                remote                   : '',
            ]
        ]

        dslFile('dsl/Application/DeployApp.dsl', [
            resourceName: getResourceName(),
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
    def "Deploy Application. with server '#targets'. Expected : #expectedOutcome : #expectedSummaryMessage"() {
        setup: 'Define the parameters for Procedure running'
        def runParams = [
            // Required
            wlstabspath              : wlstabspath,
            appname                  : appname,
            apppath                  : apppath,
            targets                  : targets,

            // Optional
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
        def pageBeforeDeploy = checkUrl(APPLICATION_PAGE_URL)
        if (pageBeforeDeploy.code == SUCCESS_RESPONSE) {
            def undeploy = undeployApplication(projectName, [
                configname : CONFIG_NAME,
                wlstabspath: wlstPath,
                appname    : appname,
            ])
            assert (undeploy.outcome == 'success')
        }

        when: 'Procedure runs: '

        def result = runTestedProcedure(projectName, procedureName, runParams, getResourceName())

        then: 'Wait until job run is completed: '

        def debugLog = result.logs
        println "Procedure log:\n$debugLog\n"

        expect: 'Outcome and Upper Summary verification'
        assert result.outcome == expectedOutcome

        if (expectedOutcome == expectedOutcomes.success && result.outcome == expectedOutcomes.success) {
            def pageAfterDeploy = checkUrl(APPLICATION_PAGE_URL)
            assert pageAfterDeploy.code == SUCCESS_RESPONSE
        }

        if (expectedSummaryMessage) {
            def upperStepSummary = getJobUpperStepSummary(result.jobId)
            assert upperStepSummary.contains(expectedSummaryMessage)
        }

        where: 'The following params will be: '
        caseId    | wlstabspath | appname          | targets       | is_library               | expectedOutcome          | expectedSummaryMessage
        // Simple positive
        'C325212' | wlstPath    | APPLICATION_NAME | ''            | 'false'                  | expectedOutcomes.success | ''

        // with TargetServerSpecified
        'C325217' | wlstPath    | APPLICATION_NAME | 'AdminServer' | checkBoxValues.unchecked | expectedOutcomes.success | ''

        // Empty wlst path should return "File  doesn't exist"
//        Not applicable any more - will be taken from configuration
//        'C325219' | ''          | APPLICATION_NAME | ''            | checkBoxValues.unchecked | expectedOutcomes.error   | expectedSummaryMessages.file_not_exists
    }


    @Unroll
    def 'DeployApplication - application context'() {
        setup: 'parameters and server state'

        def paramsStr = stringifyArray([
            // Required
            wlstabspath              : wlstabspath,
            appname                  : appname,
            apppath                  : apppath,
            targets                  : targets,

            // Optional
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
            remote                   : remote
        ])

        undeployApplication(projectName, [
            configname : CONFIG_NAME,
            wlstabspath: getWlstPath(),
            appname    : appname
        ])

        when: 'process runs'
        def result = dsl("""
                runProcess(
                    projectName    : "$HELPER_PROJECT",
                    applicationName: "$TEST_APPLICATION",
                    environmentName: '$ENVIRONMENT_NAME',
                    processName    : '$procedureName',
                    actualParameter: $paramsStr
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
            assert pageAfterDeploy.code == SUCCESS_RESPONSE
        }

        where:
        caseId    | wlstabspath | appname          | targets       | is_library               | expectedOutcome          | expectedSummaryMessage
        // Simple positive
        'C325213' | wlstPath    | APPLICATION_NAME | ''            | 'false'                  | expectedOutcomes.success | ''

        // with TargetServerSpecified
        'C325220' | wlstPath    | APPLICATION_NAME | 'AdminServer' | checkBoxValues.unchecked | expectedOutcomes.success | ''
    }

}
