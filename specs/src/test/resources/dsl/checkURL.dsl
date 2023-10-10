def projName = args.projectName
def resource = args.resourceName
def url = args.URL
def procedureName = 'CheckURL'

project projName, {
    procedure procedureName, {
        resourceName = resource

        step 'RunProcedure', {
            shell = 'cb-perl'
            command = '''
use strict;
use warnings;

use LWP::UserAgent;
use ElectricCommander;

# Disable SSL checks
$ENV{'PERL_LWP_SSL_VERIFY_HOSTNAME'} = 0;

my $ua = LWP::UserAgent->new;
#$ua->ssl_opts( verify_hostnames => 0 ,SSL_verify_mode => 0x00);

my $ec = ElectricCommander->new();

my $url = \'$[url]\';

my $request = HTTP::Request->new(GET => $url);
#print Dumper $request;

print $url . "\\n";

my $res = $ua->request($request);
print "after request \\n";


$ec->setProperty('/myJob/code', $res->code());
print $res->code() . "\\n";
    
if ($res->is_success()) {     
    my $bytes = $res->content();
    $ec->setProperty('/myJob/text', $bytes); 
    print $bytes;
}

print "after_response";
exit 0;

            '''
        }

        formalParameter 'url', defaultValue: url, {
            type = "textarea"
        }

        // Custom properties
        code = ''
        text = ''
    }


}



