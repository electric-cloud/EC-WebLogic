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

##########################
# createAndAttachCredential.pl
##########################

use ElectricCommander;

use constant {
	SUCCESS => 0,
	ERROR   => 1,
};

my $ec = new ElectricCommander();
$ec->abortOnError(0);

my $credName = "$[/myJob/config]";
my $xpath = $ec->getFullCredential("credential");
my $userName = $xpath->findvalue("//userName");
my $password = $xpath->findvalue("//password");

# Create credential
my $projName = "@PLUGIN_KEY@-@PLUGIN_VERSION@";

$ec->deleteCredential($projName, $credName);
$xpath = $ec->createCredential($projName, $credName, $userName, $password);
my $errors = $ec->checkAllErrors($xpath);

# Give config the credential's real name
my $configPath = "/projects/$projName/weblogic_cfgs/$credName";
$xpath = $ec->setProperty($configPath . "/credential", $credName);
$errors .= $ec->checkAllErrors($xpath);

# Give job launcher full permissions on the credential
my $user = "$[/myJob/launchedByUser]";
$xpath = $ec->createAclEntry("user", $user,
    {projectName => $projName,
     credentialName => $credName,
     readPrivilege => allow,
     modifyPrivilege => allow,
     executePrivilege => allow,
     changePermissionsPrivilege => allow});
$errors .= $ec->checkAllErrors($xpath);

# Attach credential to steps that will need it
$xpath = $ec->attachCredential($projName, $credName,
    {procedureName => 'StartAdminServer',
     stepName => 'StartAdminServer'});
$errors .= $ec->checkAllErrors($xpath);

$xpath = $ec->attachCredential($projName, $credName,
    {procedureName => 'StartManagedServer',
     stepName => 'StartInstance'});
$errors .= $ec->checkAllErrors($xpath);

$xpath = $ec->attachCredential($projName, $credName,
    {procedureName => 'StopManagedServer',
     stepName => 'StopInstance'});
$errors .= $ec->checkAllErrors($xpath);

$xpath = $ec->attachCredential($projName, $credName,
    {procedureName => 'StopAdminServer',
     stepName => 'StopAdminServer'});
$errors .= $ec->checkAllErrors($xpath);


$xpath = $ec->attachCredential($projName, $credName,
    {procedureName => 'CheckServerStatus',
     stepName => 'CheckServerStatus'});
$errors .= $ec->checkAllErrors($xpath);

$xpath = $ec->attachCredential($projName, $credName,
    {procedureName => 'RunDeployer',
     stepName => 'RunJob'});
$errors .= $ec->checkAllErrors($xpath);

$xpath = $ec->attachCredential($projName, $credName,
    {procedureName => 'RunWLST',
     stepName => 'RunWLST'});
$errors .= $ec->checkAllErrors($xpath);

$xpath = $ec->attachCredential($projName, $credName,
    {procedureName => 'StartApp',
     stepName => 'StartApp'});
$errors .= $ec->checkAllErrors($xpath);

$xpath = $ec->attachCredential($projName, $credName,
    {procedureName => 'StopApp',
     stepName => 'StopApp'});
$errors .= $ec->checkAllErrors($xpath);

$xpath = $ec->attachCredential($projName, $credName,
    {procedureName => 'DeployApp',
     stepName => 'DeployApp'});
$errors .= $ec->checkAllErrors($xpath);

$xpath = $ec->attachCredential($projName, $credName,
    {procedureName => 'UndeployApp',
     stepName => 'UndeployApp'});
$errors .= $ec->checkAllErrors($xpath);

$xpath = $ec->attachCredential($projName, $credName,
    {procedureName => 'StartNodeManager',
     stepName => 'StartNodeManager'});
$errors .= $ec->checkAllErrors($xpath);

$xpath = $ec->attachCredential($projName, $credName,
    {procedureName => 'StopNodeManager',
     stepName => 'StopNodeManager'});
$errors .= $ec->checkAllErrors($xpath);

$xpath = $ec->attachCredential($projName, $credName,
    {procedureName => 'CreateDatasource',
     stepName => 'CreateDatasource'});
$errors .= $ec->checkAllErrors($xpath);

$xpath = $ec->attachCredential($projName, $credName,
    {procedureName => 'DeleteDatasource',
     stepName => 'DeleteDatasource'});
$errors .= $ec->checkAllErrors($xpath);

$xpath = $ec->attachCredential($projName, $credName,
    {procedureName => 'SuspendServer',
     stepName => 'SuspendServer'});
$errors .= $ec->checkAllErrors($xpath);

$xpath = $ec->attachCredential($projName, $credName,
    {procedureName => 'ResumeServer',
     stepName => 'ResumeServer'});
$errors .= $ec->checkAllErrors($xpath);

# new procedures
$xpath = $ec->attachCredential($projName, $credName,
    {procedureName => 'CreateUser',
     stepName => 'CreateUser'});
$errors .= $ec->checkAllErrors($xpath);

$xpath = $ec->attachCredential($projName, $credName,
    {procedureName => 'DeleteUser',
     stepName => 'DeleteUser'});
$errors .= $ec->checkAllErrors($xpath);

$xpath = $ec->attachCredential($projName, $credName,
    {procedureName => 'ChangeUserPassword',
     stepName => 'ChangeUserPassword'});
$errors .= $ec->checkAllErrors($xpath);

$xpath = $ec->attachCredential($projName, $credName,
    {procedureName => 'CreateGroup',
     stepName => 'CreateGroup'});
$errors .= $ec->checkAllErrors($xpath);

$xpath = $ec->attachCredential($projName, $credName,
    {procedureName => 'DeleteGroup',
     stepName => 'DeleteGroup'});
$errors .= $ec->checkAllErrors($xpath);

$xpath = $ec->attachCredential($projName, $credName,
    {procedureName => 'StartCluster',
     stepName => 'StartCluster'});
$errors .= $ec->checkAllErrors($xpath);

$xpath = $ec->attachCredential($projName, $credName,
    {procedureName => 'StopCluster',
     stepName => 'StopCluster'});
$errors .= $ec->checkAllErrors($xpath);

if ($errors ne '') {
    
    # Cleanup the partially created configuration we just created
    $ec->deleteProperty($configPath);
    $ec->deleteCredential($projName, $credName);
    my $errMsg = 'Error creating configuration credential: ' . $errors;
    $ec->setProperty("/myJob/configError", $errMsg);
    print $errMsg;
    exit ERROR;
    
}
