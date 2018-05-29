

class StartApp extends WebLogicHelper {

    static def procedureName = 'StartApp'
    static def projectName = "EC-WebLogic Specs $procedureName"
    static def configName = 'EC-Specs WebLogic Config'

    def doSetupSpec() {
        createConfig(configName)
        setupResource()
    }

    def doCleanupSpec() {
        deleteProject(projectName)
    }

    def 'Start application with deploy and stop - positive'() {
        given:
        // Application can be not deployed or already running
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

        def stop = StopApplication(projectName, [
                configname        : configName,
                wlstabspath       : WebLogicHelper.getWlstPath(),
                appname           : WebLogicHelper.APPLICATION_NAME,

                additional_options: "",
                version_identifier: ""
        ])
//
        assert (stop.outcome == 'success' || stop.outcome == 'warning')

        deleteProject(projectName)

        when:
        def result = StartApplication(projectName, [
                configname        : configName,
                wlstabspath       : WebLogicHelper.getWlstPath(),
                appname           : WebLogicHelper.APPLICATION_NAME,

//                envscriptpath     : "",
                additional_options: "",
                version_identifier: ""
        ])

        then:
        assert result.outcome == 'success'

        def pageAfterDeploy = checkUrl(WebLogicHelper.APPLICATION_PAGE_URL)
        assert pageAfterDeploy.code == WebLogicHelper.SUCCESS_RESPONSE
    }

    def 'Start unexisting application - negative'() {
        given:
        deleteProject(projectName)
        def unexisting_app_name = '__' + WebLogicHelper.APPLICATION_NAME + '__un3x1st3nt'

        when:
        def result = StartApplication(projectName, [
                configname        : configName,
                wlstabspath       : WebLogicHelper.getWlstPath(),
                appname           : unexisting_app_name,
                additional_options: "",
                version_identifier: ""
        ])

        then:
        assert result.outcome == 'warning'
    }

}
