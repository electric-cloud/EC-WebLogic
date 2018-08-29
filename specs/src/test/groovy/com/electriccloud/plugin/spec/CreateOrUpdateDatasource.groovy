package com.electriccloud.plugin.spec

import spock.lang.IgnoreRest
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Unroll

@Requires({WebLogicHelper.testDatasource()})
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
        mysql      : 'MySQLSpecDataSource',
        updated    : 'SpecUpdatedDatasource',
        nonexisting: 'NoSuchDS'
    ]

    @Shared
    def jndiNames = [
        empty  : '',
        mysql  : "JNDI.Name.${datasources.mysql}",
        correct: 'datasources.TestJNDIName',
    ]

    @Shared
    def drivers = [
        mysql    : 'com.mysql.jdbc.Driver',
        derby    : 'org.apache.derby.jdbc.ClientXADataSource',
        incorrect: 'com.incorrect.jdbc.driver',
        emty     : '',
    ]

    @Shared
    def urls = [
        mysql    : "jdbc:mysql://${mysqlHost}:3306/customers_db",
        medrec   : "jdbc:derby://${derbyHost}:1527/medrec;ServerName=${derbyHost};databaseName=medrec;create=true",
        incorrect: "incorrect URL",
        empty    : "",
    ]

    @Shared
    def targets = [
        empty    : '',
        default  : 'AdminServer',
        correct  : 'AdminServer',
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
        double     : "JDBCResource.JDBCConnectionPoolParams.InitialCapacity=2\nResource.JDBCConnectionPoolParams.MaxCapacity=20",
        incorrect  : "incorrect additional Options",
    ]

    @Shared
    def dbNames = [
        empty : '',
        medrec: 'medrec',
        mysql : 'customers_db'
    ]

    @Shared
    def driverProps = [
        empty     : '',
        mysql     : 'user=root',
        serverName: "serverName=${derbyHost}",
        incorrect : "incorrect driver prop"
    ]

    /**
     * Test Parameters: for Where section
     */
    @Shared
    def caseIds = [
        //positive and warnings
        C000001: [name: 'C000001', ids: 'C000001', description: 'Warning no Target'],
        C000002: [name: 'C000002', ids: 'C000002', description: 'Warning no Target'],
        C000003: [name: 'C000003', ids: 'C000003', description: 'Warning no Target'],
        C000004: [name: 'C000004', ids: 'C000004', description: 'Success Create MYSQL DataSource'],
        C000005: [name: 'C000005', ids: 'C000005', description: 'Warning no Target'],
        C000006: [name: 'C000006', ids: 'C000006', description: 'Warning no Target'],
        C000007: [name: 'C000007', ids: 'C000007', description: 'Success Create MYSQL DataSource'],
        C000008: [name: 'C000008', ids: 'C000008', description: 'Success Create MYSQL DataSource'],
        C000009: [name: 'C000009', ids: 'C000009', description: 'Success Create MYSQL DataSource'],
        C000010: [name: 'C000010', ids: 'C000010', description: 'Success Create MYSQL DataSource'],
        C000011: [name: 'C000011', ids: 'C000011', description: 'Success Create MYSQL DataSource'],
        C000012: [name: 'C000012', ids: 'C000012', description: 'Success Create MYSQL DataSource'],
        C000013: [name: 'C000013', ids: 'C000013', description: 'Success remove and Update'],
        C000014: [name: 'C000014', ids: 'C000014', description: 'Success remove and Update'],
        C000015: [name: 'C000015', ids: 'C000015', description: 'Success remove and Update'],
        C000016: [name: 'C000016', ids: 'C000016', description: 'Success remove and Update'],
        //negative
        C000017: [name: 'C000017', ids: 'C000017', description: 'Error config '],
        C000018: [name: 'C000018', ids: 'C000018', description: 'Error Driver '],
        C000019: [name: 'C000019', ids: 'C000019', description: 'Error URL '],
        C000020: [name: 'C000020', ids: 'C000020', description: 'Error'],
        C000021: [name: 'C000021', ids: 'C000021', description: 'Error'],
        C000022: [name: 'C000022', ids: 'C000022', description: 'Error'],
        C000023: [name: 'C000023', ids: 'C000023', description: 'Error'],
        C000024: [name: 'C000024', ids: 'C000024', description: 'Error'],
        C000025: [name: 'C000025', ids: 'C000025', description: 'Error'],

    ]

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
credential(userName: 'root', password: 'root', credentialName: 'mysql', projectName: '$projectName')
attachCredential projectName: '$projectName',
    credentialName: 'mysql',
    procedureName: '$procedureName',
    stepName: 'RunProcedure'

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
        discardChanges()
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

        checkServerRestartOutputParameter(result.jobId)

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
        options                 | tg            | driverProperties
        additionalOptions.empty | targets.empty | driverProps.empty

    }
    /**
     *  Some additional maps for extended tests
     */
    @Shared
        confignames = [
            correct  : CONFIG_NAME,
            incorrect: "Incorrect_$CONFIG_NAME",
            empty    : '',
        ]
    @Shared
        dataSourceCredentials = [
            correct  : 'medrec',
            medrec   : 'medrec',
            mysql    : 'mysql',
            incorrect: 'incorrect_dataSourceCredentials',
            empty    : '',
        ]
    @Shared
        databaseNames = [
            merdec   : 'medrec;create=true',
            mysql    : 'customers_db',
            correct  : 'medrec;create=true',
            incorrect: 'incorrect_databaseName',
            empty    : '',
        ]
    @Shared
        updateActions = [
            doNothing      : 'do_nothing',
            removeAndCreate: 'remove_and_create',
            selectiveUpdate: 'selective_update',
            incorrect      : "incorrect Action",
        ]
    @Shared
        expectedSummaryMessages = [
            warningWithoutTargets           : "No targets are provided, the datasource will not be deployed",
            correctCreate                   : "Created datasource replaceName successfully",
            Message                         : 'Datasource replaceName exists, no further action is required',
            receated                        : "Recreated datasource replaceName",
            errorConfigurationDoesntExist   : "Configuration ${confignames.incorrect} doesn't exist",
            errorLoadDriver                 : "Cannot load driver class ${drivers.incorrect} for datasource 'replaceName'",
            errorMySQLURL                   : "The driver ${drivers.mysql} does not accept URL ${urls.incorrect}",
            errorCommon                     : "Completed with Errors",
        ]

    def configname
    def caseId
    def dataSourceName
    def dataSourceDriverClass
    def databaseUrl
    def jndiName
    def dataSourceCredential
    def databaseName
    def driverProperty
    def target
    def updateAction
    def additionalOption

    @Unroll
    @IgnoreRest
    def 'CreateORUpdateDataSource - #caseId.ids #caseId.description, DS Name: #dataSourceName'() {
        setup: 'Define the parameters for Procedure running'
        Map runParams = [
            configname                        : configname,
            ecp_weblogic_dataSourceName       : dataSourceName,
            ecp_weblogic_dataSourceDriverClass: dataSourceDriverClass,
            ecp_weblogic_databaseUrl          : databaseUrl,
            ecp_weblogic_jndiName             : jndiName,
            ecp_weblogic_dataSourceCredentials: dataSourceCredential,
            ecp_weblogic_databaseName         : databaseName,
            ecp_weblogic_driverProperties     : driverProperty,
            ecp_weblogic_targets              : target,
            ecp_weblogic_updateAction         : updateAction,
            ecp_weblogic_additionalOptions    : additionalOption,
        ]
        when: 'Procedure runs'
        def result = runProcedure(projectName, procedureName, runParams, [], getResourceName())

        then: 'Wait until job run is completed'
        def upperStepSummary = getJobUpperStepSummary(result.jobId)
//        def actualOutcome = result.outcome
//        def logs = result.logs
        logger.info(upperStepSummary)
        expect: 'Verification'
        assert result.outcome == expectedOutcome
        assert upperStepSummary =~ expectedSummaryMessage.replace('replaceName', dataSourceName)

        cleanup: 'Clean the Procedure'
        // NO Need to change the Methid above
        if (expectedOutcome == 'error') {
            discardChanges()
        }
        where: 'Table Run'
        caseId          | configname            | dataSourceName                           | dataSourceDriverClass | databaseUrl    | jndiName                                 | dataSourceCredential            /*Not Req*/ | databaseName            | driverProperty        | target          | updateAction                  | additionalOption              | expectedOutcome          | expectedSummaryMessage

        //MySQL
//
//        //Just Create with the unique params, JUST one test with target,
//        caseIds.C000001 | confignames.correct   | datasources.mysql + caseIds.C000001.name | drivers.mysql         | urls.mysql     | jndiNames.correct                        | dataSourceCredentials.mysql     /*Not Req*/ | databaseNames.empty     | driverProps.empty     | targets.empty   | updateActions.doNothing       | additionalOptions.empty       | expectedOutcomes.warning | expectedSummaryMessages.warningWithoutTargets
//        caseIds.C000002 | confignames.correct   | datasources.mysql + caseIds.C000002.name | drivers.mysql         | urls.mysql     | jndiNames.correct                        | dataSourceCredentials.mysql     /*Not Req*/ | databaseNames.mysql     | driverProps.empty     | targets.empty   | updateActions.doNothing       | additionalOptions.empty       | expectedOutcomes.warning | expectedSummaryMessages.warningWithoutTargets
//        caseIds.C000003 | confignames.correct   | datasources.mysql + caseIds.C000003.name | drivers.mysql         | urls.mysql     | jndiNames.correct                        | dataSourceCredentials.mysql     /*Not Req*/ | databaseNames.empty     | driverProps.mysql     | targets.empty   | updateActions.doNothing       | additionalOptions.empty       | expectedOutcomes.warning | expectedSummaryMessages.warningWithoutTargets
//        caseIds.C000004 | confignames.correct   | datasources.mysql + caseIds.C000004.name | drivers.mysql         | urls.mysql     | jndiNames.correct                        | dataSourceCredentials.mysql     /*Not Req*/ | databaseNames.empty     | driverProps.empty     | targets.correct | updateActions.doNothing       | additionalOptions.empty       | expectedOutcomes.success | expectedSummaryMessages.correctCreate
//        caseIds.C000005 | confignames.correct   | datasources.mysql + caseIds.C000005.name | drivers.mysql         | urls.mysql     | jndiNames.correct                        | dataSourceCredentials.mysql     /*Not Req*/ | databaseNames.empty     | driverProps.empty     | targets.empty   | updateActions.removeAndCreate | additionalOptions.empty       | expectedOutcomes.warning | expectedSummaryMessages.warningWithoutTargets
//        caseIds.C000006 | confignames.correct   | datasources.mysql + caseIds.C000006.name | drivers.mysql         | urls.mysql     | jndiNames.correct                        | dataSourceCredentials.mysql     /*Not Req*/ | databaseNames.empty     | driverProps.empty     | targets.empty   | updateActions.doNothing       | additionalOptions.maxCapacity | expectedOutcomes.warning | expectedSummaryMessages.warningWithoutTargets
//        //the tests with targets and diff other params
//        caseIds.C000007 | confignames.correct   | datasources.mysql + caseIds.C000007.name | drivers.mysql         | urls.mysql     | jndiNames.correct + caseIds.C000007.name | dataSourceCredentials.mysql     /*Not Req*/ | databaseNames.mysql     | driverProps.empty     | targets.correct | updateActions.doNothing       | additionalOptions.empty       | expectedOutcomes.success | expectedSummaryMessages.correctCreate
//        caseIds.C000008 | confignames.correct   | datasources.mysql + caseIds.C000008.name | drivers.mysql         | urls.mysql     | jndiNames.correct + caseIds.C000008.name | dataSourceCredentials.mysql     /*Not Req*/ | databaseNames.empty     | driverProps.mysql     | targets.correct | updateActions.doNothing       | additionalOptions.empty       | expectedOutcomes.success | expectedSummaryMessages.correctCreate
//        caseIds.C000009 | confignames.correct   | datasources.mysql + caseIds.C000009.name | drivers.mysql         | urls.mysql     | jndiNames.correct + caseIds.C000009.name | dataSourceCredentials.mysql     /*Not Req*/ | databaseNames.empty     | driverProps.empty     | targets.correct | updateActions.doNothing       | additionalOptions.double      | expectedOutcomes.success | expectedSummaryMessages.correctCreate
//        caseIds.C000010 | confignames.correct   | datasources.mysql + caseIds.C000010.name | drivers.mysql         | urls.mysql     | jndiNames.correct + caseIds.C000010.name | dataSourceCredentials.mysql     /*Not Req*/ | databaseNames.empty     | driverProps.empty     | targets.correct | updateActions.doNothing       | additionalOptions.maxCapacity | expectedOutcomes.success | expectedSummaryMessages.correctCreate
//        //the tests with increase params
//        caseIds.C000011 | confignames.correct   | datasources.mysql + caseIds.C000011.name | drivers.mysql         | urls.mysql     | jndiNames.correct + caseIds.C000011.name | dataSourceCredentials.mysql     /*Not Req*/ | databaseNames.mysql     | driverProps.mysql     | targets.correct | updateActions.doNothing       | additionalOptions.empty       | expectedOutcomes.success | expectedSummaryMessages.correctCreate
//        caseIds.C000012 | confignames.correct   | datasources.mysql + caseIds.C000012.name | drivers.mysql         | urls.mysql     | jndiNames.correct + caseIds.C000012.name | dataSourceCredentials.mysql     /*Not Req*/ | databaseNames.mysql     | driverProps.mysql     | targets.correct | updateActions.doNothing       | additionalOptions.double      | expectedOutcomes.success | expectedSummaryMessages.correctCreate
//        //additional tests for removeAndCreate in the Update Action (use the C000004.name)
//        caseIds.C000013 | confignames.correct   | datasources.mysql + caseIds.C000004.name | drivers.mysql         | urls.mysql     | jndiNames.correct                        | dataSourceCredentials.mysql     /*Not Req*/ | databaseNames.empty     | driverProps.empty     | targets.correct | updateActions.removeAndCreate | additionalOptions.empty       | expectedOutcomes.success | expectedSummaryMessages.receated
//        caseIds.C000014 | confignames.correct   | datasources.mysql + caseIds.C000004.name | drivers.mysql         | urls.mysql     | jndiNames.correct                        | dataSourceCredentials.mysql     /*Not Req*/ | databaseNames.mysql     | driverProps.empty     | targets.correct | updateActions.removeAndCreate | additionalOptions.empty       | expectedOutcomes.success | expectedSummaryMessages.receated
//        caseIds.C000015 | confignames.correct   | datasources.mysql + caseIds.C000004.name | drivers.mysql         | urls.mysql     | jndiNames.correct                        | dataSourceCredentials.mysql     /*Not Req*/ | databaseNames.empty     | driverProps.mysql     | targets.correct | updateActions.removeAndCreate | additionalOptions.empty       | expectedOutcomes.success | expectedSummaryMessages.receated
//        caseIds.C000016 | confignames.correct   | datasources.mysql + caseIds.C000004.name | drivers.mysql         | urls.mysql     | jndiNames.correct                        | dataSourceCredentials.mysql     /*Not Req*/ | databaseNames.empty     | driverProps.empty     | targets.correct | updateActions.removeAndCreate | additionalOptions.maxCapacity | expectedOutcomes.success | expectedSummaryMessages.receated
//        //the test with negative results

        caseIds.C000017 | confignames.incorrect | datasources.mysql + caseIds.C000017.name | drivers.mysql         | urls.mysql     | jndiNames.correct + caseIds.C000017.name | dataSourceCredentials.mysql     /*Not Req*/ | databaseNames.empty     | driverProps.empty     | targets.empty   | updateActions.doNothing       | additionalOptions.empty       | expectedOutcomes.error   | expectedSummaryMessages.errorConfigurationDoesntExist
        caseIds.C000018 | confignames.correct   | datasources.mysql + caseIds.C000018.name | drivers.incorrect     | urls.mysql     | jndiNames.correct + caseIds.C000018.name | dataSourceCredentials.mysql     /*Not Req*/ | databaseNames.empty     | driverProps.empty     | targets.correct | updateActions.doNothing       | additionalOptions.empty       | expectedOutcomes.error   | expectedSummaryMessages.errorLoadDriver
        //the same JNDI name is incorrect C000001 - because duplicate
        caseIds.C000019 | confignames.correct   | datasources.mysql + caseIds.C000019.name | drivers.mysql         | urls.incorrect | jndiNames.correct + caseIds.C000001.name | dataSourceCredentials.mysql     /*Not Req*/ | databaseNames.empty     | driverProps.empty     | targets.correct | updateActions.doNothing       | additionalOptions.empty       | expectedOutcomes.error   | expectedSummaryMessages.errorMySQLURL
        //other negative
//        caseIds.C000020 | confignames.correct   | datasources.mysql + caseIds.C000020.name | drivers.mysql         | urls.mysql     | jndiNames.correct + caseIds.C000020.name | dataSourceCredentials.incorrect /*Not Req*/ | databaseNames.empty     | driverProps.empty     | targets.correct | updateActions.doNothing       | additionalOptions.empty       | expectedOutcomes.error   | expectedSummaryMessages.warningWithoutTargets
//        caseIds.C000021 | confignames.correct   | datasources.mysql + caseIds.C000021.name | drivers.mysql         | urls.mysql     | jndiNames.correct + caseIds.C000021.name | dataSourceCredentials.mysql     /*Not Req*/ | databaseNames.incorrect | driverProps.empty     | targets.correct | updateActions.doNothing       | additionalOptions.empty       | expectedOutcomes.error   | expectedSummaryMessages.warningWithoutTargets
//        caseIds.C000022 | confignames.correct   | datasources.mysql + caseIds.C000022.name | drivers.mysql         | urls.mysql     | jndiNames.correct + caseIds.C000022.name | dataSourceCredentials.mysql     /*Not Req*/ | databaseNames.empty     | driverProps.incorrect | targets.correct | updateActions.doNothing       | additionalOptions.empty       | expectedOutcomes.error   | expectedSummaryMessages.warningWithoutTargets
//        caseIds.C000023 | confignames.correct   | datasources.mysql + caseIds.C000023.name | drivers.mysql         | urls.mysql     | jndiNames.correct + caseIds.C000023.name | dataSourceCredentials.mysql     /*Not Req*/ | databaseNames.empty     | driverProps.empty     | targets.correct | updateActions.incorrect       | additionalOptions.empty       | expectedOutcomes.error   | expectedSummaryMessages.warningWithoutTargets
//        caseIds.C000024 | confignames.correct   | datasources.mysql + caseIds.C000024.name | drivers.mysql         | urls.mysql     | jndiNames.correct + caseIds.C000024.name | dataSourceCredentials.mysql     /*Not Req*/ | databaseNames.empty     | driverProps.empty     | targets.correct | updateActions.doNothing       | additionalOptions.incorrect   | expectedOutcomes.error   | expectedSummaryMessages.warningWithoutTargets
        //the tests with with targets, but with selective updates
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
