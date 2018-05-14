#
#  Copyright 2015 Electric Cloud, Inc.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

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
        'successcriteria',
        'configname'
    );
    
    my ($config_name, $cred);
    if ($params->{configname}){
        $config_name = $params->{configname};
        $cred = $wl->get_credentials($config_name);
    }

    my $basic_cred = {};

    if ($params->{credentialName}) {
        $basic_cred = $wl->get_common_credentials('credentialName');
    }
    $wl->out(1, "Testing url: ", $params->{targeturl});
    my $ua = LWP::UserAgent->new();

    my $req = HTTP::Request->new(GET => $params->{targeturl});
    if ($params->{maxelapsedtime} && $params->{maxelapsedtime} =~ m/^\d+$/s) {
        $ua->timeout($params->{maxelapsedtime});
        $wl->out(1, "Timeout set to ", $params->{maxelapsedtime});
    }

    if (%$basic_cred) {
        $req->authorization_basic($basic_cred->{user}, $basic_cred->{password});
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
