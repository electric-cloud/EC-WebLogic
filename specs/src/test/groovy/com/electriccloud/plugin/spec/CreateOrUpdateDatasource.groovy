package com.electriccloud.plugin.spec

import spock.lang.Shared
import spock.lang.Unroll

class CreateOrUpdateDatasource extends WebLogicHelper {

    /**
     * Dsl Parameters
     */

    @Shared
    String procedureName = 'CreateOrUpdateDatasource'
    @Shared
    String projectName = "EC-WebLogic ${procedureName}"

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
        empty  : '',
        correct: 'datasources.TestJNDIName',
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
    def additionalOptions = [
        empty      : '',
        maxCapacity: 'Resource.JDBCConnectionPoolParams.MaxCapacity=20',
        double     : "JDBCResource.JDBCConnectionPoolParams.InitialCapacity=2\nResource.JDBCConnectionPoolParams.MaxCapacity=20"
    ]

    @Shared
    def dbNames = [
        empty : '',
        medrec: 'medrec'
    ]

    /**
     * Test Parameters: for Where section
     */
    @Shared
    def caseId

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
    def "CreateOrUpdateDatasource - create - procedure, options: #options, targets: #tg"() {
        setup: 'Define the parameters for Procedure running'
        def dsName = datasources.correct
        Map runParams = [
            ecp_weblogic_dataSourceName       : dsName,
            ecp_weblogic_dataSourceDriverClass: drivers.derby,
            ecp_weblogic_databaseUrl          : urls.medrec,
            ecp_weblogic_jndiName             : jndiNames.correct,
            ecp_weblogic_targets              : tg,
            ecp_weblogic_additionalOptions    : options,
            ecp_weblogic_databaseName         : 'medrec;create=true'
        ]

        deleteDatasource(dsName)
        when: 'Procedure runs: '

        def result = runProcedure(projectName, procedureName, runParams, [], getResourceName())

        then: 'Wait until job run is completed: '
        def upperStepSummary = getJobUpperStepSummary(result.jobId)
        logger.info(upperStepSummary)
        expect: 'Outcome and Upper Summary verification'

        assert result.outcome != 'error'
//        assert upperStepSummary =~ /Created datasource $dsName successfully/
        cleanup:
        deleteDatasource(dsName)
        where:
        options                       | tg
        additionalOptions.empty       | targets.empty
        additionalOptions.maxCapacity | targets.default
    }

    @Unroll
    def 'Update datasource action #updateAction'() {
        setup: 'Define the parameters for Procedure running'
        def dsName = datasources.correct
        def firstRunParams = [
            ecp_weblogic_dataSourceName       : dsName,
            ecp_weblogic_dataSourceDriverClass: drivers.derby,
            ecp_weblogic_databaseUrl          : urls.medrec,
            ecp_weblogic_jndiName             : jndiNames.correct,
            ecp_weblogic_targets              : '',
            ecp_weblogic_additionalOptions    : '',
            ecp_weblogic_updateAction         : updateAction,
            ecp_weblogic_databaseName         : 'test;create=true',
        ]

        def secondRunParams = firstRunParams << [ecp_weblogic_targets: targets.default]

        deleteDatasource(dsName)
        def firstRun = runProcedure(projectName, procedureName, firstRunParams, [], getResourceName())
        assert firstRun.outcome != 'error'
        when: 'Procedure runs: '

        def result = runProcedure(projectName, procedureName, secondRunParams, [], getResourceName())
        then:
        assert result.outcome == 'success'
        checkStepSummary(updateAction, dsName, getJobUpperStepSummary(result.jobId))
        cleanup:
        deleteDatasource(dsName)
        where:
        updateAction << ['do_nothing', 'selective_update', 'remove_and_create']
    }


    def checkStepSummary(action, name, summary) {
        if (action == 'do_nothing') {
            assert summary =~ /Datasource $name exists, no further action is required/
        } else if (action == 'selective_update') {
            assert summary =~ /Updated datasource $name/
        } else {
            assert summary =~ /Recreated datasource $name/
        }
        return true
    }

}
