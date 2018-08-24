package com.electriccloud.plugin.spec

import spock.lang.Shared
import spock.lang.Unroll

class DeleteDatasource extends WebLogicHelper {

    // expected results
    def expectedOutcome
    def expectedSummaryMessage
    def expectedJobDetailedResult


    /**
     * Dsl Parameters
     */

    @Shared
    def procedureNames = [
            createDataSource: 'CreateOrUpdateDatasource',
            deleteDataSource: 'DeleteDatasource'
    ]
    @Shared
    String projectName = "EC-WebLogic ${procedureNames.deleteDataSource}"



    /**
     * Parameters for Test Setup
     */

    /**
     * Procedure Values: test parameters Procedure values
     */

    @Shared
    def datasources = [
            derby      : 'DerbyDataSource',
            mysql      : 'MySQLDataSource',
    ]

    @Shared
    def jndiNames = [
            mysql  : "JNDI.Name.${datasources.mysql}",
            derby: 'TestJDNI',
            correct: 'datasources.TestJNDIName',
    ]

    @Shared
    def drivers = [
            mysql    : 'com.mysql.jdbc.Driver',
            derby    : 'org.apache.derby.jdbc.ClientXADataSource',
    ]

    @Shared
    def urls = [
            mysql    : "jdbc:mysql://${mysqlHost}:3306/customers_db",
            derby    : "jdbc:derby://${derbyHost}:1527/test;ServerName=${derbyHost};databaseName=test;create=true",
            medrec   : "jdbc:derby://${derbyHost}:1527/medrec;ServerName=${derbyHost};databaseName=medrec;create=true",
            incorrect: "incorrect URL",
    ]

    @Shared
    def targets = [
            default  : 'AdminServer',
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
            mysql     : 'user=root',
            derby: "user=root\nportNumber=1527\ndatabaseName=test;create=true\nserverName=${derbyHost}",
            serverName: "serverName=${derbyHost}",
            incorrect : "incorrect driver prop"
    ]


    /**
     *  Some additional maps for extended tests
     */


    @Shared
            dataSourceCredentials = [
                    derby  : 'derby',
                    mysql    : 'mysql',
            ]
    @Shared
            databaseNames = [
                    derby   : 'test',
                    mysql    : 'customers_db',
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
                    warningWithoutTargets   : "No targets are provided, the datasource will not be deployed",
                    correctCreate           : "Created datasource replaceName successfully",
                    Message                 : 'Datasource replaceName exists, no further action is required',
                    receated                : "Recreated datasource replaceName",
                    configurationDoesntExist: "Configuration replaceName doesn't exist",
            ]


