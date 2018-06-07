import spock.lang.Shared
import spock.lang.Unroll

class CreateOrUpdateConnectionFactorySuite extends WebLogicHelper {

    /**
     * Environments Variables
     */
    static String wlstPath = System.getenv('WEBLOGIC_WLST_PATH')

    /**
     * Dsl Parameters
     */

    @Shared
    def procedureName = 'CreateOrUpdateConnectionFactory'
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

    @Shared
    def jmsServers = [
        first : 'firstJMSServer',
        second: 'secondJMSServer'
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
////            Uses variables inside, so moved to datatable
//            connectionFactory_created   : 'Created Connection Factory $cfName',
//            connectionFactory_not_exists: 'Connection Factory $cfName does not exist',
    ]

    @Shared
    def sharingPolicies = [
        exclusive: 'Exclusive',
        sharable : 'Sharable'
    ]

    @Shared
    def clientPolicies = [
        restricted: 'Restricted',

    ]

    @Shared
    def additionalOptions = [
        empty          : '',
        defaultPriority: 'DefaultDeliveryParams.DefaultPriority=5'
    ]

    @Shared
    def jmsModuleName = 'TestJMSModule'

    /**
     * Test Parameters: for Where section
     */

    // Procedure params
    def configname

    // This is shared to allow interpolation in 'where' section
    @Shared
    def cf_name
    def jndi_name
    def cf_sharing_policy
    def cf_client_id_policy
    def jms_module_name

    //optional parameters
    def cf_max_messages_per_session
    def cf_xa_enabled
    def subdeployment_name
    def jms_server_name
    def update_action
    def additional_options

    // expected results
    def expectedOutcome
    def expectedSummaryMessage
    def expectedJobDetailedResult

    /**
     * Preparation actions
     */

    def doSetupSpec() {
        setupResource()
        discardChanges()
        deleteProject(projectName)
        createJMSModule(jmsModuleName)

        createConfig(pluginConfigurationNames.correct)
    }

    /**
     * Clean Up actions after test will finished
     */

    def doCleanupSpec() {
        // deleteProject(projectName)
        deleteJMSModule(jmsModuleName)
    }

    /**
     * Positive Scenarios
     */

    @Unroll
    def "Create or Update Connection Factory. additional options : '#additional_options'"() {
        setup: 'Define the parameters for Procedure running'
        def runParams = [
            configname                 : configname,
            cf_name                    : cf_name,
            jndi_name                  : jndi_name,
            cf_sharing_policy          : cf_sharing_policy,
            cf_client_id_policy        : cf_client_id_policy,
            jms_module_name            : jms_module_name,
//                ecp_weblogic_target_list   : 'AdminServer',
            cf_max_messages_per_session: cf_max_messages_per_session,
            cf_xa_enabled              : cf_xa_enabled,
            subdeployment_name         : subdeployment_name,
            jms_server_list            : jms_server_name,
            update_action              : update_action,
            additional_options         : additional_options,
        ]

        deleteConnectionFactory(jms_module_name, cf_name)

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
            assert connectionFactoryExists(jms_module_name, cf_name)
        }
        assert debugLog.contains(expectedJobDetailedResult)

        cleanup:
        deleteConnectionFactory(jms_module_name, cf_name)
        where: 'The following params will be: '
        configname                       | cf_name                     | jndi_name         | cf_sharing_policy         | cf_client_id_policy       | jms_module_name | cf_max_messages_per_session | cf_xa_enabled | subdeployment_name | jms_server_name | update_action | additional_options                | expectedOutcome          | expectedJobDetailedResult
        pluginConfigurationNames.correct | connectionFactories.correct | jndiNames.correct | sharingPolicies.exclusive | clientPolicies.restricted | jmsModuleName   | ''                          | ''            | ''                 | ''              | ''            | ''                                | expectedOutcomes.success | "Created Connection Factory $cf_name"

