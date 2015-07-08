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
        'instancename',
        'configname',
        'wlstabspath',
        'maxelapsedtime',
        'scriptlocation',
        'maxelapsedtime',
        'adminserverurl'
    );

    my $cred = $wl->get_credentials($params->{configname});
    if ($params->{maxelapsedtime} !~ m/^\d+$/s) {
        $params->{maxelapsedtime} = 180;
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
        qq|"exec(q{%s$params->{scriptlocation}%s %s$params->{instancename}%s %s$params->{adminserverurl}%s 1> %s$out_log%s 2> %s$err_log%s})"|,
        $sep, $sep, $sep, $sep, $sep, $sep, $sep, $sep, $sep, $sep
    );
    $wl->out(1, "Running command $command");
    $ENV{JAVA_OPTIONS} =
        "-Dweblogic.management.username=$cred->{user} -Dweblogic.management.password=$cred->{password}";
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


    my $render_params = {
        username => $cred->{user},
        password => $cred->{password},
        servername => $params->{instancename},
        admin_url => $cred->{weblogic_url}
    };
    my $template_path = '/myProject/jython/check_server_status.jython';
    my $template = $wl->render_template_from_property($template_path, $render_params);
    $wl->out(10, "Generated script:\n", $template);
    while (time() < $max_ts) {
        my $res = $wl->execute_jython_script(
            shell => $params->{wlstabspath},
            script_path => $ENV{COMMANDER_WORKSPACE} . '/exec.jython',
            script_content => $template,
        );
        if ($wl->dryrun() || $res->{stdout} =~ m/Server\sState:\sRUNNING/ms) {
            $wl->out(1, "EXIT_CODE: $params->{code}");
            $wl->out(1, "STDOUT:\n", $res->{stdout}) if $res->{stdout};
            $wl->out(1, "STDERR:\n", $res->{stderr}) if $res->{stderr};
            $wl->success("Managed server $params->{instancename} was started");
            exit 0;
        }
        sleep 10;
    }
    $wl->out(1, "EXIT_CODE: $params->{code}");
    $wl->out(1, "STDOUT:\n", $res->{stdout}) if $res->{stdout};
    $wl->out(1, "STDERR:\n", $res->{stderr}) if $res->{stderr};
    $wl->error("Failed to start Managed Server $params->{instancename}");
    exit 1;
}

