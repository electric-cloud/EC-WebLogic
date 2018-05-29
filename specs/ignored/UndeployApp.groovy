package ignored

class UndeployApp extends WebLogicHelper {

    static def procedureName = 'UndeployApp'
    static def projectName = "EC-WebLogic Specs $procedureName"
    static def configName = 'EC-Specs WebLogic Config'


    def doSetupSpec() {
        setupResource()
        createConfig(configName)
    }

    def doCleanupSpec() {
        deleteProject(projectName)
    }

    def 'Undeploy application'() {

        given:
        // Check application don't exists
        def pageBeforeDeploy = checkUrl(WebLogicHelper.APPLICATION_PAGE_URL)

        if (pageBeforeDeploy.code == WebLogicHelper.NOT_FOUND_RESPONSE) {
            def deploy = DeployApplication(projectName, [
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
                    remote                   : '',
            ])
            assert (deploy.outcome == 'success' || deploy.outcome == 'warning')
            deleteProject(projectName)
        }

        when:
        deleteProject(projectName)
        def result = UndeployApplication(projectName, [
                configname        : configName,
                wlstabspath       : WebLogicHelper.getWlstPath(),
                appname           : WebLogicHelper.APPLICATION_NAME,

                retire_gracefully : '',
                version_identifier: '',
                give_up           : '',

                additional_options: '',
        ])

        then:
        assert result.outcome == 'success'

        def pageAfterDeploy = checkUrl(WebLogicHelper.APPLICATION_PAGE_URL)
        assert pageAfterDeploy.code == WebLogicHelper.NOT_FOUND_RESPONSE
    }


}
