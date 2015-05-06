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

    return $self->SUPER::get_credentials($config_name, 'weblogic_cfgs');
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

