class UndeployApp extends WebLogicHelper {

    static def procedureName = 'UndeployApp'
    static def projectName = "EC-WebLogic Specs $procedureName"
    static def configName = 'EC-Specs WebLogic Config'
    static def params = [
            configname        : configName,
            wlstabspath       : getWlstPath(),
            appname           : 'sample',

            retire_gracefully : '',
            version_identifier: '',
            give_up           : '',

            additional_options: '',
    ]

    def doSetupSpec() {
        createConfig(configName)

        dslFile "dsl/procedures.dsl", [
                projectName  : projectName,
                procedureName: procedureName,
                resourceName : getResourceName(),
                params       : params
        ]

    }

    def 'Undeploy application'() {
        given:
        // Check application don't exists
        def pageBeforeDeploy = checkUrl("http://localhost:7001/sample/hello.jsp", getResourceName())


        def applicationPath = "$REMOTE_DIRECTORY/$FILENAME"

        if (pageBeforeDeploy.code != SUCCESS_RESPONSE) {
            DeployApplication(APPLICATION_NAME, "$applicationPath")
        }

        when:
        def result = runProcedure("""
        runProcedure(
            projectName: '$projectName',
            procedureName: '$procedureName',
            actualParameter: [
                 configname : '$configName',
                 wlstabspath: '$wlstPath',
                 appname : '$APPLICATION_NAME'
            ]
        )
        """, getResourceName())
        then:
        assert result.outcome == 'success'

        def pageAfterDeploy = checkUrl("http://localhost:7001/sample/hello.jsp", getResourceName())
        assert pageAfterDeploy.code != SUCCESS_RESPONSE
    }

    def DeployApplication(
            def applicationName = APPLICATION_NAME, def applicationWarPath = "$REMOTE_DIRECTORY/$FILENAME") {

        def result = runProcedure("""
        runProcedure(
            projectName: '/plugins/EC-WebLogic/project',
            procedureName: 'DeployApp',
            resourceName: '${getResourceName()}',
            actualParameter: [
                 configname : '$configName',
                 wlstabspath: '$wlstPath',
                 appname : '$applicationName',
                 apppath : "$applicationWarPath",
                 targets : 'AdminServer',
                 is_library : ""
            ]
        )
        """, getResourceName())

        return result.outcome == 'success'
    }
}
