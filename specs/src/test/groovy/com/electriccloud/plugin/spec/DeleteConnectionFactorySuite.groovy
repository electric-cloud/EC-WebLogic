package com.electriccloud.plugin.spec

import spock.lang.*

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
    @Shared
    def caseId

    // Procedure params
    @Shared
    def connectionFactoryName

    @Shared
    def jmsModuleName

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

        deleteProject(projectName)
        createJMSModule(jmsModuleNames.default)

        dslFile "dsl/procedures.dsl", [
            projectName  : projectName,
            resourceName : getResourceName(),
            procedureName: procedureName,
            params       : [
                configname     : CONFIG_NAME,
                cf_name        : '',
                jms_module_name: '',
            ]
        ]

        dslFile("dsl/Application/DeleteConnectionFactory.dsl", [
            resourceName: getResourceName()
        ])
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
    def "Delete Connection Factory. connectionFactoryName : '#connectionFactoryName', jmsModuleName : '#jmsModuleName' - procedure"() {
        setup: 'Define the parameters for Procedure running'
        def runParams = [
            cf_name        : connectionFactoryName,
            jms_module_name: jmsModuleName,
        ]

        // Create connection factory to delete unless it should not exist
        if (connectionFactoryName != connectionFactories.nonexisting) {
            createConnectionFactory(connectionFactoryName)
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
            assert !connectionFactoryExists(jmsModuleName, connectionFactoryName)
        }
        assert debugLog.contains(expectedJobDetailedResult)
        checkServerRestartOutputParameter(result.jobId)
        cleanup:
        if (expectedOutcome == expectedOutcomes.success && result.outcome != expectedOutcomes.success) {
            deleteConnectionFactory(jmsModuleName, connectionFactoryName)
        }
        where: 'The following params will be: '
        caseId    | connectionFactoryName           | jmsModuleName              | expectedOutcome          | expectedJobDetailedResult

        // delete connection factory
        'C325176' | connectionFactories.correct     | jmsModuleNames.default     | expectedOutcomes.success | "Removed Connection Factory $connectionFactoryName from the module $jmsModuleName"

        // delete non-existing connection factory
        'C325177' | connectionFactories.nonexisting | jmsModuleNames.default     | expectedOutcomes.error   | "Connection Factory $connectionFactoryName does not exist in the module $jmsModuleName"

        // delete non-existing connection factory from non-existing jms module
        'C325178' | connectionFactories.nonexisting | jmsModuleNames.nonexisting | expectedOutcomes.error   | "Connection Factory $connectionFactoryName does not exist in the module $jmsModuleName"
    }


    @Unroll
    def "Delete Connection Factory. connectionFactoryName : '#connectionFactoryName', jmsModuleName : '#jmsModuleName' - application"() {
        setup: 'Define the parameters for Procedure running'
        def paramsStr = stringifyArray([
            cf_name        : connectionFactoryName,
            jms_module_name: jmsModuleName,
        ])

        // Create connection factory to delete unless it should not exist
        if (connectionFactoryName != connectionFactories.nonexisting) {
            createConnectionFactory(connectionFactoryName)
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
            def pageAfterDeploy = checkUrl(APPLICATION_PAGE_URL)
            assert pageAfterDeploy.code == NOT_FOUND_RESPONSE
        }

        cleanup:
        if (expectedOutcome == expectedOutcomes.success && result.outcome != expectedOutcomes.success) {
            deleteConnectionFactory(jmsModuleName, connectionFactoryName)
        }

        where: 'The following params will be: '
        caseId    | connectionFactoryName           | jmsModuleName              | expectedOutcome          | expectedJobDetailedResult

        // delete connection factory
        'C325179' | connectionFactories.correct     | jmsModuleNames.default     | expectedOutcomes.success | "Removed Connection Factory $connectionFactoryName from the module $jmsModuleName"

        // delete non-existing connection factory
        'C325180' | connectionFactories.nonexisting | jmsModuleNames.default     | expectedOutcomes.error   | "Connection Factory $connectionFactoryName does not exist in the module $jmsModuleName"

        // delete non-existing connection factory from non-existing jms module
        'C325181' | connectionFactories.nonexisting | jmsModuleNames.nonexisting | expectedOutcomes.error   | "Connection Factory $connectionFactoryName does not exist in the module $jmsModuleName"
    }

    def createConnectionFactory(def name, def module_name = jmsModuleNames.default) {

        dslFile 'dsl/procedures.dsl', [
            projectName  : projectName,
            procedureName: 'CreateOrUpdateConnectionFactory',
            resourceName : getResourceName(),
            params       : [
                configname         : CONFIG_NAME,
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
                configname         : '${CONFIG_NAME}',
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
}