import spock.lang.*

class CreateOrUpdateConnectionFactory extends WebLogicHelper {
    static def projectName = 'EC-WebLogic Specs CreateOrUpdateConnectionFactory'
    static def jmsModuleName = 'TestJMSModule'
    static def configName = 'EC-Specs WebLogic Config'
    static def procedureName = 'CreateOrUpdateConnectionFactory'
    static def params = [
        configname: configName,
        cf_name: '',
        jndi_name: '',
        cf_sharing_policy: '',
        cf_client_id_policy: '',
        cf_max_messages_per_session: '',
        cf_xa_enabled: '',
        jms_module_name: '',
        subdeployment_name: '',
        jms_server_name: '',
        update_action: 'do_nothing',
        additional_options: ''
    ]

    def doSetupSpec() {
        createJMSModule(jmsModuleName)
        createConfig(configName)
        dslFile "dsl/procedures.dsl", [
            projectName: projectName,
            procedureName: procedureName,
            resourceName: getResourceName(),
            params: params
        ]
    }

    def doCleanupSpec() {
        dsl """
        deleteProject(projectName: '$projectName')
        """
    }

    def 'create connection factory'() {
        given:
        def cfName = 'SpecConnectionFactory'
        def jndiName = 'TestJNDIName'
        deleteConnectionFactory(jmsModuleName, cfName)
        when:
        def result = runProcedure("""
        runProcedure(
            projectName: '$projectName',
            procedureName: '$procedureName',
            actualParameter: [
                cf_name: '$cfName',
                jndi_name: '$jndiName',
                jms_module_name: '$jmsModuleName',
                cf_sharing_policy: 'Exclusive',
                cf_client_id_policy: 'Restricted',
            ]
        )
        """, getResourceName())
        then:
        logger.debug(result.logs)
        assert result.outcome == 'success'
        assert result.logs =~ /Connection Factory $cfName does not exist/
        assert result.logs =~ /Created Connection Factory $cfName/
        cleanup:
        deleteConnectionFactory(jmsModuleName, cfName)
    }

    @IgnoreRest
    def 'with additional options'() {
        given:
        def cfName = 'SpecConnectionFactory'
        def jndiName = 'TestJNDIName'
        deleteConnectionFactory(jmsModuleName, cfName)
        when:
        def result = runProcedure("""
        runProcedure(
            projectName: '$projectName',
            procedureName: '$procedureName',
            actualParameter: [
                cf_name: '$cfName',
                jndi_name: '$jndiName',
                jms_module_name: '$jmsModuleName',
                cf_sharing_policy: 'sharable',
                cf_client_id_policy: 'Restricted',
                additional_options: 'DefaultDeliveryParams.DefaultPriority=5'
            ]
        )
        """, getResourceName())
        then:
        logger.debug(result.logs)
        assert result.outcome == 'success'
        assert result.logs =~ /Connection Factory $cfName does not exist/
        assert result.logs =~ /Created Connection Factory $cfName/
        def prop = getConnectionFactoryProperty(jmsModuleName, cfName, 'DefaultDeliveryParams', 'DefaultPriority')
        println prop.logs
        // cleanup:
        // deleteConnectionFactory(jmsModuleName, cfName)
    }

    def "selective update"() {
        given:
        def cfName = 'SpecUpdatedCF'
        def oldJNDI = 'oldJNDI'
        def newJNDI = 'newJNDI'
        def result = runProcedure """
        runProcedure(
            projectName: '$projectName',
            procedureName: '$procedureName',
            actualParameter: [
                cf_name: '$cfName',
                jndi_name: '$oldJNDI',
                jms_module_name: '$jmsModuleName',
                cf_sharing_policy: 'Exclusive',
                cf_client_id_policy: 'Restricted'
            ]
        )
        """, getResourceName()

        assert result.outcome == 'success'
        when:
        result = runProcedure """
        runProcedure(
            projectName: '$projectName',
            procedureName: '$procedureName',
            actualParameter: [
                cf_name: '$cfName',
                jndi_name: '$newJNDI',
                jms_module_name: '$jmsModuleName',
                cf_sharing_policy: 'Exclusive',
                cf_client_id_policy: 'Restricted',
                update_action: 'selective_update'
            ]
        )
        """, getResourceName()
        then:
        println result.logs
        assert result.outcome == 'success'
        assert result.logs =~ /Found Connection Factory $cfName in the module $jmsModuleName/
        cleanup:
        deleteConnectionFactory(jmsModuleName, cfName)
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


    def deleteConnectionFactory(moduleName, name) {
        def code = """
def getJMSSystemResourcePath(jms_module_name):
    return "/JMSSystemResources/%s"%(jms_module_name)

def getJMSModulePath(jms_module_name):
    return "%s/JMSResource/%s"%(getJMSSystemResourcePath(jms_module_name),jms_module_name)

def getConnectionFactoryPath(jms_module_name,cf_name):
    return "/JMSSystemResources/%s/JMSResource/%s/ConnectionFactories/%s" % (jms_module_name, jms_module_name, cf_name)

def deleteConnectionFactory(jmsModuleName, cfName):
    bean = getMBean('%s/ConnectionFactories/' % getJMSModulePath(jmsModuleName))
    cfBean = getMBean(getConnectionFactoryPath(jmsModuleName, cfName))
    if cfBean != None:
        bean.destroyConnectionFactory(cfBean)
        print("Removed Connection Factory %s from the module %s" % (cfName, jmsModuleName))
    else:
        print("Connection Factory %s does not exist in the module %s" % (cfName, jmsModuleName))


moduleName = '$moduleName'
cfName = '$name'

connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')
edit()
startEdit()
try:
    deleteConnectionFactory(moduleName, cfName)
    activate()
except Exception, e:
    stopEdit('y')

"""
        def result = runWLST(code)
        assert result.outcome == 'success'
    }


    def getConnectionFactoryProperty(module, cfName, group, propName) {
        def code = """
def getConnectionFactoryPath(jms_module_name,cf_name):
    return "/JMSSystemResources/%s/JMSResource/%s/ConnectionFactories/%s" % (jms_module_name, jms_module_name, cf_name)

module = '$module'
cfName = '$cfName'
group = '$group'
propName = '$propName'
connect('${getUsername()}', '${getPassword()}', '${getEndpoint()}')
cd(getConnectionFactoryPath(module, cfName) + '/' + group + '/' + cfName)
ls()
"""
        def result = runWLST(code)
        assert result.outcome == 'success'
        // TODO retrieve property
        result
    }
}
