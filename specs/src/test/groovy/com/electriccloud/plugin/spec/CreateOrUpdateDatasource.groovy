package com.electriccloud.plugin.spec

import spock.lang.Shared
import spock.lang.Unroll

class CreateOrUpdateDatasource extends WebLogicHelper {

    /**
     * Dsl Parameters
     */

    @Shared
    def procedureName = 'CreateOrUpdateDatasource'
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
    def datasources = [
        correct    : 'SpecDatasource',
        updated    : 'SpecUpdatedDatasource',
        nonexisting: 'NoSuchDS'
    ]

    @Shared
    def jndiNames = [
        empty      : '',
        correct    : 'datasources.TestJNDIName',
    ]

    @Shared
    def drivers = [
        derby: 'org.apache.derby.jdbc.ClientXADataSource'
    ]

    @Shared
    def urls = [
        medrec: 'jdbc:derby://localhost:1527/medrec;ServerName=localhost;databaseName=medrec;create=true'
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

        dslFile "dsl/procedures.dsl", [
            projectName  : projectName,
            resourceName : getResourceName(),
            procedureName: procedureName,
            params       : [
                configname                        : CONFIG_NAME,
                ecp_weblogic_dataSourceName       : '',
                ecp_weblogic_dataSourceDriverClass: '',
                ecp_weblogic_databaseUrl          : '',
                ecp_weblogic_jndiName             : '',
                ecp_weblogic_dataSourceCredentials: 'medrec',
                ecp_weblogic_databaseName         : '',
                ecp_weblogic_targets              : '',
                ecp_weblogic_updateAction         : '',
                ecp_weblogic_additionalOptions    : '',
            ]
        ]

        dsl """
credential(userName: 'medrec', password: 'medrec', credentialName: 'medrec', projectName: '$projectName')
attachCredential projectName: '$projectName',
    credentialName: 'medrec',
    procedureName: '$procedureName',
    stepName: 'RunProcedure'
"""


    }

    /**
     * Clean Up actions after test will finished
     */

    def doCleanupSpec() {
    }

    /**
     * Positive Scenarios
     */

    @Unroll
    def "CreateOrUpdateDatasource - create - procedure"() {
        setup: 'Define the parameters for Procedure running'
        def dsName = datasources.correct
        def runParams = [
            ecp_weblogic_dataSourceName: dsName,
            ecp_weblogic_dataSourceDriverClass: drivers.derby,
            ecp_weblogic_databaseUrl: urls.medrec,
            ecp_weblogic_jndiName: jndiNames.correct
        ]

        deleteDatasource(dsName)
        when: 'Procedure runs: '

        def result = runTestedProcedure(projectName, procedureName, runParams, getResourceName())

        then: 'Wait until job run is completed: '

        def outcome = result.outcome
        def debugLog = result.logs

        println "Procedure log:\n$debugLog\n"
        assert outcome == 'success'

        def upperStepSummary = getJobUpperStepSummary(result.jobId)
        logger.info(upperStepSummary)

        expect: 'Outcome and Upper Summary verification'
//
//        assert result.outcome == expectedOutcome
//        if (expectedOutcome == expectedOutcomes.success && outcome == expectedOutcomes.success) {
//            assert connectionFactoryExists(jmsModuleName, connectionFactoryName)
//        }
//        assert debugLog.contains(expectedJobDetailedResult)
//
//        // Important! The value must be actually checked!!
//        def xaEnabled = getConnectionFactoryProperty(jmsModuleName, connectionFactoryName, 'TransactionParams', 'XAConnectionFactoryEnabled')
//        if (cfXaEnabled == '1') {
//            assert xaEnabled == '1'
//        } else {
//            assert xaEnabled == '0'
//        }
//
//        def defaultTargeting = getDefaultTargeting(jmsModuleName, connectionFactoryName)
//        assert defaultTargeting == '1'
        cleanup:
        deleteDatasource(dsName)
//        deleteConnectionFactory(jmsModuleName, connectionFactoryName)
//        where: 'The following params will be: '
    }
}
