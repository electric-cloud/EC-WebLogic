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
    my $params = $wl->get_params_as_hashref('wlstabspath', 'configname', 'instancename', 'successcriteria');
    my $config_name = $params->{configname};

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

    my $cmd = "$params->{wlstabspath} $path";
    my $result = $wl->run_command($cmd);

    $wl->out(1, "Success criteria: ", $params->{successcriteria});
    $wl->out(1, "Result: ", Dumper $result);
    #     $wl->process_response($result);

    my ($criteria, $server_running) = (0, 0);
    $criteria = $params->{successcriteria} eq 'RUNNING' ? 1 : 0;

    if ($result->{stdout} =~ m/Server\sState:NO_SERVER_FOUND/s) {
        $wl->error("NO_SERVER_FOUND");
        return;
    }

    if ($result->{stdout} =~ m/Server\sState:\sRUNNING/s) {
        $server_running = 1;
    }

    if ($criteria == $server_running) {
        $wl->success();
        return 1;
    }

    $wl->error("Server running: $server_running");
    return 1;
};

