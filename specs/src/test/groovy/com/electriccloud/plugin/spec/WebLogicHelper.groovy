package com.electriccloud.plugin.spec

import com.electriccloud.spec.PluginSpockTestSupport
import groovy.json.*
import spock.util.concurrent.PollingConditions

class WebLogicHelper extends PluginSpockTestSupport {
    static final def HELPER_PROJECT = 'EC-WebLogic Specs Helper'
    static final def SUCCESS_RESPONSE = '200'
    static final def NOT_FOUND_RESPONSE = '404'

    static def FILENAME = 'sample.war'
    static def REMOTE_DIRECTORY = '/tmp'
    static def ARTIFACT_NAME = 'test:sample'
    static def APPLICATION_NAME = 'sample'
    static def APPLICATION_PATH = new File(REMOTE_DIRECTORY, FILENAME)
    static def APPLICATION_PAGE_URL = "http://localhost:7001/sample/hello.jsp"

    static final def CONFIG_NAME = 'EC-Specs WebLogic Config'
    static final def ENVIRONMENT_NAME = 'EC-Weblogic Specs Env'
    static final def TEST_APPLICATION = 'EC-WebLogic Specs Application'

    def doSetupSpec() {
        setupResource()
        createConfig(CONFIG_NAME)
    }

    def doCleanupSpec() {
//        deleteProject(HELPER_PROJECT)
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
        def resPort = System.getenv('WEBLOGIC_RES_PORT') ?: '7800'
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
        return System.getenv('WEBLOGIC_ENDPOINT') ?: 't3://localhost:7001'
    }

