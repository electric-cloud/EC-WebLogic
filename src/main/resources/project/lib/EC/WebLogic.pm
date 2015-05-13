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

1;

