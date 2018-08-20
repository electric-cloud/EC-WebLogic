<img src="../../plugins/EC-WebLogic/images/CreateOrUpdateDatasource/Form.png" />

## Output

After the job runs, you can view the results on the Job Details page in ElectricFlow. The Datasource was created successfully.

<img src="../../plugins/EC-WebLogic/images/CreateOrUpdateDatasource/Summary.png" />

In the <b>CreateOrUpdateDatasource</b> step, click the Log button to see the diagnostic information:

    Using plugin EC-WebLogic-3.4.0.0
    Got parameter "configname" with value "EC-Specs WebLogic Config"
    Got parameter "ecp_weblogic_additionalOptions" with value ""
    Got parameter "ecp_weblogic_databaseName" with value "medrec;create=true"
    Got parameter "ecp_weblogic_databaseUrl" with value "jdbc:derby://localhost:1527/medrec;ServerName=localhost;databaseName=medrec;create=true"
    Got parameter "ecp_weblogic_dataSourceDriverClass" with value "org.apache.derby.jdbc.ClientXADataSource"
    Got parameter "ecp_weblogic_dataSourceName" with value "SpecDatasource"
    Got parameter "ecp_weblogic_driverProperties" with value ""
    Got parameter "ecp_weblogic_jndiName" with value "datasources.TestJNDIName"
    Got parameter "ecp_weblogic_targets" with value ""
    Got parameter "ecp_weblogic_updateAction" with value ""
    Processing template /myProject/jython/preamble.jython
    Processing template /myProject/jython/create_or_update_datasource.jython
    Script path: /opt/electriccloud/electriccommander/workspace/job_3180_20180802043311/exec_257768208601721.jython
    Running command: '/u01/oracle/oracle_common/common/bin/wlst.sh' '/opt/electriccloud/electriccommander/workspace/job_3180_20180802043311/exec_257768208601721.jython'
    Unlinking file /opt/electriccloud/electriccommander/workspace/job_3180_20180802043311/exec_257768208601721.jython
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

    No stack trace available.
    [WLST INFO] Datasource SpecDatasource does not exist
    Location changed to edit tree.
    This is a writable tree with DomainMBean as the root.
    To make changes you will need to start an edit session via startEdit().
    For more help, use help('edit').
    You already have an edit session in progress and hence WLST will
    continue with your edit session.

    Starting an edit session ...
    Started edit session, be sure to save and activate your changes once you are done.
    WARNING:No targets are provided, the datasource will not be deployed
    Saving all your changes ...
    Saved all your changes successfully.
    Activating all your changes, this may take a while ...
    The edit lock associated with this edit session is released once the activation is completed.
    Activation completed
    SUMMARY: Created datasource SpecDatasource successfully
    STDERR:
    DONE
