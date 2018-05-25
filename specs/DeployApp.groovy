import spock.lang.*
class DeployApp extends WebLogicHelper {

    static def procedureName = 'DeployApp'
    static def projectName = "EC-WebLogic Specs $procedureName"
    static def configName = 'EC-Specs WebLogic Config'

    def doSetupSpec() {
        createConfig(configName)
        setupResource()
    }

    def doCleanupSpec() {
        deleteProject(projectName)
    }

    @Ignore
    def 'Deploy application - positive'() {
        given:
        // Check application don't exists
        def pageBeforeDeploy = checkUrl(APPLICATION_PAGE_URL)

        if (pageBeforeDeploy.code == NOT_FOUND_RESPONSE) {
            def undeploy = UndeployApplication(projectName, [
                    configname        : configName,
                    wlstabspath       : getWlstPath(),
                    appname           : APPLICATION_NAME,

                    retire_gracefully : '',
                    version_identifier: '',
                    give_up           : '',

                    additional_options: ''
            ])
            assert (undeploy.outcome == 'success')
            deleteProject(projectName)
        }

        when:
        def result = DeployApplication(projectName, [
                configname               : configName,
                wlstabspath              : getWlstPath(),
                appname                  : APPLICATION_NAME,
                apppath                  : "$REMOTE_DIRECTORY/$FILENAME",
                targets                  : 'AdminServer',

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
                remote                   : ''
        ])

        then:
        logger.debug("LOGS" + result.logs)

        assert result.outcome == 'success'

        def pageAfterDeploy = checkUrl(APPLICATION_PAGE_URL)
        assert pageAfterDeploy.code == SUCCESS_RESPONSE

        cleanup:
        deleteProject(projectName)
    }

    @Ignore
    def 'Redeploy existing application - positive'() {
        given:
        // Check application don't exists
        def pageBeforeDeploy = checkUrl(APPLICATION_PAGE_URL)

        if (pageBeforeDeploy.code == NOT_FOUND_RESPONSE) {
            def pre_test_deploy = DeployApplication(projectName, [
                    configname               : configName,
                    wlstabspath              : getWlstPath(),
                    appname                  : APPLICATION_NAME,
                    apppath                  : "$REMOTE_DIRECTORY/$FILENAME",
                    targets                  : 'AdminServer',

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
                    remote                   : ''
            ])

            assert (pre_test_deploy.outcome == 'success')
            deleteProject(projectName)
        }

        when:
        def result = DeployApplication(projectName, [
                configname               : configName,
                wlstabspath              : getWlstPath(),
                appname                  : APPLICATION_NAME,
                apppath                  : "$REMOTE_DIRECTORY/$FILENAME",
                targets                  : 'AdminServer',

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
                remote                   : ''
        ])

        then:
        assert result.outcome == 'success'

        def pageAfterDeploy = checkUrl(APPLICATION_PAGE_URL)
        assert pageAfterDeploy.code == SUCCESS_RESPONSE

        cleanup:
        deleteProject(projectName)
    }

    def 'Deploy from unexisting file - negative'() {
        given:
        def file_path = REMOTE_DIRECTORY + '/' + FILENAME + '.notexistent_path.war'
        def application_name = APPLICATION_NAME + '_postfix_for_unexistent_app_name'
        when:
        def result = DeployApplication(projectName, [
                configname               : configName,
                wlstabspath              : getWlstPath(),
                appname                  : application_name,
                apppath                  : "$file_path",
                targets                  : 'AdminServer',

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
                remote                   : ''
        ])

        then:

        assert result.outcome == 'error'
//        assert (result.logs ?: '') =~ /not exists/

        cleanup:
        deleteProject(projectName)
    }

}
