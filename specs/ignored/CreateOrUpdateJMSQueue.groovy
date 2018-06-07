import com.electriccloud.spec.SpockTestSupport
import spock.lang.*

@Ignore
class CreateOrUpdateJMSQueue extends WebLogicHelper {
    static def projectName = 'EC-WebLogic Specs CreateOrUpdateJMSQueue'
    static def jmsModuleName = 'TestJMSModule'
    static def configName = CONFIG_NAME
    static def procedureName = 'CreateOrUpdateJMSQueue'
    static def deleteProcedureName = 'DeleteJMSQueue'

    static def params = [
            configname                     : configName,
            ecp_weblogic_jms_module_name   : '',
            ecp_weblogic_jms_queue_name    : '',
            ecp_weblogic_subdeployment_name: '',
            ecp_weblogic_update_action     : 'do_nothing',
            ecp_weblogic_additional_options: '',
            ecp_weblogic_jndi_name         : ''
    ]

    def doSetupSpec() {
        setupResource()
        deleteProject(projectName)
        createJMSModule(jmsModuleName)
        createConfig(CONFIG_NAME)

        dslFile "dsl/procedures.dsl", [
                projectName  : projectName,
                procedureName: procedureName,
                resourceName : getResourceName(),
                params       : params,
        ]

        dslFile 'dsl/procedures.dsl', [
                projectName  : projectName,
                procedureName: deleteProcedureName,
                resourceName : getResourceName(),
                params       : [
                        configname                  : configName,
                        ecp_weblogic_jms_module_name: '',
                        ecp_weblogic_jms_queue_name : ''
                ]
        ]
    }

    def doCleanupSpec() {
        // deleteProject(projectName)
    }

    def 'create jms queue'() {
        given:
        def queueName = 'SpecQueue'
        def jndiName = 'TestJNDIName'
        deleteJMSQueue(jmsModuleName, queueName)
        when:
        def result = runProcedure("""
        runProcedure(
            projectName: '$projectName',
            procedureName: '$procedureName',
            actualParameter: [
                ecp_weblogic_jms_module_name: '$jmsModuleName',
                ecp_weblogic_jndi_name: '$jndiName',
                ecp_weblogic_jms_queue_name: '$queueName',
            ]
        )
        """, getResourceName())
        then:
        logger.debug(result.logs)
        assert result.outcome == 'success'
        assert result.logs =~ /Queue $queueName does not exist/
        assert result.logs =~ /Created Queue $queueName/
        def queue = getQueue(jmsModuleName, queueName)
        println queue
        assert queue.jndiName == jndiName
        assert queue.subdeploymentName == queueName
        // TODO subdeployment name
        cleanup:
        deleteJMSQueue(jmsModuleName, queueName)
    }

    def 'create jms queue with additional options'() {
        given:
        def queueName = randomize('SpecQueue')
        def jndiName = 'TestJNDIName'
        deleteJMSQueue(jmsModuleName, queueName)
        when:
        def result = runProcedure("""
        runProcedure(
            projectName: '$projectName',
            procedureName: '$procedureName',
            actualParameter: [
                ecp_weblogic_jms_module_name: '$jmsModuleName',
                ecp_weblogic_jndi_name: '$jndiName',
                ecp_weblogic_jms_queue_name: '$queueName',
                ecp_weblogic_additional_options: 'MaximumMessageSize=1024'
            ]
        )
        """, getResourceName())
        then:
        logger.debug(result.logs)
        assert result.outcome == 'success'
        assert result.logs =~ /Queue $queueName does not exist/
        assert result.logs =~ /Created Queue $queueName/
        cleanup:
        deleteJMSQueue(jmsModuleName, queueName)
    }

    def 'update jms queue'() {
        given:
        def queueName = 'SpecQueue'
        def oldJNDIName = 'TestJNDIName'
        def newJNDIName = 'NewJNDIName'
        deleteJMSQueue(jmsModuleName, queueName)
        def result = runProcedure("""
        runProcedure(
            projectName: '$projectName',
            procedureName: '$procedureName',
            actualParameter: [
                ecp_weblogic_jms_module_name: '$jmsModuleName',
                ecp_weblogic_jndi_name: '$oldJNDIName',
                ecp_weblogic_jms_queue_name: '$queueName',
            ]
        )
        """, getResourceName())
        when:
        result = runProcedure """
        runProcedure(
            projectName: '$projectName',
            procedureName: '$procedureName',
            actualParameter: [
                ecp_weblogic_jms_module_name: '$jmsModuleName',
                ecp_weblogic_jndi_name: '$newJNDIName',
                ecp_weblogic_jms_queue_name: '$queueName',
                ecp_weblogic_update_action: 'selective_update'
            ]
        )
        """, getResourceName()
        then:
        logger.info(result.logs)
        assert result.outcome == 'success'
        assert result.logs =~ /Set JNDI Name $newJNDIName/
        cleanup:
        deleteJMSQueue(jmsModuleName, queueName)
    }

