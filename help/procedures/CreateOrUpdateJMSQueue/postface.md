
![image](images/CreateOrUpdateJMSQueue/Form.png)


### Output

After the job runs, you can view the results on the Job Details page in CloudBees CD. The JMS Queue was created successfully.


![image](images/CreateOrUpdateJMSQueue/Summary.png)


In the **CreateOrUpdateJMSQueue** step, click the Log button to see the diagnostic information:

    Using plugin C-WebLogic-3.3.0.0
    Got parameter "configname" with value "demo"
    Got parameter "ecp_weblogic_additional_options" with value ""
    Got parameter "ecp_weblogic_jms_module_name" with value "TestModule"
    Got parameter "ecp_weblogic_jms_queue_name" with value "SampleQueue"
    Got parameter "ecp_weblogic_jndi_name" with value "weblogic.test.Queue"
    Got parameter "ecp_weblogic_subdeployment_name" with value "SampleQueue"
    Got parameter "ecp_weblogic_target_jms_server" with value "TestServer"
    Got parameter "ecp_weblogic_update_action" with value "do_nothing"
    Processing template /myProject/jython/preamble.jython
    Processing template /myProject/jython/create_or_update_jms_queue.jython
    Script path: /opt/electriccloud/electriccommander/workspace/job_25637_20180607052545/exec_265381669060536.jython
    Running command: '/u01/oracle/oracle_common/common/bin/wlst.sh' '/opt/electriccloud/electriccommander/workspace/job_25637_20180607052545/exec_265381669060536.jython'
    Unlinking file /opt/electriccloud/electriccommander/workspace/job_25637_20180607052545/exec_265381669060536.jython
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
    [WLST INFO] Subdeployment SampleQueue does not exist
    [WLST INFO] Created Subdeployment SampleQueue
    [WLST INFO] Adding JMSServer "TestServer" to the list of targets
    Saving all your changes ...
    Saved all your changes successfully.
    Activating all your changes, this may take a while ...
    The edit lock associated with this edit session is released once the activation is completed.
    Activation completed
    Already in requested Edit Tree

    Starting an edit session ...
    Started edit session, be sure to save and activate your changes once you are done.
    No stack trace available.
    [WLST INFO] JMS Queue SampleQueue does not exist
    [WLST INFO] Created Queue SampleQueue
    [WLST INFO] Set JNDI Name weblogic.test.Queue
    [WLST INFO] Subdeployment has not changed
    [WLST INFO] Options:
    [WLST INFO] Additional Options: {}
    Saving all your changes ...
    Saved all your changes successfully.
    Activating all your changes, this may take a while ...
    The edit lock associated with this edit session is released once the activation is completed.
    Activation completed
    STDERR:
    DONE