    def createConfig(configName) {
        def endpoint = getEndpoint()
        def username = getUsername()
        def password = getPassword()
        def enableNamedSessions = System.getenv('WEBLOGIC_ENABLE_NAMED_SESSIONS') ? '1' : '0'
        if (!supportsNamedSessions()) {
            enableNamedSessions = '0'
        }
        def testConnection = System.getenv('WEBLOGIC_ENABLE_TEST_CONNECTION') ? '1' : '0'

        def pluginConfig = [
            weblogic_url         : endpoint,
            enable_named_sessions: enableNamedSessions,
            debug_level          : '0',
            wlst_path            : getWlstPath(),
            test_connection      : testConnection,
            test_connection_res  : getResourceName(),
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

    def runWLST(code, jobNameTmpl = 'helperJob') {
        code = code.trim()
        def resourceName = getResourceName()
        def shell = getWlstPath()
        def procedureName = "RunWLST-${getResourceName()}"
        def projectName = HELPER_PROJECT
        dsl """
            project '$projectName', {
                procedure '${procedureName}', {

                    jobNameTemplate = '\$[jobNameTmpl]-\$[jobId]'
                    step 'runCommand', {
                        subproject = '/plugins/EC-WebLogic/project'
                        subprocedure = 'RunWLST'
                        resourceName = '${getResourceName()}'
                        actualParameter 'wlstabspath', '\$[shellToUse]'
                        actualParameter 'scriptfilesource', 'newscriptfile'
                        actualParameter 'scriptfile', '''\$[code]'''
                    }

                    formalParameter 'jobNameTmpl', {
                        type = 'entry'
                    }
                    formalParameter 'code', {
                        type = 'textarea'
                    }

                    formalParameter 'shellToUse', {
                        type = 'entry'
                    }
                }
            }
        """
        def result = runProcedure("""
            runProcedure(
                projectName: '${projectName}',
                procedureName: '${procedureName}',
                actualParameter: [
                    code: '''$code''',
                    shellToUse: '''$shell''',
                    jobNameTmpl: '$jobNameTmpl'
                ]
            )
        """, resourceName, 120, 10)
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
        //process.waitForOrKill(10 * 1000)
        logger.debug("STDOUT: $stdout")
        logger.debug("STDERR: $stderr")
        logger.debug("Exit code: ${process.exitValue()}")

        def text = "$stdout\n$stderr"
        assert process.exitValue() == 0
        text
    }

    static def isWebLogic11() {
        return System.getenv('WEBLOGIC_VERSION') == '11g'
    }

    static def isWindows() {
        return System.properties['os.name'].toLowerCase().contains('windows')
    }

    static def supportsNamedSessions() {
        def version = System.getenv('WEBLOGIC_VERSION')
        if (!version) {
            return true
        }
        if (version =~ /12R1|11/) {
            return false
        }
        return true
    }

    def publishArtifact(String artifactName, String version, String resName) {
        println "Publishing process Started..."
        logger.debug("Publishing process Started...")
        if (artifactExists(artifactName)) {
            logger.debug("Artifact $artifactName exists")
            return
        }
        File resource = new File(this.getClass().getResource("/${resName}").toURI())

        String commanderServer = System.getProperty("COMMANDER_SERVER") ?: 'localhost'
        String username = System.getProperty('COMMANDER_USER') ?: 'admin'
        String password = System.getProperty('COMMANDER_PASSWORD') ?: 'changeme'
        String commanderHome = System.getenv('COMMANDER_HOME') ?: '/opt/EC/'
        assert commanderHome: "Env COMMANDER_HOME must be provided"

        println commanderHome

        String ectoolPath
        if (System.properties['os.name'].toLowerCase().contains('windows')) {
            ectoolPath = "bin/ectool.exe"
        } else {
            ectoolPath = "bin/ectool"
        }
        println ectoolPath
        File ectool = new File(commanderHome, ectoolPath)
        println ectool.exists()
        assert ectool.exists(): "File ${ectool.absolutePath} does not exist"

        logger.debug("ECTOOL PATH: " + ectool.absolutePath.toString())

        String command = "${ectool.absolutePath} --server $commanderServer "
        println "command: ${command}"
        logger.debug("command: ${command}")

        runCommand("${command} login ${username} ${password}")

        runCommand("${command} deleteArtifactVersion ${artifactName}:${version}")

        String publishCommand = "${command} publishArtifactVersion --version $version --artifactName ${artifactName} "
        println "publishCommand: ${publishCommand}"
        logger.debug("publishCommand: ${publishCommand}")

        if (resource.directory) {
            publishCommand += "--fromDirectory ${resource}"
        } else {
            publishCommand += "--fromDirectory ${resource.parentFile} --includePatterns $resName"
        }
        println "Before Run publishCommand: ${publishCommand}"
        logger.debug("Before Run publishCommand: ${publishCommand}")
        runCommand(publishCommand)
    }


    def downloadArtifact(String artifactName, String resource) {

        dslFile 'dsl/retrieveArtifact.dsl', [
            projectName : HELPER_PROJECT,
            resourceName: resource,
            params      : [
                'artifactName'                   : artifactName,
                'artifactVersionLocationProperty': '/myJob/retrievedArtifactVersions/retrieved',
                'overwrite'                      : 'update',
//                        'retrieveToDirectory'            : destinationDirectory,
            ]
        ]

        def result = runProcedure("""
            runProcedure(
                projectName : '$HELPER_PROJECT',
                procedureName: 'Retrieve',
                actualParameter: [
                   'artifactName'                   : '$artifactName',
                   'artifactVersionLocationProperty': '/myJob/retrievedArtifactVersions/retrieved',
                   'overwrite'                      : 'update'
                ]
            )
        """, getResourceName())

        if (result.outcome != 'success') {
            throw new RuntimeException("Can't download artifact: ${result.logs}")
        }

        def cacheDirMatch = (result.logs =~ /cacheDirectory: (.*)/)

        if (!cacheDirMatch.hasGroup()) {
            throw new RuntimeException("Cache dir is not found")
        }

        String cacheDirPath = cacheDirMatch[0][1]

        if (!cacheDirPath) {
            throw new RuntimeException("Cache dir is not found")
        }

        return cacheDirPath.replace('\\', '\\\\\\\\')
    }

    def __artifactExists(def artifactName) {

        dslFile 'dsl/artifactExists.dsl', [
            projectName : HELPER_PROJECT,
            resourceName: getResourceName(),
            params      : [
                'artifactName': artifactName
            ]
        ]

        def result = runProcedure("""
           runProcedure(
                projectName : '$HELPER_PROJECT',
                procedureName: 'ArtifactExists',
                actualParameter: [
                        'artifactName': '$artifactName'
                ]
           )
           """, getResourceName())

        logger.debug("ArtifactExists logs:" + result.logs)
        return result.logs.contains("Artifact '$artifactName' exists in repository")
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

        [code: code, text: text]
    }

    def runTestedProcedure(def projectName, procedureName, def params, def resourceName) {

        dslFile('dsl/procedures.dsl', [
            projectName  : projectName,
            procedureName: procedureName,
            resourceName : resourceName,
            params       : params
        ])

        // Stringify map
        def params_str_arr = []
        params.each() { k, v ->
            params_str_arr.push(k + " : '''" + (v ?: '') + "'''")
        }
        logger.debug("Parameters string: " + params_str_arr.toString())

        def result = runProcedure("""
            runProcedure(
                projectName: '$projectName',
                procedureName: '$procedureName',
                actualParameter: $params_str_arr
            )
                """, resourceName,
            180, // timeout
            15  // initialDelay
        )
        return result
    }

    def undeployApplication(String projectName, def params) {

        def prepareRes = dslFile 'dsl/procedures.dsl', [
            projectName  : projectName,
            procedureName: 'UndeployApp',
            resourceName : getResourceName(),
            params       : params
        ]

        waitUntil {
            prepareRes
        }

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

    def deployApplication(
        def projectName, def params, String artifactName = 'test:sample', String filename = FILENAME) {

        publishArtifact(artifactName, '1.0', FILENAME)
        def path = downloadArtifact(artifactName, getResourceName())

        // Should be present (to initialize) but empty
        params.apppath = ''

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
                 apppath    : "$path/$filename",
                 targets    : 'AdminServer',
                 is_library : ""
            ]
        )
        """, getResourceName())

        logger.debug("Hello, my dear friend " + result)

        return result
    }

    def startApplication(def projectName, def params) {
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
            ]
        )
        """, getResourceName())

        return result
    }

