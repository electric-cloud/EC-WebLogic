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
        def pageBeforeDeploy = checkUrl(APPLICATION_PAGE_URL)

        if (pageBeforeDeploy.code == NOT_FOUND_RESPONSE) {
            def deploy = DeployApplication(projectName, [
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
                    remote                   : '',
            ])
            assert (deploy.outcome == 'success' || deploy.outcome == 'warning')
            deleteProject(projectName)
        }

        when:
        deleteProject(projectName)
        def result = UndeployApplication(projectName, [
                configname        : configName,
                wlstabspath       : getWlstPath(),
                appname           : APPLICATION_NAME,

                retire_gracefully : '',
                version_identifier: '',
                give_up           : '',

                additional_options: '',
        ])

        then:
        assert result.outcome == 'success'

        def pageAfterDeploy = checkUrl(APPLICATION_PAGE_URL)
        assert pageAfterDeploy.code == NOT_FOUND_RESPONSE
    }


}
