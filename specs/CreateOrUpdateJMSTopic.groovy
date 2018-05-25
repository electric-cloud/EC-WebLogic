import spock.lang.*

class CreateOrUpdateJMSTopic extends WebLogicHelper {
    static def projectName = 'EC-WebLogic Specs CreateOrUpdateJMSTopic'
    static def jmsModuleName = 'TestJMSModule'
    static def configName = 'EC-Specs WebLogic Config'
    static def procedureName = 'CreateOrUpdateJMSTopic'
    static def deleteProcedureName = 'DeleteJMSTopic'

    static def params = [
        configname: configName,
        ecp_weblogic_jms_module_name: '',
        ecp_weblogic_jms_topic_name: '',
        ecp_weblogic_subdeployment_name: '',
        ecp_weblogic_update_action: 'do_nothing',
        ecp_weblogic_additional_options: '',
        ecp_weblogic_jndi_name: ''
    ]

    def doSetupSpec() {
        setupResource()
        deleteProject(projectName)
        createJMSModule(jmsModuleName)
        createConfig(configName)

        dslFile "dsl/procedures.dsl", [
            projectName: projectName,
            procedureName: procedureName,
            resourceName: getResourceName(),
            params: params,
        ]

        dslFile 'dsl/procedures.dsl', [
            projectName: projectName,
            procedureName: deleteProcedureName,
            resourceName: getResourceName(),
            params: [
                configname: configName,
                ecp_weblogic_jms_module_name: '',
                ecp_weblogic_jms_topic_name: ''
            ]
        ]
    }

    def doCleanupSpec() {
        deleteProject(projectName)
    }

    def 'create jms topic'() {
        given:
        def topicName = 'SpecTopic'
        def jndiName = 'TestJNDIName'
        deleteJMSTopic(jmsModuleName, topicName)
        when:
        def result = runProcedure("""
        runProcedure(
            projectName: '$projectName',
            procedureName: '$procedureName',
            actualParameter: [
                ecp_weblogic_jms_module_name: '$jmsModuleName',
                ecp_weblogic_jndi_name: '$jndiName',
                ecp_weblogic_jms_topic_name: '$topicName',
            ]
        )
        """, getResourceName())
        then:
        logger.debug(result.logs)
        assert result.outcome == 'success'
        assert result.logs =~ /Topic $topicName does not exist/
        assert result.logs =~ /Created Topic $topicName/
        def topic = getTopic(jmsModuleName, topicName)
        assert topic.jndiName == jndiName
        assert topic.subdeploymentName == topicName
        cleanup:
        deleteJMSTopic(jmsModuleName, topicName)
    }

    def 'update jms topic'() {
        given:
        def topicName = 'SpecTopic'
        def oldJNDIName = 'TestJNDIName'
        def newJNDIName = 'NewJNDIName'
        deleteJMSTopic(jmsModuleName, topicName)
        def result = runProcedure("""
        runProcedure(
            projectName: '$projectName',
            procedureName: '$procedureName',
            actualParameter: [
                ecp_weblogic_jms_module_name: '$jmsModuleName',
                ecp_weblogic_jndi_name: '$oldJNDIName',
                ecp_weblogic_jms_topic_name: '$topicName',
            ]
        )
        """, getResourceName())
        when:
        result = runProcedure("""
        runProcedure(
            projectName: '$projectName',
            procedureName: '$procedureName',
            actualParameter: [
                ecp_weblogic_jms_module_name: '$jmsModuleName',
                ecp_weblogic_jndi_name: '$newJNDIName',
                ecp_weblogic_jms_topic_name: '$topicName',
                ecp_weblogic_update_action: 'selective_update'
            ]
        )
        """, getResourceName())
        then:
        logger.info(result.logs)
        assert result.outcome == 'success'
        assert result.logs =~ /Set JNDI Name $newJNDIName/
        cleanup:
        deleteJMSTopic(jmsModuleName, topicName)
    }

