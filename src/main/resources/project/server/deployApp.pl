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
        'appname',
        'apppath',
        'configname',
        'targets',
        'is_library',
        ### new options ###
        'stage_mode',
        'deployment_plan',
        'plan_path',
        'additional_options',
        'archive_version',
        'plan_version',
        'retire_gracefully',
        'retire_timeout',
        'version_identifier',
        'upload',
        'overwrite_deployment_plan',
        'remote'
    );

    my $is_library = 'false';
    my $upload = 'false';
    my $remote = 'false';
    my $retire_gracefully = 'false';

    if ($params->{upload} && $params->{remote}) {
        $wl->bail_out(q|"Remote?" and "Upload?" options couldn't be used at the same time.|);
    }
    if ($params->{is_library}) {
        $is_library = 'true';
    }
    if ($params->{upload}) {
        $upload = 'true';
    }
    if ($params->{remote}) {
        $remote = 'true';
    }
    if ($params->{retire_gracefully}) {
        $retire_gracefully = 'true';
    }

    my $cred = $wl->get_credentials($params->{configname});
    #TO BE CHANGED TO THE NAME WITH _ _
    $params->{wlstabspath} = $cred->{wlst_path} unless ($params->{wlstabspath});

    my $check = $wl->check_executable($params->{wlstabspath});
    unless ($check->{ok}) {
        $wl->bail_out($check->{msg});
    }

    $wl->write_deployment_plan(
        path => $params->{plan_path},
        content => $params->{deployment_plan},
        overwrite => $params->{overwrite_deployment_plan}
    );

    
    my $render_params = {
        username => $cred->{user},
        password => $cred->{password},
        admin_url => $cred->{weblogic_url},

        targets => $params->{targets},
        app_name => $params->{appname},
        app_path => $params->{apppath},
        is_library => $is_library,
        ### new options ###
        stage_mode => $params->{stage_mode},
        # deployment_plan => $params->{deployment_plan},
        plan_path => $params->{plan_path},
        additional_options => $params->{additional_options},
        archive_version => $params->{archive_version},
        plan_version => $params->{plan_version},
        retire_gracefully => $retire_gracefully,
        retire_timeout => $params->{retire_timeout},
        version_identifier => $params->{version_identifier},
        upload => $upload,
        remote => $remote
    };

    my $template_path = '/myProject/jython/deploy_app.jython';
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

