import spock.lang.Ignore

class DeployApp extends WebLogicHelper {

    static def artifactName = 'test:sample'
    static def version = '1.0'

    static def procedureName = 'DeployApp'
    static def projectName = "EC-WebLogic Specs $procedureName"
    static def configName = 'EC-Specs WebLogic Config'

    def doSetupSpec() {
        createConfig(configName)

        setupResource()

        publishArtifact(artifactName, version, FILENAME)
        downloadArtifact(artifactName, REMOTE_DIRECTORY, getResourceName())
    }

//    @Ignore
    def 'Deploy application'() {
        given:
        // Check application don't exists
        def pageBeforeDeploy = checkUrl("http://localhost:7001/sample/hello.jsp")

        if (pageBeforeDeploy.code == NOT_FOUND_RESPONSE) {
            deleteProject(projectName)
            UndeployApplication(projectName, [
                    configname        : configName,
                    wlstabspath       : getWlstPath(),
                    appname           : 'sample',

                    retire_gracefully : '',
                    version_identifier: '',
                    give_up           : '',

                    additional_options: ''
            ])
        }

        when:
        deleteProject(projectName)
        def result = DeployApplication(projectName, [
                configname               : configName,
                wlstabspath              : getWlstPath(),
                appname                  : 'sample',
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

        def pageAfterDeploy = checkUrl("http://localhost:7001/sample/hello.jsp")
        assert pageAfterDeploy.code == SUCCESS_RESPONSE
    }

}
