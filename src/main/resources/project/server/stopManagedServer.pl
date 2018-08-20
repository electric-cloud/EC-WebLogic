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

# preamble.pl
$[/myProject/preamble]

my $PROJECT_NAME = '$[/myProject/projectName]';
my $PLUGIN_NAME = '@PLUGIN_NAME@';
my $PLUGIN_KEY = '@PLUGIN_KEY@';
use Data::Dumper;

$|=1;

main();

sub main {
    my $wl = EC::WebLogic->new(
        project_name => $PROJECT_NAME,
        plugin_name => $PLUGIN_NAME,
        plugin_key => $PLUGIN_KEY
    );
    my $params = $wl->get_params_as_hashref(
        'instancename',
        'configname',
        'scriptlocation'
    );

    my $cred = $wl->get_credentials($params->{configname});
    my $check = $wl->check_executable($params->{scriptlocation});
    unless ($check->{ok}) {
        $wl->bail_out($check->{msg});
    }

    my $log_path = $ENV{COMMANDER_WORKSPACE};
    my $sep = $wl->is_win() ? '\\' : '/';

    my $command = $params->{scriptlocation};
    if ($params->{instancename}) {
        $command .= " $params->{instancename}";
    }
    $command .= " $cred->{weblogic_url} $cred->{user} $cred->{password}";

    $wl->out(1, "Running command $command");
    $ENV{JAVA_OPTIONS} = "-Dweblogic.management.username=$cred->{user} -Dweblogic.management.password=$cred->{password}";
    my $res;
    $res = $wl->run_command($command);
    $wl->out(1, "STDOUT: $res->{stdout}");
    $wl->out(1, "STDERR: $res->{stderr}");
    $wl->out(1, 'Exit Code: ' . $res->{code});

    if ($res->{stdout} =~ /WLSTException:\s(.+)/) {
        $wl->bail_out($1);
    }

    # if ($wl->is_win()) {
    #     system(1, $command);
    #     $res->{code} = 0;
    # }
    # else {
    #     $res = $wl->run_command($command);
    # }

    if ($res->{code} != 0) {
        $wl->bail_out("Exit code: $res->{code}");
    }
}

