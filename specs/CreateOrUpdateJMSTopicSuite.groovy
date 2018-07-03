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
    // Required
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
        default         : 'jmsServer1',
        update          : 'jmsServer2'
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
        update     : 'JMSTopicUpdated',
        with_spaces: 'JMS Topic Name with spaces',
    ]

    @Shared
    def options = [
        oneOption     : 'Multicast.MulticastTimeToLive=5',
        twoOptions    : "DeliveryFailureParams.RedeliveryLimit=5\nMessageLoggingParams.MessageLoggingEnabled=true",
        topLevelOption: 'MessagingPerformancePreference=30'
    ]

    /**
     * Test Parameters: for Where section
     */

    // Required
    @Shared
    def caseId

    @Shared
    def jmsTopicName
    @Shared
    def jmsModuleName = jmsModules.default

    // Optional
    @Shared
    def jndiName
    @Shared
    def additionalOptions
    @Shared
    def updateAction
    @Shared
    def subdeploymentName
    @Shared
    def target

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
        createJMSModule(jmsModules.default)

        createJMSServer(targets.default)
        createJMSServer(targets.update)

        dslFile "dsl/procedures.dsl", [
            projectName  : projectName,
            resourceName : getResourceName(),
            procedureName: procedureName,
            params       : [
                configname                     : CONFIG_NAME,
                ecp_weblogic_additional_options: '',
                ecp_weblogic_update_action     : '',
                ecp_weblogic_subdeployment_name: '',
                ecp_weblogic_jms_topic_name    : '',
                ecp_weblogic_jms_module_name   : '',
                ecp_weblogic_jndi_name         : '',
                ecp_weblogic_target_jms_server : '',
            ]
        ]

        dslFile("dsl/Application/CreateOrUpdateJMSTopic.dsl", [
            resourceName   : getResourceName()
        ])
    }

    /**
     * Clean Up actions after test will finished
     */

    def doCleanupSpec() {
        // deleteProject(projectName)
    }

    @Shared
    def newTarget
    def oldTarget

    /**
     * Positive Scenarios
     */

    @Unroll
    def "#caseId. Create and Update JMS Topic. Positive - procedure with params (Topic: #jmsTopicName, module: #jmsModuleName, update action: #updateAction) - procedure"() {
        setup: 'Define the parameters for Procedure running'

        jmsModuleName = jmsModules.default
        target = targets.default

        if (jmsTopicName && jmsModuleName) {
            deleteJMSTopic(jmsModuleName, jmsTopicName)
        }

        if (updateAction != '') {
            createJMSTopic(jmsModuleName, jmsTopicName)
            target = targets.update
        }

        def runParams = [
            ecp_weblogic_additional_options: additionalOptions,
            ecp_weblogic_update_action     : updateAction,
            ecp_weblogic_subdeployment_name: subdeploymentName,
            ecp_weblogic_jms_topic_name    : jmsTopicName,
            ecp_weblogic_jms_module_name   : jmsModuleName,
            ecp_weblogic_jndi_name         : jndiName,
            ecp_weblogic_target_jms_server : target,
        ]

        when: 'Procedure runs: '

        def result = runTestedProcedure(projectName, procedureName, runParams, getResourceName())

        then: 'Wait until job run is completed: '

        def debugLog = result.logs

        println "Procedure log:\n$debugLog\n"

        expect: 'Outcome and Upper Summary verification'
        assert result.outcome == expectedOutcome

        if (expectedOutcome == expectedOutcomes.success && result.outcome == expectedOutcomes.success) {
//            assert jmsTopicExists(jmsModuleName)
        }

        if (expectedSummaryMessage) {
            def upperStepSummary = getJobUpperStepSummary(result.jobId)
            assert upperStepSummary.contains(expectedSummaryMessage)
        }

        where: 'The following params will be: '
        caseId    | updateAction                    | jmsTopicName                                    | expectedOutcome          | expectedSummaryMessage                                          | expectedJobDetailedResult
        // Create
        'C325118' | updateActions.empty             | jmsTopicNames.default                           | expectedOutcomes.success | "Created JMS Topic $jmsTopicName"                               | ''

        // Empty Name
        'C325125' | updateActions.empty             | jmsTopicNames.empty                             | expectedOutcomes.error   | "No JMS Topic name is provided"                                 | ''

        // Update
        'C325126' | updateActions.do_nothing        | jmsTopicNames.default + randomize(updateAction) | expectedOutcomes.success | "JMS Topic $jmsTopicName exists, no further action is required" | ''
        'C325127' | updateActions.selective_update  | jmsTopicNames.default + randomize(updateAction) | expectedOutcomes.success | ''                                                              | "Updated JMS Topic"
        'C325158' | updateActions.remove_and_create | jmsTopicNames.default + randomize(updateAction) | expectedOutcomes.success | ''                                                              | "Recreated JMS Topic"
    }

    @Unroll
    def "#caseId. Create and Update JMS Topic. Positive - procedure with params (Topic: #jmsTopicName, module: #jmsModuleName, update action: #updateAction) - application"() {
        setup: 'Define the parameters for Procedure running'

        jmsModuleName = jmsModules.default
        target = targets.default

        def paramsStr = stringifyArray([
            ecp_weblogic_additional_options: additionalOptions,
            ecp_weblogic_update_action     : updateAction,
            ecp_weblogic_subdeployment_name: subdeploymentName,
            ecp_weblogic_jms_topic_name    : jmsTopicName,
            ecp_weblogic_jms_module_name   : jmsModuleName,
            ecp_weblogic_jndi_name         : jndiName,
            ecp_weblogic_target_jms_server : target,
        ])

        if (jmsTopicName && jmsModuleName) {
            deleteJMSTopic(jmsModuleName, jmsTopicName)
        }

        if (updateAction != '') {
            createJMSTopic(jmsModuleName, jmsTopicName)
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
//            assert jmsTopicExists(jmsModuleName)
        }

        where: 'The following params will be: '
        caseId    | updateAction                    | jmsTopicName                                    | expectedOutcome          | expectedSummaryMessage                                                  | expectedJobDetailedResult
        // Create
        'C325170' | updateActions.empty             | jmsTopicNames.default                           | expectedOutcomes.success | "Created JMS Topic $jmsTopicName"                                       | ''

        // Empty Name
        'C325171' | updateActions.empty             | jmsTopicNames.empty                             | expectedOutcomes.error   | "No JMS Topic name is provided"                                         | ''

        // Update
        'C325172' | updateActions.do_nothing        | jmsTopicNames.default + randomize(updateAction) | expectedOutcomes.success | "JMS Topic $jmsTopicName already exists, no further action is required" | ''
        'C325173' | updateActions.selective_update  | jmsTopicNames.default + randomize(updateAction) | expectedOutcomes.success | ''                                                                      | "Updated JMS Topic"
        'C325174' | updateActions.remove_and_create | jmsTopicNames.default + randomize(updateAction) | expectedOutcomes.success | ''                                                                      | "Recreated JMS Topic"
    }

    @Unroll
    def "#caseId. Create with additional options #additionalOptions - procedure"() {
        setup: 'removing old topic'
        def jmsTopicName = jmsTopicNames.default
        def jmsModuleName = jmsModules.default
        deleteJMSTopic(jmsModuleName, jmsTopicName)
        def runParams = [
            ecp_weblogic_additional_options: additionalOptions,
            ecp_weblogic_jms_topic_name    : jmsTopicName,
            ecp_weblogic_jms_module_name   : jmsModuleName,
        ]

        when: 'procedure runs'
        def result = runTestedProcedure(projectName, procedureName, runParams, getResourceName())

        then:
        assert result.outcome == expectedOutcomes.success
        logger.debug(result.logs)
        checkResourceProperties(jmsModuleName, jmsTopicName, 'Topic', additionalOptions)

        cleanup:
        deleteJMSTopic(jmsModuleName, jmsTopicName)

        where:
        caseId    | additionalOptions
        'C325159' | options.oneOption
        'C325160' | options.twoOptions
        'C325161' | options.topLevelOption
    }

    @Unroll
    def "#caseId. Create JMS Topic With Subdeployment ( target: #target, subdeployment: #subdeploymentName ) - procedure"() {
        setup: 'Define the parameters for Procedure running'

        jmsModuleName = jmsModules.default
        def jndiName = randomize('jmsTopic')
        def subdeploymentName = randomize('JMSTopic')

        def runParams = [
            ecp_weblogic_jms_topic_name    : jmsTopicName,
            ecp_weblogic_jms_module_name   : jmsModuleName,
            ecp_weblogic_jndi_name         : jndiName,

            ecp_weblogic_subdeployment_name: subdeploymentName,
            ecp_weblogic_additional_options: additionalOptions,
            ecp_weblogic_update_action     : updateAction,
            ecp_weblogic_target_jms_server : target,
        ]

        createJMSServer(target)

        when: 'Procedure runs: '
        def result = runTestedProcedure(projectName, procedureName, runParams, getResourceName())

        then: 'Wait until job run is completed: '
        String debugLog = result.logs
        println "Procedure log:\n$debugLog\n"

        expect: 'Outcome and Upper Summary verification'
        assert result.outcome == expectedOutcome

        if (expectedJobDetailedResult) {
            assert debugLog.contains(expectedJobDetailedResult)
        }

        assert debugLog.contains("Created Subdeployment $subdeploymentName")

        cleanup:
        deleteJMSTopic(jmsModuleName, jmsTopicName)
        deleteSubDeployment(jmsModuleName, subdeploymentName)

        where: 'The following params will be: '
        caseId    | jmsTopicName          | target          | expectedOutcome          | expectedJobDetailedResult

        // Create
        'C325162' | jmsTopicNames.default | targets.default | expectedOutcomes.success | "Created JMS Topic $jmsTopicName"
        'C325163' | jmsTopicNames.default | targets.update  | expectedOutcomes.success | "Created JMS Topic $jmsTopicName"
    }

    @Unroll
    def "#caseId. Update JMS Topic With Subdeployment ( target: #oldTarget -> #newTarget,update action: #updateAction) - procedure"() {
        setup: 'Define the parameters for Procedure running'

        jmsTopicName = jmsTopicNames.default
        jmsModuleName = jmsModules.default
        jndiName = 'TestJNDIName'

        def subdeploymentName = randomize('jmsTopic')

        def firstRunParams = [
            ecp_weblogic_jms_topic_name    : jmsTopicName,
            ecp_weblogic_jms_module_name   : jmsModuleName,
            ecp_weblogic_jndi_name         : jndiName,

            ecp_weblogic_subdeployment_name: subdeploymentName,
            ecp_weblogic_additional_options: '',
            ecp_weblogic_update_action     : updateAction,
            ecp_weblogic_target_jms_server : oldTarget,
        ]

        def secondRunParams = [
            ecp_weblogic_jms_topic_name    : jmsTopicName,
            ecp_weblogic_jms_module_name   : jmsModuleName,
            ecp_weblogic_jndi_name         : jndiName,

            ecp_weblogic_subdeployment_name: subdeploymentName,
            ecp_weblogic_additional_options: '',
            ecp_weblogic_update_action     : updateAction,
            ecp_weblogic_target_jms_server : newTarget,
        ]

        def result = runTestedProcedure(projectName, procedureName, firstRunParams, getResourceName())
        assert result.outcome == 'success'

        when: 'Procedure runs: '
        result = runTestedProcedure(projectName, procedureName, secondRunParams, getResourceName())

        then: 'Wait until job run is completed: '
        String debugLog = result.logs
        println "Procedure log:\n$debugLog\n"

        expect: 'Outcome and Upper Summary verification'
        assert result.outcome == expectedOutcome

        if (expectedJobDetailedResult) {
            assert debugLog.contains(expectedJobDetailedResult)
        }

        if (expectedSummaryMessage) {
            def upperStepSummary = getJobUpperStepSummary(result.jobId)
            assert upperStepSummary == expectedSummaryMessage
        }

        if (updateAction == updateActions.remove_and_create) {
            assert debugLog.contains("Removed subdeployment $subdeploymentName")
        } else {
            assert debugLog.contains("Subdeployment $subdeploymentName already exist, targets are NOT going to be updated")
        }

        cleanup:
        deleteJMSTopic(jmsModuleName, jmsTopicName)
        deleteSubDeployment(jmsModuleName, subdeploymentName)

        where: 'The following params will be: '
        caseId    | updateAction                    | oldTarget       | newTarget      | expectedOutcome          | expectedJobDetailedResult

        // Create
        'C325168' | updateActions.selective_update  | targets.default | targets.update | expectedOutcomes.success | "Updated JMS Topic ${jmsTopicNames.default}"
        'C325169' | updateActions.remove_and_create | targets.default | targets.update | expectedOutcomes.success | "Recreated JMS Topic ${jmsTopicNames.default}"
    }


    def deleteJMSTopic(moduleName, name) {
        def code = """
def getJMSSystemResourcePath(jms_module_name):
    return "/JMSSystemResources/%s"%(jms_module_name)

def getJMSModulePath(jms_module_name):
    return "%s/JMSResource/%s"%(getJMSSystemResourcePath(jms_module_name),jms_module_name)

def getTopicPath(jms_module_name, topic_name):
    return "/JMSSystemResources/%s/JMSResource/%s/Topics/%s" % (jms_module_name, jms_module_name, topic_name)

def deleteTopic(jmsModuleName, topicName):
    bean = getMBean('%s/Topics/' % getJMSModulePath(jmsModuleName))
    topicBean = getMBean(getTopicPath(jmsModuleName, topicName))
    if topicBean != None:
        bean.destroyTopic(topicBean)
        print("Removed Topic %s from the module %s" % (topicName, jmsModuleName))
    else:
        print("Topic %s does not exist in the module %s" % (topicName, jmsModuleName))


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
