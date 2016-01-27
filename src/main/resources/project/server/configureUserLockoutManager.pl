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

$|=1;

main();


sub main {
    my $wl = EC::WebLogic->new(
        project_name => $PROJECT_NAME,
        plugin_name => $PLUGIN_NAME,
        plugin_key => $PLUGIN_KEY
    );
    #Every additional option should have following format <OPTION_NAME1>=<OPTION_VALUE1>, <OPTION_NAME2>=<OPTION_VALUE2>
    #Options separated by commas
    my $params = $wl->get_params_as_hashref(
        'wlst_abs_path',
        'lockout_enabled',
        'lockout_threshold',
        'lockout_duration',
        'configname'
    );

    my $cred = $wl->get_credentials($params->{configname});

    my $check = $wl->check_executable($params->{wlst_abs_path});

    if (!$check->{ok}) {
        $wl->bail_out($check->{msg});
    }

    my $render_params = {
        username => $cred->{user},
        password => $cred->{password},
        admin_url => $params->{weblogic_url},
        lockout_enabled => $params->{lockout_enabled},
        lockout_threshold => $params->{lockout_threshold},
        lockout_duration => $params->{lockout_duration},
    };
    my $template_path = '/myProject/jython/configure_user_lockout_manager.jython';
    my $template = $wl->render_template_from_property($template_path, $render_params);

    $wl->out(10, "Generated script:\n", $template);
    my $res = $wl->execute_jython_script(
        shell => $params->{wlst_abs_path},
        script_path => $ENV{COMMANDER_WORKSPACE} . '/exec.jython',
        script_content => $template,
    );

    $wl->process_response($res);
    return;
}
