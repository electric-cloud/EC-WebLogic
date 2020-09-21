
![image](images/DeleteJMSServer/Form.png)


### Output

After the job runs, you can view the results on the Job Details page in CloudBees CD. The JMS Server was deleted.


![image](images/DeleteJMSServer/Summary.png)


In the **DeleteJMSServer** step, click the Log button to see the diagnostic information:

    Using plugin EC-WebLogic-3.3.0.0
    Got parameter "configname" with value "demo"
    Got parameter "ecp_weblogic_jms_server_name" with value "TestServer"
    Processing template /myProject/jython/preamble.jython
    Processing template /myProject/jython/delete_jms_server.jython
    Script path: /opt/electriccloud/electriccommander/workspace/job_25645_20180607055037/exec_330800760610238.jython
    Running command: '/u01/oracle/oracle_common/common/bin/wlst.sh' '/opt/electriccloud/electriccommander/workspace/job_25645_20180607055037/exec_330800760610238.jython'
    Unlinking file /opt/electriccloud/electriccommander/workspace/job_25645_20180607055037/exec_330800760610238.jython
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
    [WLST INFO] Removed JMS Server TestServer
    Saving all your changes ...
    Saved all your changes successfully.
    Activating all your changes, this may take a while ...
    The edit lock associated with this edit session is released once the activation is completed.
    Activation completed
    STDERR:
    DONE
