project args.projectName, {
    resourceName = args.resourceName
    credential 'ds', {
        userName = 'weblogic'
        password = 'weblogic'
    }
    procedure 'PrepareDeploy', {
        description = ''
        jobNameTemplate = ''
        resourceName = ''
        timeLimit = ''
        timeLimitUnits = 'minutes'
        workspaceName = ''

        step 'Retrieve', {
            description = ''
            alwaysRun = '0'
            broadcast = '0'
            command = null
            condition = ''
            errorHandling = 'failProcedure'
            exclusiveMode = 'none'
            logFileName = null
            parallel = '0'
            postProcessor = null
            precondition = ''
            releaseMode = 'none'
            resourceName = ''
            shell = null
            subprocedure = 'Retrieve'
            subproject = '/plugins/EC-Artifact/project'
            timeLimit = ''
            timeLimitUnits = 'minutes'
            workingDirectory = null
            workspaceName = ''
            actualParameter 'artifactName', args.artifactName
            actualParameter 'artifactVersionLocationProperty', '/myJob/retrievedArtifactVersions/$[assignedResourceName]'
            actualParameter 'filterList', ''
            actualParameter 'overwrite', 'update'
            actualParameter 'retrieveToDirectory', 'deploy'
            actualParameter 'versionRange', ''
        }

        step 'DeployApp', {
            description = ''
            alwaysRun = '0'
            broadcast = '0'
            command = null
            condition = ''
            errorHandling = 'failProcedure'
            exclusiveMode = 'none'
            logFileName = null
            parallel = '0'
            postProcessor = null
            precondition = ''
            releaseMode = 'none'
            resourceName = ''
            shell = null
            subprocedure = 'DeployApp'
            subproject = '/plugins/EC-WebLogic/project'
            timeLimit = ''
            timeLimitUnits = 'minutes'
            workingDirectory = null
            workspaceName = ''
            actualParameter 'additional_options', ''
            actualParameter 'appname', args.appName
            actualParameter 'apppath', 'deploy/jms-sample.war'
            actualParameter 'archive_version', ''
            actualParameter 'configname', args.config
            actualParameter 'deployment_plan', ''
            actualParameter 'is_library', '0'
            actualParameter 'overwrite_deployment_plan', '0'
            actualParameter 'plan_path', ''
            actualParameter 'plan_version', ''
            actualParameter 'remote', '0'
            actualParameter 'retire_gracefully', '0'
            actualParameter 'retire_timeout', ''
            actualParameter 'stage_mode', ''
            actualParameter 'targets', 'AdminServer'
            actualParameter 'upload', '0'
            actualParameter 'version_identifier', ''
            actualParameter 'wlstabspath', args.wlst
        }

        step 'CreateJMSModule', {
            description = ''
            alwaysRun = '0'
            broadcast = '0'
            command = null
            condition = ''
            errorHandling = 'failProcedure'
            exclusiveMode = 'none'
            logFileName = null
            parallel = '0'
            postProcessor = null
            precondition = ''
            releaseMode = 'none'
            resourceName = ''
            shell = null
            subprocedure = 'CreateOrUpdateJMSModule'
            subproject = '/plugins/EC-WebLogic/project'
            timeLimit = ''
            timeLimitUnits = 'minutes'
            workingDirectory = null
            workspaceName = ''
            actualParameter 'configname', args.config
            actualParameter 'ecp_weblogic_jms_module_name', 'sample-module'
            actualParameter 'ecp_weblogic_target_list', 'AdminServer'
            actualParameter 'ecp_weblogic_update_action', 'do_nothing'
        }

        step 'CreateDatasource', {
            description = ''
            alwaysRun = '0'
            broadcast = '0'
            command = null
            condition = ''
            errorHandling = 'failProcedure'
            exclusiveMode = 'none'
            logFileName = null
            parallel = '0'
            postProcessor = null
            precondition = ''
            releaseMode = 'none'
            resourceName = ''
            shell = null
            subprocedure = 'CreateOrUpdateDatasource'
            subproject = '/plugins/EC-WebLogic/project'
            timeLimit = ''
            timeLimitUnits = 'minutes'
            workingDirectory = null
            workspaceName = ''
            actualParameter 'configname', args.config
            actualParameter 'ecp_weblogic_additionalOptions', ''
            actualParameter 'ecp_weblogic_databaseName', 'examples;create=true'
            actualParameter 'ecp_weblogic_databaseUrl', 'jdbc:derby://' + args.derbyHost + ':1527/examples;create=true'
            actualParameter 'ecp_weblogic_dataSourceCredentials', 'ds'
            actualParameter 'ecp_weblogic_dataSourceDriverClass', 'org.apache.derby.jdbc.ClientDriver'
            actualParameter 'ecp_weblogic_dataSourceName', 'sample-ds'
            actualParameter 'ecp_weblogic_driverProperties', ''
            actualParameter 'ecp_weblogic_jndiName', 'com.weblogic.ds.jndi'
            actualParameter 'ecp_weblogic_targets', 'AdminServer'
            actualParameter 'ecp_weblogic_updateAction', 'do_nothing'
        }

        step 'CreateJMSServer', {
            description = ''
            alwaysRun = '0'
            broadcast = '0'
            command = null
            condition = ''
            errorHandling = 'failProcedure'
            exclusiveMode = 'none'
            logFileName = null
            parallel = '0'
            postProcessor = null
            precondition = ''
            releaseMode = 'none'
            resourceName = ''
            shell = null
            subprocedure = 'CreateOrUpdateJMSServer'
            subproject = '/plugins/EC-WebLogic/project'
            timeLimit = ''
            timeLimitUnits = 'minutes'
            workingDirectory = null
            workspaceName = ''
            actualParameter 'configname', args.config
            actualParameter 'ecp_weblogic_jms_server_name', 'sample-jms-server'
            actualParameter 'ecp_weblogic_target', 'AdminServer'
            actualParameter 'ecp_weblogic_update_action', 'do_nothing'
        }

        step 'CreateQueue', {
            description = ''
            alwaysRun = '0'
            broadcast = '0'
            command = null
            condition = ''
            errorHandling = 'failProcedure'
            exclusiveMode = 'none'
            logFileName = null
            parallel = '0'
            postProcessor = null
            precondition = ''
            releaseMode = 'none'
            resourceName = ''
            shell = null
            subprocedure = 'CreateOrUpdateJMSQueue'
            subproject = '/plugins/EC-WebLogic/project'
            timeLimit = ''
            timeLimitUnits = 'minutes'
            workingDirectory = null
            workspaceName = ''
            actualParameter 'configname', args.config
            actualParameter 'ecp_weblogic_additional_options', ''
            actualParameter 'ecp_weblogic_jms_module_name', 'sample-module'
            actualParameter 'ecp_weblogic_jms_queue_name', 'sample-queue'
            actualParameter 'ecp_weblogic_jndi_name', 'com.weblgoic.jndiqueue'
            actualParameter 'ecp_weblogic_subdeployment_name', 'sample-queue-subd'
            actualParameter 'ecp_weblogic_target_jms_server', 'sample-jms-server'
            actualParameter 'ecp_weblogic_update_action', 'do_nothing'
        }

        attachCredential credentialName:'ds',
            projectName: args.projectName,
            procedureName: 'PrepareDeploy',
            stepName: 'CreateDatasource'
    }

}