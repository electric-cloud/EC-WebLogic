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
    print "Log!";
    my $check = $wl->check_executable($params->{scriptlocation});
    unless ($check->{ok}) {
        $wl->bail_out($check->{msg});
    }

    my $log_path = $ENV{COMMANDER_WORKSPACE};
    my $sep = $wl->is_win() ? '\\' : '/';
    my $err_log = $log_path . $sep . 'ecdaemon.err.log';
    my $out_log = $log_path . $sep . 'ecdaemon.out.log';
    $wl->out(1, "Log path: $log_path");
    my $command = qq|ecdaemon -- ec-perl -e |;
    $command .= qq|"exec('$params->{scriptlocation} 1> $out_log 2> $err_log')"|;
    $wl->out(1, "Running command $command");
    my $res = $wl->run_command($command);
    if ($res->{code} != 0) {
        $wl->bail_out("Can't execute");
    }
    sleep $params->{maxelapsedtime};
    open FH, $err_log;
    my $content = join '', <FH>;
    if ($content =~ m/(?:Exception\s(.+)|Fatal\serror\s(.+))/) {
        $wl->error();
    }
    elsif ($content =~ m/(?:Successfully\s(.+)|started\son\sport\s(.+))/) {
        $wl->success();
    }
    else {
        $wl->warning();
    }

}
