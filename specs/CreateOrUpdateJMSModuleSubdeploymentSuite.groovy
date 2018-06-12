import spock.lang.*

class CreateOrUpdateJMSModuleSubdeploymentSuite extends WebLogicHelper {
    /**
     * Environments Variables
     */
    static String wlstPath = System.getenv('WEBLOGIC_WLST_PATH')

    /**
     * Dsl Parameters
     */

    @Shared
    def procedureName = 'CreateOrUpdateJMSModuleSubdeployment'
    @Shared
    def projectName = "EC-WebLogic ${procedureName}"

    /**
     * Common Maps: General Maps for different fields
     */

    @Shared
    def checkBoxValues = [
        unchecked: '0',
        checked  : '1',
    ]

    /**
     * Parameters for Test Setup
     */

    /**
     * Procedure Values: test parameters Procedure values
     */
    // Required
    @Shared
    def targets = [
        default         : 'AdminServer',
        update          : 'TestSpecServer',
        single          : 'AdminServer',
        twoServers      : 'AdminServer, ManagedServer1',
        cluster         : 'Cluster1',
        nothing         : '',
        serverAndCluster: 'ManagedServer2, Cluster1',
        managedServer   : 'ManagedServer2'
    ]

    @Shared
    def jmsModules = [
        default   : 'TestJMSModule',
        unexistent: 'NoSuchModule'
    ]

    @Shared
    def jmsSubDeploymentNames = [
        empty      : '',
        default    : 'JMSModuleSubdeployment',
        with_spaces: 'JMS Topic Name with spaces',
    ]

    // Optional
    @Shared
    def updateActions = [
        empty            : '',
        do_nothing       : 'do_nothing',
        selective_update : 'selective_update',
        remove_and_create: 'remove_and_create'
    ]

    /**
     * Verification Values: Assert values
     */

    @Shared
    def expectedOutcomes = [
        success: 'success',
        error  : 'error',
        warning: 'warning',
        running: 'running',
    ]

    @Shared
    def expectedSummaryMessages = [
        empty: "",

    ]

    @Shared
    def expectedJobDetailedResults = [
        empty: '',
    ]

    @Shared
    def expectedLogParts = [

    ]

    /**
     * Test Parameters: for Where section
     */

    // Required
    @Shared
    def jmsSubdeploymentName
    @Shared
    def jmsModuleName

    // Optional
    @Shared
    def updateAction
    @Shared
    String target

    // expected results
    def expectedOutcome
    def expectedSummaryMessage
    def expectedJobDetailedResult

    /**
     * Preparation actions
     */

    def doSetupSpec() {
        setupResource()
        deleteProject(projectName)
        createConfig(CONFIG_NAME)

        discardChanges()
        createJMSModule(jmsModules.default)

        dslFile "dsl/procedures.dsl", [
            projectName  : projectName,
            resourceName : getResourceName(),
            procedureName: procedureName,
            params       : [
                configname                            : CONFIG_NAME,
                ecp_weblogic_jms_module_name          : '',
                ecp_weblogic_update_action            : 'do_nothing',
                ecp_weblogic_subdeployment_target_list: '',
                ecp_weblogic_subdeployment_name       : ''
            ]
        ]
    }

    /**
     * Clean Up actions after test will finished
     */

    def doCleanupSpec() {
        // deleteProject(projectName)
    }

    /**
     * Positive Scenarios
     */

    @Unroll
    def "Create JMS Topic. procedure with params (SubDeploymentName: #jmsSubdeploymentName, target: #target, update action: #updateAction)"() {
        setup: 'Define the parameters for Procedure running'

        jmsModuleName = randomize(jmsModules.default)

        def targetList = target.split(/\s*,\s*/)
        targetList.each {
            if (it =~ /Cluster/) {
                println "Creating cluster $it"
                ensureCluster(it)
            } else {
                ensureManagedServer(it, '7999')
            }
        }

        createJMSModule(jmsModuleName, target)

        def runParams = [
            ecp_weblogic_jms_module_name          : jmsModuleName,
            ecp_weblogic_update_action            : updateAction,
            ecp_weblogic_subdeployment_target_list: target,
            ecp_weblogic_subdeployment_name       : jmsSubdeploymentName
        ]

        if (jmsSubdeploymentName && jmsModuleName) {
            deleteSubDeployment(jmsModuleName, jmsSubdeploymentName)
        }

        if (updateAction) {
            createJMSServer(targets.default)
            createSubDeployment(jmsModuleName, jmsSubdeploymentName, targets.default)
        }

        when: 'Procedure runs: '

        def result = runTestedProcedure(projectName, procedureName, runParams, getResourceName())

        then: 'Wait until job run is completed: '

        def debugLog = result.logs

        println "Procedure log:\n$debugLog\n"

        expect: 'Outcome and Upper Summary verification'
        assert result.outcome == expectedOutcome

        if (expectedOutcome == expectedOutcomes.success && result.outcome == expectedOutcomes.success) {
//            assert jmsModuleSubdeploymentExists(jmsModuleName)
        }

        if (expectedJobDetailedResult) {
            assert debugLog.contains(expectedJobDetailedResult)
        }

        if (expectedSummaryMessage) {
            def upperStepSummary = getJobUpperStepSummary(result.jobId)
            assert upperStepSummary.contains(expectedSummaryMessage)
        }

        where: 'The following params will be: '
        updateAction        | jmsSubdeploymentName                     | target                   | expectedOutcome          | expectedSummaryMessage                    | expectedJobDetailedResult
        // Create
        updateActions.empty | jmsSubDeploymentNames.default            | targets.default          | expectedOutcomes.success | "Added 1 target(s), No targets to remove" | ''
        updateActions.empty | randomize(jmsSubDeploymentNames.default) | targets.single           | expectedOutcomes.success | "Added 1 target(s), No targets to remove" | ''
        updateActions.empty | randomize(jmsSubDeploymentNames.default) | targets.cluster          | expectedOutcomes.success | "Added 1 target(s), No targets to remove" | ''
        updateActions.empty | randomize(jmsSubDeploymentNames.default) | targets.twoServers       | expectedOutcomes.success | "Added 2 target(s), No targets to remove" | ''
        updateActions.empty | randomize(jmsSubDeploymentNames.default) | targets.serverAndCluster | expectedOutcomes.success | "Added 2 target(s), No targets to remove" | ''

        // Empty name
        updateActions.empty | randomize(jmsSubDeploymentNames.default) | targets.nothing          | expectedOutcomes.error   | "Target name is not provided"             | 'Failed to create or update JMS Module Subdeployment'
        updateActions.empty | jmsSubDeploymentNames.empty              | targets.default          | expectedOutcomes.error   | 'No Subdeployment name is provided'       | ''
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
        } else if (action == 'selective_update') {
            assert result.logs =~ /Added 1 target\(s\), Removed 1 target\(s\)/
        } else {
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
            } else {
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
        oldTargets    | newTargets
        'AdminServer' | 'Cluster1'
        'AdminServer' | 'ManagedServer1'
        'Cluster1'    | 'ManagedServer1, AdminServer'
        'Cluster1'    | 'ManagedServer1, Cluster1'
    }


}
