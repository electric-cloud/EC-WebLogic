#
#  Copyright 2015 Electric Cloud, Inc.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

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
    label     => "WebLogic - Deploy Application",
    procedure => "DeployApp",
    description =>
      "Deploys or redeploys an application or module using the weblogic",
    category => "Application Server"
);
my %runDeployer = (
    label       => "WebLogic - Run Deployer",
    procedure   => "RunDeployer",
    description => "Runs weblogic.Deployer in a free-mode",
    category    => "Application Server"
);
my %undeployApp = (
    label     => "WebLogic - Undeploy Application",
    procedure => "UndeployApp",
    description =>
      "Stops the deployment unit and removes staged files from target servers",
    category => "Application Server"
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
my %createUser = (
    label       => "WebLogic - Create User",
    procedure   => "CreateUser",
    description => "Create new user",
    category    => "Application Server"
);

my %createGroup = (
    label       => "WebLogic - Create Group",
    procedure   => "CreateGroup",
    description => "Create new group",
    category    => "Application Server"
);
my %deleteUser = (
    label       => "WebLogic - Delete User",
    procedure   => "DeleteUser",
    description => "Delete user",
    category    => "Application Server"
);
my %deleteGroup = (
    label       => "WebLogic - Delete Group",
    procedure   => "DeleteGroup",
    description => "Delete Group",
    category    => "Application Server"
);
my %addUserToGroup = (
    label       => "WebLogic - Add User To Group",
    procedure   => "AddUserToGroup",
    description => "Add User To Group",
    category    => "Application Server"
);
my %removeUserFromGroup = (
    label       => "WebLogic - Remove User From Group",
    procedure   => "RemoveUserFromGroup",
    description => "Remove User From Group",
    category    => "Application Server"
);
my %changeUserPassword = (
    label       => "WebLogic - Change User Password",
    procedure   => "ChangeUserPassword",
    description => "Change User Password",
    category    => "Application Server"
);
my %unlockUserAccount = (
    label       => "WebLogic - Unlock User Account",
    procedure   => "UnlockUserAccount",
    description => "Unlock User Account",
    category    => "Application Server"
);
my %updateApp = (
    label       => "WebLogic - Update Application (DEPRECATED)",
    procedure   => "UpdateApp",
    description => "Update Application",
    category    => "Application Server"
);
my %createDomain = (
    label       => "WebLogic - Create Domain",
    procedure   => "CreateDomain",
    description => "Create a new domain from template",
    category    => "Application Server"
);
my %createTemplate = (
    label       => "WebLogic - Create Template",
    procedure   => "CreateTemplate",
    description => "Create a domain template from an existing domain",
    category    => "Application Server"
);
my %createCluster = (
    label       => "WebLogic - Create Cluster",
    procedure   => "CreateCluster",
    description => "Create a new cluster",
    category    => "Application Server"
);
my %deleteCluster = (
    label       => "WebLogic - Delete Cluster",
    procedure   => "DeleteCluster",
    description => "Delete a cluster",
    category    => "Application Server"
);
my %createManagedServer = (
    label       => "WebLogic - Create Managed Server",
    procedure   => "createManagedServer",
    description => "Create a new managed server",
    category    => "Application Server"
);
my %deleteManagedServer = (
    label       => "WebLogic - Delete Managed Server",
    procedure   => "deleteManagedServer",
    description => "Delete a managed server",
    category    => "Application Server"
);
my %addServerToCluster = (
    label       => "WebLogic - Add Server To Cluster",
    procedure   => "AddServerToCluster",
    description => "Add server to cluster",
    category    => "Application Server"
);
my %configureUserLockoutManager = (
    label       => "WebLogic - Configure User Lockout Manager",
    procedure   => "ConfigureUserLockoutManager",
    description => "Configure User Lockout Manager",
    category    => "Application Server"
);

my %startCluster = (
    label       => "WebLogic - Start Cluster",
    procedure   => "StartCluster",
    description => "StartCluster",
    category    => "Application Server"
);
my %stopCluster = (
    label       => "WebLogic - Stop Cluster",
    procedure   => "StopCluster",
    description => "StopCluster",
    category    => "Application Server"
);
my %updateAppConfig = (
    label       => "WebLogic - Update Application Config",
    procedure   => "UpdateAppConfig",
    description => "Updates Application Config",
    category    => "Application Server"
);

my %checkClusterStatus = (
    label       => "WebLogic - Check Cluster Status",
    procedure   => "CheckClusterStatus",
    description => "Check Cluster Status",
    category    => "Application Server"
);

my %createOrUpdateJMSModule = (
    label       => "WebLogic - Create Or Update JMS Module",
    procedure   => "CreateOrUpdateJMSModule",
    description => "Creates or updates JMS module",
    category    => "Application Server"
);

my %createOrUpdateDatasource = (
    label       => "WebLogic - Create Or Update Datasource",
    procedure   => "CreateOrUpdateDatasource",
    description => "Create or update datasource",
    category    => "Application Server"
);

my %createOrUpdateConnectionFactory = (
    label       => "WebLogic - Create Or Update Connection Factory",
    procedure   => "CreateOrUpdateConnectionFactory",
    description => "Creates or updates Connection Factory",
    category    => "Application Server"
);

my %deleteConnectionFactory = (
    label       => "WebLogic - Delete Connection Factory",
    procedure   => "DeleteConnectionFactory",
    description => "Deletes Connection Factory",
    category    => "Application Server"
);

my %createOrUpdateJMSQueue = (
    label       => "WebLogic - Create Or Update JMS Queue",
    procedure   => "CreateOrUpdateJMSQueue",
    description => "Creates or updates JMS Queue",
    category    => "Application Server"
);

my %deleteJMSQueue = (
    label       => "WebLogic - Delete JMS Queue",
    procedure   => "DeleteJMSQueue",
    description => "Deletes JMS Queue",
    category    => "Application Server"
);

my %createOrUpdateJMSTopic = (
    label       => "WebLogic - Create Or Update JMS Topic",
    procedure   => "CreateOrUpdateJMSTopic",
    description => "Creates or updates JMS Topic",
    category    => "Application Server"
);

my %deleteJMSTopic = (
    label       => "WebLogic - Delete JMS Topic",
    procedure   => "DeleteJMSTopic",
    description => "Deletes JMS Topic",
    category    => "Application Server"
);

my %deleteJMSModule = (
    label       => "WebLogic - Delete JMS Module",
    procedure   => "DeleteJMSModule",
    description => "Deletes JMS module",
    category    => "Application Server"
);
my %createOrUpdateJMSModuleSubdeployment = (
    label       => "WebLogic - Create Or Update JMS Module Subdeployment",
    procedure   => "CreateOrUpdateJMSModuleSubdeployment",
    description => "Creates or updates JMS module Subdeployment",
    category    => "Application Server"
);

my %deleteJMSModuleSubdeployment = (
    label       => "WebLogic - Delete JMS Module Subdeployment",
    procedure   => "DeleteJMSModuleSubdeployment",
    description => "Deletes JMS module Subdeployment",
    category    => "Application Server"
);

my %createOrUpdateJMSServer = (
    label       => "WebLogic - Create Or Update JMS Server",
    procedure   => "CreateOrUpdateJMSServer",
    description => "Creates or updates JMS Server",
    category    => "Application Server"
);

my %deleteJMSServer = (
    label       => "WebLogic - Delete JMS Server",
    procedure   => "DeleteJMSServer",
    description => "Deletes JMS Server",
    category    => "Application Server"
);

$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/EC-WebLogic - Start App");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/EC-WebLogic - Stop App");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/EC-WebLogic - Check Server Status");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/EC-WebLogic - Deploy App");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/EC-WebLogic - Run Deployer");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/EC-WebLogic - Undeploy App");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/EC-WebLogic - Run WLST");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/EC-WebLogic - Start Admin Server");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/EC-WebLogic - Stop Admin Server");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/EC-WebLogic - Start Managed Server");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/EC-WebLogic - Stop Managed Server");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/EC-WebLogic - Check Page Status");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/EC-WebLogic - Start Node Manager");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/EC-WebLogic - Stop Node Manager");

