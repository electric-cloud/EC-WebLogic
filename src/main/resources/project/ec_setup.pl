#
#  Copyright 2021 Electric Cloud, Inc.
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

use strict;
use warnings;
no warnings 'redefine';

use XML::Simple;
use Data::Dumper;
use ElectricCommander::Util;

use subs qw(debug);
use Time::HiRes qw(time gettimeofday tv_interval);

my @logs = ();

sub debug($) {
    my ($message) = @_;
    push @logs, scalar time . ": " . $message;

    if ($ENV{EC_SETUP_DEBUG}) {
        print scalar time . ": $message\n";
    }
}

# External Credential Manageent Update:
# We're retrieving the steps with attached creds from property sheet
use JSON;
my $stepsWithCredentials = getStepsWithCredentials();
# End of External Credential Management Update

my $restartFlagName = 'WebLogicServerRestartRequired';

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
    description => "Creates a Datasource (DEPRECATED)",
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
    description => "This procedure creates a new generic JDBC Data Source or updates an existing one based on the update action.",
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
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Start Cluster");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Stop Cluster");

$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Update Application");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Update Application (DEPRECATED)");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Update Application Config");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Check Cluster Status");

$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Create Or JMS Resource");

# $batch->deleteProperty(
#     "/server/ec_customEditors/pickerStep/WebLogic - Create Or Update Datasource");

$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Create Or Update Connection Factory");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Delete Connection Factory");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Create Or Update JMS Queue");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Delete JMS Queue");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Create Or Update JMS Topic");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Delete JMS Topic");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Create Or Update JMS Module");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Delete JMS Module");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Create Or Update JMS Module Subdeployment");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Delete JMS Module Subdeployment");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Create Or Update JMS Server");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Delete JMS Server");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/WebLogic - Create or Update Datasource");

@::createStepPickerSteps = (
    \%startApp,                             \%stopApp,
    \%checkServerStatus,                    \%deployApp,
    \%runDeployer,                          \%undeployApp,
    \%runWLST,                              \%startAdminServer,
    \%stopAdminServer,                      \%startManagedServer,
    \%stopManagedServer,                    \%checkPageStatus,
    \%startNodeManager,                     \%stopNodeManager,
    \%createDatasource,                     \%deleteDatasource,
    \%suspendServer,                        \%resumeServer,
    \%createUser,                           \%createGroup,
    \%deleteUser,                           \%deleteGroup,
    \%addUserToGroup,                       \%removeUserFromGroup,
    \%changeUserPassword,                   \%unlockUserAccount,
    \%updateApp,                            \%createDomain,
    \%createTemplate,                       \%createCluster,
    \%addServerToCluster,                   \%configureUserLockoutManager,
    \%deleteCluster,                        \%createManagedServer,
    \%deleteManagedServer,                  \%startCluster,
    \%stopCluster,                          \%updateAppConfig,
    \%checkClusterStatus,                   \%createOrUpdateConnectionFactory,
    \%deleteConnectionFactory,              \%createOrUpdateJMSQueue,
    \%createOrUpdateJMSTopic,               \%deleteJMSTopic,
    \%createOrUpdateJMSModule,              \%deleteJMSModule,
    \%createOrUpdateJMSModuleSubdeployment, \%deleteJMSModuleSubdeployment,
    \%createOrUpdateJMSServer, \%deleteJMSServer, \%deleteJMSQueue,
    \%createOrUpdateDatasource
);

if ($upgradeAction eq 'upgrade') {
    migrateConfigurations($otherPluginName);
    migrateProperties($otherPluginName);
    debug "Migrated properties";
    reattachExternalCredentials($otherPluginName);
}

