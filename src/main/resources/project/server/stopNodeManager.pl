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
        'configname',
        'hostName',
        'port',
        'domainName',
        'domainPath',
        'wlstabspath',
        'maxelapsedtime',
        'nmType'
    );

    $params->{hostName} ||= 'localhost';
    if (!$params->{port} || $params->{port} !~ m/^\d+$/s) {
        $params->{port} = 5556;
    }
    $params->{domainName} ||= 'mydomain';

    my $check = $wl->check_executable($params->{wlstabspath});
    unless ($check->{ok}) {
        $wl->bail_out($check->{msg});
    }
    my $cred = $wl->get_credentials($params->{configname});
    my $render_params = {
        username => $cred->{user},
        password => $cred->{password},

        hostname => $params->{hostName},
        port => $params->{port},
        domain_name => $params->{domainName},
        domain_path => $params->{domainPath},
        node_manager_type => $params->{nmType}
    };

    my $template_path = '/myProject/jython/stop_node_manager.jython';
    my $template = $wl->render_template_from_property($template_path, $render_params);
    $wl->out(10, "Script: $template");
    my $res = $wl->execute_jython_script(
        shell => $params->{wlstabspath},
        script_path => $ENV{COMMANDER_WORKSPACE} . '/exec.jython',
        script_content => $template,
    );
    $wl->process_response($res);
}

