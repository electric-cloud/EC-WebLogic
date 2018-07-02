def projName = args.projectName
def procName = args.procedureName
def configname = args.configname
def moduleName = args.moduleName
def resName = args.resourceName
assert moduleName
def queueName = args.queueName
assert queueName
def jndiName = 'com.test.retargetJNDI'

project projName, {
    procedure procName, {
        resourceName = resName
        step 'Ensure Module', {
            subprocedure = 'CreateOrUpdateJMSModule'
            subproject = '/plugins/EC-WebLogic/project'

            actualParameter 'configname', configname
            actualParameter 'ecp_weblogic_jms_module_name', moduleName
            actualParameter 'ecp_weblogic_target_list', 'AdminServer'
            actualParameter 'ecp_weblogic_update_action', 'do_nothing'
        }

        step 'Ensure First JMS Server', {
            subprocedure = 'CreateOrUpdateJMSServer'
            subproject = '/plugins/EC-WebLogic/project'

            actualParameter 'configname', configname
            actualParameter 'ecp_weblogic_jms_server_name', 'jmsServer1'
            actualParameter 'ecp_weblogic_target', 'AdminServer'
            actualParameter 'ecp_weblogic_update_action', 'do_nothing'
        }

        step 'Ensure Second JMS Server', {
            subprocedure = 'CreateOrUpdateJMSServer'
            subproject = '/plugins/EC-WebLogic/project'

            actualParameter 'configname', configname
            actualParameter 'ecp_weblogic_jms_server_name', 'jmsServer2'
            actualParameter 'ecp_weblogic_target', 'AdminServer'
            actualParameter 'ecp_weblogic_update_action', 'do_nothing'
        }

        step 'Create JMS Queue', {
            subprocedure = 'CreateOrUpdateJMSQueue'
            subproject = '/plugins/EC-WebLogic/project'

            actualParameter 'configname', configname
            actualParameter 'ecp_weblogic_additional_options', ''
            actualParameter 'ecp_weblogic_jms_module_name', moduleName
            actualParameter 'ecp_weblogic_jms_queue_name', queueName
            actualParameter 'ecp_weblogic_jndi_name', jndiName
            actualParameter 'ecp_weblogic_subdeployment_name', queueName
            actualParameter 'ecp_weblogic_target_jms_server', 'jmsServer1'
            actualParameter 'ecp_weblogic_update_action', 'remove_and_create'
        }

        step 'Update JMS Queue', {
            subprocedure = 'CreateOrUpdateJMSQueue'
            subproject = '/plugins/EC-WebLogic/project'

            actualParameter 'configname', configname
            actualParameter 'ecp_weblogic_additional_options', ''
            actualParameter 'ecp_weblogic_jms_module_name', moduleName
            actualParameter 'ecp_weblogic_jms_queue_name', queueName
            actualParameter 'ecp_weblogic_jndi_name', jndiName
            actualParameter 'ecp_weblogic_subdeployment_name', queueName
            actualParameter 'ecp_weblogic_target_jms_server', 'jmsServer2'
            actualParameter 'ecp_weblogic_update_action', 'remove_and_create'
        }

        formalParameter 'test', {
            type = 'entry'
        }
    }
}
