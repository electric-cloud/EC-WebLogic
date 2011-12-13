if ($promoteAction eq "promote") {

   $batch->setProperty("/server/ec_customEditors/pluginStep/WebLogic - Check Page Status",
        {
           description => "Check the status of a page on a given URL.",
           value       => '$[/plugins/@PLUGIN_KEY@-@PLUGIN_VERSION@/project/ui_forms/checkPageStatusForm]'
        }
       );

    $batch->setProperty("/server/ec_customEditors/pluginStep/WebLogic - Check Server Status",

        {
           description => "Check the status of the given server URL.",
           value       => '$[/plugins/@PLUGIN_KEY@-@PLUGIN_VERSION@/project/ui_forms/checkServerStatusForm]'
        }

       );
   
    $batch->setProperty("/server/ec_customEditors/pluginStep/WebLogic - Start Admin Server",

        {
           description => "Starts a WebLogic Admin Server.",
           value       => '$[/plugins/@PLUGIN_KEY@-@PLUGIN_VERSION@/project/ui_forms/startAdminServerForm]'
        }

       );
       
        $batch->setProperty("/server/ec_customEditors/pluginStep/WebLogic - Stop Admin Server",

        {
           description => "Stops a WebLogic admin server.",
           value       => '$[/plugins/@PLUGIN_KEY@-@PLUGIN_VERSION@/project/ui_forms/stopAdminServerForm]'
        }

       );
       
    $batch->setProperty("/server/ec_customEditors/pluginStep/WebLogic - Start Managed Server",

        {
           description => "Starts a WebLogic managed server.",
           value       => '$[/plugins/@PLUGIN_KEY@-@PLUGIN_VERSION@/project/ui_forms/startManagedServerForm]'
        }

       );
       
        $batch->setProperty("/server/ec_customEditors/pluginStep/WebLogic - Stop Managed Server",

        {
           description => "Stops a WebLogic managed server.",
           value       => '$[/plugins/@PLUGIN_KEY@-@PLUGIN_VERSION@/project/ui_forms/stopManagedServerForm]'
        }

       );
       
    $batch->setProperty("/server/ec_customEditors/pluginStep/WebLogic - Run Deployer",

        {
           description => "Runs weblogic.Deployer in a free-mode.",
           value       => '$[/plugins/@PLUGIN_KEY@-@PLUGIN_VERSION@/project/ui_forms/runDeployerForm]'
        }

       );
       
    $batch->setProperty("/server/ec_customEditors/pluginStep/WebLogic - Run WLST",

        {
           description => "Runs Jython scripts using weblogic.WLST.",
           value       => '$[/plugins/@PLUGIN_KEY@-@PLUGIN_VERSION@/project/ui_forms/runWlstForm]'
        }

       );
       
       $batch->setProperty("/server/ec_customEditors/pluginStep/WebLogic - Start App",

        {
           description => "Starts an application.",
           value       => '$[/plugins/@PLUGIN_KEY@-@PLUGIN_VERSION@/project/ui_forms/startAppForm]'
        }

       );
       
       $batch->setProperty("/server/ec_customEditors/pluginStep/WebLogic - Stop App",

        {
           description => "Stops an application.",
           value       => '$[/plugins/@PLUGIN_KEY@-@PLUGIN_VERSION@/project/ui_forms/stopAppForm]'
        }

       );
       
       $batch->setProperty("/server/ec_customEditors/pluginStep/WebLogic - Deploy App",

        {
           description => "Deploys or redeploys an application or module.",
           value       => '$[/plugins/@PLUGIN_KEY@-@PLUGIN_VERSION@/project/ui_forms/deployAppForm]'
        }

       );
       
       $batch->setProperty("/server/ec_customEditors/pluginStep/WebLogic - Undeploy App",

        {
           description => "Stops the deployment unit and removes staged files from target servers.",
           value       => '$[/plugins/@PLUGIN_KEY@-@PLUGIN_VERSION@/project/ui_forms/undeployAppForm]'
        }

       );

} elsif ($promoteAction eq "demote") {

}

