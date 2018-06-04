import spock.lang.*

class DeleteJMSModuleSuite extends WebLogicHelper {
    /**
     * Environments Variables
     */
    static String wlstPath = System.getenv('WEBLOGIC_WLST_PATH')

    /**
     * Dsl Parameters
     */

    @Shared
    def procedureName = 'DeleteJMSModule'
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
    def configName

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
        createConfig(pluginConfigurationNames.correct)

        createJMSModule(jmsModuleNames.default)
//        createConnectionFactory(jmsModuleNames.default, connectionFactories.correct)
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
    def "Delete JMS Module. (Module : #jmsModuleName, configuration name : #configName) - procedure"() {
        setup: 'Define the parameters for Procedure running'
        def runParams = [
                configname                  : configName,
                ecp_weblogic_jms_module_name: jmsModuleName,
        ]

        // Create JMS Module to delete unless it should not exist
        if (jmsModuleName != jmsModuleNames.nonexisting) {
            createJMSModule(jmsModuleName)
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
            assert !checkJMSModuleExists(jmsModuleName)
        }

        if (expectedJobDetailedResult) {
            assert debugLog.contains(expectedJobDetailedResult)
        }

        if (expectedSummaryMessage) {
            assert upperStepSummary.contains(expectedSummaryMessage)
        }
        where: 'The following params will be: '
        configName                       | jmsModuleName              | expectedOutcome          | expectedJobDetailedResult                         | expectedSummaryMessage

        // delete JMS Module
        pluginConfigurationNames.correct | jmsModuleNames.default     | expectedOutcomes.success | "JMS Module $jmsModuleName has been deleted"      | "Deleted JMS System Module $jmsModuleName"

        // delete non-existing jms module from non-existing connection factory
        pluginConfigurationNames.correct | jmsModuleNames.nonexisting | expectedOutcomes.error   | "JMS System Module $jmsModuleName does not exist" | ''

        // check with empty configuration
        pluginConfigurationNames.empty   | jmsModuleNames.default     | expectedOutcomes.error   | ''                                                | ''

        // Check with wrong configuration name
        pluginConfigurationNames.empty   | jmsModuleNames.default     | expectedOutcomes.error   | ''                                                | ''

    }

    def checkJMSModuleExists(jmsModuleName) {
        def code = """
jmsModuleName = '${jmsModuleName}'
target = 'AdminServer'

def getJMSResource(name):
    if (name == None or name == ''):
        raise Exception("No JMS Module Name is provided")
    mbean = getMBean('/JMSSystemResources/%s' % name)
    if mbean == None:
        return None
    else:
        print("Got JMS Bean %s" % mbean)
        return mbean.getJMSResource()

connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')
cd('/')

jmsResource = getJMSResource(jmsModuleName)
print("Found JMS Resource %s" % jmsModuleName)
if jmsResource == None:
    print("JMS Resource %s does not exist" % jmsModuleName)
else:
    print("JMS Resource %s exists" % jmsModuleName)
"""
        def result = runWLST(code)
        assert result.outcome == 'success'
        return result.logs?.contains("JMS Resource $jmsModuleName exists")
    }
//
//    def createConnectionFactory(jmsModuleName, name) {
//        def code = """
//resource_name = '$name'
//cfName = '$name'
//jmsModuleName = '${jmsModuleName}'
//target = 'AdminServer'
//
//def getJMSResource(name):
//    if (name == None or name == ''):
//        raise Exception("No JMS Module Name is provided")
//    mbean = getMBean('/JMSSystemResources/%s' % name)
//    if mbean == None:
//        return None
//    else:
//        print("Got JMS Bean %s" % mbean)
//        return mbean.getJMSResource()
//
//def getConnectionFactoryPath(jms_module_name,cf_name):
//    return "/JMSSystemResources/%s/JMSResource/%s/ConnectionFactories/%s" % (jms_module_name, jms_module_name, cf_name)
//
//connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')
//cd('/')
//edit()
//
//jmsResource = getJMSResource(jmsModuleName)
//print("Found JMS Resource %s" % jmsModuleName)
//if jmsResource == None:
//    raise Exception("JMS Resource %s does not exist" % jmsModuleName)
//cf = getMBean(getConnectionFactoryPath(jmsModuleName, cfName))
//update = False
//if cf == None:
//    print("Connection Factory %s does not exist" % cfName)
//    startEdit()
//    cf = jmsResource.createConnectionFactory(cfName)
//    print("Created Connection Factory %s" % cfName)
//    activate()
//else:
//    print("Connection Factory %s already exists" % cfName)
//"""
//        def result = runWLST(code)
//        assert result.outcome == 'success'
//    }

}
