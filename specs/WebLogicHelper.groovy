import com.electriccloud.spec.PluginSpockTestSupport
import groovy.json.*

class WebLogicHelper extends PluginSpockTestSupport {
    static final def HELPER_PROJECT = 'EC-WebLogic Specs Helper'
    static final def SUCCESS_RESPONSE = '200'
    static final def NOT_FOUND_RESPONSE = '404'

    static def FILENAME = 'sample.war'
    static def REMOTE_DIRECTORY = '/tmp'
    static def APPLICATION_NAME = 'sample'

    def doSetupSpec() {
        setupResource()
        deleteProject(HELPER_PROJECT)
    }

    def doCleanupSpec() {
        deleteProject(HELPER_PROJECT)
    }

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

    static def getResourceHost() {
        def resHost = System.getenv('WEBLOGIC_RES_HOST') ?: getResourceName()
        assert resHost
        resHost
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

    static def getAdminServerName() {
        def adminServerName = 'AdminServer'
        return adminServerName
    }

    static def getEndpoint() {
        return 't3://localhost:7001'
    }

    def createConfig(configName) {
        def endpoint = getEndpoint()
        def username = getUsername()
        def password = getPassword()
        def pluginConfig = [
            weblogic_url  : endpoint,
            enable_named_sessions: 'true',
            debug_level: '10',
            wlst_path: getWlstPath(),
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

    def setupResource() {
        def host = getResourceHost()
        def name = getResourceName() ?: host

        dsl """
          resource '$name', {
            hostName = '$host'
            port = 7808
          }
        """
    }

    def runWLST(code) {
        code = code.trim()
        def resourceName = getResourceName()
        def procedureName = 'RunWLST'
        dsl """
            project '${HELPER_PROJECT}', {
                procedure '${procedureName}', {
                    step 'runCommand', {
                        resourceName = '${getResourceName()}'
                        shell = '${getWlstPath()}'
                        command = '''\$[code]'''
                    }

                    formalParameter 'code', {
                        type = 'textarea'
                    }
                }
            }
        """
        def result = runProcedure("""
            runProcedure(
                projectName: '${HELPER_PROJECT}',
                procedureName: '${procedureName}',
                actualParameter: [
                    code: '''$code'''
                ]
            )
        """, resourceName)
        result
    }

    def deleteProject(name) {
        dsl """
        deleteProject(projectName: '$name')
        """
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
        String commanderHome = System.getenv('COMMANDER_HOME') ?: '/opt/EC/'
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

        dslFile 'dsl/retrieveArtifact.dsl', [
                projectName : HELPER_PROJECT,
                resourceName: resource,
                params      : [
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
                procedureName: 'Retrieve',
                actualParameter: [
                   'artifactName' : 'test:sample',
                   'artifactVersionLocationProperty': '/myJob/retrievedArtifactVersions/retrieved',
                   'overwrite' : 'update',
                   'retrieveToDirectory' : '${destinationDirectory}'
                ]
            )
        """, getResourceName())
    }

    def checkUrl(String url) {

        dslFile 'dsl/checkURL.dsl', [
                projectName : HELPER_PROJECT,
                resourceName: getResourceName(),
                URL         : url
        ]

        def result = runProcedure("""
   runProcedure(
       projectName: '$HELPER_PROJECT',
       procedureName: 'CheckURL',
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

    def UndeployApplication(String projectName, def params) {
        def wlstPath = getWlstPath()
        deleteProject(projectName)
        dslFile 'dsl/procedures.dsl', [
                projectName  : projectName,
                procedureName: 'UndeployApp',
                resourceName : getResourceName(),
                params       : params
        ]

        def result = runProcedure("""
        runProcedure(
            projectName: '$projectName',
            procedureName: 'UndeployApp',
            actualParameter: [
                 wlstabspath: '$wlstPath',
                 appname : '$APPLICATION_NAME'
            ]
        )
        """, getResourceName())


        return result
    }

    def DeployApplication(def projectName, def params) {

        def wlstPath = getWlstPath()
        def artifactName = 'test:sample'
        def version = '1.0'

        publishArtifact(artifactName, version, FILENAME)
        downloadArtifact(artifactName, REMOTE_DIRECTORY, getResourceName())

        dslFile 'dsl/procedures.dsl', [
                projectName  : projectName,
                procedureName: 'DeployApp',
                resourceName : getResourceName(),
                params       : params
        ]

        def result = runProcedure("""
        runProcedure(
            projectName: '$projectName',
            procedureName: 'DeployApp',
            actualParameter: [
                 wlstabspath: '$wlstPath',
                 appname : '$APPLICATION_NAME',
                 apppath : "$REMOTE_DIRECTORY/$FILENAME",
                 targets : 'AdminServer',
                 is_library : ""
            ]
        )
        """, getResourceName())

        return result
    }

    def createJMSModule(name) {
        def code = """
resource_name = '$name'
target = 'AdminServer'
connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')
cd('/')
edit()
if cmo.lookupJMSSystemResource(resource_name):
    print "Resource %s alreay exists" % resource_name
else:
    startEdit()
    cmo.createJMSSystemResource(resource_name)
    cd("/JMSSystemResources/%s" % resource_name)
    cmo.addTarget(getMBean("/Servers/%s" % target))
    activate()
"""
        def result = runWLST(code)
        assert result.outcome == 'success'
    }


    def createJMSServer(name) {
        def code = """
connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')

jmsServerName = '$name'
targetName = '${getAdminServerName()}'

bean = getMBean('/JMSServers/%s' % jmsServerName)
if bean == None:
    edit()
    startEdit()
    cd('/')
    print "Creating JMS Server %s" % jmsServerName
    cmo.createJMSServer(jmsServerName)
    cd("/JMSServers/%s" % jmsServerName)
    cmo.addTarget(getMBean("/Servers/%s" % targetName))
    activate()
else:
    print "JMS Server already exists"
"""
        def result = runWLST(code)
        assert result.outcome == 'success'
        result
    }

    def createSubDeployment(moduleName, subName, serverName) {
        def code = """
def getSubDeploymentPath(jms_module_name, subdeployment_name):
    return "/JMSSystemResources/%s/SubDeployments/%s" % (jms_module_name, subdeployment_name)

def createOrUpdateSubdeployment(jmsModuleName, subName, jmsServerName):
    subdeployment = getMBean(getSubDeploymentPath(jmsModuleName, subName))
    if subdeployment == None:
        print("Subdeployment %s does not exist" % subName)
        jmsModuleBean = getMBean('/JMSSystemResources/' + jmsModuleName)
        if jmsModuleBean == None:
            raise Exception('JMS Resource %s does not exist' % jmsModuleName)
        subdeployment = jmsModuleBean.createSubDeployment(subName)
        print("Created Subdeployment %s" % subName)

    cd(getSubDeploymentPath(jmsModuleName, subName))
    if jmsServerName == '':
        raise Exception('JMS Server Name is not provided!')

    jmsServerBean = getMBean('/JMSServers/' + jmsServerName)
    if jmsServerBean == None:
        raise Exception('JMS Server %s does not exist' % jmsServerName)
    currentTargets = subdeployment.getTargets()
    for target in currentTargets:
        print('Found target: %s' % target.objectName)
        if target.objectName == jmsServerBean.objectName:
            print('Target %s is already in the list of targets' % jmsServerBean.objectName)
            return

    targets = [jmsServerBean.objectName]
    set('Targets', jarray.array(targets, ObjectName))
    print "Set JMS Subdeployment Targets: %s" % targets
    return subdeployment

connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')
edit()
startEdit()
createOrUpdateSubdeployment('$moduleName', '$subName', '$serverName')
activate()
"""
        def result = runWLST(code)
        assert result.outcome == 'success'
    }

    def deleteSubDeployment(moduleName, subName) {
        def code = """
def getSubDeploymentPath(jms_module_name, subdeployment_name):
    return "/JMSSystemResources/%s/SubDeployments/%s" % (jms_module_name, subdeployment_name)

def deleteSubDeployment(jmsModuleName, subName):
    jmsModuleBean = getMBean('/JMSSystemResources/' + jmsModuleName)
    subBean = getMBean(getSubDeploymentPath(jmsModuleName, subName))
    if subBean != None:
        jmsModuleBean.destroySubDeployment(subBean)
        print "Deleted subdeployment"
    else:
        print "Subdeployment does not exist"

connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')
edit()
startEdit()
deleteSubDeployment('$moduleName', '$subName')
activate()
"""
        def result = runWLST(code)
        assert result.outcome == 'success'
    }

    def getQueue(jmsModule, queue) {
        def code = """
connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')
module = '$jmsModule'
queue = '$queue'
cd('/JMSSystemResources/%s/JMSResource/%s/Queues/%s' % (module, module, queue))
jndiName = get('JNDIName')
subdeployment = get('SubDeploymentName')
print 'JSON{"jndiName": "%s", "subdeploymentName": "%s"}/JSON' % (jndiName, subdeployment)
"""
        def result = runWLST(code)
        assert result.outcome == 'success'
        def group = (result.logs =~ /JSON(\{.+?\})\/JSON/)
        def json = group[0][1]
        return new JsonSlurper().parseText(json)
    }

    def getTopic(jmsModule, topic) {
        def code = """
connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')
module = '$jmsModule'
topic = '$topic'
cd('/JMSSystemResources/%s/JMSResource/%s/Topics/%s' % (module, module, topic))
jndiName = get('JNDIName')
subdeployment = get('SubDeploymentName')
print 'JSON{"jndiName": "%s", "subdeploymentName": "%s"}/JSON' % (jndiName, subdeployment)
"""
        def result = runWLST(code)
        assert result.outcome == 'success'
        def group = (result.logs =~ /JSON(\{.+?\})\/JSON/)
        def json = group[0][1]
        return new JsonSlurper().parseText(json)
    }
}