$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Start App");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Stop App");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Check Server Status");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Deploy App");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Run Deployer");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Undeploy App");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Run WLST");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Start Admin Server");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Stop Admin Server");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Start Managed Server");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Stop Managed Server");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Check Page Status");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Start Node Manager");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Stop Node Manager");

$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Start Application");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Stop Application");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Deploy Application");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Undeploy Application");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Create Datasource");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Delete Datasource");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Create Data Source");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Delete Data Source");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Suspend Server");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Resume Server");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Start Cluster");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Stop Cluster");

$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Update Application");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Update Application (DEPRECATED)");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Update Application Config");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Check Cluster Status");

$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Create Or JMS Resource");

# $batch->deleteProperty(
#     "/server/ec_customEditors/pickerStep/WebLogic - Create Or Update Datasource");

$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Create Or Update Connection Factory");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Delete Connection Factory");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Create Or Update JMS Queue");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Delete JMS Queue");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Create Or Update JMS Topic");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Delete JMS Topic");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Create Or Update JMS Module");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Delete JMS Module");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Create Or Update JMS Module Subdeployment");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Delete JMS Module Subdeployment");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Create Or Update JMS Server");
$batch->deleteProperty(
    "/server/ec_customEditors/pickerStep/WebLogic - Delete JMS Server");


