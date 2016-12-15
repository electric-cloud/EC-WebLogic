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
    my $params = $wl->get_params_as_hashref(
        'wlstabspath',
        'configname',
        'clustername',
        'successcriteria',
        'maxelapsedtime'
    );

    $wl->out(1, "Executable file: ", $params->{wlstabspath});
    my $check = $wl->check_executable($params->{wlstabspath});

    unless ($check->{ok}) {
        $wl->bail_out($check->{msg});
    }
    $params->{wlstabspath} = $wl->esc_args($params->{wlstabspath});
    my $config_name = $params->{configname};
    my $cred = $wl->get_credentials($config_name);
    if ($cred->{java_home}) {
        $ENV{JAVA_HOME} = $cred->{java_home};
    }
    print Dumper $cred;
    exit 0;

}
#     my $render_params = {
#         username => $cred->{user},
#         password => $cred->{password},
#         servername => $params->{instancename},
#         admin_url => $cred->{weblogic_url}
#     };
#     my $template_path = '/myProject/jython/check_server_status.jython';

#     my $template = $wl->render_template_from_property($template_path, $render_params);

#     $wl->out(10, "Generated script:\n", $template);

#     my $path = $wl->generate_exec_path();
#     open FH, '>', $path;
#     print FH $template;
#     close FH;

#     my $exec_result;
#     $path = $wl->esc_args($path);
#     my $cmd = "$params->{wlstabspath} $path";
#     if ($params->{maxelapsedtime} && $params->{maxelapsedtime} > 0) {
#         $exec_result = $wl->do_while(
#             sub {
#                 return check_server_status($wl, $params, $cmd);
#             },
#             1,
#             $params->{maxelapsedtime}
#         );
#     }
#     else {
#         $exec_result = check_server_status($wl, $params, $cmd);
#     }

#     unlink $path;

#     if ($exec_result > 0) {
#         $wl->out(1, "Criteria was met");
#         $wl->success();
#         return;
#     }

#     $wl->out(1, "Criteria wasn't met");
#     $wl->error();
#     return;
# };

# sub check_server_status {
#     my ($wl, $params, $cmd) = @_;

#     $wl->out(1, '=' x 60);
#     $wl->out(1, "Checking server status");
#     my $result = $wl->run_command($cmd);

#     if ($result->{stderr} =~ m/^TIMEOUT\n$/s) {
#         return -2;
#     }
#     $wl->out(1, "Success criteria: ", $params->{successcriteria});
#     my ($criteria, $server_running) = (0, 0);
#     $criteria = $params->{successcriteria} eq 'RUNNING' ? 1 : 0;

#     $wl->out(1, "EXIT_CODE:", $result->{code});
#     $wl->out(1, "STDOUT: ", $result->{stdout});
#     $wl->out(1, "STDERR: ", $result->{stderr});

#     if ($result->{stdout} =~ m/Server\sState:NO_SERVER_FOUND/s) {
#         # error
#         # $wl->error("NO_SERVER_FOUND");
#         $wl->out(1, "NO_SERVER_FOUND");
#         return -1;
#     }

#     if ($result->{stdout} =~ m/Server\sState:\sRUNNING/s) {
#         $server_running = 1;
#     }

#     if ($criteria == $server_running) {
#         # Success, criteria was met
#         return 1;
#     }

#     # criteria wasn't met
#     return 0;
# }

