# preamble.pl
$[/myProject/preamble]

use LWP::UserAgent;
use Data::Dumper;
use HTTP::Request;

my $PROJECT_NAME = '$[/myProject/projectName]';
my $PLUGIN_NAME = '@PLUGIN_NAME@';
my $PLUGIN_KEY = '@PLUGIN_KEY@';

$| = 1;

main();

sub main {
    my $wl = EC::WebLogic->new(
        project_name => $PROJECT_NAME
    );
    my $params = $wl->get_params_as_hashref(
        'credentialName',
        'maxelapsedtime',
        'targeturl',
        'successcriteria'
    );
    my $cred = {};

    if ($params->{credentialName}) {
        $cred = $wl->get_common_credentials('credentialName');
    }
    $wl->out(1, "Testing url: ", $params->{targeturl});
    my $ua = LWP::UserAgent->new();

    my $req = HTTP::Request->new(GET => $params->{targeturl});
    if ($params->{maxelapsedtime} && $params->{maxelapsedtime} =~ m/^\d+$/s) {
        $ua->timeout($params->{maxelapsedtime});
        $wl->out(1, "Timeout set to ", $params->{maxelapsedtime});
    }

    if (%$cred) {
        $req->authorization_basic($cred->{user}, $cred->{password});
    }

    my $regexps = {
        pagefound => '^2\d\d$',
        pagenotfound => '^404$',
        pageerror => '^(?:5\d\d|4\d\d)$'
    };
    my $criteria_met = sub {
        my $check = shift;

        if ($check =~ m/$regexps->{$params->{successcriteria}}/is) {
            return 1;
        }
        return 0;
    };

    my $result;
    eval {
        # $result = $ua->get($params->{targeturl});
        $result = $ua->request($req);
        1;
    } or do {
        $wl->error($@);
    };

    #     $wl->out(1, "Result: ", Dumper $result);
    if ($criteria_met->($result->code())) {
        $wl->out(1, "Criteria met");
        $wl->success();
        return;
    }

    $wl->out(1, "Criteria wasn't met");
    $wl->set_property(summary => "Criteria not met. HTTP response code: " . $result->code());
    $wl->error();
    return;
}
