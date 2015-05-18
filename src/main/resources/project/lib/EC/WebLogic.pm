package EC::WebLogic;
use strict;
use warnings;
use Data::Dumper;
use ElectricCommander;
use Carp;

use base 'EC::Plugin::Core';

sub after_init_hook {
    my ($self, %params) = @_;

    $self->{plugin_name} = 'EC-WebLogic';
    my $dryrun = 0;

    if ($self->{plugin_key}) {
        eval {
            $dryrun = $self->ec()->getProperty(
                "/plugins/$self->{plugin_key}/project/dryrun"
            )->findvalue('//value')->string_value();
        };
    }
    if ($dryrun) {
        $self->dryrun(1);
    }
}


sub get_credentials {
    my ($self, $config_name) = @_;

    return $self->SUPER::get_credentials(
        $config_name => {
            userName => 'user',
            password => 'password',
            java_home => 'java_home',
            weblogic_url => 'weblogic_url'
        },
        'weblogic_cfgs');
}


sub get_common_credentials {
    my ($self, $cred_name) = @_;

    my $xpath = $self->ec()->getFullCredential($cred_name, {
        jobStepId => $ENV{COMMANDER_JOBSTEPID}
    });
    if (!defined $xpath) {
        $self->error("Can't find common credential", $cred_name);
    }

    my $credentials = {
        user => $xpath->findvalue('//credential/userName') . '',
        password => $xpath->findvalue('//credential/password') . ''
    };

    return $credentials;
}


sub process_response {
    my ($self, $result) = @_;

    if (!exists $result->{stdout} || !exists $result->{stderr} || !exists $result->{code}) {
        $self->bail_out("Unknown error occured");
    }
    # result code is > 0, so, it's an error
    if ($result->{code}) {
        $self->error($result->{stderr});
    }
}

## %arams = (
## shell => '/path/to/wlst.sh,
## timeout => 100,
## options => '-a b -c d',
## script_path => '/path/to/jython_script',
## script_content => 'print hello world',
## )
sub execute_jython_script {
    my ($self, %params) = @_;

    if (!$params{shell}) {
        croak "Missing shell param";
    }

    my $check = $self->dryrun() ?
        {ok => 1} : $self->check_executable($params{shell});

    unless ($check->{ok}) {
        $self->bail_out($check->{msg});
    }

    if (!$params{script_path}) {
        croak "Missing script_path parameter";
    }

    if ($params{script_content} && -e $params{script_path}) {
        !$self->dryrun() && croak "Script file $params{script_path} already exists";
    }

    if (!$self->dryrun() && $params{script_content}) {
        open FH, '>', $params{script_path};
        print FH $params{script_content};
        close FH;
    }

    my $command = $params{shell} . ' ';
    if ($params{options}) {
        $command .= $params{options} . ' ';
    }

    $command .= $params{script_path};
    my $retval;
    $self->set_property(wlstLine => $command);
    $retval = $self->run_command($command);

    # cleanup now.
    if ($params{script_content}) {
        $self->out(1, "Unlinking file $params{script_path}");
        !$self->dryrun() && unlink $params{script_path};
    }

    return $retval;
}



1;