        // with additional options
        pluginConfigurationNames.correct | connectionFactories.correct | jndiNames.correct | sharingPolicies.exclusive | clientPolicies.restricted | jmsModuleName   | ''                          | ''            | ''                 | ''              | ''            | additionalOptions.defaultPriority | expectedOutcomes.success | "Created Connection Factory $cf_name"
    }

    @Unroll
    def "CreateOrUpdateConnectionFactory - update_action : '#update_action', jms_server_list: #jmsServerList, wls_instance_list: #wlstInstanceList"() {
        setup:
        createJMSModule(jmsModuleName)
        def subdeploymentName = 'sub1'
        when:
        def runParamsFirst = [
            configname         : pluginConfigurationNames.correct,
            cf_name            : connectionFactories.updated,
            jndi_name          : jndiNames.recreateOld,
            jms_module_name    : jmsModuleName,
            cf_sharing_policy  : sharingPolicies.exclusive,
            cf_client_id_policy: clientPolicies.restricted,
        ]

        def resultFirst = runTestedProcedure(projectName, procedureName, runParamsFirst, getResourceName())
        assert resultFirst.outcome == 'success'

        def jmsServerName = 'jmsServer1'
        createJMSServer(jmsServerName)
        and:
        def runParamsSecond = [
            configname         : pluginConfigurationNames.correct,
            cf_name            : connectionFactories.updated,
            jndi_name          : jndiNames.recreateNew,
            jms_module_name    : jmsModuleName,
            cf_sharing_policy  : sharingPolicies.exclusive,
            cf_client_id_policy: clientPolicies.restricted,
            update_action      : update_action,
            subdeployment_name : subdeploymentName,
            jms_server_list    : jmsServerName
        ]

        def resultSecond = runTestedProcedure(projectName, procedureName, runParamsSecond, getResourceName())

        then:
        logger.debug(resultSecond.logs)
        assert resultSecond.outcome == expectedOutcome

        def resultTargets = getSubdeploymentTargets(jmsModuleName, subdeploymentName)
        logger.debug(resultTargets.logs)
        assert resultTargets.logs.contains(jmsServerName)

        cleanup:
        deleteConnectionFactory(jmsModuleName, connectionFactories.updated)
        deleteSubDeployment(jmsModuleName, subdeploymentName)

        where:
        update_action       | expectedOutcome
        'remove_and_create' | expectedOutcomes.success
        'selective_update'  | expectedOutcomes.success
        'do_nothing'        | expectedOutcomes.success
    }

    @Unroll
    def 'create with WLS target #wlstInstanceList, JMS target #jmsServerList'() {
        setup:
        def cfName = 'ConnectionFactoryWith Targets'
        deleteConnectionFactory(jmsModuleName, cfName)
        createJMSServer(jmsServers.first)
        createJMSServer(jmsServers.second)
        def subdeploymentName = cfName
        when:
        def runParamsSecond = [
            configname         : pluginConfigurationNames.correct,
            cf_name            : connectionFactories.updated,
            jndi_name          : jndiNames.recreateNew,
            jms_module_name    : jmsModuleName,
            cf_sharing_policy  : sharingPolicies.exclusive,
            cf_client_id_policy: clientPolicies.restricted,
            update_action      : 'do_nothing',
            subdeployment_name : subdeploymentName,
            jms_server_list    : jmsServerList,
            wls_instance_list  : wlstInstanceList
        ]

        def resultSecond = runTestedProcedure(projectName, procedureName, runParamsSecond, getResourceName())

        then:
        logger.debug(resultSecond.logs)
        assert resultSecond.outcome == 'success'

        // def resultTargets = getSubdeploymentTargets(jmsModuleName, subdeploymentName)
        // logger.debug(resultTargets.logs)
        // assert resultTargets.logs.contains(newTarget)

        cleanup:
        deleteConnectionFactory(jmsModuleName, connectionFactories.updated)
        deleteSubDeployment(jmsModuleName, subdeploymentName)

        where:
        jmsServerList                               | wlstInstanceList
        "${jmsServers.first}"                       | ""
        "${jmsServers.second}"                      | 'AdminServer'
        "${jmsServers.first}"                       | 'AdminServer'
        ''                                          | 'AdminServer'
        "${jmsServers.first}, ${jmsServers.second}" | 'AdminServer'
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
cfBean = getMBean(getConnectionFactoryPath(moduleName, cfName))
if cfBean == None:
    print "Connection Factory %s does not exist" % cfName
else:
    edit()
    startEdit()
    deleteConnectionFactory(moduleName, cfName)
    activate()

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

    def getSubdeploymentTargets(module, subdeployment) {
        def code = """
module = '$module'
subdeployment = '$subdeployment'
connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')
bean = getMBean('/JMSSystemResources/' + module + '/SubDeployments/' + subdeployment)
print bean
targets = bean.getTargets()
for t in targets:
    print 'Target: ' + str(t.objectName)
"""
        def result = runWLST(code)
        assert result.outcome == 'success'
        result
    }
}
