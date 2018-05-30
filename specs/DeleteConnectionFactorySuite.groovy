import spock.lang.Shared
import spock.lang.Unroll

class DeleteConnectionFactorySuite extends WebLogicHelper {
    /**
     * Environments Variables
     */
    static String wlstPath = System.getenv('WEBLOGIC_WLST_PATH')

    /**
     * Dsl Parameters
     */

    @Shared
    def procedureName = 'DeleteConnectionFactory'
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
    def pluginConfigurationNames = [
            empty    : '',
            correct  : CONFIG_NAME,
            incorrect: 'incorrect config Name',
    ]

    @Shared
    def connectionFactories = [
            correct    : 'SpecConnectionFactory',
            nonexisting: 'NoSuchCF'
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
            empty: '',
    ]

    @Shared
    def jmsModuleNames = [
            default    : 'TestJMSModule',
            nonexisting: 'NoSuchJMSModule'
    ]

    /**
     * Test Parameters: for Where section
     */

    // Procedure params
    @Shared
    def configname

    @Shared
    def cf_name

    @Shared
    def jms_module_name

    // expected results
    def expectedOutcome
    def expectedSummaryMessage
    def expectedJobDetailedResult

    /**
     * Preparation actions
     */

    def doSetupSpec() {
        setupResource()
        createConfig(pluginConfigurationNames.correct)

        deleteProject(projectName)
        createJMSModule(jmsModuleNames.default)
    }

    /**
     * Clean Up actions after test will finished
     */

    def doCleanupSpec() {
        deleteProject(projectName)

        deleteConfiguration('EC-WebLogic', pluginConfigurationNames.correct)
        deleteConfiguration('EC-WebLogic', pluginConfigurationNames.incorrect)
    }

    /**
     * Positive Scenarios
     */

    @Unroll
    def "Delete Connection Factory. Positive and negative - procedure"() {
        setup: 'Define the parameters for Procedure running'
        def runParams = [
                configname     : configname,
                cf_name        : cf_name,
                jms_module_name: jms_module_name,
        ]

        // Create connection factory to delete unless it should not exist
        if (cf_name != connectionFactories.nonexisting){
            createConnectionFactory(cf_name)
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
            assert !connectionFactoryExists(jms_module_name, cf_name)
        }
        assert debugLog.contains(expectedJobDetailedResult)
//        assert upperStepSummary.contains(expectedSummaryMessage)

        cleanup:
        if (connectionFactoryExists(jms_module_name, cf_name)) {
            deleteConnectionFactory(jms_module_name, cf_name)
        }

        where: 'The following params will be: '
        configname                       | cf_name                         | jms_module_name            | expectedOutcome          | expectedJobDetailedResult

        // delete connection factory
        pluginConfigurationNames.correct | connectionFactories.correct     | jmsModuleNames.default     | expectedOutcomes.success | "Removed Connection Factory $cf_name from the module $jms_module_name"

        // delete non-existing connection factory
        pluginConfigurationNames.correct | connectionFactories.nonexisting | jmsModuleNames.default     | expectedOutcomes.error   | "Connection Factory $cf_name does not exist in the module $jms_module_name"

        // delete non-existing connection factory from non-existing jms module
        pluginConfigurationNames.correct | connectionFactories.nonexisting | jmsModuleNames.nonexisting | expectedOutcomes.error   | "Connection Factory $cf_name does not exist in the module $jms_module_name"


    }

    def createConnectionFactory(def name, def module_name = jmsModuleNames.default) {

        dslFile 'dsl/procedures.dsl', [
                projectName  : projectName,
                procedureName: 'CreateOrUpdateConnectionFactory',
                resourceName : getResourceName(),
                params       : [
                        configname         : "${pluginConfigurationNames.correct}",
                        cf_name            : "$name",
                        jms_module_name    : "$module_name",
                        cf_sharing_policy  : "Exclusive",
                        cf_client_id_policy: "Restricted"
                ]
        ]

        def result = runProcedure """
        runProcedure(
            projectName: '$projectName',
            procedureName: 'CreateOrUpdateConnectionFactory',
            actualParameter: [
                configname         : '${pluginConfigurationNames.correct}',
                cf_name            : '$name',
                jms_module_name    : '$module_name',
                cf_sharing_policy  : 'Exclusive',
                cf_client_id_policy: 'Restricted'
            ]
        )
        """, getResourceName()

        assert result.outcome == 'success'
    }

    def connectionFactoryExists(def moduleName, def name) {
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