    /**
     * Test Parameters: for Where section
     */
    @Shared
    def caseIds = [
            // Positive tests
            C364058: [ids: 'C364058', description: 'Delete Mysql DataSource'],
            C364059: [ids: 'C364059', description: 'Delete Derby DataSource'],
            C364066: [ids: 'C364066', description: 'Delete DataSource without WLST Absolute Path'],
            C364060: [ids: 'C364060', description: 'Delete DataSource that is not exist'],
            // Negative tests
            C364061: [ids: 'C364061', description: 'Unable to Delete DataSource without configName'],
            C364062: [ids: 'C364062', description: 'Unable to Delete DataSource with invalid configName'],
            C364064: [ids: 'C364064', description: 'Unable to Delete DataSource without Data Source Name'],
            C364065: [ids: 'C364065', description: 'Unable to Delete Data source with invalid WLST Absolute Path']

    ]







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
                ecp_weblogic_dataSourceCredentials: dataSourceCredentials.mysql,
                ecp_weblogic_databaseName         : '',
                ecp_weblogic_targets              : '',
                ecp_weblogic_updateAction         : '',
                ecp_weblogic_additionalOptions    : '',
                ecp_weblogic_driverProperties     : ''
        ]
        dslFile "dsl/procedures.dsl", [
                projectName  : projectName,
                resourceName : getResourceName(),
                procedureName: procedureNames.createDataSource,
                params       : params
        ]


        dslFile "dsl/procedures.dsl", [
            projectName  : projectName,
            resourceName : getResourceName(),
            procedureName: procedureNames.deleteDataSource,
            params       : [
                configname : CONFIG_NAME,
                dsname     : '',
                wlstabspath: ''
            ]
        ]

        //Create Mysql and Derby Credentials
        dsl """
            credential(userName: 'root', password: 'root', credentialName: 'mysql', projectName: '$projectName')
            credential(userName: 'root', password: 'root', credentialName: 'derby', projectName: '$projectName')
            attachCredential projectName: '$projectName',
                credentialName: 'mysql',
                procedureName: '$procedureNames.createDataSource',
                stepName: 'RunProcedure'
            attachCredential projectName: '$projectName',
                credentialName: 'derby',
                procedureName: '$procedureNames.createDataSource',
                stepName: 'RunProcedure'
            """
    }

    def doCleanupSpec() {
    }




    @Unroll
    def "Delete DataSource #tcId - #tcDescription"(){

        setup: "Create DataSource" (CONFIG_NAME,
                dataSource, driver, url, jdniName, dataSourceCreds, dataBaseName, driverProperty, targets.default, updateAction, additionalOptions.maxCapacity)

        when: 'Delete Data source #dataSource '
        def result = 'Delete DataSource'(CONFIG_NAME, dataSource, wlsPath, getResourceName())
        def jobSummary = getJobUpperStepSummary(result.jobId)
        def jobOutcome = result.outcome

        then:
        assert jobOutcome == 'success'
        assert jobSummary == "Datasource $dataSource has been deleted"

        where:
        // Test-Case details
        tcId             << [caseIds.C364058.ids, caseIds.C364059.ids, caseIds.C364066.ids]
        tcDescription    << [caseIds.C364058.description, caseIds.C364059.description, caseIds.C364066.description]
        // Test Data
        dataSource       << [datasources.mysql, datasources.derby, datasources.mysql]
        driver           << [drivers.mysql, drivers.derby, drivers.mysql]
        url              << [urls.mysql, urls.derby, urls.mysql]
        jdniName         << [jndiNames.mysql, jndiNames.derby, jndiNames.mysql]
        dataSourceCreds  << [dataSourceCredentials.mysql, dataSourceCredentials.derby, dataSourceCredentials.mysql]
        dataBaseName     << [databaseNames.mysql, databaseNames.derby, databaseNames.mysql]
        driverProperty   << [driverProps.mysql, driverProps.derby, driverProps.mysql]
        wlsPath          << [getWlstPath(), getWlstPath(), '']
        updateAction     << [updateActions.removeAndCreate, updateActions.removeAndCreate, updateActions.removeAndCreate]

    }

    @Unroll
    def "Delete DataSource #testCaseId - #testCaseDescription"(){

        when: 'Delete Data source #dataSource '
        def result = 'Delete DataSource'(CONFIG_NAME, datasources.mysql, getWlstPath(), getResourceName())
        def jobSummary = getJobUpperStepSummary(result.jobId)
        def jobOutcome = result.outcome

        then:
        assert jobOutcome == 'warning'
        assert jobSummary == "Datasource $datasources.mysql does not exist"

        where:
        testCaseId          << [caseIds.C364060.ids]
        testCaseDescription << [caseIds.C364060.description]
    }


    @Unroll
    def "Unable to Delete DataSource #testCaseId - #tcDescription"(){
        setup: "Create DataSource" (CONFIG_NAME,
                datasources.mysql,
                drivers.mysql,
                urls.mysql,
                jndiNames.mysql,
                dataSourceCredentials.mysql,
                databaseNames.mysql,
                driverProps.mysql,
                targets.default,
                updateActions.removeAndCreate, additionalOptions.maxCapacity)

        when: 'Delete Data source #dataSource '
        //def result
        def result = 'Delete DataSource'(configName, dataSource, wlsPath, resource)
        def jobSummary = getJobUpperStepSummary(result.jobId)

        then:
        assert result.outcome == jobOutcome
        assert jobSummary == errorMessage

        where:
        // Test-Case details
        testCaseId    << [caseIds.C364061.ids, caseIds.C364062.ids, caseIds.C364064.ids, caseIds.C364065.ids]
        tcDescription << [caseIds.C364061.description, caseIds.C364062.description, caseIds.C364064.description, caseIds.C364065.description]
        // Test Data
        resource      << [getResourceName(), getResourceName(), getResourceName(), getResourceName()]
        configName    << ['', 'someTestConfig', CONFIG_NAME, CONFIG_NAME]
        dataSource    << [datasources.mysql, datasources.mysql, '', datasources.mysql]
        wlsPath       << [getWlstPath(), getWlstPath(), getWlstPath(), '/u01/test/test.sh']
        jobOutcome    << ['error', 'error', 'error', 'error']
        errorMessage  << ["Configuration configname doesn't exist.\n", "Configuration someTestConfig doesn't exist.\n", "The required value is missing\n\n", "File /u01/test/test.sh doesn't exist\n"]

    }






    def 'Create DataSource'(configname, dataSourceName, dataSourceDriverClass, databaseUrl, jndiName, dataSourceCredential, databaseName, driverProperty, target, updateAction, additionalOption){
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
        runProcedure(projectName, procedureNames.createDataSource, runParams, [], getResourceName())
    }

    def 'Delete DataSource'(configName, dataSourceName, wlsPath, resoureName){
        def runParams = [
                configname: configName,
                dsname: dataSourceName,
                wlstabspath: wlsPath
        ]
        runProcedure(projectName, procedureNames.deleteDataSource, runParams, [], resoureName)
    }




}
