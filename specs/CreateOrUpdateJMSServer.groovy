import spock.lang.*

class CreateOrUpdateJMSServer extends WebLogicHelper {
    static def projectName = 'EC-WebLogic Specs CreateOrUpdateJMSServer'
    static def configName = 'EC-Specs WebLogic Config'
    static def procedureName = 'CreateOrUpdateJMSServer'
    static def deleteProcedureName = 'DeleteJMSServer'

    static def params = [
        configname: configName,
        ecp_weblogic_jms_server_name: '',
        ecp_weblogic_update_action: 'do_nothing',
        ecp_weblogic_target: ''
    ]

    def doSetupSpec() {
        setupResource()
        deleteProject(projectName)
        createConfig(configName)
        discardChanges()

        dslFile "dsl/procedures.dsl", [
            projectName: projectName,
            procedureName: procedureName,
            resourceName: getResourceName(),
            params: params,
        ]

        dslFile 'dsl/procedures.dsl', [
            projectName: projectName,
            procedureName: deleteProcedureName,
            resourceName: getResourceName(),
            params: [
                configname: configName,
                ecp_weblogic_jms_server_name: '',
            ]
        ]
    }

    def doCleanupSpec() {
        // deleteProject(projectName)
    }

    @Unroll
    def 'create jms server'() {
        given:
        def jmsServerName = randomize('SpecJMSServer')
        when:
        def result = runProcedure("""
        runProcedure(
            projectName: '$projectName',
            procedureName: '$procedureName',
            actualParameter: [
                ecp_weblogic_jms_server_name: '$jmsServerName',
                ecp_weblogic_target: 'AdminServer',
            ]
        )
        """, getResourceName())
        then:
        logger.debug(result.logs)
        assert result.outcome == 'success'
        cleanup:
        deleteJMSServer(jmsServerName)
    }

    @Unroll
    def 'update #action jms server'() {
        given:
        def serverName = 'TestSpecServer'
        def jmsServer = randomize('SpecJMSServer')
        ensureManagedServer(serverName, '7999')
        deleteJMSServer(jmsServer)
        def result = runProcedure("""
        runProcedure(
            projectName: '$projectName',
            procedureName: '$procedureName',
            actualParameter: [
                ecp_weblogic_jms_server_name: '$jmsServer',
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
                ecp_weblogic_jms_server_name: '$jmsServer',
                ecp_weblogic_target: '$serverName',
                ecp_weblogic_update_action: '$action'
            ]
        )
        """, getResourceName())
        then:
        logger.debug(result.logs)
        assert result.outcome == 'success'

        if (action == 'do_nothing') {
            assert result.logs =~ /JMS Server $jmsServer exists, no further action is required/
        }
        else if (action == 'selective_update') {
            assert result.logs =~ /Updated JMS Server/
        }
        else {
            assert result.logs =~ /Recreated JMS Server/
        }
        cleanup:
        deleteJMSServer(jmsServer)
        where:
        action << ['do_nothing', 'selective_update', 'remove_and_create']
    }

    def 'delete jms server'() {
        given:
        def jmsServerName = randomize('SpecJMSServer')
        def result = runProcedure("""
        runProcedure(
            projectName: '$projectName',
            procedureName: '$procedureName',
            actualParameter: [
                ecp_weblogic_jms_server_name: '$jmsServerName',
                ecp_weblogic_target: 'AdminServer',
            ]
        )
        """, getResourceName())
        assert result.outcome == 'success'
        when:
        result = runProcedure("""
        runProcedure(
            projectName: '$projectName',
            procedureName: '$deleteProcedureName',
            actualParameter: [
                ecp_weblogic_jms_server_name: '$jmsServerName',
            ]
        )
        """, getResourceName())
        then:
        logger.debug(result.logs)
        assert result.outcome == 'success'
        assert result.logs =~ /Removed JMS Server/
    }

    def 'fails to delete non-existing server'() {
        when:
        def result = runProcedure("""
        runProcedure(
            projectName: '$projectName',
            procedureName: '$deleteProcedureName',
            actualParameter: [
                ecp_weblogic_jms_server_name: 'NoSuchServer',
            ]
        )
        """, getResourceName())
        then:
        logger.debug(result.logs)
        assert result.outcome == 'error'
    }

}
