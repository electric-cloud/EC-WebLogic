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
my $PLUGIN_KEY  = '@PLUGIN_KEY@';
use Data::Dumper;
use File::Spec;

$| = 1;

main();

sub main {
    my $wl = EC::WebLogic->new(
        project_name => $PROJECT_NAME,
        plugin_name  => $PLUGIN_NAME,
        plugin_key   => $PLUGIN_KEY
    );
    my $params = $wl->get_params_as_hashref(
        'configname',
        'ecp_weblogic_jms_topic_name',
        'ecp_weblogic_jndi_name',
        'ecp_weblogic_jms_module_name',
        'ecp_weblogic_subdeployment_name',
        'ecp_weblogic_jms_server_name',
        'ecp_weblogic_update_action',
        'ecp_weblogic_additional_options'
    );
    my $cred = $wl->get_credentials($params->{configname});
    if ( $cred->{java_home} ) {
        $wl->out( 1, "JAVA_HOME was provided" );
    }

    my $render_params = {
        username     => $cred->{user},
        password     => $cred->{password},
        weblogic_url => $cred->{weblogic_url},
        admin_url    => $cred->{weblogic_url},
    };

    my $wlst_path = $wl->get_wlst_path();

    $render_params = { %$params, %$render_params };
    my $template_path = '/myProject/jython/create_or_update_jms_topic.jython';
    my $template = $wl->render_template_from_property( $template_path, $render_params );

    $wl->out(2, "Generated script:\n", $template);

    my $res = $wl->execute_jython_script(
        shell          => $wlst_path,
        script_path    => File::Spec->catfile($ENV{COMMANDER_WORKSPACE}, 'exec.jython'),
        script_content => $template,
    );
    $wl->process_response($res);
    return;
}

