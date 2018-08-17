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
        correct: 'org.apache.derby.jdbc.ClientXADataSource',
        derby: 'org.apache.derby.jdbc.ClientXADataSource',
        incorrect: 'org.incorrect.jdbc.driver',
        emty: '',
    ]

    @Shared
    def urls = [
        correct: "jdbc:derby://${derbyHost}:1527/medrec;ServerName=${derbyHost};databaseName=medrec;create=true",
        medrec: "jdbc:derby://${derbyHost}:1527/medrec;ServerName=${derbyHost};databaseName=medrec;create=true",
        incorrect: "incorrect URL",
        empty: "",
    ]

    @Shared
    def targets = [
        empty  : '',
        default: 'AdminServer',
        correct: 'AdminServer',
        incorrect: 'IncorrectServer'
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

    @Shared
    def driverProps = [
        empty     : '',
        serverName: "serverName=${derbyHost}"
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

        def params = [
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
            ecp_weblogic_driverProperties     : ''
        ]

        dslFile "dsl/procedures.dsl", [
            projectName  : projectName,
            resourceName : getResourceName(),
            procedureName: procedureName,
            params       : params
        ]

        dslFile "dsl/pipeline.dsl", [
            projectName  : projectName,
            resourceName : resourceName,
            procedureName: procedureName,
            params       : params
        ]

        dsl """
credential(userName: 'medrec', password: 'medrec', credentialName: 'medrec', projectName: '$projectName')
attachCredential projectName: '$projectName',
    credentialName: 'medrec',
    procedureName: '$procedureName',
    stepName: 'RunProcedure'
    
attachCredential projectName: '$projectName',
    credentialName: 'medrec',
    taskName: 'RunProcedure',
    pipelineName: "$procedureName",
    stageName: "Stage"

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
    def "CreateOrUpdateDatasource - create - procedure, options: #options, targets: #tg, driver properties #driverProperties"() {
        setup: 'Define the parameters for Procedure running'
        def dsName = datasources.correct
        Map runParams = [
            ecp_weblogic_dataSourceName       : dsName,
            ecp_weblogic_dataSourceDriverClass: drivers.derby,
            ecp_weblogic_databaseUrl          : urls.medrec,
            ecp_weblogic_jndiName             : jndiNames.correct,
            ecp_weblogic_targets              : tg,
            ecp_weblogic_additionalOptions    : options,
            ecp_weblogic_databaseName         : 'medrec;create=true',
            ecp_weblogic_driverProperties     : driverProperties
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
        options                       | tg              | driverProperties
        additionalOptions.empty       | targets.empty   | driverProps.empty
        additionalOptions.maxCapacity | targets.default | driverProps.serverName
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

    def 'Create datasource pipeline '() {
        setup: 'Define the parameters for Procedure running'
        def dsName = datasources.correct
        Map runParams = [
            ecp_weblogic_dataSourceName       : dsName,
            ecp_weblogic_dataSourceDriverClass: drivers.derby,
            ecp_weblogic_databaseUrl          : urls.medrec,
            ecp_weblogic_jndiName             : jndiNames.correct,
            ecp_weblogic_targets              : tg,
            ecp_weblogic_additionalOptions    : options,
            ecp_weblogic_databaseName         : 'medrec;create=true',
            ecp_weblogic_driverProperties     : driverProperties
        ]

        deleteDatasource(dsName)
        when: 'Procedure runs: '

        def result = runPipeline(projectName, procedureName, runParams, resourceName)
        println result.logs

        then: 'Wait until job run is completed: '
        assert result.outcome != 'error'
        cleanup:
        deleteDatasource(dsName)
        where:
        options                       | tg              | driverProperties
        additionalOptions.empty       | targets.empty   | driverProps.empty

    }
    /**
     *  Some additional maps for extended tests
     */
    @Shared
        confignames =[
            correct : CONFIG_NAME,
            incorrect: "Incorrect_$CONFIG_NAME",
            empty: '',
        ]
    @Shared
        dataSourceCredentials = [
            correct : 'medrec',
            incorrect: 'incorrect_dataSourceCredentials',
            empty: '',
        ]
    
    @Unroll
    def 'CreateORUpdateDataSource - Positive: #testCaseID.name #testCaseID.description'(){
        setup:      'Define the parameters for Procedure running'
        def configname
        def dataSourceName
        def dataSourceDriverClass
        def databaseUrl
        def jndiName
        def dataSourceCredentials
        def databaseName
        def driverPropertie
        def target
        def updateAction
        def additionalOption
        Map runParams = [
            configname                        : configname,
            ecp_weblogic_dataSourceName       : dataSourceName,
            ecp_weblogic_dataSourceDriverClass: dataSourceDriverClass,
            ecp_weblogic_databaseUrl          : databaseUrl,
            ecp_weblogic_jndiName             : jndiName,
            ecp_weblogic_dataSourceCredentials: dataSourceCredentials,
            ecp_weblogic_databaseName         : databaseName,
            ecp_weblogic_driverProperties     : driverPropertie,
            ecp_weblogic_targets              : target,
            ecp_weblogic_updateAction         : updateAction,
            ecp_weblogic_additionalOptions    : additionalOption,
        ]
        when:       'Procedure runs'
        then:       'Wait until job run is completed: '
        cleanup:    'Run after each test from Test Case Table'
        
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
