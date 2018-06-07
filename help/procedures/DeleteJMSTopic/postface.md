<img src="../../plugins/EC-WebLogic/images/DeleteJMSTopic/Form.png" />

## Output

After the job runs, you can view the results on the Job Details page in ElectricFlow. The JMS Queue was deleted.

<img src="../../plugins/EC-WebLogic/images/DeleteJMSTopic/Summary.png" />

In the <b>DeleteJMSTopic</b> step, click the Log button to see the diagnostic information:

    Using plugin EC-WebLogic-3.3.0.0
    Got parameter "configname" with value "demo"
    Got parameter "ecp_weblogic_jms_module_name" with value "TestModule"
    Got parameter "ecp_weblogic_jms_topic_name" with value "SampleTopic"
    Processing template /myProject/jython/preamble.jython
    Processing template /myProject/jython/delete_jms_topic.jython
    Script path: /opt/electriccloud/electriccommander/workspace/job_25641_20180607054339/exec_788152218826341.jython
    Running command: '/u01/oracle/oracle_common/common/bin/wlst.sh' '/opt/electriccloud/electriccommander/workspace/job_25641_20180607054339/exec_788152218826341.jython'
    Unlinking file /opt/electriccloud/electriccommander/workspace/job_25641_20180607054339/exec_788152218826341.jython
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
    [WLST INFO] Removed JMS Topic SampleTopic from the module TestModule
    Saving all your changes ...
    Saved all your changes successfully.
    Activating all your changes, this may take a while ...
    The edit lock associated with this edit session is released once the activation is completed.
    Activation completed
    STDERR:
    DONE
