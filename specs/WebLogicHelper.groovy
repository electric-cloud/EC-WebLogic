import com.electriccloud.spec.PluginSpockTestSupport
import groovy.json.*

class WebLogicHelper extends PluginSpockTestSupport {
    static final def HELPER_PROJECT = 'EC-WebLogic Specs Helper'
    static final def SUCCESS_RESPONSE = '200'
    static final def NOT_FOUND_RESPONSE = '404'

    static def FILENAME = 'sample.war'
    static def REMOTE_DIRECTORY = '/tmp'
    static def APPLICATION_NAME = 'sample'
    static def APPLICATION_PATH = new File(REMOTE_DIRECTORY, FILENAME)
    static def APPLICATION_PAGE_URL = "http://localhost:7001/sample/hello.jsp"

    static final def CONFIG_NAME = 'EC-Specs WebLogic Config'

    def doSetupSpec() {
        setupResource()
        createConfig(CONFIG_NAME)
    }

    def doCleanupSpec() {
        deleteProject(HELPER_PROJECT)
//        deleteProject('EC-Spec Helper')
    }

    static def getWlstPath() {
        def path = System.getenv('WEBLOGIC_WLST_PATH')
        assert path
        return path
    }

    static def getResourceName() {
        def resName = System.getenv('WEBLOGIC_RES_NAME')
        assert resName
        return resName
    }

    static def getResourceHost() {
        def resHost = System.getenv('WEBLOGIC_RES_HOST') ?: getResourceName()
        assert resHost
        resHost
    }

    static def getResourcePort() {
        def resPort = System.getenv('WEBLOGIC_RES_PORT')
        assert resPort
        resPort
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
                weblogic_url         : endpoint,
                enable_named_sessions: 'true',
                debug_level          : '10',
                wlst_path            : getWlstPath(),
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
        def port = getResourcePort()

        dsl """
          resource '$name', {
            hostName = '$host'
            port     = '$port'
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

        runProcedure("""
            runProcedure(
                projectName : '$HELPER_PROJECT',
                procedureName: 'Retrieve',
                actualParameter: [
                   'artifactName'        : '$artifactName',
                   'artifactVersionLocationProperty': '/myJob/retrievedArtifactVersions/retrieved',
                   'overwrite'           : 'update',
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

        logger.debug("CheckURL result: " + result.toString())

        def text = null
        def code = getJobProperty("/myJob/code", result.jobId)
        if (code == SUCCESS_RESPONSE) {
            text = getJobProperty("/myJob/text", result.jobId)
        }

        [ code: code, text: text ]
    }

    def runTestedProcedure(def projectName, procedureName, def params, def resourceName) {

        deleteProject(projectName)

        dslFile('dsl/procedures.dsl', [
                projectName  : projectName,
                procedureName: procedureName,
                resourceName : resourceName,
                params       : params
        ])

        // Stringify map
        def params_str_arr = []
        params.each() { k, v ->
            params_str_arr.push(k + " : '" + (v ?: '') + "'")
        }
        logger.debug("Parameters string: " + params_str_arr.toString())

        def result = runProcedure("""
            runProcedure(
                projectName: '$projectName',
                procedureName: '$procedureName',
                actualParameter: $params_str_arr

            )
                """, resourceName
        )
        return result
    }

    def UndeployApplication(String projectName, def params) {
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
                 wlstabspath: '${params.wlstabspath}',
                 appname    : '${params.appname}'
            ]
        )
        """, getResourceName())

        return result
    }

    def DeployApplication(def projectName, def params) {
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
                 wlstabspath: '${params.wlstabspath}',
                 appname    : '${params.appname}',
                 apppath    : "${params.apppath}",
                 targets    : 'AdminServer',
                 is_library : ""
            ]
        )
        """)

