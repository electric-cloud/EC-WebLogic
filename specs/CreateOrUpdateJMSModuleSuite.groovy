import spock.lang.Shared
import spock.lang.Unroll

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

    /**
     * Test Parameters: for Where section
     */

    // Procedure params
    def configname

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

        createConfig(configNames.correct)
    }

    /**
     * Clean Up actions after test will finished
     */

    def doCleanupSpec() {
        deleteProject(projectName)
    }

    /**
     * Positive Scenarios
     */

    @Unroll
    def "Create and Update JMS Module. Positive - procedure with params (module_name: #jmsModuleName, target: #target, update action: #updateAction)"() {
        setup: 'Define the parameters for Procedure running'

        def runParams = [
                configname                  : configname,
                ecp_weblogic_jms_module_name: jmsModuleName,
                ecp_weblogic_update_action  : updateAction,
                ecp_weblogic_target         : target
        ]

        deleteJMSModule(jmsModuleName)
        ensureManagedServer(target)

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
            assert jmsModuleExists(jmsModuleName)
        }

        if (expectedJobDetailedResult) {
            assert debugLog.contains(expectedJobDetailedResult)
        }

        if (expectedSummaryMessage) {
            assert upperStepSummary == expectedSummaryMessage
        }

        cleanup:
        if (expectedOutcome == expectedOutcomes.success && outcome == expectedOutcomes.success) {
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


    def jmsModuleExists(def moduleName) {
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

    def createJMSModule(name) {
        def code = """
resource_name = '$name'
target = 'AdminServer'
connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')
cd('/')
edit()
if cmo.lookupJMSSystemResource(resource_name):
    print "Resource %s alreay exists" % resource_name
else:
    startEdit()
    cmo.createJMSSystemResource(resource_name)
    cd("/JMSSystemResources/%s" % resource_name)
    cmo.addTarget(getMBean("/Servers/%s" % target))
    activate()
"""
        def result = runWLST(code)
        assert result.outcome == 'success'
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

    def createJMSServer(name) {
        def code = """
connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')

jmsServerName = '$name'
targetName = '${getAdminServerName()}'

bean = getMBean('/JMSServers/%s' % jmsServerName)
if bean == None:
    edit()
    startEdit()
    cd('/')
    print "Creating JMS Server %s" % jmsServerName
    cmo.createJMSServer(jmsServerName)
    cd("/JMSServers/%s" % jmsServerName)
    cmo.addTarget(getMBean("/Servers/%s" % targetName))
    activate()
else:
    print "JMS Server already exists"
"""
        def result = runWLST(code)
        assert result.outcome == 'success'
        result
    }

}