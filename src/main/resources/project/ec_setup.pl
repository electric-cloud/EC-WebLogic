
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

my %startApp = (
    label       => "WebLogic - Start App",
    procedure   => "StartApp",
    description => "Starts an application.",
    category    => "Application Server"
);
my %stopApp = (
    label       => "WebLogic - Stop App",
    procedure   => "StopApp",
    description => "Stops an application.",
    category    => "Application Server"
);
my %checkServerStatus = (
    label       => "WebLogic - Check Server Status",
    procedure   => "CheckServerStatus",
    description => "Check the status of the given server URL.",
    category    => "Application Server"
);
my %deployApp = (
    label       => "WebLogic - Deploy App",
    procedure   => "DeployApp",
    description => "Deploys or redeploys an application or module using the weblogic.",
    category    => "Application Server"
);
my %runDeployer = (
    label       => "WebLogic - Run Deployer",
    procedure   => "RunDeployer",
    description => "Runs weblogic.Deployer in a free-mode.",
    category    => "Application Server"
);
my %undeployApp = (
    label       => "WebLogic - Undeploy App",
    procedure   => "UndeployApp",
    description => "Stops the deployment unit and removes staged files from target servers.",
    category    => "Application Server"
);
my %runWLST = (
    label       => "WebLogic - Run WLST",
    procedure   => "RunWLST",
    description => "Runs Jython scripts using weblogic.WLST.",
    category    => "Application Server"
);
my %startAdminServer = (
    label       => "WebLogic - Start Admin Server",
    procedure   => "StartAdminServer",
    description => "Starts the WebLogic Admin Server.",
    category    => "Application Server"
);
my %stopAdminServer = (
    label       => "WebLogic - Stop Admin Server",
    procedure   => "StopAdminServer",
    description => "Stops the WebLogic Admin Server.",
    category    => "Application Server"
);
my %startManagedServer = (
    label       => "WebLogic - Start Managed Server",
    procedure   => "StartManagedServer",
    description => "Starts a WebLogic Managed Server.",
    category    => "Application Server"
);
my %stopManagedServer = (
    label       => "WebLogic - Stop Managed Server",
    procedure   => "StopManagedServer",
    description => "Stops a WebLogic Managed Server.",
    category    => "Application Server"
);
my %checkPageStatus = (
    label       => "WebLogic - Check Page Status",
    procedure   => "CheckPageStatus",
    description => "Check the status of the given page URL.",
    category    => "Application Server"
);
@::createStepPickerSteps = (\%startApp, \%stopApp,
                            \%checkServerStatus, \%deployApp,
                            \%runDeployer, \%undeployApp,
                            \%runWLST, \%startAdminServer,
                            \%stopAdminServer, \%startManagedServer,
                            \%stopManagedServer, \%checkPageStatus);