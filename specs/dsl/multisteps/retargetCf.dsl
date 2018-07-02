def projName = args.projectName
def procName = args.procedureName
def configname = args.configname
def moduleName = args.moduleName
def resName = args.resourceName
def wlst = args.wlst
assert moduleName
def cfName = args.cfName
assert cfName
def jndiName = 'com.test.retargetCF'

project projName, {
    procedure procName, {
        resourceName = resName

        step 'Ensure Managed Server', {
            subprocedure = 'createManagedServer'
            subproject = '/plugins/EC-WebLogic/project'

            actualParameter 'configname', configname
            actualParameter 'listen_address', ''
            actualParameter 'listen_port', ''
            actualParameter 'server_name', 'ManagedServer'
            actualParameter 'wlst_abs_path', wlst

            errorHandling = 'ignore'
        }

        step 'Ensure Module', {
            subprocedure = 'CreateOrUpdateJMSModule'
            subproject = '/plugins/EC-WebLogic/project'

            actualParameter 'configname', configname
            actualParameter 'ecp_weblogic_jms_module_name', moduleName
            actualParameter 'ecp_weblogic_target_list', 'AdminServer, ManagedServer'
            actualParameter 'ecp_weblogic_update_action', 'do_nothing'
        }


        step 'Create ConnectionFactory', {
            subprocedure = 'CreateOrUpdateConnectionFactory'
            subproject = '/plugins/EC-WebLogic/project'

            actualParameter 'additional_options', ''
            actualParameter 'cf_client_id_policy', 'restricted'
            actualParameter 'cf_max_messages_per_session', ''
            actualParameter 'cf_name', cfName
            actualParameter 'cf_sharing_policy', 'exclusive'
            actualParameter 'cf_xa_enabled', '1'
            actualParameter 'configname', configname
            actualParameter 'jms_module_name', moduleName
            actualParameter 'jms_server_list', ''
            actualParameter 'jndi_name', jndiName
            actualParameter 'subdeployment_name', cfName
            actualParameter 'update_action', 'remove_and_create'
            actualParameter 'wls_instance_list', 'AdminServer'
        }

        step 'Recreate ConnectionFactory', {
            subprocedure = 'CreateOrUpdateConnectionFactory'
            subproject = '/plugins/EC-WebLogic/project'

            actualParameter 'additional_options', ''
            actualParameter 'cf_client_id_policy', 'restricted'
            actualParameter 'cf_max_messages_per_session', ''
            actualParameter 'cf_name', cfName
            actualParameter 'cf_sharing_policy', 'exclusive'
            actualParameter 'cf_xa_enabled', '1'
            actualParameter 'configname', configname
            actualParameter 'jms_module_name', moduleName
            actualParameter 'jms_server_list', ''
            actualParameter 'jndi_name', jndiName
            actualParameter 'subdeployment_name', cfName
            actualParameter 'update_action', 'remove_and_create'
            actualParameter 'wls_instance_list', 'ManagedServer'
        }

    }
}
