<img src="../../plugins/EC-WebLogic/images/CreateOrUpdateConnectionFactory/Form.png" />

## Output

After the job runs, you can view the results on the Job Details page in CloudBees CD. The Connection Factory was created successfully.

<img src="../../plugins/EC-WebLogic/images/CreateOrUpdateConnectionFactory/Summary.png" />

In the <b>CreateOrUpdateConnectionFactory</b> step, click the Log button to see the diagnostic information:

    Using plugin EC-WebLogic-3.3.0.0
    Got parameter "additional_options" with value ""
    Got parameter "cf_client_id_policy" with value "restricted"
    Got parameter "cf_max_messages_per_session" with value "10"
    Got parameter "cf_name" with value "TestCF"
    Got parameter "cf_sharing_policy" with value "exclusive"
    Got parameter "cf_xa_enabled" with value "1"
    Got parameter "configname" with value "demo"
    Got parameter "jms_module_name" with value "TestModule"
    Got parameter "jms_server_list" with value ""
    Got parameter "jndi_name" with value "weblogic.test.CF"
    Got parameter "subdeployment_name" with value ""
    Got parameter "update_action" with value "do_nothing"
    Got parameter "wls_instance_list" with value "AdminServer"
    Processing template /myProject/jython/preamble.jython
    Processing template /myProject/jython/create_or_update_connection_factory.jython
    Script path: /opt/electriccloud/electriccommander/workspace/job_25636_20180607052153/exec_108308602360286.jython
    Running command: '/u01/oracle/oracle_common/common/bin/wlst.sh' '/opt/electriccloud/electriccommander/workspace/job_25636_20180607052153/exec_108308602360286.jython'
    Unlinking file /opt/electriccloud/electriccommander/workspace/job_25636_20180607052153/exec_108308602360286.jython
    EXIT_CODE: 0
    STDOUT:
    Initializing WebLogic Scripting Tool (WLST) ...

    Welcome to WebLogic Server Administration Scripting Shell

    Type help() for help on available commands

    WebLogic version is: WebLogic Server 12.2.1.3.0
    Connecting to t3://localhost:7001 with userid weblogic ...
    Successfully connected to Admin Server "AdminServer" that belongs to domain "base_domain".

    Warning: An insecure protocol was used to connect to the server.
    To ensure on-the-wire security, the SSL port or Admin port should be used instead.

    Location changed to edit tree.
    This is a writable tree with DomainMBean as the root.
    To make changes you will need to start an edit session via startEdit().
    For more help, use help('edit').

    Starting an edit session ...
    Started edit session, be sure to save and activate your changes once you are done.
    No stack trace available.
    [WLST INFO] Connection Factory TestCF does not exist
    [WLST INFO] Created Connection Factory TestCF
    [WLST INFO] Set JNDI Name to weblogic.test.CF
    [WLST INFO] Set default targeting
    [WLST INFO] Options:
    [WLST INFO] Additional Options: {}

    All changes that are made but not yet activated are:

    MBean Changed : com.bea:Name=TestModule,Type=weblogic.j2ee.descriptor.wl.JMSBean,Parent=[base_domain]/JMSSystemResources[TestModule],Path=JMSResource
    Operation Invoked : create
    Attribute Modified : ConnectionFactories
    Attributes Old Value : null
    Attributes New Value : TestCF
    Server Restart Required : false

    Validating changes ...
    Validated the changes successfully
    Saving all your changes ...
    Saved all your changes successfully.
    Activating all your changes, this may take a while ...
    The edit lock associated with this edit session is released once the activation is completed.
    Activation completed
    SUMMARY: Created Connection Factory TestCF, Set Default Targeting
    STDERR:
    DONE
