class UndeployApp extends WebLogicHelper {

    static def procedureName = 'UndeployApp'
    static def projectName = "EC-WebLogic Specs $procedureName"
    static def configName = 'EC-Specs WebLogic Config'
    static def params = [
            configname               : configName,
            wlstabspath              : getWlstPath(),
            appname                  : 'sample',

            apppath                  : "",
            targets                  : '',
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

    def doSetupSpec() {
        createConfig(configName)

        dslFile "dsl/procedures.dsl", [
                projectName: projectName,
                procedureName: procedureName,
                resourceName: getResourceName(),
                params: params
        ]

    }

    def 'Undeploy application'() {
        given:
        // Check application don't exists
        def pageBeforeDeploy = checkUrl("http://localhost:7001/sample/hello.jsp", getResourceName())

        assert pageBeforeDeploy.code == SUCCESS_RESPONSE

        def applicationName = 'sample'

        when:
        def result = runProcedure("""
        runProcedure(
            projectName: '$projectName',
            procedureName: '$procedureName',
            actualParameter: [
                 configname : '$configName',
                 wlstabspath: '$wlstPath',
                 appname : '$applicationName',
                 targets : '',
            ]
        )
        """, getResourceName())
        then:
        assert result.outcome == 'success'

        def pageAfterDeploy = checkUrl("http://localhost:7001/sample/hello.jsp", getResourceName())
        assert pageAfterDeploy.code != SUCCESS_RESPONSE
    }

}
