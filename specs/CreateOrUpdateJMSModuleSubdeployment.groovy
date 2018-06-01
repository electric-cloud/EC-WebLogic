import spock.lang.*

class CreateOrUpdateJMSModuleSubdeployment extends WebLogicHelper {
    static def projectName = 'EC-WebLogic Specs CreateOrUpdateJMSModuleSubdeployment'
    static def configName = 'EC-Specs WebLogic Config'
    static def procedureName = 'CreateOrUpdateJMSModuleSubdeployment'
    static def deleteProcedureName = 'DeleteJMSModuleSubdeployment'

    static def params = [
        configname: configName,
        ecp_weblogic_jms_module_name: '',
        ecp_weblogic_update_action: 'do_nothing',
        ecp_weblogic_subdeployment_target_list: '',
        ecp_weblogic_subdeployment_name: ''
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
                ecp_weblogic_jms_module_name: '',
                ecp_weblogic_subdeployment_name: ''
            ]
        ]
    }

    def doCleanupSpec() {
        // deleteProject(projectName)
    }

    @Unroll
    def 'create subdeployment target #targets'() {
        given:
        def jmsModuleName = randomize('TestJMSModule')
        def subdeploymentName = 'sub1'
        deleteJMSModule(jmsModuleName)
        def targetList = targets.split(/\s*,\s*/)
        targetList.each {
            if (it =~ /Cluster/) {
                println "Creating cluster $it"
                ensureCluster(it)
            }
            else {
                ensureManagedServer(it, '7999')
            }
        }
        createJMSModule(jmsModuleName, targets)
        when:
        def result = runProcedure("""
        runProcedure(
            projectName: '$projectName',
            procedureName: '$procedureName',
            actualParameter: [
                ecp_weblogic_jms_module_name: '$jmsModuleName',
                ecp_weblogic_subdeployment_target_list: '$targets',
                ecp_weblogic_subdeployment_name: '$subdeploymentName',
            ]
        )
        """, getResourceName())
        then:
        logger.debug(result.logs)
        assert result.outcome == 'success'
        assert result.logs =~ /Created SubDeployment $subdeploymentName in the module $jmsModuleName/
        assert result.logs =~ /No targets to remove/
        assert result.logs =~ /Added ${targetList.size()} target/
        targetList.each {
            def targetName = ''
            if (it =~ /Cluster/) {
                targetName = "Cluster \"${it}\""
            }
            else {
                targetName = "Server \"${it}\""
            }
            assert result.logs =~ /Adding target $targetName to the list of targets/
        }

        cleanup:
        deleteJMSModule(jmsModuleName)
        where:
        targets << ['AdminServer', 'TestManagedServer', 'TestCluster', 'TestMSServer1, TestMSServer2', 'TestCluster, TestManagedServer']
    }

    @Unroll
    def 'update #action jms subdeployment'() {
        given:
        def serverName = 'TestSpecServer'
        def jmsModuleName = randomize('SpecModule')
        ensureManagedServer(serverName, '7999')
        deleteJMSModule(jmsModuleName)
        createJMSModule(jmsModuleName, 'AdminServer, ' + serverName)
        def subdeploymentName = 'sub1'
        def result = runProcedure("""
        runProcedure(
            projectName: '$projectName',
            procedureName: '$procedureName',
            actualParameter: [
                ecp_weblogic_jms_module_name: '$jmsModuleName',
                ecp_weblogic_subdeployment_target_list: 'AdminServer',
                ecp_weblogic_subdeployment_name: '$subdeploymentName'
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
                ecp_weblogic_subdeployment_target_list: '$serverName',
                ecp_weblogic_update_action: '$action',
                ecp_weblogic_subdeployment_name: '$subdeploymentName'
            ]
        )
        """, getResourceName())
        then:
        logger.debug(result.logs)
        assert result.outcome == 'success'

        if (action == 'do_nothing') {
            assert result.logs =~ /SubDeployment $subdeploymentName exists in the module $jmsModuleName, no further action is required/
        }
        else if (action == 'selective_update') {
            assert result.logs =~ /Added 1 target\(s\), Removed 1 target\(s\)/
        }
        else {
            assert result.logs =~ /Added 1 target\(s\)/
        }
        cleanup:
        deleteJMSModule(jmsModuleName)
        where:
        action << ['do_nothing', 'selective_update', 'remove_and_create']
    }

    @Unroll
    def 'change the list of targets #oldTargets -> #newTargets'() {
        given:
        def jmsModuleName = randomize('SpecJMSModule')
        def jmsModuleTargets = oldTargets + ',' + newTargets
        jmsModuleTargets.split(/\s*,\s*/).each {
            if (it =~ /Cluster/) {
                println 'Creating cluster'
                ensureCluster(it)
            }
            else {
                ensureManagedServer(it, '7999')
            }
        }
        deleteJMSModule(jmsModuleName)
        createJMSModule(jmsModuleName, jmsModuleTargets)
        def subdeploymentName = 'sub1'
        def result = runProcedure """
        runProcedure(
            projectName: '$projectName',
            procedureName: '$procedureName',
            actualParameter: [
                ecp_weblogic_jms_module_name: '$jmsModuleName',
                ecp_weblogic_subdeployment_target_list: '$oldTargets',
                ecp_weblogic_update_action: 'selective_update',
                ecp_weblogic_subdeployment_name: '$subdeploymentName'
            ]
        )
        """, getResourceName()
        assert result.outcome == 'success'
        when:
        result = runProcedure """
        runProcedure(
            projectName: '$projectName',
            procedureName: '$procedureName',
            actualParameter: [
                ecp_weblogic_jms_module_name: '$jmsModuleName',
                ecp_weblogic_subdeployment_target_list: '$newTargets',
                ecp_weblogic_update_action: 'selective_update',
                ecp_weblogic_subdeployment_name: '$subdeploymentName'
            ]
        )
        """, getResourceName()
        then:
        logger.debug(result.logs)
        assert result.outcome == 'success'
        // TODO check actual targets
        cleanup:
        deleteJMSModule(jmsModuleName)
        where:
        oldTargets               | newTargets
        'AdminServer'            | 'Cluster1'
        'AdminServer'            | 'ManagedServer1'
        'Cluster1'               | 'ManagedServer1, AdminServer'
        'Cluster1'               | 'ManagedServer1, Cluster1'
    }


    def 'delete subdeployment'() {
        given:
        def jmsModuleName = randomize('SpecModule')
        createJMSModule(jmsModuleName)
        def subdeploymentName = 'sub1'
        def result = runProcedure """
        runProcedure(
            projectName: '$projectName',
            procedureName: '$procedureName',
            actualParameter: [
                ecp_weblogic_jms_module_name: '$jmsModuleName',
                ecp_weblogic_subdeployment_target_list: 'AdminServer',
                ecp_weblogic_update_action: 'selective_update',
                ecp_weblogic_subdeployment_name: '$subdeploymentName'
            ]
        )
        """, getResourceName()
        assert result.outcome == 'success'
        when:
        result = runProcedure("""
        runProcedure(
            projectName: '$projectName',
            procedureName: '$deleteProcedureName',
            actualParameter: [
                ecp_weblogic_jms_module_name: '$jmsModuleName',
                ecp_weblogic_subdeployment_name: '$subdeploymentName'
            ]
        )
        """, getResourceName())
        then:
        logger.debug(result.logs)
        assert result.outcome == 'success'
    }

    def 'fails to delete non-existing subdeployment'() {
        given:
        def jmsModuleName = 'SpecJMSModule'
        createJMSModule(jmsModuleName)
        def subdeploymentName = 'sub1'
        when:
        def result = runProcedure("""
        runProcedure(
            projectName: '$projectName',
            procedureName: '$deleteProcedureName',
            actualParameter: [
                ecp_weblogic_jms_module_name: '$jmsModuleName',
                ecp_weblogic_subdeployment_name: '$subdeploymentName'
            ]
        )
        """, getResourceName())
        then:
        logger.debug(result.logs)
        assert result.outcome == 'error'
        cleanup:
        deleteJMSModule(jmsModuleName)
    }


    def 'fails to associate a target which is not a subtarget of jms module'() {
        given:
        def jmsModuleName = randomize('SpecModule')
        deleteJMSModule(jmsModuleName)
        createJMSModule(jmsModuleName, 'AdminServer')
        def msName = 'TestManagedServer'
        ensureManagedServer(msName, '7999')
        def subdeploymentName = 'sub1'
        when:
        def result = runProcedure("""
        runProcedure(
            projectName: '$projectName',
            procedureName: '$procedureName',
            actualParameter: [
                ecp_weblogic_jms_module_name: '$jmsModuleName',
                ecp_weblogic_subdeployment_target_list: '$msName',
                ecp_weblogic_subdeployment_name: '$subdeploymentName',
            ]
        )
        """, getResourceName())
        then:
        assert result.outcome == 'error'
        assert result.logs =~ /For example, the target "$msName" is not a subtarget of "AdminServer"/
        cleanup:
        deleteJMSModule(jmsModuleName)
    }

    def targetName(target) {
        if (target =~ /Cluster/) {
            return 'Cluster "' + target +'"'
        }
        else {
            return 'Server "' + target + '"'
        }
    }

}
