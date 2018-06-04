import spock.lang.*

class CreateOrUpdateJMSQueueSuite extends WebLogicHelper {
    /**
     * Environments Variables
     */
    static String wlstPath = System.getenv('WEBLOGIC_WLST_PATH')

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
    def configNames = [
            empty    : '',
            correct  : CONFIG_NAME,
            incorrect: 'incorrect config Name',
    ]

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
            default: 'AdminServer',
            update : 'TestSpecServer'
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
            correct  : '-subscriptionDurability Durable',
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
    def configname

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
    def expectedOutcome
    def expectedSummaryMessage
    def expectedJobDetailedResult

    /**
     * Preparation actions
     */

    def doSetupSpec() {
        setupResource()
        deleteProject(projectName)
        createJMSModule(jmsModules.default)

        discardChanges()
        createConfig(configNames.correct)
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
    @Ignore
    def "Create or Update JMS Queue (module_name: #jmsModuleName, target: #target, update action: #updateAction)"() {
        setup: 'Define the parameters for Procedure running'

        jndiName = 'TestJNDIName'

        def runParams = [
                configname                     : configname,
                ecp_weblogic_jms_queue_name    : jmsQueueName,
                ecp_weblogic_jms_module_name   : jmsModuleName,
                ecp_weblogic_jndi_name         : jndiName,

                ecp_weblogic_subdeployment_name: subdeploymentName,
                ecp_weblogic_jms_server_name   : target,
                ecp_weblogic_additional_options: additionalOptions,
                ecp_weblogic_update_action     : updateAction,
        ]

        deleteJMSQueue(jmsModuleName, jmsQueueName)
        ensureManagedServer(target, '7999')

        if (updateAction) {
            createJMSModule(jmsModuleName, targets.default)
        }

        when: 'Procedure runs: '

        def result = runTestedProcedure(projectName, procedureName, runParams, getResourceName())

        then: 'Wait until job run is completed: '

        def outcome = result.outcome
        def debugLog = result.logs

        println "Procedure log:\n$debugLog\n"

        def upperStepSummary = getJobUpperStepSummary(result.jobId)
        logger.info(upperStepSummary)

        expect: 'Outcome and Upper Summary verification'
        assert outcome == expectedOutcome
        if (expectedOutcome == expectedOutcomes.success && outcome == expectedOutcomes.success) {
            assert jmsQueueExists(jmsQueueName)
        }

        if (expectedJobDetailedResult) {
            assert debugLog.contains(expectedJobDetailedResult)
        }

        if (expectedSummaryMessage) {
            assert upperStepSummary == expectedSummaryMessage
        }

        cleanup:
        if (expectedOutcome == expectedOutcomes.success && outcome == expectedOutcomes.success) {
            deleteJMSModule(jmsQueueName)
        }

        where: 'The following params will be: '
        configname          | jmsQueueName      | jmsModuleName      | updateAction        | target          | additionalOptions         | expectedOutcome          | expectedSummaryMessage | expectedJobDetailedResult
        // Create
        configNames.correct | jmsQueues.default | jmsModules.default | updateActions.empty | targets.default | additionalOptionsIs.empty | expectedOutcomes.success | ''            | "Created Queue $jmsQueueName"
//        configNames.correct | jmsQueues.default | jmsModules.default | updateActions.empty             | targets.default | additionalOptionsIs.correct   | expectedOutcomes.success | "Created JMS Queue $jmsModuleName" | ''
//        configNames.correct | jmsQueues.default | jmsModules.default | updateActions.empty             | targets.default | additionalOptionsIs.incorrect | expectedOutcomes.error   | "Change me"                        | ''

        // Update
//        configNames.correct | jmsQueues.update  | jmsModules.default | updateActions.do_nothing        | targets.update  | additionalOptionsIs.empty     | expectedOutcomes.success | ''                                 | "JMS Queue $jmsModuleName exists, no further action is required"
//        configNames.correct | jmsQueues.update  | jmsModules.default | updateActions.selective_update  | targets.update  | additionalOptionsIs.empty     | expectedOutcomes.success | ''                                 | "Updated JMS Queue"
//        configNames.correct | jmsQueues.update  | jmsModules.default | updateActions.remove_and_create | targets.update  | additionalOptionsIs.empty     | expectedOutcomes.success | ''                                 | "Recreated JMS Queue"
    }

    def jmsQueueExists(def queueName) {
        // TODO implement
        return true
        def code = """
def getJMSSystemResourcePath(jms_module_name):
    return "/JMSSystemResources/%s"%(jms_module_name)

def getJMSModulePath(jms_module_name):
    return "%s/JMSResource/%s"%(getJMSSystemResourcePath(jms_module_name),jms_module_name)

def getConnectionFactoryPath(jms_module_name,cf_name):
    return "/JMSSystemResources/%s/JMSResource/%s/ConnectionFactories/%s" % (jms_module_name, jms_module_name, cf_name)

def connectionFactoryExists(jmsModuleName, cfName):
    bean = getMBean('%s/ConnectionFactories/' % getJMSModulePath(jmsModuleName))
    cfBean = getMBean(getConnectionFactoryPath(jmsModuleName, cfName))
    if cfBean != None:
        print("Connection Factory %s exists in module %s" % (cfName, jmsModuleName))
    else:
        print("Connection Factory %s does not exist in the module %s" % (cfName, jmsModuleName))


moduleName = '$moduleName'
cfName = '$name'

connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')
try:
    connectionFactoryExists(moduleName, cfName)
except Exception, e:
   print("Exception", e)
"""
        def result = runWLST(code)
        assert result.outcome == 'success'

        return (result.logs =~ /Connection Factory $name exists in module $moduleName/)
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

    def getConnectionFactoryProperty(module, cfName, group, propName) {
        def code = """
def getConnectionFactoryPath(jms_module_name,cf_name):
    return "/JMSSystemResources/%s/JMSResource/%s/ConnectionFactories/%s" % (jms_module_name, jms_module_name, cf_name)

module = '$module'
cfName = '$cfName'
group = '$group'
propName = '$propName'
connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')
cd(getConnectionFactoryPath(module, cfName) + '/' + group + '/' + cfName)
print "PROPERTY: %s" % get(propName)
"""
        def result = runWLST(code)
        assert result.outcome == 'success'
        // TODO retrieve property
        result
    }
}
