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
        project_name => $PROJECT_NAME
    );
    my $params = $wl->get_params_as_hashref(qw{
        wlstabspath
        configname
        clustername
        successcriteria
        maxelapsedtime
    });

    my $config_name = $params->{configname};
    my $cred = $wl->get_credentials($config_name);

    my $maxelapsedtime = 0;
    if ($params->{maxelapsedtime}) {
        if ($params->{maxelapsedtime} !~ m/^[0-9]+$/s) {
            $wl->bail_out("Max elapsed time should be a positive integer.");
        }
        $maxelapsedtime = $params->{maxelapsedtime};
    }
    $params->{successcriteria} = $params->{successcriteria} eq 'RUNNING' ? 'RUNNING' : 'SHUTDOWN';
    my $render_params = {
        username => $cred->{user},
        password => $cred->{password},
        admin_url => $cred->{weblogic_url},
        maxelapsedtime => $maxelapsedtime,
        cluster_name => $params->{clustername},
        success_criteria => $params->{successcriteria}
    };
    my $template_path = '/myProject/jython/check_cluster_status.jython';
    my $template = $wl->render_template_from_property($template_path, $render_params);
    $wl->out(10, "Generated script:\n", $template);
    my $wlst_path = $wl->get_wlst_path($params, $cred);
    my $res = $wl->execute_jython_script(
        shell => $wlst_path,
        script_path => $ENV{COMMANDER_WORKSPACE} . '/exec.jython',
        script_content => $template,
    );

    $wl->process_response($res);
    return;
}
