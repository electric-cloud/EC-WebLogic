import spock.lang.*

class RunWLSTSuite extends WebLogicHelper {

    /**
     * Dsl Parameters
     */

    @Shared
    def procedureName = 'RunWLST'
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
    def wlstCode = [
        empty  : ''
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

    /**
     * Test Parameters: for Where section
     */

    // Procedure params
    @Shared
    def wlstFilePath
    @Shared
    def additionalEnvs
    @Shared
    def additionalCommands
    @Shared
    def scriptFileSource
    @Shared
    def scriptFilePath
    @Shared
    def scriptFile
    @Shared
    def webJarPath

    // expected results
    def expectedOutcome
    def expectedSummaryMessage
    def expectedJobDetailedResult

    /**
     * Preparation actions
     */

    def doSetupSpec() {
        setupResource()
        deleteProject(projectName)

        createConfig(CONFIG_NAME)
        discardChanges()

        dslFile "dsl/procedures.dsl", [
            projectName  : projectName,
            resourceName : getResourceName(),
            procedureName: procedureName,
            params       : [
//                configname        : CONFIG_NAME,
                wlstabspath       : getWlstPath(),
                additional_envs   : '',
                additionalcommands: '',
                scriptfilesource  : '',
                scriptfilepath    : '',
                scriptfile        : '',
                webjarpath        : '',
            ]
        ]

    }

    /**
     * Clean Up actions after test will finished
     */

    def doCleanupSpec() {
//        deleteProject(projectName)
    }

    /**
     * Positive Scenarios
     */

    @Unroll
    def "RunWLST - complex code"() {
        setup: 'Define the parameters for Procedure running'
        scriptFileSource = 'newscriptfile'

        File sampleComplexCodeFile = new File(this.getClass().getResource("/resources/sampleWLSTScript.jython").toURI())
        assert sampleComplexCodeFile
        scriptFile = sampleComplexCodeFile.text
        assert scriptFile


        def runParams = [
            wlstabspath       : getWlstPath(),
            additional_envs   : additionalEnvs,
            additionalcommands: additionalCommands,
            scriptfilesource  : scriptFileSource,
            scriptfilepath    : scriptFilePath,
            scriptfile        : scriptFile,
            webjarpath        : webJarPath,
        ]

        when: 'Procedure runs: '

        def result = runTestedProcedure(projectName, procedureName, runParams, getResourceName())

        then: 'Wait until job run is completed: '

        def debugLog = result.logs
        println "Procedure log:\n$debugLog\n"

        expect: 'Outcome and Upper Summary verification'
        assert result.outcome == 'success'
        assert debugLog.contains("script returns SUCCESS")

    }

    def 'RunWLST - fail with exception - negative'() {
        setup: 'Define the parameters for Procedure running'
        scriptFileSource = 'newscriptfile'
        scriptFile = """
connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')
raise Exception("Expected exception")
"""

        def runParams = [
            wlstabspath       : getWlstPath(),
            additional_envs   : additionalEnvs,
            additionalcommands: additionalCommands,
            scriptfilesource  : scriptFileSource,
            scriptfilepath    : scriptFilePath,
            scriptfile        : scriptFile,
            webjarpath        : webJarPath,
        ]

        when: 'Procedure runs: '

        def result = runTestedProcedure(projectName, procedureName, runParams, getResourceName())

        then: 'Wait until job run is completed: '

        def debugLog = result.logs

        println "Procedure log:\n$debugLog\n"

        def upperStepSummary = getJobUpperStepSummary(result.jobId)
        logger.info(upperStepSummary)

        expect: 'Outcome and Upper Summary verification: '
        assert result.outcome == 'error'
        assert debugLog.contains("java.lang.Exception: java.lang.Exception: Expected exception")


    }


}
