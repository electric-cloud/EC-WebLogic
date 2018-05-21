class DeployApp extends WebLogicHelper {

    static def artifactName = 'test:sample'
    static def version = '1.0'

    static def weblogicResource = getResourceName()

    static def procedureName = 'DeployApp'
    static def projectName = "EC-WebLogic Specs $procedureName"
    static def configName = 'EC-Specs WebLogic Config'
    static def params = [
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
            remote                   : '',
    ]

    // TODO: check what environment is used
    def static serverHostname = System.getProperty('COMMANDER_SERVER') ?: '10.200.1.150'


    def doSetupSpec() {
        createConfig(configName)

        setupRepository(serverHostname)
        setupResource(weblogicResource)

        publishArtifact(artifactName, version, FILENAME)

        dslFile "dsl/procedures.dsl", [
                projectName: projectName,
                procedureName: procedureName,
                resourceName: getResourceName(),
                params: params
        ]

        downloadArtifact(artifactName, REMOTE_DIRECTORY, weblogicResource)

//        // Check page procedure
//        def targetUrl = 'http://weblogic:7001/sample/hello.jsp'
//        dslFile "dsl/procedures.dsl", [
//                projectName: projectName,
//                subProjectName : '/plugins/EC-WebLogic/project',
//                procedureName: 'CheckPageStatus',
//                params: [
//                    successcriteria : 'pagefound',
//                    targeturl      : targetUrl
//                ]
//        ]
    }

    def 'Deploy application'() {
        given:
        // Check application don't exists
        def pageBeforeDeploy = checkUrl("http://localhost:7001/sample/hello.jsp", getResourceName())

        if (pageBeforeDeploy.code != SUCCESS_RESPONSE){
            undeployApplication(APPLICATION_NAME)
        }

        def applicationWarPath = "$REMOTE_DIRECTORY/$FILENAME"
        when:
        def result = runProcedure("""
        runProcedure(
            projectName: '$projectName',
            procedureName: '$procedureName',
            actualParameter: [
                 configname : '$configName',
                 wlstabspath: '$wlstPath',
                 appname : '$APPLICATION_NAME',
                 apppath : "$applicationWarPath",
                 targets : 'AdminServer',
            ]
        )
        """, getResourceName())
        then:
//        logger.debug(result.logs)
        assert result.outcome == 'success'

        def pageAfterDeploy = checkUrl("http://localhost:7001/sample/hello.jsp", getResourceName())
        assert pageAfterDeploy.code == SUCCESS_RESPONSE
    }


//    def 'Check deployment'() {
//        given:
//        def pr = 1
//
//        when:
//        def result = runProcedure("""
//          runProcedure(
//            projectName: '$projectName',
//            procedureName : 'CheckPageStatus',
//              actualParameter: [
//                successcriteria : 'pagefound',
//                targeturl      : '$targetUrl'
//              ]
//          )""", getResourceName())
//        then:
//        logger.debug(result.logs)
//        assert result.outcome == 'success'
//    }

    // TODO: remove ( Will be set by Build procedure )
    def setupRepository(String serverHost) {
        dsl """
        repository 'default', {
            url = "https://${serverHost}:8200"
        }
        """
    }

    def setupResource(String resourceHostName) {
        dsl """
          resource '$resourceHostName', {
            hostName = '$resourceHostName'
            port = 7808
          }
        """
    }

    def undeployApplication(def applicationName = APPLICATION_NAME){
        def result = runProcedure("""
        runProcedure(
            projectName: '/plugins/EC-WebLogic/project',
            procedureName: 'UndeployApp',
            resourceName: '${getResourceName()}',
            actualParameter: [
                 configname : '$configName',
                 wlstabspath: '$wlstPath',
                 appname : '$applicationName'
            ]
        )
        """, getResourceName())

        return result.outcome == 'success'
    }
}
