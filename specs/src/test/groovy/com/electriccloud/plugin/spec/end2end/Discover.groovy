package com.electriccloud.plugin.spec.end2end

import com.electriccloud.plugin.spec.WebLogicHelper
import groovy.json.JsonSlurper
import spock.lang.*


//Does not work on weblogic 11 due to test application
@Stepwise
@IgnoreIf({ WebLogicHelper.isWebLogic11() })
@Requires({WebLogicHelper.end2end()})
class Discover extends WebLogicHelper {
    @Shared
    def projectName = "EC-WebLogic Discovery"

    @Shared
    def procedureName = 'Discover'

    @Shared
    def discoveredProject = 'WLDiscovered'

    @Shared
    def discoveredEnvironment = 'WLDiscoveredEnv'

    @Shared
    def discoveredApp = 'WLDiscoveredApp'

    def doSetupSpec() {
        deleteProject(discoveredProject)
        setupResource()
        createConfig(WebLogicHelper.CONFIG_NAME)
        deleteProject(projectName)
        String artifactName = 'weblogic:for_discovery'
        publishArtifact(artifactName, '1.0.0', 'jms-sample.war')
        dslFile "dsl/complex/discovery.dsl", [
            config      : WebLogicHelper.CONFIG_NAME,
            artifactName: artifactName,
            projectName : projectName,
            wlst        : wlstPath,
            resourceName: resourceName,
            appName     : demoAppName,
            derbyHost   : derbyHost
        ]
        def result = runProcedure(
            projectName, 'PrepareDeploy', [:], [],
            resourceName, 210)
        assert result.outcome == 'success'
        dsl """
project '$projectName', {
    credential 'wl', {
        userName = '${username}'
        password = '${password}'
    }
}
"""
        dslFile 'dsl/procedures.dsl', [
            projectName  : projectName,
            resourceName : resourceName,
            procedureName: procedureName,
            params       : [
                ecp_weblogic_resourceName      : '',
                ecp_weblogic_hostname          : '',
                ecp_weblogic_resPort           : '',
                ecp_weblogic_oracleHome        : '',
                ecp_weblogic_wlstPath          : '',
                ecp_weblogic_connectionHostname: '',
                ecp_weblogic_connectionProtocol: '',
                ecp_weblogic_credential        : 'wl',
                ecp_weblogic_envProjectName    : '',
                ecp_weblogic_envName           : '',
                ecp_weblogic_appProjName       : '',
                ecp_weblogic_appName           : '',
                ecp_weblogic_objectNames       : ''
            ]
        ]

        dsl """
attachCredential credentialName: 'wl', 
    procedureName: '$procedureName',
    projectName: '$projectName', 
    stepName: 'RunProcedure'
"""
    }

    def 'run with resource only'() {
        given:
        def discoveredResourceName = 'discovered wl'
        dsl "deleteResource resourceName: '$discoveredResourceName'"
        when:
        def result = runProcedure(
            projectName,
            procedureName,
            [
                ecp_weblogic_resourceName      : discoveredResourceName,
                ecp_weblogic_hostname          : resourceHost,
                ecp_weblogic_resPort           : resourcePort,
                ecp_weblogic_oracleHome        : oracleHome,
                ecp_weblogic_connectionProtocol: 't3'
            ]
            , [], discoveredResourceName)
        then:
        assert result.outcome == 'success'
        logger.info(result.logs)
        checkDiscoveredResources(result.jobId)
    }

    def 'run with environment name'() {
        given:
        deleteProject(discoveredProject)
        def discoveredResourceName = 'discovered wl'
        dsl "deleteResource resourceName: '$discoveredResourceName'"
        when:
        def result = runProcedure(
            projectName,
            procedureName,
            [
                ecp_weblogic_resourceName      : discoveredResourceName,
                ecp_weblogic_hostname          : resourceHost,
                ecp_weblogic_resPort           : resourcePort,
                ecp_weblogic_oracleHome        : oracleHome,
                ecp_weblogic_connectionProtocol: 't3',
                ecp_weblogic_envProjectName    : discoveredProject,
                ecp_weblogic_envName           : discoveredEnvironment
            ]
            , [], discoveredResourceName)
        then:
        assert result.outcome == 'success'
        logger.info(result.logs)
        checkDiscoveredResources(result.jobId)
        assert dsl("getEnvironment projectName: '$discoveredProject', environmentName: '$discoveredEnvironment'")
    }

    def 'run with object names'() {
        given:
        deleteProject(discoveredProject)
        dsl """
deleteArtifact artifactName: 'weblogic.discovered:jms-demo-app-deployed'
"""
        def discoveredResourceName = 'discovered wl'
        dsl "deleteResource resourceName: '$discoveredResourceName'"
        when:
        def result = runProcedure(
            projectName,
            procedureName,
            [
                ecp_weblogic_resourceName      : discoveredResourceName,
                ecp_weblogic_hostname          : resourceHost,
                ecp_weblogic_resPort           : resourcePort,
                ecp_weblogic_oracleHome        : oracleHome,
                ecp_weblogic_connectionProtocol: 't3',
                ecp_weblogic_envProjectName    : discoveredProject,
                ecp_weblogic_envName           : discoveredEnvironment,
                ecp_weblogic_appProjName       : discoveredProject,
                ecp_weblogic_appName           : discoveredApp,
                ecp_weblogic_objectNames       : """Queue:sample-module:sample-queue
Datasource:sample-ds
AppDeployment:jms-demo-app-deployed
"""
            ]
            , [], discoveredResourceName)
        then:
        assert result.outcome != 'error'
        logger.info(result.logs)
        checkDiscoveredResources(result.jobId)
        def application = dsl "getApplication projectName: '$discoveredProject', applicationName: '$discoveredApp'"
        assert application
        def processSteps = dsl "getProcessSteps projectName: '$discoveredProject', applicationName: '$discoveredApp', processName: 'Deploy'"
        debug(processSteps)
        println processSteps
        assert processSteps.processStep?.find { it.processStepName == 'Create JMS Module sample-module' }
        assert processSteps.processStep?.find { it.processStepName == 'Create JMS Queue sample-queue' }
        assert processSteps.processStep?.find { it.processStepName == 'Create Datasouce sample-ds' }
    }

    def 'deploy discovered project'() {
        setup:
        dsl """
project '$discoveredProject', {
    credential 'Datasource sample-ds', {
        userName = 'weblogic'
        password = 'weblogic'
    }
}
"""
        when:
        def result = runProcedure("""
runProcess(projectName: '$discoveredProject', applicationName: '$discoveredApp', processName: 'Deploy', environmentName: '$discoveredEnvironment')
""", resourceName, 150)
        then:
        assert result.outcome == 'success'
    }

    def 'oracle home is not provided'() {
        when:
        def discoveredResourceName = 'discovered wl'
        def result = runProcedure(
            projectName,
            procedureName,
            [
                ecp_weblogic_resourceName: discoveredResourceName,
                ecp_weblogic_hostname    : resourceHost,
                ecp_weblogic_resPort     : resourcePort
            ],
            [],
            discoveredResourceName)
        then:
        assert result.outcome == 'error'
    }

    def checkDiscoveredResources(jobId) {
        def discoveredResourcesJson = getJobProperty('/myJob/discoveredResources', jobId)
        def data = new JsonSlurper().parseText(discoveredResourcesJson)
        assert data.Server
        assert data.SubDeployment
        assert data.Datasource
        return true
    }


    def getOracleHome() {
        if (isWindows() ) {
            return 'C:/Oracle/Middleware/Oracle_Home'
        }
        else {
            return '/u01/oracle'
        }
    }

}