# Disabling this branch of logic temporary
if (0 && ($upgradeAction eq "upgrade")) {
    patch_configs("/plugins/$otherPluginName/project/weblogic_cfgs");
    my $query   = $commander->newBatch();
    my $newcfg  = $query->getProperty("/plugins/$pluginName/project/weblogic_cfgs");
    my $oldcfgs = $query->getProperty("/plugins/$otherPluginName/project/weblogic_cfgs");
    my $creds   = $query->getCredentials("\$[/plugins/$otherPluginName]");

    local $self->{abortOnError} = 0;
    $query->submit();

    # if new plugin does not already have cfgs
    if ($query->findvalue($newcfg, "code") eq "NoSuchProperty") {

        # if old cfg has some cfgs to copy
        if ($query->findvalue($oldcfgs, "code") ne "NoSuchProperty") {
            $batch->clone({
                    path      => "/plugins/$otherPluginName/project/weblogic_cfgs",
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
            $batch->clone({
                    path      => "/plugins/$otherPluginName/project/credentials/$cred",
                    cloneName => "/plugins/$pluginName/project/credentials/$cred"
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
            if ($xpath->findvalue("//code") eq "NoSuchAclEntry") {
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
            } ## end if ($xpath->findvalue(...))

            for my $step (@$stepsWithCredentials) {
                # Attach the credential to the appropriate steps
                $batch->attachCredential(
                    "\$[/plugins/$pluginName/project]",
                    $cred, {
                        procedureName => $step->{procedureName},
                        stepName      => $step->{stepName}
                    }
                );
            }
        } ## end for (@nodes)
    } ## end if ($nodes)

    reattachExternalCredentials($otherPluginName);
} ## end if (0 && ($upgradeAction...))

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
        my $t        = $commander->getProperties({propertySheetId => $configs});
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
                my $sheet = $commander->getProperties({propertySheetId => $c->{propertySheetId}});
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

    } ## end for my $c (@$cfg_list)
    return 1;
} ## end sub patch_configs

my @proceduresWithPossibleRestart = qw(
    CreateCluster
    CreateOrUpdateDatasource
    CreateOrUpdateConnectionFactory
    CreateOrUpdateJMSModuleSubdeployment
    CreateOrUpdateJMSModule
    CreateOrUpdateJMSQueue
    CreateOrUpdateJMSTopic
    CreateOrUpdateJMSServer
    DeployApp
    DeleteDatasource
    DeleteConnectionFactory
    DeleteJMSModule
    DeleteJMSServer
    DeleteJMSTopic
    DeleteJMSQueue
    DeleteJMSModuleSubdeployment
    UndeployApp
);

my @formalOutputParameters = map {{formalOutputParameterName => $restartFlagName, procedureName => $_}} @proceduresWithPossibleRestart;

if ($promoteAction eq 'promote') {

    reattachExternalConfigurations($otherPluginName);
    ## Check if agent supports formalOutputParameters API,
    if (exists $ElectricCommander::Arguments{getFormalOutputParameters}) {
        my $versions = $commander->getVersions();

        if (my $version = $versions->findvalue('//version')) {
            require ElectricCommander::Util;
            ElectricCommander::Util->import('compareMinor');

            if (compareMinor($version, '8.3') >= 0) {
                checkAndSetOutputParameters(@formalOutputParameters);
            }
        }
    }
}

sub checkAndSetOutputParameters {
    my (@parameters) = @_;

    # Form flatten unique list of procedureNames
    # and get all parameters for defined procedures
    my $query = $commander->newBatch();
    my %subs  = ();
    foreach my $param (@parameters) {
        my $proc_name = $param->{procedureName};
        $subs{$proc_name} = 1;
    }

    foreach (keys %subs) {
        $subs{$_} = $query->getFormalOutputParameters($otherPluginName, {procedureName => $_});
    }
    $query->submit();

    my @params_to_create = ();
    foreach my $proc_name (keys %subs) {
        my $response_for_params = $query->find($proc_name);

        push @params_to_create, checkMissingOutputParameters(\@parameters, $response_for_params);
    }

    createMissingOutputParameters(@params_to_create);
} ## end sub checkAndSetOutputParameters

sub checkMissingOutputParameters {
    my ($parameters, $response) = @_;
    my @parameters = @{$parameters};

    # This is list of keys to build unique parameter's indices
    my @key_parts        = ('formalOutputParameterName', 'procedureName');
    my @params_to_create = ();

    my %parsed_parameters = ();
    if ($response) {
        my @defined_params = ($response->findnodes('formalOutputParameter'));

        if (@defined_params) {
            for my $param (@defined_params) {
                my $key = join('_', map {$param->find($_)->string_value()} @key_parts);

                # Setting a flag parameter that parameter is already created
                $parsed_parameters{$key} = 1;
            }
        }
    }

    foreach my $param (@parameters) {
        my $key = join('_', map {$param->{$_} || ''} @key_parts);

        if (!exists $parsed_parameters{$key}) {
            push(
                @params_to_create, [
                    $pluginName,
                    $param->{formalOutputParameterName},
                    {procedureName => $param->{procedureName}}
                ]
            );
        }
    }

    return @params_to_create;
} ## end sub checkMissingOutputParameters

sub createMissingOutputParameters {
    my (@params_to_create) = @_;

    my @responses = ();
    if (@params_to_create) {
        my $create_batch = $commander->newBatch();
        push @responses, $create_batch->createFormalOutputParameter(@$_) foreach (@params_to_create);
        $create_batch->submit();
    }
    # print Dumper \@responses
    return 1;
}

sub reattachExternalCredentials {
    my ($otherPluginName) = @_;

    my $configName  = getConfigLocation($otherPluginName);
    my $configsPath = "/plugins/$otherPluginName/project/$configName";

    my $xp = $commander->getProperty($configsPath);

    my $id    = $xp->findvalue('//propertySheetId')->string_value();
    my $props = $commander->getProperties({propertySheetId => $id});
    for my $node ($props->findnodes('//property/propertySheetId')) {
        my $configPropertySheetId = $node->string_value();
        my $config                = $commander->getProperties({propertySheetId => $configPropertySheetId});

        # iterate through props to get credentials.
        for my $configRow ($config->findnodes('//property')) {
            my $propName  = $configRow->findvalue('propertyName')->string_value();
            my $propValue = $configRow->findvalue('value')->string_value();
            # print "Name $propName, value: $propValue\n";
            if ($propName =~ m/credential$/s && $propValue =~ m|^\/|s) {
                for my $step (@$stepsWithCredentials) {
                    $batch->attachCredential({
                            projectName    => $pluginName,
                            procedureName  => $step->{procedureName},
                            stepName       => $step->{stepName},
                            credentialName => $propValue,
                        }
                    );
                    #    debug "Attached credential to $step->{stepName}";
                }
                print "Reattaching $propName with val: $propValue\n";
            }
        }
        # exit 0;
    } ## end for my $node ($props->findnodes...)
} ## end sub reattachExternalCredentials

sub getConfigLocation {
    my ($otherPluginName) = @_;

    my $configName = eval {$commander->getProperty("/plugins/$otherPluginName/project/ec_configPropertySheet")->findvalue('//value')->string_value} || 'weblogic_cfgs';

    return $configName;
}

sub getStepsWithCredentials {
    my $retval = [];
    eval {
        my $pluginName = '@PLUGIN_NAME@';
        my $stepsJson  = $commander->getProperty("/projects/$pluginName/procedures/CreateConfiguration/ec_stepsWithAttachedCredentials")->findvalue('//value')
            ->string_value;
        $retval = decode_json($stepsJson);
    };
    return $retval;
}

sub reattachExternalConfigurations {
    my ($otherPluginName) = @_;

    my %migrated = ();
    # For the configurations that exists while the plugin was deleted
    # The api is new so it requires the upgraded version of the agent
    eval {
        my $cfgs = $commander->getPluginConfigurations({
                pluginKey => '@PLUGIN_KEY@',
            }
        );
        my @creds = ();
        for my $cfg ($cfgs->findnodes('//pluginConfiguration/credentialMappings/parameterDetail')) {
            my $value = $cfg->findvalue('parameterValue')->string_value();
            push @creds, $value;
        }

        for my $cred (@creds) {
            next if $migrated{$cred};
            for my $stepWithCreds (@$stepsWithCredentials) {
                $commander->attachCredential({
                        projectName    => "/plugins/$pluginName/project",
                        credentialName => $cred,
                        procedureName  => $stepWithCreds->{procedureName},
                        stepName       => $stepWithCreds->{stepName}
                    }
                );
            }
            $migrated{$cred} = 1;
            debug "Migrated $cred";
        }
        1;
    } or do {
        debug "getPluginConfiguration API is not supported on the promoting agent, falling back";
        for my $stepWithCreds (@$stepsWithCredentials) {
            my $step = $commander->getStep({
                    projectName   => "/plugins/$otherPluginName/project",
                    procedureName => $stepWithCreds->{procedureName},
                    stepName      => $stepWithCreds->{stepName},
                }
            );
            for my $attachedCred ($step->findnodes('//attachedCredentials/credentialName')) {
                my $credName = $attachedCred->string_value();
                $commander->attachCredential({
                        projectName    => "/plugins/$pluginName/project",
                        credentialName => $credName,
                        procedureName  => $stepWithCreds->{procedureName},
                        stepName       => $stepWithCreds->{stepName}
                    }
                );
                $migrated{$credName} = 1;
                debug "Migrated credential $credName to $stepWithCreds->{procedureName}";
            }
        } ## end for my $stepWithCreds (...)
    };
} ## end sub reattachExternalConfigurations

sub migrateConfigurations {
    my ($otherPluginName) = @_;

    my $configName = getConfigLocation($otherPluginName);
    # my $configName = eval {
    #     $commander->getProperty("/plugins/$otherPluginName/project/ec_configPropertySheet")->findvalue('//value')->string_value
    # } || 'ec_plugin_cfgs';

    $commander->clone({
            path      => "/plugins/$otherPluginName/project/$configName",
            cloneName => "/plugins/$pluginName/project/$configName"
        }
    );

    my $xpath = $commander->getCredentials("/plugins/$otherPluginName/project");
    for my $credential ($xpath->findnodes('//credential')) {
        my $credName = $credential->findvalue('credentialName')->string_value;

        # If credential name starts with "/", it means that it is a reference.
        # We do not need to clone it.
        # if ($credName !~ m|^\/|s) {
        debug "Migrating old configuration $credName";
        $batch->clone({
                path      => "/plugins/$otherPluginName/project/credentials/$credName",
                cloneName => "/plugins/$pluginName/project/credentials/$credName"
            }
        );
        $batch->deleteAclEntry({
                principalName  => "project: $otherPluginName",
                projectName    => $pluginName,
                credentialName => $credName,
                principalType  => 'user'
            }
        );
        $batch->deleteAclEntry({
                principalType  => 'user',
                principalName  => "project: $pluginName",
                credentialName => $credName,
                projectName    => $pluginName,
            }
        );

        $batch->createAclEntry({
                principalType              => 'user',
                principalName              => "project: $pluginName",
                projectName                => $pluginName,
                credentialName             => $credName,
                objectType                 => 'credential',
                readPrivilege              => 'allow',
                modifyPrivilege            => 'allow',
                executePrivilege           => 'allow',
                changePermissionsPrivilege => 'allow'
            }
        );
        #}

        for my $step (@$stepsWithCredentials) {
            $batch->attachCredential({
                    projectName    => $pluginName,
                    procedureName  => $step->{procedureName},
                    stepName       => $step->{stepName},
                    credentialName => $credName,
                }
            );
            debug "Attached credential to $step->{stepName}";
        }
    } ## end for my $credential ($xpath...)
} ## end sub migrateConfigurations

sub migrateProperties {
    my ($otherPluginName) = @_;
    my $clonedPropertySheets
        = eval {decode_json($commander->getProperty("/plugins/$otherPluginName/project/ec_clonedProperties")->findvalue('//value')->string_value);};
    unless ($clonedPropertySheets) {
        debug "No properties to migrate";
        return;
    }

    for my $prop (@$clonedPropertySheets) {
        $commander->clone({
                path      => "/plugins/$otherPluginName/project/$prop",
                cloneName => "/plugins/$pluginName/project/$prop"
            }
        );
        debug "Cloned $prop"
    }
}
