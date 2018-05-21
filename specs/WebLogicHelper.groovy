import spock.lang.*
import com.electriccloud.spec.*

class WebLogicHelper extends PluginSpockTestSupport {
    static final def HELPER_PROJECT = 'EC-WebLogic Specs Helper'
    static final def SUCCESS_RESPONSE = '200'

    static def getWlstPath() {
        def path = System.getenv('WEBLOGIC_WLST_PATH')
        assert path
        path
    }

    static def getResourceName() {
        def resName = System.getenv('WEBLOGIC_RES_NAME')
        assert resName
        resName
    }

    static def getUsername() {
        def username = System.getenv('WEBLOGIC_USERNAME')
        assert username
        username
    }

    static def getPassword() {
        def password = System.getenv('WEBLOGIC_PASSWORD')
        assert password
        password
    }

    def createConfig(configName) {
        def endpoint = 't3://localhost:7001'
        def username = getUsername()
        def password = getPassword()
        def pluginConfig = [
                weblogic_url         : endpoint,
                enable_named_sessions: 'true',
                debug_level          : '10'
        ]
        def props = [confPath: 'weblogic_cfgs']
        if (System.getenv('RECREATE_CONFIG')) {
            props.recreate = true
        }
        createPluginConfiguration(
                'EC-WebLogic',
                configName,
                pluginConfig,
                username,
                password,
                props
        )
    }

    static def runWLST(code) {
        code = code.trim()
        println "+++$code+++"
        dsl """
            project '${HELPER_PROJECT}', {
                procedure 'runWlst', {
                    step 'runCommand', {
                        resourceName = '${getResourceName()}'
                        shell = '${getWlstPath()}'
                        command = '''\$[wlst_code]'''
                    }

                    formalParameter 'wlst_code', {
                        type = 'textarea'
                    }
                }
            }
        """
        def result = runProcedureDsl """
            runProcedure(
                projectName: '${HELPER_PROJECT}',
                procedureName: 'runWlst',
                actualParameter: [
                    'wlst_code': '''$code'''
                ]
            )
        """
        println result
        result
    }

    def runCommand(command) {
        logger.debug("Command: $command")
        def stdout = new StringBuilder()
        def stderr = new StringBuilder()
        def process = command.execute()
        process.consumeProcessOutput(stdout, stderr)
        process.waitForOrKill(20 * 1000)
        logger.debug("STDOUT: $stdout")
        logger.debug("STDERR: $stderr")
        logger.debug("Exit code: ${process.exitValue()}")
        def text = "$stdout\n$stderr"
        assert process.exitValue() == 0
        text
    }

    def publishArtifact(String artifactName, String version, String resName) {
        File resource = new File(this.getClass().getResource("/resources/${resName}").toURI())

        String commanderServer = System.getProperty("COMMANDER_SERVER") ?: 'localhost'
        String username = System.getProperty('COMMANDER_USER') ?: 'admin'
        String password = System.getProperty('COMMANDER_PASSWORD') ?: 'changeme'
        String commanderHome = System.getenv('COMMANDER_HOME')
        assert commanderHome

        File ectool = new File(commanderHome, "bin/ectool")
        assert ectool.exists()
        logger.debug(ectool.absolutePath.toString())

        String command = "${ectool.absolutePath} --server $commanderServer "
        runCommand("${command} login ${username} ${password}")

        runCommand("${command} deleteArtifactVersion ${artifactName}:${version}")

        String publishCommand = "${command} publishArtifactVersion --version $version --artifactName ${artifactName} "
        if (resource.directory) {
            publishCommand += "--fromDirectory ${resource}"
        } else {
            publishCommand += "--fromDirectory ${resource.parentFile} --includePatterns $resName"
        }
        runCommand(publishCommand)
    }

    def downloadArtifact(String artifactName, String destinationDirectory, String resource) {

        def procName = 'Retrieve'
        dslFile 'dsl/procedures.dsl', [
                projectName   : HELPER_PROJECT,
                procedureName : procName,
                subProjectName: '/plugins/EC-Artifact/project',
                resourceName  : resource,
                params        : [
                        'artifactName'                   : artifactName,
                        'artifactVersionLocationProperty': '/myJob/retrievedArtifactVersions/retrieved',
                        'overwrite'                      : 'update',
                        'retrieveToDirectory'            : destinationDirectory,
                ]
        ]
//

        runProcedure("""
            runProcedure(
                projectName : '$HELPER_PROJECT',
                procedureName: '$procName',
                resourceName: '$resource',
                actualParameter: [
                   'artifactName' : 'test:sample',
                   'artifactVersionLocationProperty': '/myJob/retrievedArtifactVersions/retrieved',
                   'overwrite' : 'update',
                   'retrieveToDirectory' : '${destinationDirectory}'
                ]
            )
        """, getResourceName())
    }

    def checkUrl(String url, String resource) {

        dslFile('dsl/checkURL.dsl', [
                projectName : HELPER_PROJECT,
                resourceName: resource,
                URL         : url
        ])

        def result = runProcedure("""
   runProcedure(
       projectName: '$HELPER_PROJECT',
       procedureName: 'CheckURL',
       resourceName: '$resourceName'
   )
""", getResourceName())

        logger.debug(result.toString())

        def text = null

        def code = getJobProperty("/myJob/code", result.jobId)
        if (code == SUCCESS_RESPONSE) {
            text = getJobProperty("/myJob/text", result.jobId)
        }

        [
                code: code,
                text: text
        ]
    }

    def runProcedure(String dslString, resourceName = null, timeout = 120) {
        assert dslString
        def result = dsl(dslString)

        assert result.jobId
        def jobId = result.jobId

        def status = jobStatus(jobId).status
        def counter = 1
        while (status != 'completed'){
            status = jobStatus(jobId).status

            logger.debug("status: " + status)

            if (counter < 32){
                counter *= 2
                logger.debug("Sleeping " + counter + " seconds")

                sleep(counter * 1000)
            }
            else if (counter > timeout){
                throw new RuntimeException("Timeout on jobCompleted : " + result.jobId)
            }
        }

        def logs = readJobLogs(result.jobId, resourceName)
        def outcome = jobStatus(result.jobId).outcome
        [logs: logs, outcome: outcome, jobId: result.jobId]
    }
}


