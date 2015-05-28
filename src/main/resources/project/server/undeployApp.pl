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

    my $params = $wl->get_params_as_hashref(
        'appname',
        'wlstabspath',
        'gracefulmode',
        'configname'
    );

    my $cred = $wl->get_credentials($params->{configname});
    my $gracefulmode = 'false';

    if ($params->{gracefulmode}) {
        $params->{gracefulmode} = 'true'
    }

    my $render_params = {
        username => $cred->{user},
        password => $cred->{password},
        admin_url => $params->{weblogic_url},
        app_name => $params->{appname},
        gracefulmode => $gracefulmode,
    };

    my $template_path = '/myProject/jython/undeploy_app.jython';
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