    def 'recreate jms topic'() {
        given:
        def topicName = 'SpecTopic'
        def oldJNDIName = 'TestJNDIName'
        def newJNDIName = 'NewJNDIName'
        deleteJMSTopic(jmsModuleName, topicName)
        def result = runProcedure("""
        runProcedure(
            projectName: '$projectName',
            procedureName: '$procedureName',
            actualParameter: [
                ecp_weblogic_jms_module_name: '$jmsModuleName',
                ecp_weblogic_jndi_name: '$oldJNDIName',
                ecp_weblogic_jms_topic_name: '$topicName',
            ]
        )
        """, getResourceName())
        def jmsServerName = 'TestJMSServer'
        createJMSServer(jmsServerName)
        def subdeploymentName = randomize('TestSubdeployment')
        createSubDeployment(jmsModuleName, subdeploymentName, jmsServerName)
        when:
        result = runProcedure("""
        runProcedure(
            projectName: '$projectName',
            procedureName: '$procedureName',
            actualParameter: [
                ecp_weblogic_jms_module_name: '$jmsModuleName',
                ecp_weblogic_jndi_name: '$newJNDIName',
                ecp_weblogic_jms_topic_name: '$topicName',
                ecp_weblogic_update_action: 'remove_and_create',
                ecp_weblogic_subdeployment_name: '$subdeploymentName',
            ]
        )
        """, getResourceName())
        then:
        logger.info(result.logs)
        assert result.outcome == 'success'
        cleanup:
        deleteJMSTopic(jmsModuleName, topicName)
        deleteSubDeployment(jmsModuleName, subdeploymentName)
    }

    def 'delete jms topic'() {
        given:
        def topicName = 'SpecTopic'
        def result = runProcedure("""
        runProcedure(
            projectName: '$projectName',
            procedureName: '$procedureName',
            actualParameter: [
                ecp_weblogic_jms_module_name: '$jmsModuleName',
                ecp_weblogic_jms_topic_name: '$topicName',
            ]
        )
        """, getResourceName())
        when:
        result = runProcedure """
        runProcedure(
            projectName: '$projectName',
            procedureName: '$deleteProcedureName',
            actualParameter: [
                ecp_weblogic_jms_module_name: '$jmsModuleName',
                ecp_weblogic_jms_topic_name: '$topicName'
            ]
        )
        """, getResourceName()
        then:
        logger.debug(result.logs)
        assert result.outcome == 'success'
        assert result.logs =~ /Removed JMS Topic $topicName from the module $jmsModuleName/
    }

    def 'delete non-existing jms topic'() {
        given:
        def topicName = 'NoSuchTopic'
        when:
        def result = runProcedure """
        runProcedure(
            projectName: '$projectName',
            procedureName: '$deleteProcedureName',
            actualParameter: [
                ecp_weblogic_jms_module_name: '$jmsModuleName',
                ecp_weblogic_jms_topic_name: '$topicName'
            ]
        )
        """, getResourceName()
        then:
        logger.debug(result.logs)
        assert result.outcome == 'error'
    }

    def deleteJMSTopic(moduleName, name) {
        def code = """
def getJMSSystemResourcePath(jms_module_name):
    return "/JMSSystemResources/%s"%(jms_module_name)

def getJMSModulePath(jms_module_name):
    return "%s/JMSResource/%s"%(getJMSSystemResourcePath(jms_module_name),jms_module_name)

def getTopicPath(jms_module_name,queue_name):
    return "/JMSSystemResources/%s/JMSResource/%s/Topics/%s" % (jms_module_name, jms_module_name, queue_name)

def deleteTopic(jmsModuleName, cfName):
    bean = getMBean('%s/Topics/' % getJMSModulePath(jmsModuleName))
    queueBean = getMBean(getTopicPath(jmsModuleName, cfName))
    if queueBean != None:
        bean.destroyTopic(queueBean)
        print("Removed Topic %s from the module %s" % (cfName, jmsModuleName))
    else:
        print("Topic %s does not exist in the module %s" % (cfName, jmsModuleName))


moduleName = '$moduleName'
topicName = '$name'

connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')
edit()
startEdit()
try:
    deleteTopic(moduleName, topicName)
    activate()
except Exception, e:
    stopEdit('y')

"""
        def result = runWLST(code)
        assert result.outcome == 'success'
    }

}
