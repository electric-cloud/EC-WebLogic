import spock.lang.*

class CreateOrUpdateJMSModuleSuite extends WebLogicHelper {
    /**
     * Environments Variables
     */
    static String wlstPath = System.getenv('WEBLOGIC_WLST_PATH')

    /**
     * Dsl Parameters
     */

    @Shared
    def procedureName = 'CreateOrUpdateJMSModule'
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
    def connectionFactories = [
        correct    : 'SpecConnectionFactory',
        updated    : 'SpecUpdatedCF',
        nonexisting: 'NoSuchCF'
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

    /**
     * Test Parameters: for Where section
     */

    // Required
    @Shared
    def jmsModuleName
    @Shared
    def target

    // Optional
    @Shared
    def updateAction

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

        dslFile "dsl/procedures.dsl", [
            projectName  : projectName,
            resourceName : getResourceName(),
            procedureName: procedureName,
            params       : [
                configname                  : CONFIG_NAME,
                ecp_weblogic_jms_module_name: '',
                ecp_weblogic_update_action  : '',
                ecp_weblogic_target_list    : '',
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
    def "Create and Update JMS Module. Positive - procedure with params (Module: #jmsModuleName, target: #target, update action: #updateAction )"() {
        setup: 'Define the parameters for Procedure running'

        def runParams = [
            ecp_weblogic_jms_module_name: jmsModuleName,
            ecp_weblogic_update_action  : updateAction,
            ecp_weblogic_target_list    : target
        ]

        deleteJMSModule(jmsModuleName)
        ensureManagedServer(target, '7999')

        if (updateAction) {
            createJMSModule(jmsModuleName, targets.default)
        }

        when: 'Procedure runs: '

        def result = runTestedProcedure(projectName, procedureName, runParams, getResourceName())

        then: 'Wait until job run is completed: '

        def debugLog = result.logs

        println "Procedure log:\n$debugLog\n"

        def upperStepSummary = getJobUpperStepSummary(result.jobId)
        logger.info(upperStepSummary)

        expect: 'Outcome and Upper Summary verification'
        assert result.outcome == expectedOutcome

        if (expectedOutcome == expectedOutcomes.success && result.outcome == expectedOutcomes.success) {
            assert jmsModuleExists(jmsModuleName)
        }

        if (expectedJobDetailedResult) {
            assert debugLog.contains(expectedJobDetailedResult)
        }

        if (expectedSummaryMessage) {
            assert upperStepSummary.contains(expectedSummaryMessage)
        }

        cleanup:
        if (expectedOutcome == expectedOutcomes.success && result.outcome == expectedOutcomes.success) {
            deleteJMSModule(jmsModuleName)
        }

        where: 'The following params will be: '
        configname          | jmsModuleName                                | updateAction                    | target          | expectedOutcome          | expectedSummaryMessage                     | expectedJobDetailedResult
        // Create
        configNames.correct | jmsModules.default                           | updateActions.empty             | targets.default | expectedOutcomes.success | "Created JMS System Module $jmsModuleName" | ''

        // Update
        configNames.correct | jmsModules.default + randomize(updateAction) | updateActions.do_nothing        | targets.update  | expectedOutcomes.success | ''                                         | "JMS System Module $jmsModuleName exists, no further action is required"
        configNames.correct | jmsModules.default + randomize(updateAction) | updateActions.selective_update  | targets.update  | expectedOutcomes.success | ''                                         | "Updated JMS System Module"
        configNames.correct | jmsModules.default + randomize(updateAction) | updateActions.remove_and_create | targets.update  | expectedOutcomes.success | ''                                         | "Recreated JMS System Module"
    }

    @Unroll
    def "Update JMS Module Targets. Positive - procedure with params (old targets: #oldTargets, new targets: #newTargets, update action: #updateAction)"() {
        setup: 'Define the parameters for Procedure running'
        def updateAction = 'selective_update'
        def jmsModuleName = randomize('TargetList')
        def expectedOutcome = expectedOutcomes.success
        def runParams = [
            ecp_weblogic_jms_module_name: jmsModuleName,
            ecp_weblogic_update_action  : updateAction,
            ecp_weblogic_target_list    : newTargets
        ]
        // Create targets and create JMS module with them
        deleteJMSModule(jmsModuleName)
        prepareTargets(oldTargets)
        prepareTargets(newTargets)
        createJMSModule(jmsModuleName, oldTargets)
        expectedSummaryMessage = buildExpectedSummary(oldTargets, newTargets)
        when: 'Procedure runs: '

        def result = runTestedProcedure(projectName, procedureName, runParams, getResourceName())

        then: 'Wait until job run is completed: '

        def debugLog = result.logs

        logger.debug("Procedure log:\n$debugLog\n")

        def upperStepSummary = getJobUpperStepSummary(result.jobId)
        logger.debug("Summary: " + upperStepSummary)

        expect: 'Outcome and Upper Summary verification'
        assert result.outcome == expectedOutcome
        if (expectedOutcome == expectedOutcomes.success && result.outcome == expectedOutcomes.success) {
            assert jmsModuleExists(jmsModuleName)
        }

        if (expectedSummaryMessage) {
            assert upperStepSummary.contains(expectedSummaryMessage)
        }

        def expectedLogs = buildExpectedLogs(oldTargets, newTargets)
        logger.debug("Expected log :" + expectedLogs)
        expectedLogs.each {
            assert debugLog.contains(it)
        }

        cleanup:
        deleteJMSModule(jmsModuleName)

        where: 'The following params will be: '
        updateAction        | oldTargets      | newTargets
        'selective_update'  | targets.default | targets.twoServers
        'selective_update'  | targets.default | targets.cluster
        'selective_update'  | targets.cluster | targets.twoServers
        'remove_and_create' | targets.cluster | targets.serverAndCluster
        'selective_update'  | targets.nothing | targets.managedServer
        'remove_and_create' | targets.nothing | targets.cluster
    }

    def jmsModuleExists(def moduleName) {
        def code = """
def getJMSSystemResourcePath(jms_module_name):
    return "/JMSSystemResources/%s"%(jms_module_name)

def getJMSModulePath(jms_module_name):
    return "%s/JMSResource/%s"%(getJMSSystemResourcePath(jms_module_name),jms_module_name)

def jmsModuleExists(jmsModuleName):
    bean = getMBean(getJMSModulePath(jmsModuleName))
    if bean != None:
        print("JMS Module %s exists " % jmsModuleName)
    else:
        print("JMS Module %s does not exist" % jmsModuleName)

moduleName = '$moduleName'

connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')
try:
    jmsModuleExists(moduleName)
except Exception, e:
   print("Exception", e)
"""
        def result = runWLST(code)
        assert result.outcome == 'success'

        return (result.logs =~ /JMS Module $moduleName exists/)
    }

    def deleteConnectionFactory(moduleName, name) {
        def code = """
def getJMSSystemResourcePath(jms_module_name):
    return "/JMSSystemResources/%s"%(jms_module_name)

def getJMSModulePath(jms_module_name):
    return "%s/JMSResource/%s"%(getJMSSystemResourcePath(jms_module_name),jms_module_name)

def getConnectionFactoryPath(jms_module_name,cf_name):
    return "/JMSSystemResources/%s/JMSResource/%s/ConnectionFactories/%s" % (jms_module_name, jms_module_name, cf_name)

def deleteConnectionFactory(jmsModuleName, cfName):
    bean = getMBean('%s/ConnectionFactories/' % getJMSModulePath(jmsModuleName))
    cfBean = getMBean(getConnectionFactoryPath(jmsModuleName, cfName))
    if cfBean != None:
        bean.destroyConnectionFactory(cfBean)
        print("Removed Connection Factory %s from the module %s" % (cfName, jmsModuleName))
    else:
        print("Connection Factory %s does not exist in the module %s" % (cfName, jmsModuleName))


moduleName = '$moduleName'
cfName = '$name'

connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')
edit()
startEdit()
try:
    deleteConnectionFactory(moduleName, cfName)
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

    def prepareTargets(listString) {
        listString.split(/\s*,\s*/).each {
            if (it =~ /Cluster/) {
                ensureCluster(it)
            } else {
                ensureManagedServer(it)
            }
        }
    }


    def buildExpectedSummary(oldTargets, newTargets) {
        def oldTargetsList = oldTargets.split(/\s*,\s*/)
        def newTargetsList = newTargets.split(/\s*,\s*/)
        def intersection = oldTargetsList.findAll { oldTg ->
            newTargetsList.find { it == oldTg }
        }.size()
        def added = newTargetsList.size() - intersection
        def removed = oldTargetsList.size() - intersection
        def first = added ? "Added ${added} target(s)" : 'No new targets were added'
        def second = removed ? "Removed ${removed} target(s)" : 'No targets were removed'
        return "$first, $second"
    }

    def buildExpectedLogs(oldTargets, newTargets) {
        def retval = []
        newTargets.split(/\s*,\s*/).each { newTg ->
            if (!oldTargets.split(/\s*,\s*/).find { it == newTg })
                retval << "Adding target ${getTargetName(newTg)}"
        }

        oldTargets.split(/\s*,\s*/).each { oldTg ->
            if (!newTargets.split(/\s*,\s*/).find { it == oldTg })
                retval << "Removing target ${getTargetName(oldTg)}"
        }
        return retval
    }

    def getTargetName(tg) {
        if (tg =~ /Cluster/) {
            return "Cluster \"${tg}\""
        } else {
            return "Server \"${tg}\""
        }
    }
}