        return result
    }

    def StartApplication(def projectName, def params) {
        dslFile 'dsl/procedures.dsl', [
                projectName  : projectName,
                procedureName: 'StartApp',
                resourceName : getResourceName(),
                params       : params
        ]

        def result = runProcedure("""
        runProcedure(
            projectName: '$projectName',
            procedureName: 'StartApp',
            actualParameter: [
                 wlstabspath: '${params.wlstabspath}',
                 appname    : '${params.appname}',

                 additional_options : "",
                 version_identifier : ""
            ]
        )
        """, getResourceName())

        return result
    }

    def StopApplication(def projectName, def params) {
        dslFile 'dsl/procedures.dsl', [
                projectName  : projectName,
                procedureName: 'StopApp',
                resourceName : getResourceName(),
                params       : params
        ]

        def result = runProcedure("""
        runProcedure(
            projectName: '$projectName',
            procedureName: 'StopApp',
            actualParameter: [
                 wlstabspath: '${params.wlstabspath}',
                 appname    : '${params.appname}',

                 additional_options : "",
                 version_identifier : ""
            ]
        )
        """, getResourceName())

        return result
    }

    def createJMSModule(name, targets = 'AdminServer') {
        def code = """import re
resource_name = '$name'
targets = '$targets'
print 'Targets ' + targets
connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')
if cmo.lookupJMSSystemResource(resource_name):
    print "Resource %s alreay exists" % resource_name
else:
    try:
        cd('/')
        edit()
        startEdit()
        cmo.createJMSSystemResource(resource_name)
        cd("/JMSSystemResources/%s" % resource_name)
        if targets != '':
            for targetName in re.split('\\\\s*,\\\\s*', targets):
                targetBean = getMBean('/Servers/' + targetName)
                if targetBean == None:
                    targetBean = getMBean('/Clusters/' + targetName)
                cmo.addTarget(targetBean)
                print "Adding target %s" % targetBean.objectName
        activate()
    except Exception, e:
        stopChanges('y')

"""
        def result = runWLST(code)
        assert result.outcome == 'success'
    }

    def deleteJMSServer(name) {
        def code = """
connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')

jmsServerName = '$name'

bean = getMBean('/JMSServers/%s' % jmsServerName)
if bean == None:
    print "JMS Server %s does not exist" % jmsServerName
else:
    edit()
    startEdit()
    cd('/')
    print "Deleting JMS Server %s" % jmsServerName
    cmo.destroyJMSServer(bean)
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

    def deleteJMSModule(jmsModule) {
        assert jmsModule
        def code = """
connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')
module = '$jmsModule'
cd('/')
bean = getMBean('/JMSSystemResources/' + module)
print bean
edit()
startEdit()
if bean != None:
    cmo.destroyJMSSystemResource(bean)
else:
    print "JMS Module %s does not exist" % module
activate()

"""
        def result = runWLST(code)
        assert result.outcome == 'success'
    }


    def ensureManagedServer(msName, port = '7999') {
        def code = """
msName = '$msName'
port = '$port'
connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')
cd('/')
bean = getMBean('/Servers/' + msName)
print bean
if bean == None:
    edit()
    startEdit()
    cmo.createServer(msName)
    cd('/Servers/' + msName)
    cmo.setListenAddress('localhost')
    cmo.setListenPort(int(port))
    activate()
else:
    print "Server %s already exists" % msName
"""
        def result = runWLST(code)
        assert result.outcome == 'success'
    }


    def ensureCluster(clName) {
        def code = """
clName = '$clName'
connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')
cd('/')
bean = getMBean('/Clusters/' + clName)
print bean
if bean == None:
    edit()
    startEdit()
    cmo.createCluster(clName)
    cd('/Clusters/' + clName)
    cmo.setClusterMessagingMode('unicast')
    cmo.setClusterBroadcastChannel('')
    cmo.setClusterAddress('localhost')
    activate()
else:
    print "Cluster %s already exists" % clName
"""
        def result = runWLST(code)
        assert result.outcome == 'success'
    }

    def discardChanges() {
        def code = """connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')
try:
    stopEdit('y')
except WLSTException, e:
    print 'Cannot stop edit'
    print str(e)
"""
        def result = runWLST(code)
        assert result.outcome == 'success'
        def notInEditTree = result.logs =~ /Cannot call Edit functions when you are not in the Edit tree/
        return notInEditTree
    }

    def stringifyArray(def params) {
        def params_str_arr = []
        params.each() { k, v ->
            params_str_arr.push(k + " : '" + (v ?: '') + "'")
        }

        // toString() will join with ', '
        return params_str_arr.toString()
    }

    def createWorkspace(def workspaceName) {
        def isWindows = System.getenv("IS_WINDOWS");
        def workspacePath = "/tmp";
        if (isWindows) {
            workspacePath = "C:/workspace";
        }
        def workspaceResult = dsl """
try {
            createWorkspace(
                workspaceName: '${workspaceName}',
                agentDrivePath: '${workspacePath}',
                agentUnixPath: '/tmp',
                local: '1'
            )
} catch (Exception e) {}
        """

        return workspaceResult
    }

    def getJobUpperStepSummary(def jobId) {
        assert jobId
        def summary = null
        def property = "/myJob/jobSteps/RunProcedure/summary"
        println "Trying to get the summary, property: $property, jobId: $jobId"
        try {
            summary = getJobProperty(property, jobId)
        } catch (Throwable e) {
            logger.error("Can't retrieve Upper Step Summary from the property: '$property'; check job: " + jobId)
        }
        return summary
    }
}
