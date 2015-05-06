=head1 NAME

preamble.pl

=head1 DESCRIPTION

Preamble for application server plugins. Imports necessary modules.

=cut

use strict;
use warnings;
use ElectricCommander::PropDB;
use ElectricCommander::PropMod;
use Carp;

$| = 1;

my $ec = ElectricCommander->new();

my $load = sub {
    my $property_path = shift;

    ElectricCommander::PropMod::loadPerlCodeFromProperty(
        $ec, $property_path
    ) or do {
        croak "Can't load property $property_path";
    };
};

$load->('/myProject/EC::Plugin::Core');
$load->('/myProject/EC::WebLogic');

