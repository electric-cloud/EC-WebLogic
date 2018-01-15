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
    # $wl->dryrun(1);
    my $params = $wl->get_params_as_hashref(
        'scriptfilesource', # radio button
        'wlstabspath', # wlst abs path
        'scriptfile', # script file content
        'scriptfilepath', # jython script abs path
        'additionalcommands', # additional commands for jython
        'webjarpath', # path to wl.jar, will be included in the classpath
        'additional_envs', # additional environment variables
        'configname' #configuration
    );


    my ($config_name, $cred);
    if ($params->{configname}){
        $config_name = $params->{configname};
        $cred = $wl->get_credentials($config_name);
    }


    if ($params->{additional_envs}) {
        my $tagsmap = $wl->parse_tagsmap($params->{additional_envs});
        for my $key (keys %$tagsmap) {
            $wl->out(1, "Setting ENV variable '$key' to '$tagsmap->{$key}'");
            $ENV{$key} = $tagsmap->{$key};
        }
    }
    
    if ($cred){
        $params->{wlstabspath} = $cred->{wlst_path} unless ($params->{wlstabspath});
    }

    my %wl_params = (
        shell => $params->{wlstabspath},
        script_path => $params->{scriptfilepath}
    );

    if ($params->{additionalcommands}) {
        $wl_params{options} = $params->{additionalcommands};
    }

    if ($params->{scriptfilesource} eq 'newscriptfile') {
        # will create new script file
        my $content = $params->{scriptfile};
        $wl_params{script_content} = $content;
        $wl_params{script_path} = $ENV{COMMANDER_WORKSPACE} . '/exec.jython';
    }
    my $res = $wl->execute_jython_script(%wl_params);

    $wl->out(1, "EXIT_CODE: ", $res->{code}, "\n");
    $wl->out(1, "STDOUT: ", $res->{stdout}, "\n");
    $wl->out(1, "STDERR: ", $res->{stderr}, "\n");
    $wl->out(1, "DONE\n");
    if ($res->{code} ne 0) {
        $wl->error();
        return;
    }

    $wl->success();
    return;
}


