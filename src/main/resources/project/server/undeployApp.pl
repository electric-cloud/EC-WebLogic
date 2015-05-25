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
        'javapath',
        'gracefulmode',
        'javaparams',
        'configname',
        'additionalcommands',
        'envscriptpath',
        'webjarpath',
    );

    my $cred = undef;

    if ($params->{configname}) {
        $cred = $wl->get_credentials($params->{configname});
    }

    my $command = $params->{javapath};

    if ($params->{javaparams}) {
        $command .= " $params->{javaparams} ";
    }

    if ($params->{webjarpath}) {
        $ENV{CLASSPATH} .= $params->{webjarpath};
    }

    if ($params->{envscriptpath}) {
        my $check = $wl->check_executable($params->{envscriptpath});
        if (!$check->{ok}) {
            $wl->bail_out($check->{msg});
        }

        `$params->{envscriptpath}`;
    }

    $command .= ' ' . $MAIN_CLASS;

    if ($cred) {
        if ($cred->{weblogic_url}) {
            $command .= ' -adminurl ' . $cred->{weblogic_url};
        }

        if ($cred->{user}) {
            $command .= ' -username ' . $cred->{user};
        }
        if ($cred->{password}) {
            $command .= ' -password ' . $cred->{password};
        }
    }

    $command .= ' -undeploy ';
    $command .= ' -name ' . $params->{appname};

    if ($params->{additionalcommands}) {
        $command .= " $params->{additionalcommands} ";
    }

    $wl->set_property(undeployAppLine => $wl->safe_cmd($command));
    my $res = $wl->run_command($command);
    $wl->process_response($res);
}
