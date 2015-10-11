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
    my $params = $wl->get_params_as_hashref(
        'scriptlocation',
        'maxelapsedtime'
    );

    $params->{maxelapsedtime} ||= 60;
    if ($params->{maxelapsedtime} !~ m/^\d+$/s) {
        $wl->bail_out("Timeout should be a positive integer value");
    }
    my $check = $wl->check_executable($params->{scriptlocation});
    unless ($check->{ok}) {
        $wl->bail_out($check->{msg});
    }

    my $log_path = $ENV{COMMANDER_WORKSPACE};
    my $sep = $wl->is_win() ? '\\' : '/';
    my $err_log = $log_path . $sep . 'ecdaemon.err.log';
    open TMP, '>', $err_log;
    close TMP;
    my $out_log = $log_path . $sep . 'ecdaemon.out.log';
    open TMP, '>', $out_log;
    close TMP;
    my $command = qq|ecdaemon -- ec-perl -e |;
    $sep = $wl->is_win() ? q|"| : q|'|;
    $command .= sprintf(
        qq|"exec(q{%s$params->{scriptlocation}%s 1> %s$out_log%s 2> %s$err_log%s})"|,
        $sep, $sep, $sep, $sep, $sep, $sep
    );

    $wl->out(1, "Running command $command");
    my $res;
    if ($wl->is_win()) {
        system(1, $command);
        $res->{code} = 0;
    }
    else {
        $res = $wl->run_command($command);
    }
    if ($res->{code} != 0) {
        if ($res->{stdout}) {
            $wl->out(1, "STDOUT:\n", $res->{stdout});
        }
        if ($res->{stderr}) {
            $wl->out(1, "STDERR:\n", $res->{stderr});
        }
        $wl->bail_out("Exit code: $res->{code}");
    }
    my $content;
    my $max_ts = time() + $params->{maxelapsedtime};
    while (time() < $max_ts) {
        $content = '';
        open FH, $err_log or $wl->bail_out("Can't open log: $!");

        while (my $line = <FH>) {
            $content .= $line;

        }
        if ($content =~ m/started\son\sport/ms) {
            $wl->out(1, "RESULT:\n", $content);
            $wl->success();
            exit 0;
        }
        if ($content =~ m/could\snot\sobtain\sexclusive\slock/ims) {
            $wl->out(1, "RESULT:\n", $content);
            $wl->error('Could not obtain exclusive lock, maybe, NodeManager is already running.');
            exit 1;
        }
        $wl->out(1, "Not started, will try again");
        sleep 1;
    }

    $wl->out(1, "RESULT:\n$content");
    $wl->error();
}

