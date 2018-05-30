class StopApp extends WebLogicHelper {

    static def procedureName = 'StartApp'
    static def projectName = "EC-WebLogic Specs $procedureName"
    static def configName = CONFIG_NAME

    def doSetupSpec() {
        setupResource()
        createConfig(CONFIG_NAME)
    }

    def doCleanupSpec() {
        deleteProject(projectName)
    }

    def 'Stop application with deploy and start - positive'() {
        given:
        // Application can be not deployed or already stopped
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

        def start = StartApplication(projectName, [
                configname        : configName,
                wlstabspath       : WebLogicHelper.getWlstPath(),
                appname           : WebLogicHelper.APPLICATION_NAME,

                additional_options: "",
                version_identifier: ""
        ])
        assert (start.outcome == 'success' || start.outcome == 'warning')
        deleteProject(projectName)

        when:
        def result = StopApplication(projectName, [
                configname        : configName,
                wlstabspath       : WebLogicHelper.getWlstPath(),
                appname           : WebLogicHelper.APPLICATION_NAME,

                additional_options: "",
                version_identifier: ""
        ])

        then:
        assert result.outcome == 'success'

        def pageAfterDeploy = checkUrl(WebLogicHelper.APPLICATION_PAGE_URL)
        assert pageAfterDeploy.code == WebLogicHelper.NOT_FOUND_RESPONSE
    }

}
