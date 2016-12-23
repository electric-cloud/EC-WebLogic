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

    my $params = $wl->get_params_as_hashref(
        'appname',
        'wlstabspath',
        'retire_gracefully',
        'configname',
        'version_identifier',
        'additional_options',
        'give_up'
    );

    my $cred = $wl->get_credentials($params->{configname});
    my $retire_gracefully = 'false';

    if ($params->{retire_gracefully}) {
        $retire_gracefully = 'true';
    }

    $wl->out(1, "AdditionalOptions: $params->{additional_options}");
    my $give_up = 0;
    if ($params->{give_up}) {
        $give_up = 1;
    }
    my $render_params = {
        username => $cred->{user},
        password => $cred->{password},
        admin_url => $cred->{weblogic_url},
        app_name => $params->{appname},
        retire_gracefully => $retire_gracefully,
        additional_options => $params->{additional_options},
        version_identifier => $params->{version_identifier},
        give_up => $give_up
    };

    my $template_path = '/myProject/jython/undeploy_app.jython';
    my $template = $wl->render_template_from_property($template_path, $render_params);

    $wl->out(10, "Generated script:\n", $template);
    my $res = $wl->execute_jython_script(
        shell => $params->{wlstabspath},
        script_path => $ENV{COMMANDER_WORKSPACE} . '/exec.jython',
        script_content => $template,
    );

    $wl->process_response($res);
    return;
}
