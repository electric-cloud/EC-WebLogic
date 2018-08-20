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

my $MAIN_CLASS = 'weblogic.Deployer';

use Data::Dumper;

$|=1;

main();


sub main {
    my $wl = EC::WebLogic->new(
        project_name => $PROJECT_NAME,
        plugin_name => $PLUGIN_NAME,
        plugin_key => $PLUGIN_KEY
    );

    # TODO: remove javapath, javaparams, additionalcommands, webjarpath, envscriptpath field.
    my $params = $wl->get_params_as_hashref(
        'wlstabspath',
        'servername',
        'ignoresessions',
        'configname',
        'timeoutserver',
        'force',
        'block'
    );

    if (!$params->{timeoutserver}) {
        $params->{timeoutserver} = 0;
    }
    my $cred = $wl->get_credentials($params->{configname});

    for my $key (qw/ignoresessions force block/) {
        if ($params->{$key}) {
            $params->{$key} = 'true';
        }
        else {
            $params->{$key} = 'false';
        }
    }
    my $render_params = {
        username => $cred->{user},
        password => $cred->{password},
        admin_url => $cred->{weblogic_url},

        server_name => $params->{servername},
        ignore_sessions => $params->{ignoresessions},
        timeout => $params->{timeoutserver},
        force => $params->{force},
        block => $params->{block}
    };

    if ($render_params->{timeout} !~ m/^\d+$/) {
        $wl->bail_out("Timeout should be a positive integer value");
    }
    my $template_path = '/myProject/jython/suspend_server.jython';
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

