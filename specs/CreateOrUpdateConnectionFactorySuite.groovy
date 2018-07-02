import spock.lang.*

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

    @Shared
    def targets = [
        empty  : '',
        default: 'AdminServer'
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
    def sharingPolicies = [
        exclusive: 'Exclusive',
        sharable : 'Sharable'
    ]

    @Shared
    def clientPolicies = [
        restricted: 'Restricted',

    ]

    @Shared
    def additionalOptionsIs = [
        empty          : '',
        defaultPriority: 'DefaultDeliveryParams.DefaultPriority=5'
    ]

    /**
     * Test Parameters: for Where section
     */
    @Shared
    def caseId

    // Procedure params
    def configname

    // This is shared to allow interpolation in 'where' section
    @Shared
    def connectionFactoryName
    def jndiName
    def cfSharingPolicy
    def cfClientIdPolicy
    @Shared
    def jmsModuleName = 'TestJMSModule'

    //optional parameters
    def cfMaxMessagesPerSession
    def cfXaEnabled
    def subdeploymentName
    def jmsServerName
    @Shared
    def updateAction
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
        createConfig(CONFIG_NAME)

        discardChanges()
        deleteProject(projectName)

        createJMSModule(jmsModuleName)
        createJMSServer(jmsServers.first)
        createJMSServer(jmsServers.second)
        deleteConnectionFactory(jmsModuleName, connectionFactories.updated)


        dslFile "dsl/procedures.dsl", [
            projectName  : projectName,
            resourceName : getResourceName(),
            procedureName: procedureName,
            params       : [
                configname                 : CONFIG_NAME,
                cf_name                    : '',
                jndi_name                  : '',
                cf_sharing_policy          : '',
                cf_client_id_policy        : '',
                jms_module_name            : '',
                wls_instance_list          : '',
                cf_max_messages_per_session: '',
                cf_xa_enabled              : '',
                subdeployment_name         : '',
                jms_server_list            : '',
                update_action              : '',
                additional_options         : '',
            ]
        ]

        dslFile("dsl/Application/CreateOrUpdateConnectionFactory.dsl", [
            resourceName   : getResourceName()
        ])
    }

    /**
     * Clean Up actions after test will finished
     */

    def doCleanupSpec() {
        deleteJMSModule(jmsModuleName)
    }

    /**
     * Positive Scenarios
     */

    @Unroll
    def "#caseId. CreateOrUpdateConnectionFactory - create with additional options : '#additionalOptions', cfXaEnabled : '#cfXaEnabled' - procedure"() {
        setup: 'Define the parameters for Procedure running'

        cfSharingPolicy = sharingPolicies.exclusive
        cfClientIdPolicy = clientPolicies.restricted
//        jndiName = jndiNames.correct

        def runParams = [
            cf_name                    : connectionFactoryName,
            jndi_name                  : jndiName,
            cf_sharing_policy          : cfSharingPolicy,
            cf_client_id_policy        : cfClientIdPolicy,
            jms_module_name            : jmsModuleName,
            wls_instance_list          : '',
            cf_max_messages_per_session: cfMaxMessagesPerSession,
            cf_xa_enabled              : cfXaEnabled,
            subdeployment_name         : subdeploymentName,
            jms_server_list            : jmsServerName,
            update_action              : updateAction,
            additional_options         : additionalOptions,
        ]

        deleteConnectionFactory(jmsModuleName, connectionFactoryName)

        when: 'Procedure runs: '

        def result = runTestedProcedure(projectName, procedureName, runParams, getResourceName())

        then: 'Wait until job run is completed: '

        def outcome = result.outcome
        def debugLog = result.logs

        println "Procedure log:\n$debugLog\n"

        def upperStepSummary = getJobUpperStepSummary(result.jobId)
        logger.info(upperStepSummary)

        expect: 'Outcome and Upper Summary verification'

        assert result.outcome == expectedOutcome
        if (expectedOutcome == expectedOutcomes.success && outcome == expectedOutcomes.success) {
            assert connectionFactoryExists(jmsModuleName, connectionFactoryName)
        }
        assert debugLog.contains(expectedJobDetailedResult)

        // Important! The value must be actually checked!!
        def xaEnabled = getConnectionFactoryProperty(jmsModuleName, connectionFactoryName, 'TransactionParams', 'XAConnectionFactoryEnabled')
        if (cfXaEnabled == '1') {
            assert xaEnabled == '1'
        } else {
            assert xaEnabled == '0'
        }
        cleanup:
        deleteConnectionFactory(jmsModuleName, connectionFactoryName)
        where: 'The following params will be: '
        caseId  | connectionFactoryName       | cfXaEnabled | additionalOptions                   | expectedOutcome          | expectedJobDetailedResult
        'C324901' | connectionFactories.correct | ''          | ''                                  | expectedOutcomes.success | "Created Connection Factory $connectionFactoryName"
        'C325027' | connectionFactories.correct | '0'         | ''                                  | expectedOutcomes.success | "Created Connection Factory $connectionFactoryName"

        // with additional options
        'C325026' | connectionFactories.correct | '1'         | additionalOptionsIs.defaultPriority | expectedOutcomes.success | "Created Connection Factory $connectionFactoryName"
    }

    @Unroll
    def '#caseId. CreateOrUpdateConnectionFactory - create with WLS target #wlstInstanceList, JMS target #jmsServerList - procedure'() {
        setup:
        def subdeploymentName = 'Subdeployment for CF'
        def runParams = [
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

        when:
        def result = runTestedProcedure(projectName, procedureName, runParams, getResourceName())

        then:
        logger.debug(result.logs)
        assert result.outcome == 'success'

        // def resultTargets = getSubdeploymentTargets(jmsModuleName, subdeploymentName)
        // logger.debug(resultTargets.logs)
        // assert resultTargets.logs.contains(newTarget)

        cleanup:
        deleteConnectionFactory(jmsModuleName, connectionFactories.updated)
        deleteSubDeployment(jmsModuleName, subdeploymentName)

        where:
        caseId    | jmsServerList                               | wlstInstanceList
        'C325028' | "${jmsServers.first}"                       | ""
        'C325029' | "${jmsServers.second}"                      | 'AdminServer'
        'C325030' | "${jmsServers.first}"                       | 'AdminServer'
        'C325031' | ''                                          | 'AdminServer'
        'C325032' | "${jmsServers.first}, ${jmsServers.second}" | 'AdminServer'
    }

    @Unroll
    def "#caseId. CreateOrUpdateConnectionFactory - update_action : '#updateAction' - procedure"() {
        setup:
        createJMSModule(jmsModuleName)

        def subdeploymentName = randomize('sub1')

        def runParamsFirst = [
            cf_name            : connectionFactories.updated,
            jndi_name          : jndiNames.recreateOld,
            jms_module_name    : jmsModuleName,
            cf_sharing_policy  : sharingPolicies.exclusive,
            cf_client_id_policy: clientPolicies.restricted,
            subdeployment_name : '',
            jms_server_list    : ''
        ]

        def resultFirst = runTestedProcedure(projectName, procedureName, runParamsFirst, getResourceName())
        assert resultFirst.outcome == 'success'

        def jmsServerName = randomize('jmsServer1')
        createJMSServer(jmsServerName)

        when:
        def runParamsSecond = [
            cf_name            : connectionFactories.updated,
            jndi_name          : jndiNames.recreateNew,
            jms_module_name    : jmsModuleName,
            cf_sharing_policy  : sharingPolicies.exclusive,
            cf_client_id_policy: clientPolicies.restricted,
            update_action      : updateAction,
            subdeployment_name : subdeploymentName,
            jms_server_list    : jmsServerName
        ]

        def resultSecond = runTestedProcedure(projectName, procedureName, runParamsSecond, getResourceName())

        then:
        logger.debug(resultSecond.logs)
        assert resultSecond.outcome == expectedOutcome

        def resultTargets = getSubdeploymentTargets(jmsModuleName, subdeploymentName)
        logger.debug(resultTargets.logs)
        if (updateAction == 'do_nothing') {
            assert !resultTargets.logs.contains(jmsServerName)
        }
        else {
            assert resultTargets.logs.contains(jmsServerName)
        }

        cleanup:
        deleteConnectionFactory(jmsModuleName, connectionFactories.updated)
        deleteSubDeployment(jmsModuleName, subdeploymentName)

        where:
        caseId    | updateAction        | expectedOutcome
        'C325033' | 'remove_and_create' | expectedOutcomes.success
        'C325034' | 'selective_update'  | expectedOutcomes.success
        'C325035' | 'do_nothing'        | expectedOutcomes.success
    }

    @Unroll
    def "#caseId. CreateOrUpdateConnectionFactory - update_action : '#updateAction' - application"() {
        setup:
        createJMSModule(jmsModuleName)
        def subdeploymentName = 'sub1'
        def jmsServerName = 'jmsServer1'

        def runParamsFirst = [
            cf_name            : connectionFactories.updated,
            jndi_name          : jndiNames.recreateOld,
            jms_module_name    : jmsModuleName,
            cf_sharing_policy  : sharingPolicies.exclusive,
            cf_client_id_policy: clientPolicies.restricted,
        ]

        def resultFirst = runTestedProcedure(projectName, procedureName, runParamsFirst, getResourceName())

        assert resultFirst.outcome == 'success'
        createJMSServer(jmsServerName)

        when:
        def paramsStr = stringifyArray([
            cf_name            : connectionFactories.updated,
            jndi_name          : jndiNames.recreateNew,
            jms_module_name    : jmsModuleName,
            cf_sharing_policy  : sharingPolicies.exclusive,
            cf_client_id_policy: clientPolicies.restricted,
            update_action      : updateAction,
            subdeployment_name : subdeploymentName,
            jms_server_list    : jmsServerName
        ])

        def result = dsl("""
                runProcess(
                    projectName    : "$HELPER_PROJECT",
                    applicationName    : "$TEST_APPLICATION",
                    environmentName: '$ENVIRONMENT_NAME',
                    processName    : '$procedureName',
                    actualParameter: $paramsStr
                )
            """, [resourceName : getResourceName()])

        then: 'wait until process finishes'
        waitUntil {
            jobCompleted result
        }

        def logs = getJobLogs(result.jobId)
        logger.debug("Process logs: " + logs)

        assert jobStatus(result.jobId).outcome == expectedOutcome

        if (updateAction != 'do_nothing') {
            def resultTargets = getSubdeploymentTargets(jmsModuleName, subdeploymentName)
            logger.debug("getSubdeploymentTargets logs: " + resultTargets.logs)
            assert resultTargets.logs.contains(jmsServerName)
        }

        cleanup:
        deleteConnectionFactory(jmsModuleName, connectionFactories.updated)
        deleteSubDeployment(jmsModuleName, subdeploymentName)

        where:
        caseId    | updateAction        | expectedOutcome
        'C325036' | 'remove_and_create' | expectedOutcomes.success
        'C325037' | 'selective_update'  | expectedOutcomes.success
        'C325038' | 'do_nothing'        | expectedOutcomes.success
    }

    @Unroll
    def 'multi-step procedures #dslFileName #procName'() {
        given:
        def queueName = 'test queue'
        def moduleName = dslFileName
        deleteJMSModule(moduleName)
        def cfName = 'TestCFRetarget'
        def wlst = getWlstPath()
        def args = [
            projectName: projectName,
            procedureName: procName,
            configname: CONFIG_NAME,
            cfName: cfName,
            moduleName: moduleName,
            resourceName: getResourceName(),
            wlst: wlst
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
        assert result.logs =~ /Recreated Connection Factory TestCFRetarget, Subdeployment name does not require update/
        where:
        dslFileName                | procName
        'retargetCf'               | 'Retarget ConnectionFactory with recreation'
        // 'updateCFSubdeploymentName'| 'Update SD Name for ConnectionFactory (Selective Update)'
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
        def result = runWLST(code, "ConnectionFactoryExists_${name}")
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
        def result = runWLST(code, "DeleleteCF_${name}")
        assert result.outcome == 'success'
    }

    def getConnectionFactoryProperty(module, cfName, propGroup, propName) {
        def code = """
def getConnectionFactoryPath(jms_module_name,cf_name):
    return "/JMSSystemResources/%s/JMSResource/%s/ConnectionFactories/%s" % (jms_module_name, jms_module_name, cf_name)

module = '$module'
cfName = '$cfName'
group = '$propGroup'
propName = '$propName'
connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')
cd(getConnectionFactoryPath(module, cfName) + '/' + group + '/' + cfName)
print "VALUE:" + " %s" % get(propName)
"""
        def result = runWLST(code, "GetCFProperty_${propGroup}_${propName}")
        assert result.outcome == 'success'
        def group = (result.logs =~ /VALUE:\s(.+?)/)
        def value = group[0][1]
        return value
    }

    def getSubdeploymentTargets(module, subdeployment) {
        def code = """
module = '$module'
subdeployment = '$subdeployment'
connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')
bean = getMBean('/JMSSystemResources/' + module + '/SubDeployments/' + subdeployment)
if bean == None:
    print("Subdeployment %s does not exist" % subdeployment)
else:
    targets = bean.getTargets()
    for t in targets:
        print 'Target: ' + str(t.objectName)
"""
        def result = runWLST(code, "GetSubdeploymentTargets_${module}_${subdeployment}")
        assert result.outcome == 'success'
        result
    }

}
