import spock.lang.*

class CreateOrUpdateJMSQueueSuite extends WebLogicHelper {
    /**
     * Dsl Parameters
     */

    @Shared
    def procedureName = 'CreateOrUpdateJMSQueue'
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
    @Shared
    def jmsQueues = [
        default    : 'SpecQueue',
        updated    : 'SpecUpdatedQueue',
        nonexisting: 'NoSuchQueue'
    ]

    @Shared
    def jndiNames = [
        empty      : '',
        correct    : 'TestJNDIName',
        recreateOld: 'OldJNDIName',
        recreateNew: 'NewJNDIName',
    ]

    @Shared
    def targets = [
        default: 'JMSServer1',
        update : 'JMSServer2'
    ]
    @Shared
    def updateActions = [
        empty            : '',
        do_nothing       : 'do_nothing',
        selective_update : 'selective_update',
        remove_and_create: 'remove_and_create'
    ]
    @Shared
    def jmsModules = [
        default   : 'TestJMSModule',
        unexistent: 'NoSuchModule'
    ]

    @Shared
    //* Optional Parameter
    def additionalOptionsIs = [
        empty    : '',
        correct  : 'MaximumMessageSize=1024',
        incorrect: 'incorrect Additional Options',
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

    // Procedure params
    // Required
    @Shared
    def jmsQueueName
    @Shared
    def jmsModuleName

    // Optional
    @Shared
    def jndiName
    @Shared
    def subdeploymentName
    @Shared
    def target
    @Shared
    def updateAction
    @Shared
    def additionalOptions

    // expected results
    String expectedOutcome
    String expectedSummaryMessage
    String expectedJobDetailedResult

    /**
     * Preparation actions
     */

    def doSetupSpec() {
        setupResource()
        deleteProject(projectName)
        createConfig(CONFIG_NAME)

        discardChanges()
        dslFile "dsl/procedures.dsl", [
            projectName  : projectName,
            resourceName : getResourceName(),
            procedureName: procedureName,
            params       : [
                configname                     : CONFIG_NAME,

                ecp_weblogic_jms_queue_name    : '',
                ecp_weblogic_jms_module_name   : '',
                ecp_weblogic_jndi_name         : '',

                ecp_weblogic_additional_options: '',
                ecp_weblogic_subdeployment_name: '',
                ecp_weblogic_update_action     : '',
                ecp_weblogic_target_jms_server : '',
            ]
        ]
        createJMSModule(jmsModules.default)

        dslFile("dsl/Application/CreateOrUpdateJMSQueue.dsl", [
            projectName    : projectName,
            resourceName   : getResourceName()
        ])
    }

    /**
     * Clean Up actions after test will finished
     */

    def doCleanupSpec() {
//        deleteProject(projectName)
    }

    /**
     * Positive Scenarios
     */

    @Unroll
    def "Create or Update JMS Queue ( Queue name: #jmsQueueName target: #target, additional options: #additionalOptions, update action: #updateAction) - procedure"() {
        setup: 'Define the parameters for Procedure running'

        jmsModuleName = jmsModules.default
        jndiName = 'TestJNDIName'

        def runParams = [
            ecp_weblogic_jms_queue_name    : jmsQueueName,
            ecp_weblogic_jms_module_name   : jmsModuleName,
            ecp_weblogic_jndi_name         : jndiName,

            ecp_weblogic_subdeployment_name: subdeploymentName,
            ecp_weblogic_additional_options: additionalOptions,
            ecp_weblogic_update_action     : updateAction,
            ecp_weblogic_target_jms_server : target,
        ]


        if (updateAction) {
            createJMSQueue(jmsModuleName, jmsQueueName)
        }

        when: 'Procedure runs: '

        def result = runTestedProcedure(projectName, procedureName, runParams, getResourceName())

        then: 'Wait until job run is completed: '

        String debugLog = result.logs
        println "Procedure log:\n$debugLog\n"

        expect: 'Outcome and Upper Summary verification'
        assert result.outcome == expectedOutcome

        if (expectedOutcome == expectedOutcomes.success && result.outcome == expectedOutcomes.success) {
            assert jmsQueueExists(jmsModuleName, jmsQueueName)
        }

        if (expectedJobDetailedResult) {
            assert debugLog.contains(expectedJobDetailedResult)
        }

        if (expectedSummaryMessage) {
            def upperStepSummary = getJobUpperStepSummary(result.jobId)
            assert upperStepSummary == expectedSummaryMessage
        }

        cleanup:
        deleteJMSQueue(jmsModuleName, jmsQueueName)

        where: 'The following params will be: '
        jmsQueueName      | updateAction                    | target          | additionalOptions             | expectedOutcome          | expectedJobDetailedResult

        // Create
        jmsQueues.default | updateActions.empty             | targets.default | additionalOptionsIs.empty     | expectedOutcomes.success | "Created Queue $jmsQueueName"

        // With additional options
        jmsQueues.default | updateActions.empty             | targets.default | additionalOptionsIs.correct   | expectedOutcomes.success | "Created Queue $jmsQueueName"

        // With incorrect additional options
        jmsQueues.default | updateActions.empty             | targets.default | additionalOptionsIs.incorrect | expectedOutcomes.error   | 'Options: incorrect Additional Options'

        // Update options
        jmsQueues.updated | updateActions.do_nothing        | targets.update  | additionalOptionsIs.empty     | expectedOutcomes.success | "No action is required"
        jmsQueues.updated | updateActions.selective_update  | targets.update  | additionalOptionsIs.empty     | expectedOutcomes.success | "Doing selective update"
        jmsQueues.updated | updateActions.remove_and_create | targets.update  | additionalOptionsIs.empty     | expectedOutcomes.success | "Removed JMS Queue $jmsQueueName from the module " // $jmsModuleName"
    }

    @Unroll
    def "Create or Update JMS Queue ( Queue name: #jmsQueueName target: #target, additional options: #additionalOptions, update action: #updateAction) - application"() {
        setup: 'Define the parameters for Procedure running'

        jmsModuleName = jmsModules.default
        jndiName = 'TestJNDIName'

        def paramsStr = stringifyArray([
            ecp_weblogic_jms_queue_name    : jmsQueueName,
            ecp_weblogic_jms_module_name   : jmsModuleName,
            ecp_weblogic_jndi_name         : jndiName,

            ecp_weblogic_subdeployment_name: subdeploymentName,
            ecp_weblogic_additional_options: additionalOptions,
            ecp_weblogic_update_action     : updateAction,
            ecp_weblogic_target_jms_server : target,
        ])

        if (updateAction) {
            createJMSQueue(jmsModuleName, jmsQueueName)
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

        assert jobStatus(result.jobId).outcome == expectedOutcome

        if (expectedOutcome == expectedOutcomes.success && result.outcome == expectedOutcomes.success) {
            assert jmsQueueExists(jmsModuleName, jmsQueueName)
        }

        cleanup:
        deleteJMSQueue(jmsModuleName, jmsQueueName)

        where: 'The following params will be: '
        jmsQueueName      | updateAction                    | target          | additionalOptions             | expectedOutcome          | expectedJobDetailedResult

        // Create
        jmsQueues.default | updateActions.empty             | targets.default | additionalOptionsIs.empty     | expectedOutcomes.success | "Created Queue $jmsQueueName"

        // With additional options
        jmsQueues.default | updateActions.empty             | targets.default | additionalOptionsIs.correct   | expectedOutcomes.success | "Created Queue $jmsQueueName"

        // With incorrect additional options
        jmsQueues.default | updateActions.empty             | targets.default | additionalOptionsIs.incorrect | expectedOutcomes.error   | 'Options: incorrect Additional Options'

        // Update options
        jmsQueues.updated | updateActions.do_nothing        | targets.update  | additionalOptionsIs.empty     | expectedOutcomes.success | "No action is required"
        jmsQueues.updated | updateActions.selective_update  | targets.update  | additionalOptionsIs.empty     | expectedOutcomes.success | "Doing selective update"
        jmsQueues.updated | updateActions.remove_and_create | targets.update  | additionalOptionsIs.empty     | expectedOutcomes.success | "Removed JMS Queue $jmsQueueName from the module " // $jmsModuleName"
    }


    @Unroll
    def "Create JMS Queue With Subdeployment ( Queue name: #jmsQueueName target: #target, additional options: #additionalOptions, update action: #updateAction) - procedure"() {
        setup: 'Define the parameters for Procedure running'

        jmsModuleName = jmsModules.default
        jndiName = 'TestJNDIName'
        def subdeploymentName = randomize('JMSQueue')

        def runParams = [
            ecp_weblogic_jms_queue_name    : jmsQueueName,
            ecp_weblogic_jms_module_name   : jmsModuleName,
            ecp_weblogic_jndi_name         : jndiName,

            ecp_weblogic_subdeployment_name: subdeploymentName,
            ecp_weblogic_additional_options: additionalOptions,
            ecp_weblogic_update_action     : updateAction,
            ecp_weblogic_target_jms_server : target,
        ]

        createJMSServer(target)

        if (updateAction) {
            createJMSQueue(jmsModuleName, jmsQueueName)
        }

        when: 'Procedure runs: '

        def result = runTestedProcedure(projectName, procedureName, runParams, getResourceName())

        then: 'Wait until job run is completed: '

        String debugLog = result.logs
        println "Procedure log:\n$debugLog\n"

        expect: 'Outcome and Upper Summary verification'
        assert result.outcome == expectedOutcome

        if (expectedOutcome == expectedOutcomes.success && result.outcome == expectedOutcomes.success) {
            assert jmsQueueExists(jmsModuleName, jmsQueueName)
        }

        if (expectedJobDetailedResult) {
            assert debugLog.contains(expectedJobDetailedResult)
        }

        assert debugLog.contains("Created Subdeployment $subdeploymentName")

        if (expectedSummaryMessage) {
            def upperStepSummary = getJobUpperStepSummary(result.jobId)
            assert upperStepSummary == expectedSummaryMessage
        }

        cleanup:
        deleteJMSQueue(jmsModuleName, jmsQueueName)
        deleteSubDeployment(jmsModuleName, subdeploymentName)

        where: 'The following params will be: '
        jmsQueueName      | updateAction                    | target          | additionalOptions             | expectedOutcome          | expectedJobDetailedResult

        // Create
        jmsQueues.default | updateActions.empty             | targets.default | additionalOptionsIs.empty     | expectedOutcomes.success | "Created Queue $jmsQueueName"

        // With additional options
        jmsQueues.default | updateActions.empty             | targets.default | additionalOptionsIs.correct   | expectedOutcomes.success | "Created Queue $jmsQueueName"

    }

    @Unroll
    def "Update JMS Queue With Subdeployment ( Queue name: #jmsQueueName oldTarget: #oldTarget, newTarget: #newTarget, additional options: #additionalOptions, update action: #updateAction) - procedure"() {
        setup: 'Define the parameters for Procedure running'

        jmsModuleName = jmsModules.default
        jndiName = 'TestJNDIName'
        def subdeploymentName = randomize('JMSQueue')

        def firstRunParams = [
            ecp_weblogic_jms_queue_name    : jmsQueueName,
            ecp_weblogic_jms_module_name   : jmsModuleName,
            ecp_weblogic_jndi_name         : jndiName,

            ecp_weblogic_subdeployment_name: subdeploymentName,
            ecp_weblogic_additional_options: '',
            ecp_weblogic_update_action     : updateAction,
            ecp_weblogic_target_jms_server : oldTarget,
        ]

        def secondRunParams = [
            ecp_weblogic_jms_queue_name    : jmsQueueName,
            ecp_weblogic_jms_module_name   : jmsModuleName,
            ecp_weblogic_jndi_name         : jndiName,

            ecp_weblogic_subdeployment_name: subdeploymentName,
            ecp_weblogic_additional_options: '',
            ecp_weblogic_update_action     : updateAction,
            ecp_weblogic_target_jms_server : newTarget,
        ]

        createJMSServer(oldTarget)
        createJMSServer(newTarget)

        def result = runTestedProcedure(projectName, procedureName, firstRunParams, getResourceName())
        assert result.outcome == 'success'

        when: 'Procedure runs: '

        result = runTestedProcedure(projectName, procedureName, secondRunParams, getResourceName())

        then: 'Wait until job run is completed: '

        String debugLog = result.logs
        println "Procedure log:\n$debugLog\n"

        expect: 'Outcome and Upper Summary verification'
        assert result.outcome == expectedOutcome

        if (expectedOutcome == expectedOutcomes.success && result.outcome == expectedOutcomes.success) {
            assert jmsQueueExists(jmsModuleName, jmsQueueName)
        }

        if (expectedJobDetailedResult) {
            assert debugLog.contains(expectedJobDetailedResult)
        }

        if (expectedSummaryMessage) {
            def upperStepSummary = getJobUpperStepSummary(result.jobId)
            assert upperStepSummary == expectedSummaryMessage
        }

        if (updateAction == updateActions.remove_and_create) {
            assert debugLog.contains("Removed subdeployment $subdeploymentName")
        }
        else {
            assert debugLog.contains("Subdeployment $subdeploymentName already exist, targets are NOT going to be updated")
        }

        cleanup:
        deleteJMSQueue(jmsModuleName, jmsQueueName)
        deleteSubDeployment(jmsModuleName, subdeploymentName)

        where: 'The following params will be: '
        jmsQueueName      | updateAction                    | oldTarget         | newTarget      | expectedOutcome          | expectedJobDetailedResult

        jmsQueues.default | updateActions.selective_update  | targets.default   | targets.update | expectedOutcomes.success | "JMS Queue $jmsQueueName has been updated"

        jmsQueues.default | updateActions.remove_and_create | targets.default   | targets.update | expectedOutcomes.success | "JMS Queue $jmsQueueName has been recreated"

    }

    @Unroll
    def 'multi-step procedures #dslFileName #procName'() {
        given:
        def queueName = 'test queue'
        def moduleName = dslFileName
        deleteJMSModule(moduleName)
        def args = [
            projectName: projectName,
            procedureName: procName,
            configname: CONFIG_NAME,
            queueName: queueName,
            moduleName: moduleName,
            resourceName: getResourceName()
        ]
        dslFile "dsl/multisteps/${dslFileName}.dsl", args
        when:
        def result = runProcedure ("""
            runProcedure(
                projectName: '$projectName',
                procedureName: '$procName'
            )
        """, getResourceName())
        then:
        logger.debug(result.logs)
        assert result.outcome == 'success'
        cleanup:
        deleteJMSModule(moduleName)
        where:
        dslFileName        | procName
        'retargetJMSQueue' | 'Retarget JMS Queue with recreation'
        'updateQueueSDName'| 'Update SD Name for Queue (Selective Update)'
    }

    def jmsQueueExists(def moduleName, def name) {
        def code = """
def getJMSSystemResourcePath(jms_module_name):
    return "/JMSSystemResources/%s"%(jms_module_name)

def getJMSModulePath(jms_module_name):
    return "%s/JMSResource/%s"%(getJMSSystemResourcePath(jms_module_name),jms_module_name)

def getJMSQueuePath(jms_module_name, queue):
    return "/JMSSystemResources/%s/JMSResource/%s/Queues/%s" % (jms_module_name, jms_module_name, queue)

def jmsQueueExists(jmsModuleName, queueName):
    queueBean = getMBean(getJMSQueuePath(jmsModuleName, queueName))
    if queueBean != None:
        print("JMS Queue %s exists in module %s" % (queueName, jmsModuleName))
    else:
        print("JMS Queue %s does not exist in the module %s" % (queueName, jmsModuleName))

moduleName = '$moduleName'
queueName = '$name'

connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')
try:
    jmsQueueExists(moduleName, queueName)
except Exception, e:
    print("Exception", e)
"""
        def result = runWLST(code)
        assert result.outcome == 'success'

        return (result.logs =~ /JMS Queue $name exists in module $moduleName/)
    }

    def deleteJMSQueue(moduleName, name) {
        def code = """
def getJMSSystemResourcePath(jms_module_name):
    return "/JMSSystemResources/%s"%(jms_module_name)

def getJMSModulePath(jms_module_name):
    return "%s/JMSResource/%s"%(getJMSSystemResourcePath(jms_module_name),jms_module_name)

def getQueuePath(jms_module_name,queue_name):
    return "/JMSSystemResources/%s/JMSResource/%s/Queues/%s" % (jms_module_name, jms_module_name, queue_name)

def deleteQueue(jmsModuleName, cfName):
    bean = getMBean('%s/Queues/' % getJMSModulePath(jmsModuleName))
    queueBean = getMBean(getQueuePath(jmsModuleName, cfName))
    if queueBean != None:
        bean.destroyQueue(queueBean)
        print("Removed Queue %s from the module %s" % (cfName, jmsModuleName))
    else:
        print("Queue %s does not exist in the module %s" % (cfName, jmsModuleName))


moduleName = '$moduleName'
queueName = '$name'

connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')
edit()
startEdit()
try:
    deleteQueue(moduleName, queueName)
    activate()
except Exception, e:
    stopEdit('y')

"""
        def result = runWLST(code)
        assert result.outcome == 'success'
    }


}
