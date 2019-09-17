package com.electriccloud.plugin.spec

import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Shared
import spock.lang.Stepwise

@Ignore("Only for running in local instances, will break the whole suite")
class StartStopServer extends WebLogicHelper {
    @Shared
    String projectName = "EC-WebLogic StopStartServer"

    @Shared
    def scriptsLocation = [
        stopServer        : '/u01/oracle/user_projects/domains/base_domain/bin/stopWebLogic.sh',
        startServer       : '/u01/oracle/user_projects/domains/base_domain/bin/startWebLogic.sh',
        startManagedServer: '/u01/oracle/user_projects/domains/base_domain/bin/startManagedWebLogic.sh',
        stopManagedServer : '/u01/oracle/user_projects/domains/base_domain/bin/stopManagedWebLogic.sh',
    ]

    def doSetupSpec() {
        setupResource()
        createConfig(CONFIG_NAME)
        deleteProject(projectName)

        dslFile "dsl/procedures.dsl", [
            projectName  : projectName,
            resourceName : getResourceName(),
            procedureName: 'StartAdminServer',
            params       : [
                configname       : CONFIG_NAME,
                scriptlocation   : '',
                admininstancename: '',
                wlstabspath      : '',
                maxelapsedtime   : '',
            ]
        ]

        dslFile "dsl/procedures.dsl", [
            projectName  : projectName,
            resourceName : getResourceName(),
            procedureName: 'StopAdminServer',
            params       : [
                configname    : CONFIG_NAME,
                scriptlocation: '',
            ]
        ]

        dslFile "dsl/procedures.dsl", [
            projectName  : projectName,
            resourceName : getResourceName(),
            procedureName: 'StopManagedServer',
            params       : [
                configname    : CONFIG_NAME,
                scriptlocation: '',
                instancename  : ''
            ]
        ]
        dslFile "dsl/procedures.dsl", [
            projectName  : projectName,
            resourceName : getResourceName(),
            procedureName: 'StartManagedServer',
            params       : [
                configname    : CONFIG_NAME,
                scriptlocation: '',
                instancename  : '',
                adminserverurl: '',
                wlstabspath   : '',
                maxelapsedtime: ''
            ]
        ]
    }

    def 'stop admin server'() {
        when:
        def result = runProcedure(projectName, 'StopAdminServer',
            [scriptlocation: scriptsLocation.stopServer], [],
            getResourceName())
        then:
        assert result.outcome != 'error'
    }


    def 'start admin server'() {
        when:
        def result = runProcedure(projectName, 'StartAdminServer', [
            scriptlocation   : scriptsLocation.startServer,
            admininstancename: 'AdminServer',
            wlstabspath      : wlstPath,
            maxelapsedtime   : 40
        ], [], getResourceName())
        then:
        assert result.outcome != 'error'
    }

    def 'stop managed server'() {
        when:
        def result = runProcedure(projectName, 'StopManagedServer', [
            scriptlocation: scriptsLocation.stopManagedServer,
            instancename  : 'AdminServer',
        ], [], resourceName)
        then:
        assert result.outcome != 'error'
    }

    def 'start managed server'() {
        when:
        def result = runProcedure(projectName, 'StartManagedServer', [
            scriptlocation: scriptsLocation.startManagedServer,
            instancename  : 'AdminServer',
            wlstabspath: wlstPath,
            maxelapsedtime: 60,
            adminserverurl: getEndpoint()
        ], [], resourceName)
        then:
        assert result.outcome != 'error'
    }
}
