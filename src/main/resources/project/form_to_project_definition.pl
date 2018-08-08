use strict;
use warnings;
use XML::Simple qw(XMLout XMLin);
use Data::Dumper;


my $form_xml_path = $ARGV[0] or die "no path";
open my $fh, $form_xml_path or die $!;
my $xml = join('', <$fh>);
close $fh;


my $struct = XMLin($xml);
die Dumper $struct;
