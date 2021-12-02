#
#  Copyright 2021 CloudBees, Inc.
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

#*****************************************************************************
use strict;
use warnings;

use ElectricCommander;
use ElectricCommander::PropDB;
use ElectricCommander::PropMod;

use Data::Dumper;

#*****************************************************************************
use constant {
    SUCCESS => 0,
    ERROR   => 1,
};

#*****************************************************************************
my $ec = ElectricCommander->new();
$ec->abortOnError(0);

my $projName = '$[/myProject/projectName]';
my $pluginName = '@PLUGIN_NAME@';
my $pluginKey = '@PLUGIN_KEY@';

ElectricCommander::PropMod::loadPerlCodeFromProperty($ec, '/myProject/EC::Plugin::Core');
ElectricCommander::PropMod::loadPerlCodeFromProperty($ec, '/myProject/EC::WebLogic');

my $wl = EC::WebLogic->new(
    project_name => $projName,
    plugin_name  => $pluginName,
    plugin_key   => $pluginKey,
);

my $retval = $wl->testConnection();
if ($retval == SUCCESS) {
    $wl->logger->info("Successfully connected to the WebLogic instance.");
}

exit $retval;

#*****************************************************************************
