package com.electriccloud.plugin.spec

import spock.lang.Shared

class DeleteDatasource extends WebLogicHelper {

    /**
     * Dsl Parameters
     */

    @Shared
    String procedureName = 'DeleteDatasource'
    @Shared
    String projectName = "EC-WebLogic ${procedureName}"


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
                configname : CONFIG_NAME,
                dsname     : '',
                wlstabspath: ''
            ]
        ]

    }

    def 'delete datasource'() {
        setup:
        def dsName = 'some_ds'
        createDatasource(dsName)
        def runParams = [
            dsname: dsName
        ]
        when:
        def result = runProcedure(projectName, procedureName, runParams, [], getResourceName())
        then:
        assert result.outcome == 'success'
        assert getJobUpperStepSummary(result.jobId) == 'Datasource ' + dsName + ' has been deleted'
    }


    def 'delete non-existsing datasource'() {
        when:
        def result = runProcedure(projectName, procedureName, [dsname: 'no_such_ds'], [], getResourceName() )
        then:
        assert result.outcome == 'warning'
        assert getJobUpperStepSummary(result.jobId) == 'Datasource no_such_ds does not exist'
    }


    def createDatasource(dsName) {
        def code = """
dsName = '$dsName'
connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')
edit()
startEdit()

cd('/JDBCSystemResources')

if getMBean('/JDBCSystemResources/' + dsName) != None:
    print "DS already exists"
    sys.exit(0)

bean = cmo.createJDBCSystemResource(dsName)
bean.getJDBCResource().setName(dsName)
resource = bean.getJDBCResource()
driver = resource.getJDBCDriverParams()
driverprops = driver.getProperties()
params = resource.getJDBCDataSourceParams()

dbName = driverprops.createProperty('databaseName')
dbName.setValue('testdb;create=true')
driver.setUrl('jdbc:derby://${derbyHost}:1527/testdb;ServerName=${derbyHost};databaseName=testdb;create=true')

save()
activate()
"""
        def result = runWLST(code, "CreateDatasource_${dsName}")
        assert result.outcome == 'success'
    }

}
