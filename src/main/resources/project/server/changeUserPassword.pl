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

    my $params = $wl->get_params_as_hashref(
        'wlst_abs_path',
        'configname',
        'old_user_credentials',
        'new_user_credentials',
        'domain_name',
        'realm_name'
    );

    my $cred = $wl->get_credentials($params->{configname});
    my $old_user_creds = $wl->get_common_credentials('old_user_credentials');
    my $new_user_creds = $wl->get_common_credentials('new_user_credentials');

    $params->{wlst_abs_path} = $cred->{wlst_path} unless ($params->{wlst_abs_path});
    my $check = $wl->check_executable($params->{wlst_abs_path});

    if (!$check->{ok}) {
        $wl->bail_out($check->{msg});
    }

    my $render_params = {
        wl_username => $cred->{user},
        wl_password => $cred->{password},
        admin_url => $cred->{weblogic_url},
        old_user => $old_user_creds->{user},
        old_password => $old_user_creds->{password},
        new_user => $new_user_creds->{user},
        new_password => $new_user_creds->{password},
        domain_name => $params->{domain_name},
        realm_name => $params->{realm_name}
    };
    my $template_path = '/myProject/jython/change_user_password.jython';
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
