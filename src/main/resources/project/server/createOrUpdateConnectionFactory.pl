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
        'cf_name',
        'jndi_name',
        'cf_sharing_policy',
        'cf_client_id_policy',
        'cf_max_messages_per_session',
        'cf_xa_enabled',
        'cf_attach_jmsx_userid',
        'jms_module_name',
        'subdeployment_name',
        'jms_server_name',
        'server_name'
    );
    my $cred = $wl->get_credentials($params->{configname});
    if ( $cred->{java_home} ) {
        $wl->out( 1, "JAVA_HOME was provided" );
    }
    $params->{wlstabspath} = $cred->{wlst_path} unless ($params->{wlstabspath});

    my $render_params = {
        username     => $cred->{user},
        password     => $cred->{password},
        weblogic_url => $cred->{weblogic_url},
        admin_url    => $cred->{weblogic_url},
    };

    my $wlst_path = $wl->get_wlst_path($params, $cred);

    #rewrite to map when get chance
    $render_params->{$_} = $params->{$_} foreach keys %{ $params };

    my $template_path = '/myProject/jython/create_or_update_connection_factory.jython';
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
