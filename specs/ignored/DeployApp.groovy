package ignored

import com.electriccloud.spec.SpockTestSupport
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


    def 'Deploy application - positive'() {
        given:
        // Check application don't exists
        def pageBeforeDeploy = checkUrl(WebLogicHelper.APPLICATION_PAGE_URL)

        if (pageBeforeDeploy.code == WebLogicHelper.NOT_FOUND_RESPONSE) {
            def undeploy = UndeployApplication(projectName, [
                    configname        : configName,
                    wlstabspath       : WebLogicHelper.getWlstPath(),
                    appname           : WebLogicHelper.APPLICATION_NAME,

                    retire_gracefully : '',
                    version_identifier: '',
                    give_up           : '',

                    additional_options: ''
            ])
            assert (undeploy.outcome == 'success' || undeploy.outcome == 'warning')
            deleteProject(projectName)
        }

        when:
        def result = DeployApplication(projectName, [
                configname               : configName,
                wlstabspath              : WebLogicHelper.getWlstPath(),
                appname                  : WebLogicHelper.APPLICATION_NAME,
                apppath                  : "$WebLogicHelper.REMOTE_DIRECTORY/$WebLogicHelper.FILENAME",
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
        SpockTestSupport.logger.debug("LOGS" + result.logs)

        assert result.outcome == 'success'

        def pageAfterDeploy = checkUrl(WebLogicHelper.APPLICATION_PAGE_URL)
        assert pageAfterDeploy.code == WebLogicHelper.SUCCESS_RESPONSE

        cleanup:
        deleteProject(projectName)
    }


    def 'Redeploy existing application - positive'() {
        given:
        // Check application don't exists
        def pageBeforeDeploy = checkUrl(WebLogicHelper.APPLICATION_PAGE_URL)

        if (pageBeforeDeploy.code == WebLogicHelper.NOT_FOUND_RESPONSE) {
            def pre_test_deploy = DeployApplication(projectName, [
                    configname               : configName,
                    wlstabspath              : WebLogicHelper.getWlstPath(),
                    appname                  : WebLogicHelper.APPLICATION_NAME,
                    apppath                  : "$WebLogicHelper.REMOTE_DIRECTORY/$WebLogicHelper.FILENAME",
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
                wlstabspath              : WebLogicHelper.getWlstPath(),
                appname                  : WebLogicHelper.APPLICATION_NAME,
                apppath                  : "$WebLogicHelper.REMOTE_DIRECTORY/$WebLogicHelper.FILENAME",
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

        def pageAfterDeploy = checkUrl(WebLogicHelper.APPLICATION_PAGE_URL)
        assert pageAfterDeploy.code == WebLogicHelper.SUCCESS_RESPONSE

        cleanup:
        deleteProject(projectName)
    }

    def 'Deploy from unexisting file - negative'() {
        given:
        def file_path = WebLogicHelper.REMOTE_DIRECTORY + '/' + WebLogicHelper.FILENAME + '.notexistent_path.war'
        def application_name = WebLogicHelper.APPLICATION_NAME + '_postfix_for_unexistent_app_name'
        when:
        def result = DeployApplication(projectName, [
                configname               : configName,
                wlstabspath              : WebLogicHelper.getWlstPath(),
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
        assert result.logs =~ /No file or directory found at the specified application path: $file_path/

        cleanup:
        deleteProject(projectName)
    }

}
