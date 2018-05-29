import spock.lang.*

class CreateOrUpdateJMSModule extends WebLogicHelper {
    static def projectName = 'EC-WebLogic Specs CreateOrUpdateJMSModule'
    static def configName = 'EC-Specs WebLogic Config'
    static def procedureName = 'CreateOrUpdateJMSModule'
    static def deleteProcedureName = 'DeleteJMSModule'

    static def params = [
        configname: configName,
        ecp_weblogic_jms_module_name: '',
        ecp_weblogic_update_action: 'do_nothing',
        ecp_weblogic_target: ''
    ]

    def doSetupSpec() {
        setupResource()
        deleteProject(projectName)
        createConfig(configName)

        dslFile "dsl/procedures.dsl", [
            projectName: projectName,
            procedureName: procedureName,
            resourceName: getResourceName(),
            params: params,
        ]

        // dslFile 'dsl/procedures.dsl', [
        //     projectName: projectName,
        //     procedureName: deleteProcedureName,
        //     resourceName: getResourceName(),
        //     params: [
        //         configname: configName,
        //         ecp_weblogic_jms_module_name: '',
        //         ecp_weblogic_jms_topic_name: ''
        //     ]
        // ]
    }

    def doCleanupSpec() {
        // deleteProject(projectName)
    }

    def 'create jms module'() {
        given:
        def jmsModuleName = randomize('TestJMSModule')
        when:
        def result = runProcedure("""
        runProcedure(
            projectName: '$projectName',
            procedureName: '$procedureName',
            actualParameter: [
                ecp_weblogic_jms_module_name: '$jmsModuleName',
                ecp_weblogic_target: 'AdminServer',
            ]
        )
        """, getResourceName())
        then:
        logger.debug(result.logs)
        assert result.outcome == 'success'
        cleanup:
        deleteJMSModule(jmsModuleName)
    }

    @Unroll
    def 'update #action jms module'() {
        given:
        def serverName = 'TestSpecServer'
        def jmsModuleName = randomize('SpecModule')
        ensureManagedServer(serverName)
        deleteJMSModule(jmsModuleName)
        def result = runProcedure("""
        runProcedure(
            projectName: '$projectName',
            procedureName: '$procedureName',
            actualParameter: [
                ecp_weblogic_jms_module_name: '$jmsModuleName',
                ecp_weblogic_target: 'AdminServer',
            ]
        )
        """, getResourceName())
        assert result.outcome == 'success'
        when:
        result = runProcedure("""
        runProcedure(
            projectName: '$projectName',
            procedureName: '$procedureName',
            actualParameter: [
                ecp_weblogic_jms_module_name: '$jmsModuleName',
                ecp_weblogic_target: '$serverName',
                ecp_weblogic_update_action: '$action'
            ]
        )
        """, getResourceName())
        then:
        logger.debug(result.logs)
        assert result.outcome == 'success'

        if (action == 'do_nothing') {
            assert result.logs =~ /JMS System Module $jmsModuleName exists, no further action is required/
        }
        else if (action == 'selective_update') {
            assert result.logs =~ /Updated JMS System Module/
        }
        else {
            assert result.logs =~ /Recreated JMS System Module/
        }
        cleanup:
        deleteJMSModule(jmsModuleName)
        where:
        action << ['do_nothing', 'selective_update', 'remove_and_create']
    }

}
