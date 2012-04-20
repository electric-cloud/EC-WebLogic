my %startApp = (
    label       => "WebLogic - Start Application",
    procedure   => "StartApp",
    description => "Start an application.",
    category    => "Application Server"
);
my %stopApp = (
    label       => "WebLogic - Stop Application",
    procedure   => "StopApp",
    description => "Stop an application",
    category    => "Application Server"
);
my %checkServerStatus = (
    label       => "WebLogic - Check Server Status",
    procedure   => "CheckServerStatus",
    description => "Check the status of the given server URL.",
    category    => "Application Server"
);
my %deployApp = (
    label       => "WebLogic - Deploy Application",
    procedure   => "DeployApp",
    description => "Deploy or redeploy an application or module using the weblogic",
    category    => "Application Server"
);
my %runDeployer = (
    label       => "WebLogic - Run Deployer",
    procedure   => "RunDeployer",
    description => "Run weblogic.Deployer in a free-mode",
    category    => "Application Server"
);
my %undeployApp = (
    label       => "WebLogic - Undeploy Application",
    procedure   => "UndeployApp",
    description => "Stop the deployment unit and removes staged files from target servers",
    category    => "Application Server"
);
my %runWLST = (
    label       => "WebLogic - Run WLST",
    procedure   => "RunWLST",
    description => "Run Jython scripts using weblogic1.WLST",
    category    => "Application Server"
);
my %startAdminServer = (
    label       => "WebLogic - Start Admin Server",
    procedure   => "StartAdminServer",
    description => "Start the WebLogic Admin Server",
    category    => "Application Server"
);
my %stopAdminServer = (
    label       => "WebLogic - Stop Admin Server",
    procedure   => "StopAdminServer",
    description => "Stop the WebLogic Admin Server",
    category    => "Application Server"
);
my %startManagedServer = (
    label       => "WebLogic - Start Managed Server",
    procedure   => "StartManagedServer",
    description => "Start a WebLogic Managed Server",
    category    => "Application Server"
);
my %stopManagedServer = (
    label       => "WebLogic - Stop Managed Server",
    procedure   => "StopManagedServer",
    description => "Stop a WebLogic Managed Server",
    category    => "Application Server"
);
my %checkPageStatus = (
    label       => "WebLogic - Check Page Status",
    procedure   => "CheckPageStatus",
    description => "Check the status of the given page URL",
    category    => "Application Server"
);
my %startNodeManager = (
    label       => "WebLogic - Start Node Manager",
    procedure   => "StartNodeManager",
    description => "Start the WebLogic Node Manager",
    category    => "Application Server"
);
my %stopNodeManager = (
    label       => "WebLogic - Stop Node Manager",
    procedure   => "StopNodeManager",
    description => "Stop the WebLogic Node Manager",
    category    => "Application Server"
);
$batch->deleteProperty("/server/ec_customEditors/pickerStep/EC-WebLogic - Start App");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/EC-WebLogic - Stop App");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/EC-WebLogic - Check Server Status");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/EC-WebLogic - Deploy App");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/EC-WebLogic - Run Deployer");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/EC-WebLogic - Undeploy App");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/EC-WebLogic - Run WLST");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/EC-WebLogic - Start Admin Server");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/EC-WebLogic - Stop Admin Server");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/EC-WebLogic - Start Managed Server");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/EC-WebLogic - Stop Managed Server");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/EC-WebLogic - Check Page Status");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/EC-WebLogic - Start Node Manager");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/EC-WebLogic - Stop Node Manager");

$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Start App");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Stop App");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Check Server Status");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Deploy App");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Run Deployer");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Undeploy App");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Run WLST");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Start Admin Server");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Stop Admin Server");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Start Managed Server");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Stop Managed Server");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Check Page Status");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Start Node Manager");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Stop Node Manager");

$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Start Application");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Stop Application");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Deploy Application");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Undeploy Application");

@::createStepPickerSteps = (\%startApp, \%stopApp,
                            \%checkServerStatus, \%deployApp,
                            \%runDeployer, \%undeployApp,
                            \%runWLST, \%startAdminServer,
                            \%stopAdminServer, \%startManagedServer,
                            \%stopManagedServer, \%checkPageStatus, \%startNodeManager, \%stopNodeManager);
							

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
            
            # Attach the credential to the appropriate steps
            $batch->attachCredential("\$[/plugins/$pluginName/project]", $cred, {
                procedureName => 'StartNodeManager',
                stepName => 'StartNodeManager'
            });
            
            # Attach the credential to the appropriate steps
            $batch->attachCredential("\$[/plugins/$pluginName/project]", $cred, {
                procedureName => 'StopNodeManager',
                stepName => 'StopNodeManager'
            });
        }
    }
}
							