@::createStepPickerSteps = (
    \%startApp,                    \%stopApp,
    \%checkServerStatus,           \%deployApp,
    \%runDeployer,                 \%undeployApp,
    \%runWLST,                     \%startAdminServer,
    \%stopAdminServer,             \%startManagedServer,
    \%stopManagedServer,           \%checkPageStatus,
    \%startNodeManager,            \%stopNodeManager,
    \%createDatasource,            \%deleteDatasource,
    \%suspendServer,               \%resumeServer,
    \%createUser,                  \%createGroup,
    \%deleteUser,                  \%deleteGroup,
    \%addUserToGroup,              \%removeUserFromGroup,
    \%changeUserPassword,          \%unlockUserAccount,
    \%updateApp,                   \%createDomain,
    \%createTemplate,              \%createCluster,
    \%addServerToCluster,          \%configureUserLockoutManager,
    \%deleteCluster,               \%createManagedServer,
    \%deleteManagedServer,         \%startCluster,
    \%stopCluster,                 \%updateAppConfig,
    \%checkClusterStatus,          \%createOrUpdateConnectionFactory,
    \%deleteConnectionFactory,     \%createOrUpdateJMSQueue,
    \%createOrUpdateJMSTopic,      \%deleteJMSTopic,
    \%createOrUpdateJMSModule,     \%deleteJMSModule,
    \%createOrUpdateJMSModuleSubdeployment, \%deleteJMSModuleSubdeployment,
    \%createOrUpdateJMSServer, \%deleteJMSServer, \%deleteJMSQueue
);

