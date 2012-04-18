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

@::createStepPickerSteps = (\%startApp, \%stopApp,
                            \%checkServerStatus, \%deployApp,
                            \%runDeployer, \%undeployApp,
                            \%runWLST, \%startAdminServer,
                            \%stopAdminServer, \%startManagedServer,
                            \%stopManagedServer, \%checkPageStatus, \%startNodeManager, \%stopNodeManager);