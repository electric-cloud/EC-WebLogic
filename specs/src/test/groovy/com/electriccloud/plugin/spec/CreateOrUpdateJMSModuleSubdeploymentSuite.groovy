package com.electriccloud.plugin.spec

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
        with_spaces: 'Name with spaces',
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
    @Shared
    def caseId
    @Shared
    def oldTargets
    @Shared
    def newTargets

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
                ecp_weblogic_update_action            : '',
                ecp_weblogic_subdeployment_target_list: '',
                ecp_weblogic_subdeployment_name       : ''
            ]
        ]

        dslFile("dsl/Application/CreateOrUpdateJMSModuleSubdeployment.dsl", [
            resourceName: getResourceName()
        ])
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
    def "#caseId. Create JMS Subdeployment. (SubDeploymentName: #jmsSubdeploymentName, target: #target) - procedure"() {
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

        when: 'Procedure runs: '

        def result = runTestedProcedure(projectName, procedureName, runParams, getResourceName())

        then: 'Wait until job run is completed: '

        def debugLog = result.logs

        println "Procedure log:\n$debugLog\n"

        expect: 'Outcome and Upper Summary verification'
        assert result.outcome == expectedOutcome

        if (expectedJobDetailedResult) {
            assert debugLog.contains(expectedJobDetailedResult)
        }
        checkServerRestartOutputParameter(result.jobId)

        if (expectedSummaryMessage) {
            def upperStepSummary = getJobUpperStepSummary(result.jobId)
            assert upperStepSummary.contains(expectedSummaryMessage)
        }

        where: 'The following params will be: '
        caseId    | jmsSubdeploymentName                     | target                   | expectedOutcome          | expectedSummaryMessage                    | expectedJobDetailedResult
        // Create
        'C325053' | jmsSubDeploymentNames.default            | targets.default          | expectedOutcomes.success | "Added 1 target(s), No targets to remove" | ''
        'C325054' | jmsSubDeploymentNames.with_spaces        | targets.default          | expectedOutcomes.success | "Added 1 target(s), No targets to remove" | ''
        'C325055' | randomize(jmsSubDeploymentNames.default) | targets.single           | expectedOutcomes.success | "Added 1 target(s), No targets to remove" | ''
        'C325056' | randomize(jmsSubDeploymentNames.default) | targets.cluster          | expectedOutcomes.success | "Added 1 target(s), No targets to remove" | ''
        'C325057' | randomize(jmsSubDeploymentNames.default) | targets.twoServers       | expectedOutcomes.success | "Added 2 target(s), No targets to remove" | ''
        'C325058' | randomize(jmsSubDeploymentNames.default) | targets.serverAndCluster | expectedOutcomes.success | "Added 2 target(s), No targets to remove" | ''
        /// Negative create
        'C325065' | randomize(jmsSubDeploymentNames.default) | targets.nothing          | expectedOutcomes.error   | "Target name is not provided"             | 'Failed to create or update JMS Module Subdeployment'
        'C325066' | jmsSubDeploymentNames.empty              | targets.default          | expectedOutcomes.error   | 'No Subdeployment name is provided'       | ''
    }

    @Unroll
    def "#caseId. Create JMS Subdeployment. (SubDeploymentName: #jmsSubdeploymentName, target: #target, update action: #updateAction) - application"() {
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

        def paramsStr = stringifyArray([
            ecp_weblogic_jms_module_name          : jmsModuleName,
            ecp_weblogic_update_action            : updateAction,
            ecp_weblogic_subdeployment_target_list: target,
            ecp_weblogic_subdeployment_name       : jmsSubdeploymentName
        ])

        if (jmsSubdeploymentName && jmsModuleName) {
            deleteSubDeployment(jmsModuleName, jmsSubdeploymentName)
        }

        when: 'process runs'
        def result = dsl("""
                runProcess(
                    projectName    : "$HELPER_PROJECT",
                    applicationName: "$TEST_APPLICATION",
                    environmentName: '$ENVIRONMENT_NAME',
                    processName    : '$procedureName',
                    actualParameter:  $paramsStr
                )
            """, [resourceName: getResourceName()])

        then: 'wait until process finishes'
        waitUntil {
            jobCompleted result
        }

        def logs = getJobLogs(result.jobId)
        logger.debug("Process logs: " + logs)

        def outcome = jobStatus(result.jobId).outcome
        assert outcome == expectedOutcome

        if (outcome == 'success' && expectedJobDetailedResult) {
            assert logs.contains(expectedJobDetailedResult)
        }

        where: '#caseId. The following params will be: '
        caseId    | jmsSubdeploymentName                     | target                   | expectedOutcome          | expectedSummaryMessage                    | expectedJobDetailedResult
        // Create
        'C325067' | jmsSubDeploymentNames.default            | targets.default          | expectedOutcomes.success | "Added 1 target(s), No targets to remove" | ''
        'C325068' | jmsSubDeploymentNames.with_spaces        | targets.default          | expectedOutcomes.success | "Added 1 target(s), No targets to remove" | ''
        'C325069' | randomize(jmsSubDeploymentNames.default) | targets.single           | expectedOutcomes.success | "Added 1 target(s), No targets to remove" | ''
        'C325070' | randomize(jmsSubDeploymentNames.default) | targets.cluster          | expectedOutcomes.success | "Added 1 target(s), No targets to remove" | ''
        'C325071' | randomize(jmsSubDeploymentNames.default) | targets.twoServers       | expectedOutcomes.success | "Added 2 target(s), No targets to remove" | ''
        'C325072' | randomize(jmsSubDeploymentNames.default) | targets.serverAndCluster | expectedOutcomes.success | "Added 2 target(s), No targets to remove" | ''

        // Negative
        'C325073'        | randomize(jmsSubDeploymentNames.default) | targets.nothing          | expectedOutcomes.error   | "Target name is not provided"             | 'Failed to create or update JMS Module Subdeployment'
        'C325074'        | jmsSubDeploymentNames.empty              | targets.default          | expectedOutcomes.error   | 'No Subdeployment name is provided'       | ''
    }

    @Unroll
    def '#caseId. Update #updateAction jms subdeployment - procedure'() {
        given:

        def serverName = 'TestSpecServer'
        def jmsModuleName = randomize('SpecModule')
        ensureManagedServer(serverName, '7999')
        createJMSModule(jmsModuleName, 'AdminServer, ' + serverName)

        def subdeploymentName = 'sub1'
        def createResult = runProcedure("""
        runProcedure(
            projectName:   '$projectName',
            procedureName: '$procedureName',
            actualParameter: [
                ecp_weblogic_jms_module_name:           '$jmsModuleName',
                ecp_weblogic_subdeployment_target_list: 'AdminServer',
                ecp_weblogic_subdeployment_name:        '$subdeploymentName'
            ]
        )
        """, getResourceName())
        assert createResult.outcome == 'success'

        when:
        def result = runProcedure("""
        runProcedure(
            projectName: '$projectName',
            procedureName: '$procedureName',
            actualParameter: [
                ecp_weblogic_jms_module_name:           '$jmsModuleName',
                ecp_weblogic_subdeployment_target_list: '$serverName',
                ecp_weblogic_update_action:             '$updateAction',
                ecp_weblogic_subdeployment_name:        '$subdeploymentName'
            ]
        )
        """, getResourceName())

        then:
        logger.debug(result.logs)
        assert result.outcome == 'success'

        if (updateAction == 'do_nothing') {
            assert result.logs =~ /SubDeployment $subdeploymentName exists in the module $jmsModuleName, no further action is required/
        } else if (updateAction == 'selective_update') {
            assert result.logs =~ /Added 1 target\(s\), Removed 1 target\(s\)/
        } else if (updateAction == 'remove_and_create') {
            assert result.logs =~ /Added 1 target\(s\)/
        }
        cleanup:
        deleteJMSModule(jmsModuleName)

        where:
        caseId    | updateAction
        'C325075' | 'do_nothing'
        'C325076' | 'selective_update'
        'C325077' | 'remove_and_create'
    }

    @Unroll
    def '#caseId. Change the list of targets #oldTargets -> #newTargets - procedure'() {
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

        createJMSModule(jmsModuleName, jmsModuleTargets)

        def subdeploymentName = 'sub1'

        def result = runProcedure """
        runProcedure(
            projectName: '$projectName',
            procedureName: '$procedureName',
            actualParameter: [
                ecp_weblogic_jms_module_name:           '$jmsModuleName',
                ecp_weblogic_subdeployment_target_list: '$oldTargets',
                ecp_weblogic_update_action:             'selective_update',
                ecp_weblogic_subdeployment_name:        '$subdeploymentName'
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
                ecp_weblogic_jms_module_name:           '$jmsModuleName',
                ecp_weblogic_subdeployment_target_list: '$newTargets',
                ecp_weblogic_update_action:             'selective_update',
                ecp_weblogic_subdeployment_name:        '$subdeploymentName'
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
        caseId    | oldTargets    | newTargets
        'C325079' | 'AdminServer' | 'Cluster1'
        'C325081' | 'AdminServer' | 'ManagedServer1'
        'C325082' | 'Cluster1'    | 'ManagedServer1, AdminServer'
        'C325083' | 'Cluster1'    | 'ManagedServer1, Cluster1'
    }


}
