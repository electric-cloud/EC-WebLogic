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
        'configname',
        'host_name',
        'port',
        'domainName',
        'domainPath',
        'wlstabspath',
        'maxelapsedtime',
        'nmType'
    );

    $params->{maxelapsedtime} ||= 60;
    if ($params->{maxelapsedtime} !~ m/^\d+$/s) {
        $wl->bail_out("Timeout should be a positive integer value");
    }
    $params->{host_name} ||= 'localhost';
    if (!$params->{port} || $params->{port} !~ m/^\d+$/s) {
        $params->{port} = 5556;
    }
    $params->{domainName} ||= 'mydomain';

    my $check = $wl->check_executable($params->{wlstabspath});
    unless ($check->{ok}) {
        $wl->bail_out($check->{msg});
    }
    my $cred = $wl->get_credentials($params->{configname});
    my $render_params = {
        username => $cred->{user},
        password => $cred->{password},

        hostname => $params->{host_name},
        port => $params->{port},
        domain_name => $params->{domainName},
        domain_path => $params->{domainPath},
        node_manager_type => $params->{nmType}
    };

    my $template_path = '/myProject/jython/stop_node_manager.jython';
    my $template = $wl->render_template_from_property($template_path, $render_params);
    $wl->out(10, "Script: $template");
    my $res = $wl->execute_jython_script(
        shell => $params->{wlstabspath},
        script_path => $ENV{COMMANDER_WORKSPACE} . '/exec.jython',
        script_content => $template,
    );
    $wl->process_response($res);
}