if ($upgradeAction eq "upgrade") {
    my $query = $commander->newBatch();
    my $newcfg = $query->getProperty(
        "/plugins/$pluginName/project/weblogic_cfgs");
    my $oldcfgs = $query->getProperty(
        "/plugins/$otherPluginName/project/weblogic_cfgs");
	my $creds = $query->getCredentials(
        "\$[/plugins/$otherPluginName]");

	local $self->{abortOnError} = 0;
    $query->submit();

    # if new plugin does not already have cfgs
    if ($query->findvalue($newcfg,"code") eq "NoSuchProperty") {
        # if old cfg has some cfgs to copy
        if ($query->findvalue($oldcfgs,"code") ne "NoSuchProperty") {
            $batch->clone({
                path => "/plugins/$otherPluginName/project/weblogic_cfgs",
                cloneName => "/plugins/$pluginName/project/weblogic_cfgs"
            });
        }
    }
	
	# Copy configuration credentials and attach them to the appropriate steps
    my $nodes = $query->find($creds);
    if ($nodes) {
        my @nodes = $query->{xpath}->findnodes("credential/credentialName", $nodes);
        for (@nodes) {
            my $cred = $_->string_value;

            # Clone the credential
            $batch->clone({
                path => "/plugins/$otherPluginName/project/credentials/$cred",
                cloneName => "/plugins/$pluginName/project/credentials/$cred"
            });

            # Make sure the credential has an ACL entry for the new project principal
            my $xpath = $commander->getAclEntry("user", "project: $pluginName", {
                projectName => $otherPluginName,
                credentialName => $cred
            });
            if ($xpath->findvalue("//code") eq "NoSuchAclEntry") {
                $batch->deleteAclEntry("user", "project: $otherPluginName", {
                    projectName => $pluginName,
                    credentialName => $cred
                });
                $batch->createAclEntry("user", "project: $pluginName", {
                    projectName => $pluginName,
                    credentialName => $cred,
                    readPrivilege => 'allow',
                    modifyPrivilege => 'allow',
                    executePrivilege => 'allow',
                    changePermissionsPrivilege => 'allow'
                });
            }
            
            $batch->attachCredential("\$[/plugins/$pluginName/project]", $cred, {
                procedureName => 'StartApp',
                stepName => 'StartApp'
            });

            # Attach the credential to the appropriate steps
            $batch->attachCredential("\$[/plugins/$pluginName/project]", $cred, {
                procedureName => 'StopApp',
                stepName => 'StopApp'
            });
            
            # Attach the credential to the appropriate steps
            $batch->attachCredential("\$[/plugins/$pluginName/project]", $cred, {
                procedureName => 'CheckServerStatus',
                stepName => 'CheckServerStatus'
            });
            
            # Attach the credential to the appropriate steps
            $batch->attachCredential("\$[/plugins/$pluginName/project]", $cred, {
                procedureName => 'DeployApp',
                stepName => 'DeployApp'
            });
            
            # Attach the credential to the appropriate steps
            $batch->attachCredential("\$[/plugins/$pluginName/project]", $cred, {
                procedureName => 'RunDeployer',
                stepName => 'RunJob'
            });
            
            # Attach the credential to the appropriate steps
            $batch->attachCredential("\$[/plugins/$pluginName/project]", $cred, {
                procedureName => 'UndeployApp',
                stepName => 'UndeployApp'
            });
            
            # Attach the credential to the appropriate steps
            $batch->attachCredential("\$[/plugins/$pluginName/project]", $cred, {
                procedureName => 'RunWLST',
                stepName => 'RunWLST'
            });
            
            # Attach the credential to the appropriate steps
            $batch->attachCredential("\$[/plugins/$pluginName/project]", $cred, {
                procedureName => 'StartAdminServer',
                stepName => 'StartAdminServer'
            });
            
            # Attach the credential to the appropriate steps
            $batch->attachCredential("\$[/plugins/$pluginName/project]", $cred, {
                procedureName => 'StopAdminServer',
                stepName => 'StopAdminServer'
            });
            
            # Attach the credential to the appropriate steps
            $batch->attachCredential("\$[/plugins/$pluginName/project]", $cred, {
                procedureName => 'StartManagedServer',
                stepName => 'StartInstance'
            });
            
            # Attach the credential to the appropriate steps
            $batch->attachCredential("\$[/plugins/$pluginName/project]", $cred, {
                procedureName => 'StopManagedServer',
                stepName => 'StopInstance'
            });
            
            # Attach the credential to the appropriate steps
            $batch->attachCredential("\$[/plugins/$pluginName/project]", $cred, {
                procedureName => 'CheckPageStatus',
                stepName => 'CheckPageStatus'
            });


        }
    }
}
