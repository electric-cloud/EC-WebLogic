package com.electriccloud.plugin.spec.end2end

import com.electriccloud.plugin.spec.WebLogicHelper
import spock.lang.Ignore
import spock.lang.IgnoreIf
import spock.lang.IgnoreRest
import spock.lang.Narrative
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Stepwise

@Stepwise
@Narrative("""
Datasource Use-case with URL checks
""")
@Requires({WebLogicHelper.end2end()})
@IgnoreIf({ WebLogicHelper.isWebLogic11() && WebLogicHelper.testDatasource() })
class DatasourceDemo extends WebLogicHelper {
    @Shared
    def projectName = "EC-WebLogic Datasource Demo"

    @Shared
    def artifactName = 'weblogic:ds-demo-app'

    @Shared
    def jndiName = 'com.weblogic.sampleDB'

    @Shared
    def resName = 'WL Datasource Demo Resource'

    @Shared
    def jmsModuleName = 'Datasource Demo'


    @Shared def applicationName = 'Datasource Demo App'
    @Shared def processName = 'Deploy'
    @Shared def tierMapName = 'WebLogic'
    @Shared def driverName = 'org.apache.derby.jdbc.ClientXADataSource'
    @Shared def dbName = 'sample;create=true'
    @Shared def dbUrl = "jdbc:derby://${derbyHost}:1527/sample;ServerName=${derbyHost};databaseName=sample;create=true"
    @Shared def dsName = 'SampleDataSource'
    @Shared dslParams = [
        projectName: projectName,
        config: CONFIG_NAME,
        resName: resName,
        artifactName: artifactName,
        jmsModuleName: jmsModuleName,
        wlst: getWlstPath(),
        jndi: jndiName,
        appName: demoAppName,
        appPath: demoAppPath,
        driverName: driverName,
        dbName: dbName,
        dbUrl: dbUrl,
        dsName: dsName,
        updateAction: 'remove_and_create',
        userName: 'medrec',
        password: 'medrec',
    ]

    def doSetupSpec() {
        setupResource()
        createConfig(CONFIG_NAME)
        deleteProject(projectName)
        publishArtifact(artifactName, '1.0.0', demoAppPath)
        def host = getResourceHost()
        def port = getResourcePort()

        dsl """
          resource '$resName', {
            hostName = '$host'
            port     = '$port'
          }
        """

        deleteDatasource(dsName)
        dslFile "dsl/EndToEnd/datasourceDemo.dsl", dslParams
    }


    def 'first deploy'() {
        when:
        def result = runProcess(projectName, demoAppName, 'Deploy', 'WebLogic')
        then:
        assert result.outcome == 'success'
        checkPage()
    }


    def 'second deploy'() {
        when:
        def result = runProcess(projectName, demoAppName, 'Deploy', 'WebLogic')
        then:
        assert result.outcome == 'success'
        checkPage()
    }




    def checkPage() {
//        http://10.200.1.168:7001/SampleJMSApplication/DemoServlet?ds_name=com.weblogic.SampleDB
        def host = getResourceHost()
        def resName = getResourceName()
        dsl """
        project '$projectName', {
            procedure 'Check Page', {
                step 'Check Queue & Topic', {
                    shell = 'ec-groovy'
                    command = '''
                        println "http://localhost:7001/\$[dsUrl]".toURL().text
                    '''
                    resourceName = '${resName}'
                }

                formalParameter 'dsUrl', {
                    type = 'entry'
                }
            }
        }
        """

        def url = "jms-sample/DemoServlet?ds_name=${jndiName}"

        def response = runProcedure("""
            runProcedure projectName: '$projectName',
            procedureName: 'Check Page',
            actualParameter: [
                dsUrl: '$url',
            ]
            """, resName
        )

        assert response.outcome == 'success'
        logger.info(response.logs)
        assert response.logs =~ /Connected to db/
        assert response.logs =~ /DS found/
        return true
    }
}
