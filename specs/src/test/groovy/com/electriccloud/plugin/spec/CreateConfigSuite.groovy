package com.electriccloud.plugin.spec

import spock.lang.*
import com.electriccloud.plugins.annotations.*
import com.electriccloud.plugin.spec.DataSource.CreateConfigDS

class CreateConfigSuite extends WebLogicHelper{
    static String procedureName = "CreateConfiguration"
    @Shared
    String projectName = "${pluginName} Specs ${procedureName}"
    @Shared
    String configName = "${pluginName}-Specs-${procedureName}-${CreateConfigDS.configName.correct}"
    static String resourceName = CreateConfigDS.resourceName.local
    @Shared
    def TCs = [
            C000001: [ids: 'C000001', description: 'Create single ChangeTask, only required fields filled'],
    ]
    @Shared
    List creds = [getUsername(), getPassword()]


    def doSetupSpec() {
        configName = randomize(configName)
        def params = [
                config               : "",
                weblogic_url         : "",
                wlst_path            : "",
                debug_level          : "",
                enable_named_sessions: "",
                java_home            : "",
                java_vendor          : "",
                mw_home              : "",
                test_connection_res  : "",
                test_connection      : "",
        ]

        importProject(projectName, "dsl/RunProcedure.dsl", [
                projectName   : projectName,
                procedureName : procedureName,
                stepName      : procedureName,
                subprojectName: pluginName,
                resourceName  : resourceName,
                params        : params,
        ])
    }

    static def weblogicUrl

    def wlstPath
    @Shared
    def debugLevel
    @Shared
    List credential
    @Shared
    def enableNamedSessions
    @Shared
    def testConnectionRes
    @Shared
    def testConnection


    //common parameters
    @Shared
    def expectedOutcome
    @Shared
    def expectedError

    @Unroll
    @Sanity
    def "Create configuration - only required fields filled #testCaseID.ids #testCaseID.description"() {
        given:
        println "Given Part"
        def runParams
        def result

        when:
        println "When Part"
        runParams = [
                config               : configName,
                weblogic_url         : weblogicUrl,
                wlst_path            : wlstPath,
                debug_level          : debugLevel,
                enable_named_sessions: enableNamedSessions,
                java_home            : "",
                java_vendor          : "",
                mw_home              : "",
                test_connection_res  : testConnectionRes,
                test_connection      : testConnection,
        ]
        result = runProcedure(projectName, procedureName, runParams, credential)

        then:
        println "Then Part"
        assert result.jobId
        println getJobLink(result.jobId)

        where:
        caseId      | configName            | weblogicUrl                        | wlstPath                        | debugLevel                      | credential | enableNamedSessions                          | testConnectionRes                        | testConnection
        TCs.C000003 | randomize(configName) | CreateConfigDS.weblogicUrl.correct | CreateConfigDS.wlstPath.correct | CreateConfigDS.debugLevel.debug | creds      | CreateConfigDS.enableNamedSessions.unchecked | CreateConfigDS.testConnectionRes.correct | CreateConfigDS.testConnection.checked

    }


}
