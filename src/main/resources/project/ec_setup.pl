no warnings qw/redefine/;
use XML::Simple;
use Data::Dumper;
my %startApp = (
    label       => "WebLogic - Start Application",
    procedure   => "StartApp",
    description => "Starts an application",
    category    => "Application Server"
);
my %stopApp = (
    label       => "WebLogic - Stop Application",
    procedure   => "StopApp",
    description => "Stops an application",
    category    => "Application Server"
);
my %checkServerStatus = (
    label       => "WebLogic - Check Server Status",
    procedure   => "CheckServerStatus",
    description => "Checks the status of the given server URL",
    category    => "Application Server"
);
my %deployApp = (
    label       => "WebLogic - Deploy Application",
    procedure   => "DeployApp",
    description => "Deploys or redeploys an application or module using the weblogic",
    category    => "Application Server"
);
my %runDeployer = (
    label       => "WebLogic - Run Deployer",
    procedure   => "RunDeployer",
    description => "Runs weblogic.Deployer in a free-mode",
    category    => "Application Server"
);
my %undeployApp = (
    label       => "WebLogic - Undeploy Application",
    procedure   => "UndeployApp",
    description => "Stops the deployment unit and removes staged files from target servers",
    category    => "Application Server"
);
my %runWLST = (
    label       => "WebLogic - Run WLST",
    procedure   => "RunWLST",
    description => "Runs Jython scripts using weblogic1.WLST",
    category    => "Application Server"
);
my %startAdminServer = (
    label       => "WebLogic - Start Admin Server",
    procedure   => "StartAdminServer",
    description => "Starts the WebLogic Admin Server",
    category    => "Application Server"
);
my %stopAdminServer = (
    label       => "WebLogic - Stop Admin Server",
    procedure   => "StopAdminServer",
    description => "Stops the WebLogic Admin Server",
    category    => "Application Server"
);
my %startManagedServer = (
    label       => "WebLogic - Start Managed Server",
    procedure   => "StartManagedServer",
    description => "Starts a WebLogic Managed Server",
    category    => "Application Server"
);
my %stopManagedServer = (
    label       => "WebLogic - Stop Managed Server",
    procedure   => "StopManagedServer",
    description => "Stops a WebLogic Managed Server",
    category    => "Application Server"
);
my %checkPageStatus = (
    label       => "WebLogic - Check Page Status",
    procedure   => "CheckPageStatus",
    description => "Checks the status of the given page URL",
    category    => "Application Server"
);
my %startNodeManager = (
    label       => "WebLogic - Start Node Manager",
    procedure   => "StartNodeManager",
    description => "Starts the WebLogic Node Manager",
    category    => "Application Server"
);
my %stopNodeManager = (
    label       => "WebLogic - Stop Node Manager",
    procedure   => "StopNodeManager",
    description => "Stops the WebLogic Node Manager",
    category    => "Application Server"
);
my %createDatasource = (
    label       => "WebLogic - Create Datasource",
    procedure   => "CreateDatasource",
    description => "Creates a Datasource",
    category    => "Application Server"
);
my %deleteDatasource = (
    label       => "WebLogic - Delete Datasource",
    procedure   => "DeleteDatasource",
    description => "Deletes a Datasource",
    category    => "Application Server"
);
my %suspendServer = (
    label       => "WebLogic - Suspend Server",
    procedure   => "SuspendServer",
    description => "Suspends a server",
    category    => "Application Server"
);
my %resumeServer = (
    label       => "WebLogic - Resume Server",
    procedure   => "ResumeServer",
    description => "Resumes a server",
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
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Create Datasource");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Delete Datasource");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Create Data Source");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Delete Data Source");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Suspend Server");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Resume Server");

@::createStepPickerSteps = (\%startApp, \%stopApp,
                            \%checkServerStatus, \%deployApp,
                            \%runDeployer, \%undeployApp,
                            \%runWLST, \%startAdminServer,
                            \%stopAdminServer, \%startManagedServer,
                            \%stopManagedServer, \%checkPageStatus, 
							\%startNodeManager, \%stopNodeManager, 
							\%createDatasource, \%deleteDatasource, 
							\%suspendServer, \%resumeServer);
							

if ($upgradeAction eq "upgrade") {
    patch_configs("/plugins/$otherPluginName/project/weblogic_cfgs");
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
        my @nodes = $nodes->findnodes('credential/credentialName');
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

            # Attach the credential to the appropriate steps
            $batch->attachCredential("\$[/plugins/$pluginName/project]", $cred, {
                procedureName => 'CreateDatasource',
                stepName => 'CreateDatasource'
            });

            # Attach the credential to the appropriate steps
            $batch->attachCredential("\$[/plugins/$pluginName/project]", $cred, {
                procedureName => 'DeleteDatasource',
                stepName => 'DeleteDatasource'
            });

            # Attach the credential to the appropriate steps
            $batch->attachCredential("\$[/plugins/$pluginName/project]", $cred, {
                procedureName => 'SuspendServer',
                stepName => 'SuspendServer'
            });

            # Attach the credential to the appropriate steps
            $batch->attachCredential("\$[/plugins/$pluginName/project]", $cred, {
                procedureName => 'ResumeServer',
                stepName => 'ResumeServer'
            });
        }
    }
}


sub patch_configs {
    my ($config_path) = @_;

    my $configs = '';
    eval {
        my $res = $commander->getProperty($config_path);
        $configs = $res->findvalue('//propertySheetId')->string_value();
    };
    unless ($configs) {
        return;
    }
    my $cfg_list = undef;
    eval {
        my $t = $commander->getProperties({propertySheetId => $configs});
        my $cfg_data = XMLin($t->{_xml});

        $cfg_list = $cfg_data->{response}->{propertySheet}->{property};
        if (ref $cfg_list eq 'HASH') {
            $cfg_list = [$cfg_list];
        }
        if (ref $cfg_list ne 'ARRAY') {
            $cfg_list = [];
        }
    };


    for my $c (@$cfg_list) {
        my $debug_level = undef;

        eval {
            my $prop = $commander->getProperty($config_path . '/' . $c->{propertyName});
            eval {
                my $sheet = $commander->getProperties({
                    propertySheetId => $c->{propertySheetId}
                });
                $sheet = XMLin($sheet->{_xml});
                for my $p (@{$sheet->{response}->{propertySheet}->{property}}) {
                    if ($p->{propertyName} eq 'debug_level') {
                        if (!ref $p->{value} && $p->{value} =~ m/^\d+$/s) {
                            $debug_level = $p->{value};
                        }
                    }
                }
            };
            1;
        } or do {
            next;
        };
        defined $debug_level and next;
        $debug_level = 1;
        $commander->setProperty($config_path . '/' . $c->{propertyName} . '/debug_level' => $debug_level);

    }
    return 1;
}