    def stopApplication(def projectName, def params) {
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
        print ("Exception : ", e)
        stopEdit('y')

"""
        def result = runWLST(code, "CreateJMSModule_$name")
        assert result.outcome == 'success'
    }

    def createJMSTopic(moduleName, name) {
        def code = """
def getJMSResource(name):
    if (name == None or name == ''):
        raise Exception("No JMS Module Name is provided")
    mbean = getMBean('/JMSSystemResources/%s' % name)
    if mbean == None:
        return None
    else:
        print("Got JMS Bean %s" % mbean)
        return mbean.getJMSResource()

def getJMSTopicPath(jmsModule, topic):
    return "/JMSSystemResources/%s/JMSResource/%s/Topics/%s" % (jmsModule, jmsModule, topic)

jmsModuleName = '${moduleName}'
topicName = '${name}'

connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')
edit()
startEdit()

try:
    jmsResource = getJMSResource(jmsModuleName)
    print("Found JMS Resource %s" % jmsModuleName)
    if jmsResource == None:
        raise Exception("JMS Resource %s does not exist" % jmsModuleName)
    jmsTopic = getMBean(getJMSTopicPath(jmsModuleName, topicName))
    update = False
    if jmsTopic == None:
        print("JMS Topic %s does not exist" % topicName)
        jmsTopic = jmsResource.createTopic(topicName)
        print("Created Topic %s" % topicName)
        successMessage = 'Created JMS Topic %s' % topicName
    else:
        print("Found JMS Topic %s in the module %s" % (topicName, jmsModuleName))
    activate()
except Exception, e:
    stopEdit('y')
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
        def result = runWLST(code, "CreateJMSServer_$name")
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
try:
  startEdit()
  createOrUpdateSubdeployment('$moduleName', '$subName', '$serverName')
  activate()
except WLSTException, e:
    print str(e)
    stopEdit()
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
        def result = runWLST(code, "DeleteSubDeployment_${moduleName}_${subName}")
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
        def result = runWLST(code, "getQueue_$queue")
        assert result.outcome == 'success'
        def group = (result.logs =~ /JSON(\{.+?\})\/JSON/)
        String json = group[0][1]
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
print 'JSON' + '{"jndiName": "%s", "subdeploymentName": "%s"}/JSON' % (jndiName, subdeployment)
"""
        def result = runWLST(code, "GetJMSTopic_$topic")
        assert result.outcome == 'success'
        def group = (result.logs =~ /JSON(\{.+?\})\/JSON/)
        String json = group[0][1]
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
        def result = runWLST(code, "DeleteJMSModule_${jmsModule}")
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
    edit()
    undo('true', 'y')
    stopEdit('y')
except WLSTException, e:
    print 'Cannot stop edit'
    print str(e)
"""
        def result = runWLST(code, "DiscardChanges")
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
        def isWindows = System.getenv("IS_WINDOWS")
        def workspacePath = "/tmp"
        if (isWindows) {
            workspacePath = "C:/workspace"
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

    def runProcedure(dslString, resourceName = null, timeout = 120, initialDelay = 0) {
        assert dslString
        def result = dsl(dslString)

        PollingConditions poll = new PollingConditions(timeout: timeout, initialDelay: initialDelay, factor: 2)
        poll.eventually {
            jobStatus(result.jobId).status == 'completed'
        }

        def logs = readJobLogs(result.jobId, resourceName)
        def outcome = jobStatus(result.jobId).outcome
        [logs: logs, outcome: outcome, jobId: result.jobId]
    }

    def createJMSQueue(def moduleName, def name) {
        def code = """
def getJMSResource(name):
    if (name == None or name == ''):
        raise Exception("No JMS Module Name is provided")
    mbean = getMBean('/JMSSystemResources/%s' % name)
    if mbean == None:
        return None
    else:
        print("Got JMS Bean %s" % mbean)
        return mbean.getJMSResource()

def getJMSQueuePath(jmsModule, queue):
    return "/JMSSystemResources/%s/JMSResource/%s/Queues/%s" % (jmsModule, jmsModule, queue)


jmsModuleName = '$moduleName'
queueName = '$name'

connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')

try:
  edit()
  startEdit()
  jmsResource = getJMSResource(jmsModuleName)
  print("Found JMS Resource %s" % jmsModuleName)
  if jmsResource == None:
      raise Exception("JMS Resource %s does not exist" % jmsModuleName)
  jmsQueue = getMBean(getJMSQueuePath(jmsModuleName, queueName))
  update = False
  if jmsQueue == None:
      print("JMS Queue %s does not exist" % queueName)
      jmsQueue = jmsResource.createQueue(queueName)
      print("Created Queue %s" % queueName)

      # everything is fine, commiting
      activate()
  else:
      print("Found JMS Queue %s in the module %s" % (queueName, jmsModuleName))
except Exception, e:
    print("Failed to create JMS Queue", e)
    stopEdit('y')
"""
        def result = runWLST(code)
        assert result.outcome == 'success'
    }


    def deleteDatasource(dsName) {
        def code = """
def deleteDatasource(dsName):
    if not dsName:
        raise Exception('Datasource name is not provided')
    bean = getMBean('/JDBCSystemResources/' + dsName)
    if bean != None:
        parentBean = getMBean('/JDBCSystemResources')
        parentBean.destroyJDBCSystemResource(bean)
        print "Removed Datasource %s" % dsName
    else:
        print "Datasource %ss does not exist" % dsName

connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')
edit()
startEdit()
deleteDatasource('$dsName')
save()
activate()
"""
        def result = runWLST(code, "DeleteDatasource_${dsName}")
        assert result.outcome == 'success'
    }


    def deployJMSTestApplication(targetServer) {
        String artifactName = 'test:jms'
        String appName = 'SampleJMSApplication'

        publishArtifact(artifactName, '1.0', getResourceName())
        def path = downloadArtifact(artifactName, getResourceName())

        deployApplication(HELPER_PROJECT, [
            configname : CONFIG_NAME,
            wlstabspath: getWlstPath(),
            appname    : appName,
            apppath    : path,
            targets    : targetServer,
            is_library : ""
        ], artifactName, 'SampleJMSApplication.war')
    }

    def getResourceProperty(module, cfName, resType, propGroup, propName) {
        assert module
        assert cfName
        assert resType
        def code = """
def getResourcePath(jms_module_name,resource_name, resource_type):
    resource_type += 's'
    return "/JMSSystemResources/%s/JMSResource/%s/%s/%s" % (jms_module_name, jms_module_name, resource_type, resource_name)

module = '$module'
cfName = '$cfName'
type = '$resType'
group = '$propGroup'
propName = '$propName'
connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')
if group:
    cd(getResourcePath(module, cfName, type) + '/' + group + '/' + cfName)
else:
    cd(getResourcePath(module, cfName, type))

print "VALUE:" + '+' + str(get(propName)) + '+'
# just an ending comment to distinguish the last quote
"""
        def result = runWLST(code, "GetResourceProperty_${propGroup}_${propName}")
        assert result.outcome == 'success'
        def group = (result.logs =~ /VALUE:\+(.+?)\+/)
        def value = group[0][1]
        return value
    }

    def checkResourceProperties(moduleName, resName, resType, additionalOptions) {
        assert additionalOptions
        additionalOptions.split(/\n+/).each {
            def (key, value) = it.split('=')
            def groupOption = key.split(/\./)
            def group = ''
            def option = ''
            if (groupOption.size() > 1) {
                group = groupOption.getAt(0)
                option = groupOption.getAt(1)
            } else {
                option = key
            }
            if (value == 'true') {
                value = '1'
            }
            if (value == 'false') {
                value = '0'
            }
            def actualValue = getResourceProperty(moduleName, resName, resType, group, option)
            logger.debug("${group}.${option} = $actualValue")
            assert actualValue == value: "Actual option value for $group.$option is $actualValue, expected is $value"
        }
    }

    def cleanup() {
        logger.info(">>>>>>>FINISHED WITH FEATURE: ${getClass().simpleName} Spec: ${specificationContext.currentIteration.name}")
    }

    def getJobLogs(def jobId) {
        assert jobId
        def logs
        try {
            logs = getJobProperty("/myJob/debug_logs", jobId)
        } catch (Throwable e) {
            logs = "Possible exception in logs; check job $jobId. $e"
        }
        logs
    }

    static def testDatasource() {
        def test = System.getenv('WEBLOGIC_TEST_DATASOURCE')
        return test ? true : false
    }

    static def testJMS() {
        def testJMS = System.getenv('WEBLOGIC_TEST_JMS')
        return testJMS ? true : false
    }

    static def end2end() {
        return System.getenv('WEBLOGIC_TEST_END2END') ? true : false
    }

    def runProcess(projectName, appName, processName, tierMapName) {
        def result = runProcedure """
            runProcess(
                projectName: '$projectName',
                applicationName: '$appName',
                processName: '$processName',
                tierMapName: '$tierMapName'
            )
        """, getResourceName(), 180, 15
        return result
    }

    def getDemoAppName() {
        return 'jms-sample'
    }

    def getDemoAppPath() {
        return 'jms-sample.war'
    }


    def runPipeline(String projectName, String pipelineName, Map params, String resourceName = null, int timeout = 120) {
        def actualParameters = []
        params.each { k, v ->
            actualParameters << k + ': """' + v + '"""'
        }

        def pipelineDsl = """
runPipeline(projectName: '$projectName', pipelineName: '$pipelineName', actualParameter: [${actualParameters.join(',')}])
"""
        def result = dsl pipelineDsl
        def runtimeId = result.flowRuntime?.flowRuntimeId
        assert runtimeId
        def poll = createPoll(timeout)
        poll.eventually {
            pipelineCompleted(result)
        }

        def task = dsl("getPipelineStageRuntimeTasks flowRuntimeId: '$runtimeId', stageName: 'Stage'")?.task[0]
        def logs = readJobLogs(task.jobId, resourceName)
        def status = task.status
        return [logs: logs, flowRuntimeId: runtimeId, status: task.status]
    }

    def getDerbyHost() {
        def host = System.getenv('WEBLOGIC_DERBY_HOST') ?: 'localhost'
        return host
    }


    def getMysqlHost() {
        def host = System.getenv('WEBLOGIC_MYSQL_HOST') ?: 'localhost'
        return host
    }


    def getOutputParameters(jobId, stepName) {
        def details = dsl "getJobDetails jobId: '$jobId'"
        def step = details?.job?.jobStep.find { it.stepName == stepName }
        assert step : "Step $stepName is not found in job $jobId"
        def parameters = dsl "getOutputParameters jobStepId: '${step.jobStepId}'"
        return parameters?.outputParameter
    }

    def checkServerRestartOutputParameter(jobId) {
        if (isWindows()) {
            println "Will not check output parameters on windows"
            return true
        }
        def parameters = getOutputParameters(jobId, 'RunProcedure')
        def restart = parameters.find { it.outputParameterName == 'WebLogicServerRestartRequired'}
        assert restart : "Output parameter WebLogicServerRestartRequired does not exist"
        return true

    }
}
