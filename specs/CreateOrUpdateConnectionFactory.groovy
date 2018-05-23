class CreateOrUpdateConnectionFactory extends WebLogicHelper {
    static def projectName = 'EC-WebLogic Specs CreateOrUpdateConnectionFactory'
    static def jmsModuleName = 'TestJMSModule'
    static def configName = 'EC-Specs WebLogic Config'
    static def procedureName = 'CreateOrUpdateConnectionFactory'
    static def params = [
            configname                 : configName,
            cf_name                    : '',
            jndi_name                  : '',
            cf_sharing_policy          : '',
            cf_client_id_policy        : '',
            cf_max_messages_per_session: '',
            cf_xa_enabled              : '',
            cf_attach_jmsx_userid      : '',
            jms_module_name            : '',
            subdeployment_name         : '',
            jms_server_name            : '',
            server_name                : '',
    ]

    def doSetupSpec() {
        setupResource()
        createConfig(configName)
        createJMSModule(jmsModuleName)
        dslFile "dsl/procedures.dsl", [
                projectName  : projectName,
                procedureName: procedureName,
                resourceName : getResourceName(),
                params       : params
        ]
    }

    def doCleanupSpec() {
        deleteProject(projectName)
    }

    def 'create connection factory'() {
        given:
        def cfName = 'SpecConnectionFactory'
        def jndiName = 'TestJNDIName'
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
                cf_client_id_policy: 'Restricted'
            ]
        )
        """, getResourceName())
        then:
        logger.debug(result.logs)
        assert result.outcome == 'success'
    }

    static def createJMSModule(name) {
        def code = """
resource_name = '$name'
target = 'AdminServer'
cd('/')
if cmo.lookupJMSSystemResource(resource_name):
    print "Resource %s alreay exists" % resource_name
else:
    startEdit()
    cmo.createJMSSystemResource(resource_name)
    cd("/JMSSystemResources/%s" % resource_name)
    cmo.addTarget(getMBean("/Servers/%s" % target))
    # cmo.createSubDeployment(’subdeployment0′)
    activate()
"""
        code
    }
}