    def 'recreate jms queue'() {
        given:
        def queueName = 'SpecQueue'
        def oldJNDIName = 'TestJNDIName'
        def newJNDIName = 'NewJNDIName'
        deleteJMSQueue(jmsModuleName, queueName)
        def result = runProcedure("""
        runProcedure(
            projectName: '$projectName',
            procedureName: '$procedureName',
            actualParameter: [
                ecp_weblogic_jms_module_name: '$jmsModuleName',
                ecp_weblogic_jndi_name: '$oldJNDIName',
                ecp_weblogic_jms_queue_name: '$queueName',
            ]
        )
        """, getResourceName())
        def jmsServerName = 'TestJMSServer'
        createJMSServer(jmsServerName)
        def subdeploymentName = randomize('TestSubdeployment')
        createSubDeployment(jmsModuleName, subdeploymentName, jmsServerName)
        when:
        result = runProcedure """
        runProcedure(
            projectName: '$projectName',
            procedureName: '$procedureName',
            actualParameter: [
                ecp_weblogic_jms_module_name: '$jmsModuleName',
                ecp_weblogic_jndi_name: '$newJNDIName',
                ecp_weblogic_jms_queue_name: '$queueName',
                ecp_weblogic_update_action: 'remove_and_create',
                ecp_weblogic_subdeployment_name: '$subdeploymentName',
            ]
        )
        """, getResourceName()
        then:
        logger.info(result.logs)
        assert result.outcome == 'success'
        cleanup:
        deleteJMSQueue(jmsModuleName, queueName)
        deleteSubDeployment(jmsModuleName, subdeploymentName)
    }

    def 'delete jms queue'() {
        given:
        def queueName = randomize('SpecQueue')
        def result = runProcedure("""
        runProcedure(
            projectName: '$projectName',
            procedureName: '$procedureName',
            actualParameter: [
                ecp_weblogic_jms_module_name: '$jmsModuleName',
                ecp_weblogic_jms_queue_name: '$queueName',
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
                    ecp_weblogic_jms_queue_name: '$queueName'
                ]
            )
        """, getResourceName()
        then:
        assert result.outcome == 'success'
        logger.info(result.logs)
        assert result.logs =~ /Removed JMS Queue $queueName from the module $jmsModuleName/
    }

    def "delete non-existing queue"() {
        given:
        def queueName = randomize('SpecQueue')
        deleteJMSQueue(jmsModuleName, queueName)
        when:
        def result = runProcedure """
            runProcedure(
                projectName: '$projectName',
                procedureName: '$deleteProcedureName',
                actualParameter: [
                    ecp_weblogic_jms_module_name: '$jmsModuleName',
                    ecp_weblogic_jms_queue_name: '$queueName'
                ]
            )
        """, getResourceName()
        then:
        assert result.outcome == 'error'
        assert result.logs =~ /JMS Queue $queueName does not exist in the module $jmsModuleName/
    }

    def deleteJMSQueue(moduleName, name) {
        def code = """
def getJMSSystemResourcePath(jms_module_name):
    return "/JMSSystemResources/%s"%(jms_module_name)

def getJMSModulePath(jms_module_name):
    return "%s/JMSResource/%s"%(getJMSSystemResourcePath(jms_module_name),jms_module_name)

def getQueuePath(jms_module_name,queue_name):
    return "/JMSSystemResources/%s/JMSResource/%s/Queues/%s" % (jms_module_name, jms_module_name, queue_name)

def deleteQueue(jmsModuleName, cfName):
    bean = getMBean('%s/Queues/' % getJMSModulePath(jmsModuleName))
    queueBean = getMBean(getQueuePath(jmsModuleName, cfName))
    if queueBean != None:
        bean.destroyQueue(queueBean)
        print("Removed Queue %s from the module %s" % (cfName, jmsModuleName))
    else:
        print("Queue %s does not exist in the module %s" % (cfName, jmsModuleName))


moduleName = '$moduleName'
queueName = '$name'

connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')
edit()
startEdit()
try:
    deleteQueue(moduleName, queueName)
    activate()
except Exception, e:
    stopEdit('y')

"""
        def result = runWLST(code)
        assert result.outcome == 'success'
    }

}
