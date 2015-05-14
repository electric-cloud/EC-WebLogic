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
        'instancename',
        'successcriteria',
        'maxelapsedtime'
    );
    my $config_name = $params->{configname};

    if ($params->{maxelapsedtime} && $params->{maxelapsedtime} !~ m/^\d+$/s) {
        $wl->bail_out("Max elapsed time must be a number");
    }

    if ($params->{maxelapsedtime}) {
        $wl->out(1, "Maximum elapsed time was set to: ", $params->{maxelapsedtime});
    }
    my $cred = $wl->get_credentials($config_name);
    if ($cred->{java_home}) {
        $ENV{JAVA_HOME} = $cred->{java_home};
    }
    my $render_params = {
        username => $cred->{user},
        password => $cred->{password},
        servername => $params->{instancename},
        admin_url => $cred->{weblogic_url}
    };
    my $template_path = '/myProject/jython/check_server_status.jython';

    my $script = $wl->render_template_from_property($template_path, $render_params);
    my $path = $ENV{COMMANDER_WORKSPACE} . '/exec.jython';
    open FH, '>>', $path;
    print FH $script;
    close FH;

    my $exec_result;
    my $cmd = "$params->{wlstabspath} $path";
    if ($params->{maxelapsedtime} && $params->{maxelapsedtime} > 0) {
        $exec_result = $wl->do_while(
            sub {
                return check_server_status($wl, $params, $cmd);
            },
            1,
            $params->{maxelapsedtime}
        );
    }
    else {
        $exec_result = check_server_status($wl, $params, $cmd);
    }

    if ($exec_result > 0) {
        $wl->out(1, "Criteria was met");
        $wl->success();
        return;
    }

    $wl->out(1, "Criteria wasn't met");
    $wl->error();
    return;
};

sub check_server_status {
    my ($wl, $params, $cmd) = @_;

    $wl->out(1, '=' x 60);
    $wl->out(1, "Checking server status");
    my $result = $wl->run_command($cmd);

    if ($result->{stderr} =~ m/^TIMEOUT\n$/s) {
        return -2;
    }
    $wl->out(1, "Success criteria: ", $params->{successcriteria});
    my ($criteria, $server_running) = (0, 0);
    $criteria = $params->{successcriteria} eq 'RUNNING' ? 1 : 0;

    $wl->out(1, "Command output: ", $result->{stdout});

    if ($result->{stdout} =~ m/Server\sState:NO_SERVER_FOUND/s) {
        # error
        # $wl->error("NO_SERVER_FOUND");
        $wl->out(1, "NO_SERVER_FOUND");
        return -1;
    }

    if ($result->{stdout} =~ m/Server\sState:\sRUNNING/s) {
        $server_running = 1;
    }

    if ($criteria == $server_running) {
        # Success, criteria was met
        return 1;
    }

    # criteria wasn't met
    return 0;
}