if ( $upgradeAction eq "upgrade" ) {
    patch_configs("/plugins/$otherPluginName/project/weblogic_cfgs");
    my $query = $commander->newBatch();
    my $newcfg =
      $query->getProperty("/plugins/$pluginName/project/weblogic_cfgs");
    my $oldcfgs =
      $query->getProperty("/plugins/$otherPluginName/project/weblogic_cfgs");
    my $creds = $query->getCredentials("\$[/plugins/$otherPluginName]");

    local $self->{abortOnError} = 0;
    $query->submit();

    # if new plugin does not already have cfgs
    if ( $query->findvalue( $newcfg, "code" ) eq "NoSuchProperty" ) {

        # if old cfg has some cfgs to copy
        if ( $query->findvalue( $oldcfgs, "code" ) ne "NoSuchProperty" ) {
            $batch->clone(
                {
                    path => "/plugins/$otherPluginName/project/weblogic_cfgs",
                    cloneName => "/plugins/$pluginName/project/weblogic_cfgs"
                }
            );
        }
    }

    # Copy configuration credentials and attach them to the appropriate steps
    my $nodes = $query->find($creds);
    if ($nodes) {
        my @nodes = $nodes->findnodes('credential/credentialName');
        for (@nodes) {
            my $cred = $_->string_value;

            # Clone the credential
            $batch->clone(
                {
                    path =>
                      "/plugins/$otherPluginName/project/credentials/$cred",
                    cloneName =>
                      "/plugins/$pluginName/project/credentials/$cred"
                }
            );

       # Make sure the credential has an ACL entry for the new project principal
            my $xpath = $commander->getAclEntry(
                "user",
                "project: $pluginName",
                {
                    projectName    => $otherPluginName,
                    credentialName => $cred
                }
            );
            if ( $xpath->findvalue("//code") eq "NoSuchAclEntry" ) {
                $batch->deleteAclEntry(
                    "user",
                    "project: $otherPluginName",
                    {
                        projectName    => $pluginName,
                        credentialName => $cred
                    }
                );
                $batch->createAclEntry(
                    "user",
                    "project: $pluginName",
                    {
                        projectName                => $pluginName,
                        credentialName             => $cred,
                        readPrivilege              => 'allow',
                        modifyPrivilege            => 'allow',
                        executePrivilege           => 'allow',
                        changePermissionsPrivilege => 'allow'
                    }
                );
            }

            # Attach the credential to the appropriate steps
            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'StartApp',
                    stepName      => 'StartApp'
                }
            );

            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'StopApp',
                    stepName      => 'StopApp'
                }
            );

            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'CheckServerStatus',
                    stepName      => 'CheckServerStatus'
                }
            );

            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'DeployApp',
                    stepName      => 'DeployApp'
                }
            );

            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'RunDeployer',
                    stepName      => 'RunJob'
                }
            );

            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'UndeployApp',
                    stepName      => 'UndeployApp'
                }
            );

            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'RunWLST',
                    stepName      => 'RunWLST'
                }
            );

            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'StartAdminServer',
                    stepName      => 'StartAdminServer'
                }
            );

            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'StopAdminServer',
                    stepName      => 'StopAdminServer'
                }
            );

            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'StartManagedServer',
                    stepName      => 'StartInstance'
                }
            );

            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'StopManagedServer',
                    stepName      => 'StopInstance'
                }
            );

            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'CheckPageStatus',
                    stepName      => 'CheckPageStatus'
                }
            );

            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'StartNodeManager',
                    stepName      => 'StartNodeManager'
                }
            );

            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'StopNodeManager',
                    stepName      => 'StopNodeManager'
                }
            );

            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'CreateDatasource',
                    stepName      => 'CreateDatasource'
                }
            );

            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'DeleteDatasource',
                    stepName      => 'DeleteDatasource'
                }
            );

            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'SuspendServer',
                    stepName      => 'SuspendServer'
                }
            );

            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'ResumeServer',
                    stepName      => 'ResumeServer'
                }
            );

            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'CreateUser',
                    stepName      => 'CreateUser'
                }
            );

            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'CreateGroup',
                    stepName      => 'CreateGroup'
                }
            );

            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'DeleteUser',
                    stepName      => 'DeleteUser'
                }
            );

            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'DeleteGroup',
                    stepName      => 'DeleteGroup'
                }
            );

            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'AddUserToGroup',
                    stepName      => 'AddUserToGroup'
                }
            );

            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'RemoveUserFromGroup',
                    stepName      => 'RemoveUserFromGroup'
                }
            );

            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'ChangeUserPassword',
                    stepName      => 'ChangeUserPassword'
                }
            );

            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'UnlockUserAccount',
                    stepName      => 'UnlockUserAccount'
                }
            );

            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'UpdateApp',
                    stepName      => 'UpdateApp'
                }
            );

            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'CreateDomain',
                    stepName      => 'CreateDomain'
                }
            );

            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'CreateCluster',
                    stepName      => 'CreateCluster'
                }
            );
            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'DeleteCluster',
                    stepName      => 'DeleteCluster'
                }
            );

            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'CreateManagedServer',
                    stepName      => 'CreateManagedServer'
                }
            );
            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'DeleteManagedServer',
                    stepName      => 'DeleteManagedServer'
                }
            );

            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'AddServerToCluster',
                    stepName      => 'AddServerToCluster'
                }
            );

            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'ConfigureUserLockoutManager',
                    stepName      => 'ConfigureUserLockoutManager'
                }
            );

            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'StartCluster',
                    stepName      => 'StartCluster'
                }
            );
            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'StopCluster',
                    stepName      => 'StopCluster'
                }
            );
            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'UpdateAppConfig',
                    stepName      => 'UpdateAppConfig'
                }
            );
            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'CheckClusterStatus',
                    stepName      => 'CheckClusterStatus'
                }
            );

            # $batch->attachCredential(
            #     "\$[/plugins/$pluginName/project]",
            #     $cred,
            #     {
            #         procedureName => 'CreateOrUpdateDatasource',
            #         stepName      => 'CreateOrUpdateDatasource'
            #     }
            # );

            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'CreateOrUpdateConnectionFactory',
                    stepName      => 'CreateOrUpdateConnectionFactory'
                }
            );

            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'DeleteConnectionFactory',
                    stepName      => 'DeleteConnectionFactory'
                }
            );

            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'CreateOrUpdateJMSQueue',
                    stepName      => 'CreateOrUpdateJMSQueue'
                }
            );

            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'DeleteJMSQueue',
                    stepName      => 'DeleteJMSQueue'
                }
            );

            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'CreateOrUpdateJMSTopic',
                    stepName      => 'CreateOrUpdateJMSTopic'
                }
            );
            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'DeleteJMSTopic',
                    stepName      => 'DeleteJMSTopic'
                }
            );
            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'CreateOrUpdateJMSModule',
                    stepName      => 'CreateOrUpdateJMSModule'
                }
            );
            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'DeleteJMSModule',
                    stepName      => 'DeleteJMSModule'
                }
            );
            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'CreateOrUpdateJMSModuleSubdeployment',
                    stepName      => 'CreateOrUpdateJMSModuleSubdeployment'
                }
            );
            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'DeleteJMSModuleSubdeployment',
                    stepName      => 'DeleteJMSModuleSubdeployment'
                }
            );
            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'CreateOrUpdateJMSServer',
                    stepName      => 'CreateOrUpdateJMSServer'
                }
            );
            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'DeleteJMSServer',
                    stepName      => 'DeleteJMSServer'
                }
            );
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
        my $t = $commander->getProperties( { propertySheetId => $configs } );
        my $cfg_data = XMLin( $t->{_xml} );

        $cfg_list = $cfg_data->{response}->{propertySheet}->{property};
        if ( ref $cfg_list eq 'HASH' ) {
            $cfg_list = [$cfg_list];
        }
        if ( ref $cfg_list ne 'ARRAY' ) {
            $cfg_list = [];
        }
    };

    for my $c (@$cfg_list) {
        my $debug_level = undef;

        eval {
            my $prop =
              $commander->getProperty(
                $config_path . '/' . $c->{propertyName} );
            eval {
                my $sheet = $commander->getProperties(
                    { propertySheetId => $c->{propertySheetId} } );
                $sheet = XMLin( $sheet->{_xml} );
                for
                  my $p ( @{ $sheet->{response}->{propertySheet}->{property} } )
                {
                    if ( $p->{propertyName} eq 'debug_level' ) {
                        if ( !ref $p->{value} && $p->{value} =~ m/^\d+$/s ) {
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
        $commander->setProperty( $config_path . '/'
              . $c->{propertyName}
              . '/debug_level' => $debug_level );

    }
    return 1;
}
