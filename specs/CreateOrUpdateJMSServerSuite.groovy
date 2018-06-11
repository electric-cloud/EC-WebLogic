import spock.lang.*

class CreateOrUpdateJMSServerSuite extends WebLogicHelper {

    /**
     * Dsl Parameters
     */

    @Shared
    def procedureName = 'CreateOrUpdateJMSServer'
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
    def jmsServerNames = [
        empty  : '',
        default: 'TestJMSServerName',
    ]

    // Optional
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

    def doSetupSpec() {
        setupResource()
        deleteProject(projectName)

        createConfig(CONFIG_NAME)
        discardChanges()

        dslFile "dsl/procedures.dsl", [
            projectName  : projectName,
            procedureName: procedureName,
            resourceName : getResourceName(),
            params       : [
                configname                  : CONFIG_NAME,
                ecp_weblogic_jms_server_name: '',

                ecp_weblogic_update_action  : '',
                ecp_weblogic_target         : '',
            ],
        ]

    }

    @Shared
    def jmsServerName
    @Shared
    def updateAction
    @Shared
    def target = 'AdminServer'

    def expectedOutcome
    def expectedSummaryMessage

    @Unroll
    def 'CreateOrUpdateJMSServer. JMS Server name: #jmsServerName, Update Action: #updateAction, Target: #target'() {
        setup: 'Define the parameters for Procedure running'

        def runParams = [
            ecp_weblogic_jms_server_name: jmsServerName,

            ecp_weblogic_update_action  : updateAction,
            ecp_weblogic_target         : target,
        ]

        ensureManagedServer(target, '7999')

        if (updateAction != '') {
            createJMSServer(jmsServerName, targets.default)
        }

        when: 'Procedure runs: '

        def result = runTestedProcedure(projectName, procedureName, runParams, getResourceName())

        then: 'Wait until job run is completed: '

        def debugLog = result.logs
        println "Procedure log:\n$debugLog\n"

        // Saving for cleanup section
        def outcome = result.outcome

        expect: 'Outcome and Upper Summary verification'
        assert result.outcome == expectedOutcome

        if (expectedOutcome == expectedOutcomes.success && result.outcome == expectedOutcomes.success) {
//            assert jmsServerExists(jmsModuleName)
        }

        if (expectedSummaryMessage) {
            def upperStepSummary = getJobUpperStepSummary(result.jobId)
            assert upperStepSummary.contains(expectedSummaryMessage)
        }

        cleanup: 'Remove created entity'
        if (expectedOutcome == expectedOutcomes.success && outcome == expectedOutcomes.success) {
            deleteJMSServer(jmsServerName)
        }

        where: 'The following params will be: '
        updateAction                    | jmsServerName                                    | target          | expectedOutcome          | expectedSummaryMessage
        // Create
        updateActions.empty             | jmsServerNames.default                           | targets.default | expectedOutcomes.success | "Created JMS Server $jmsServerName"

        // Empty Name
        updateActions.empty             | jmsServerNames.empty                             | targets.default | expectedOutcomes.error   | "No JMS Server Name is provided"

        // Update
        updateActions.do_nothing        | jmsServerNames.default + randomize(updateAction) | targets.default | expectedOutcomes.success | "JMS Server $jmsServerName exists, no further action is required"

        // Selective without change of target
        updateActions.selective_update  | jmsServerNames.default + randomize(updateAction) | targets.default | expectedOutcomes.success | "Targets are not changed, update is not needed"

        // Selective with changed target
        updateActions.selective_update  | jmsServerNames.default + randomize(updateAction) | targets.update  | expectedOutcomes.success | "Updated JMS Server $jmsServerName, Removed target Server \"${targets.default}\", Added target Server \"${targets.update}\""

        // Recreate
        updateActions.remove_and_create | jmsServerNames.default + randomize(updateAction) | targets.default | expectedOutcomes.success | "Recreated JMS Server $jmsServerName, Added target Server \"$target\""
    }

    def deleteJMSServer(name) {
        def code = """

"""
    }

    def createJMSServer(name, target) {
        def code = """
connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')

jmsServerName = '$name'
targetName = '$target'

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
        def result = runWLST(code, "CreateJMSServer_$name")
        assert result.outcome == 'success'
        result
    }
}
