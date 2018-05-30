import spock.lang.*

class CreateOrUpdateConnectionFactory extends WebLogicHelper {
    static def projectName = 'EC-WebLogic Specs CreateOrUpdateConnectionFactory'
    static def jmsModuleName = 'TestJMSModule'
    static def configName = 'EC-Specs WebLogic Config'
    static def procedureName = 'CreateOrUpdateConnectionFactory'
    static def deleteProcedureName = 'DeleteConnectionFactory'

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
        setupResource()
        discardChanges()
        deleteProject(projectName)
        createJMSModule(jmsModuleName)
        createConfig(configName)

        // TODO create resource
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
                cf_name: '',
                jms_module_name: ''
            ]
        ]
    }

    def doCleanupSpec() {
        deleteProject(projectName)
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
        cleanup:
        deleteConnectionFactory(jmsModuleName, cfName)
    }

    def 'recreate'() {
        given:
        def cfName = 'SpecUpdatedCF'
        deleteConnectionFactory(jmsModuleName, cfName)
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
        def jmsServerName = 'jmsServer1'
        createJMSServer(jmsServerName)
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
                update_action: 'remove_and_create',
                subdeployment_name: 'Sub1',
                jms_server_name: '$jmsServerName'
            ]
        )
        """, getResourceName()
        then:
        logger.debug(result.logs)
        assert result.outcome == 'success'
        cleanup:
        deleteConnectionFactory(jmsModuleName, cfName)

    }

    def "selective update"() {
        given:
        def cfName = 'SpecUpdatedCF'
        deleteConnectionFactory(jmsModuleName, cfName)
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
        def jmsServerName = 'jmsServer1'
        createJMSServer(jmsServerName)
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
                update_action: 'selective_update',
                subdeployment_name: 'Sub1',
                jms_server_name: '$jmsServerName'
            ]
        )
        """, getResourceName()
        then:
        logger.info(result.logs)
        assert result.outcome == 'success'
        assert result.logs =~ /Found Connection Factory $cfName in the module $jmsModuleName/
        assert result.logs =~ /Set JNDI Name to $newJNDI/
        def properties = getJobProperties(result.jobId)
        assert properties.WebLogicServerRestartRequired == 'true'
        cleanup:
        deleteConnectionFactory(jmsModuleName, cfName)
    }

    def "delete connection factory"() {
        given:
        def cfName = 'SpecUpdatedCF'
        def result = runProcedure """
        runProcedure(
            projectName: '$projectName',
            procedureName: '$procedureName',
            actualParameter: [
                cf_name: '$cfName',
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
            procedureName: '$deleteProcedureName',
            actualParameter: [
                cf_name: '$cfName',
                jms_module_name: '$jmsModuleName'
            ]
        )
        """, getResourceName()
        then:
        assert result.outcome == 'success'
        assert result.logs =~ /Removed Connection Factory $cfName from the module $jmsModuleName/
    }

    def "delete non-existing connection factory"() {
        given:
        def cfName = 'NoSuchCF'
        when:
        def result = runProcedure """
        runProcedure(
            projectName: '$projectName',
            procedureName: '$deleteProcedureName',
            actualParameter: [
                cf_name: '$cfName',
                jms_module_name: '$jmsModuleName'
            ]
        )
        """, getResourceName()
        then:
        assert result.outcome == 'error'
        assert result.logs =~ /Connection Factory $cfName does not exist in the module $jmsModuleName/
    }

    def "delete non-existing connection factory from non-existing jms module"() {
        given:
        def cfName = 'NoSuchCF'
        def jmsModule = 'NoSuhModule'
        when:
        def result = runProcedure """
        runProcedure(
            projectName: '$projectName',
            procedureName: '$deleteProcedureName',
            actualParameter: [
                cf_name: '$cfName',
                jms_module_name: '$jmsModule'
            ]
        )
        """, getResourceName()
        then:
        assert result.outcome == 'error'
        assert result.logs =~ /Connection Factory $cfName does not exist in the module $jmsModule/
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
print "PROPERTY: %s" % get(propName)
"""
        def result = runWLST(code)
        assert result.outcome == 'success'
        // TODO retrieve property
        result
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
}
