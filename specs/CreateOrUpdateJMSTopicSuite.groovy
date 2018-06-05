import spock.lang.*

class CreateOrUpdateJMSTopicSuite extends WebLogicHelper {
    /**
     * Environments Variables
     */
    static String wlstPath = System.getenv('WEBLOGIC_WLST_PATH')

    /**
     * Dsl Parameters
     */

    @Shared
    def procedureName = 'CreateOrUpdateJMSTopic'
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
    //* Required Parameter (need incorrect and empty value)
    def configNames = [
            empty    : '',
            correct  : CONFIG_NAME,
            incorrect: 'incorrect config Name',
    ]

    @Shared
    def jndiNames = [
            empty      : '',
            correct    : 'TestJNDIName',
            recreateOld: 'OldJNDIName',
            recreateNew: 'NewJNDIName',
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
    def jmsTopicNames = [
            empty      : '',
            default    : 'JMSTopic',
            with_spaces: 'JMS Topic Name with spaces',
    ]

    /**
     * Test Parameters: for Where section
     */

    // Required
    def configname
    @Shared
    def jmsTopicName
    @Shared
    def jmsModuleName

    // Optional
    @Shared
    def jndiName
    @Shared
    def additionalOptions
    @Shared
    def updateAction
    @Shared
    def subdeploymentName
    @Shared def target

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

        createConfig(configNames.correct)
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
    def "Create and Update JMS Topic. Positive - procedure with params (Topic: #jmsTopicName, update action: #updateAction)"() {
        setup: 'Define the parameters for Procedure running'

        jmsModuleName = jmsModules.default
        targets = 'SpecServer'

        def runParams = [
                configname                     : configname,
                ecp_weblogic_additional_options: additionalOptions,
                ecp_weblogic_update_action     : updateAction,
                ecp_weblogic_subdeployment_name: subdeploymentName,
                ecp_weblogic_jms_topic_name    : jmsTopicName,
                ecp_weblogic_jms_module_name   : jmsModuleName,
//                ecp_weblogic_jms_server_name   : targets,
                ecp_weblogic_jndi_name         : jndiName,
        ]

        ensureManagedServer(targets, '7999')

        if (updateAction) {
//            createJMSTopic(jmsModuleName, targets.default)
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
//            assert jmsModuleExists(jmsModuleName)
        }

        if (expectedJobDetailedResult) {
            assert debugLog.contains(expectedJobDetailedResult)
        }

        if (expectedSummaryMessage) {
            assert upperStepSummary.contains(expectedSummaryMessage)
        }

        cleanup:
        if (expectedOutcome == expectedOutcomes.success && outcome == expectedOutcomes.success) {
            deleteJMSTopic(jmsModuleName, jmsTopicName)
        }

        where: 'The following params will be: '
        configname          | updateAction                    | jmsTopicName                                    | expectedOutcome          | expectedSummaryMessage            | expectedJobDetailedResult
        // Create
        configNames.correct | updateActions.empty             | jmsTopicNames.default                           | expectedOutcomes.success | "Created JMS Topic $jmsTopicName" | ''

        // Empty Name
//        configNames.correct | updateActions.empty             | jmsTopicNames.empty                             | expectedOutcomes.error   | ""                                | ''

        // Update
//        configNames.correct | updateActions.do_nothing        | jmsTopicNames.default + randomize(updateAction) | expectedOutcomes.success | ''                                | "JMS Topic $jmsTopicName exists, no further action is required"
//        configNames.correct | updateActions.selective_update  | jmsTopicNames.default + randomize(updateAction) | expectedOutcomes.success | ''                                | "Updated JMS Topic"
//        configNames.correct | updateActions.remove_and_create | jmsTopicNames.default + randomize(updateAction) | expectedOutcomes.success | ''                                | "Recreated JMS Topic"
    }

//    @Unroll
//    def "Update JMS Module Targets. Positive - procedure with params (old targets: #oldTargets, new targets: #newTargets update action: #updateAction)"() {
//        setup: 'Define the parameters for Procedure running'
//        def configname = configNames.correct
//        def updateAction = 'selective_update'
//        def jmsModuleName = randomize('TargetList')
//        def expectedOutcome = expectedOutcomes.success
//        def runParams = [
//                configname                  : configname,
//                ecp_weblogic_jms_module_name: jmsModuleName,
//                ecp_weblogic_update_action  : updateAction,
//                ecp_weblogic_target_list    : newTargets
//        ]
//        // Create targets and create JMS module with them
//        deleteJMSModule(jmsModuleName)
//        prepareTargets(oldTargets)
//        prepareTargets(newTargets)
//        createJMSModule(jmsModuleName, oldTargets)
//        expectedSummaryMessage = buildExpectedSummary(oldTargets, newTargets)
//        when: 'Procedure runs: '
//
//        def result = runTestedProcedure(projectName, procedureName, runParams, getResourceName())
//
//        then: 'Wait until job run is completed: '
//
//        def outcome = result.outcome
//        def debugLog = result.logs
//
//        logger.debug("Procedure log:\n$debugLog\n")
//
//        def upperStepSummary = getJobUpperStepSummary(result.jobId)
//        logger.debug("Summary: " + upperStepSummary)
//
//        expect: 'Outcome and Upper Summary verification'
//        assert result.outcome == expectedOutcome
//        if (expectedOutcome == expectedOutcomes.success && outcome == expectedOutcomes.success) {
//            assert jmsModuleExists(jmsModuleName)
//        }
//
//        if (expectedSummaryMessage) {
//            assert upperStepSummary.contains(expectedSummaryMessage)
//        }
//
//        def expectedLogs = buildExpectedLogs(oldTargets, newTargets)
//        logger.debug("Expected log :" + expectedLogs)
//        expectedLogs.each {
//            assert debugLog.contains(it)
//        }
//
//        cleanup:
//        deleteJMSModule(jmsModuleName)
//
//        where: 'The following params will be: '
//        oldTargets      | newTargets
//        targets.default | targets.twoServers
//        targets.default | targets.cluster
//        // 'selective_update'              | targets.cluster    | targets.twoServers
//        // 'remove_and_create'             | targets.cluster    | targets.serverAndCluster
//        // 'selective_update'              | targets.nothing    | targets.managedServer
//        // 'remove_and_create'             | targets.nothing    | targets.cluster
//    }

    def deleteJMSTopic(moduleName, name) {
        def code = """
def getJMSSystemResourcePath(jms_module_name):
    return "/JMSSystemResources/%s"%(jms_module_name)

def getJMSModulePath(jms_module_name):
    return "%s/JMSResource/%s"%(getJMSSystemResourcePath(jms_module_name),jms_module_name)

def getTopicPath(jms_module_name,queue_name):
    return "/JMSSystemResources/%s/JMSResource/%s/Topics/%s" % (jms_module_name, jms_module_name, queue_name)

def deleteTopic(jmsModuleName, cfName):
    bean = getMBean('%s/Topics/' % getJMSModulePath(jmsModuleName))
    queueBean = getMBean(getTopicPath(jmsModuleName, cfName))
    if queueBean != None:
        bean.destroyTopic(queueBean)
        print("Removed Topic %s from the module %s" % (cfName, jmsModuleName))
    else:
        print("Topic %s does not exist in the module %s" % (cfName, jmsModuleName))


moduleName = '$moduleName'
topicName = '$name'

connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')
edit()
startEdit()
try:
    deleteTopic(moduleName, topicName)
    activate()
except Exception, e:
    stopEdit('y')

"""
        def result = runWLST(code)
        assert result.outcome == 'success'
    }

}
