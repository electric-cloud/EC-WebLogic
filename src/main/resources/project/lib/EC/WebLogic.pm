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

package EC::WebLogic;
use strict;
use warnings;
use subs qw/parallel_exec_support/;

use Data::Dumper;
use ElectricCommander;
use Carp;

use base 'EC::Plugin::Core';

our $ENABLE_PARALLEL_EXEC_SUPPORT = 1;

# functions 
sub parallel_exec_support {
    my ($p) = @_;

    if (defined $p) {
        $ENABLE_PARALLEL_EXEC_SUPPORT = $p;
    }
    return $ENABLE_PARALLEL_EXEC_SUPPORT;
}


sub out {
    my ($self, $level, @message) = @_;

    if ($self->{_credentials}->{password}) {
        my $password = $self->{_credentials}->{password};
        $password = quotemeta($password);
        map {s/$password/********/gms;$_} @message;
    }
    $level ||= 1;
    return $self->SUPER::out(1, @message);
}


sub after_init_hook {
    my ($self, %params) = @_;

    $self->{plugin_name} = 'EC-WebLogic';
    $self->{_credentials} = {};
    my $dryrun = 0;

    if ($self->{plugin_key}) {
        eval {
            $dryrun = $self->ec()->getProperty(
                "/plugins/$self->{plugin_key}/project/dryrun"
            )->findvalue('//value')->string_value();
        };
    }
    if ($dryrun) {
        $self->dbg("Dryrun enabled");
        $self->dryrun(1);
    }
}


sub generate_exec_path {
    my $wl = shift;
    my $path;

    if (parallel_exec_support) {
        my $rnd = gen_random_numbers(42);
        $path = $ENV{COMMANDER_WORKSPACE} . "/exec_$rnd.jython";
    }
    else {
        $path = $ENV{COMMANDER_WORKSPACE} . '/exec.jython';
    }
    # $path = $wl->esc_args($path);
    $wl->out(1, "Path: $path");
    return $path;
}


sub get_credentials {
    my ($self, $config_name) = @_;

    my $cred = $self->SUPER::get_credentials(
        $config_name => {
            userName => 'user',
            password => 'password',
            java_home => 'java_home',
            java_vendor => 'java_vendor',
            mw_home => 'mw_home',
            weblogic_url => 'weblogic_url',
            debug_level => 'debug_level',
            enable_exclusive_sessions => 'enable_exclusive_sessions',
            enable_named_sessions => 'enable_named_sessions'
        },
        'weblogic_cfgs');

    if (defined $cred->{debug_level}) {
        $self->debug_level($cred->{debug_level});
        $self->out(3, "Debug level set to ", $self->debug_level())
    }

    if ($cred->{java_home}) {
        $ENV{JAVA_HOME} = $cred->{java_home};
        $self->out(10, "JAVA_HOME was set to $cred->{java_home}");
    }
    if ($cred->{java_vendor}) {
        $ENV{JAVA_VENDOR} = $cred->{java_vendor};
        $self->out(10, "JAVA_VENDOR was set to $cred->{java_vendor}");
    }
    if ($cred->{mw_home}) {
        $ENV{MW_HOME} = $cred->{mw_home};
        $self->out(10, "MW_HOME was set to $cred->{mw_home}");
    }

    $self->{_credentials} = $cred;
    return $cred;
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


sub get_step_credential {
    my ($self, $cred_name) = @_;

    return {} unless $cred_name;

    my $xpath = $self->ec->getFullCredential($cred_name);
    my $user_name = $xpath->findvalue('//userName')->string_value;
    my $password = $xpath->findvalue('//password')->string_value;

    return {userName => $user_name, password => $password};
}

sub write_deployment_plan {
    my ($self, %params) = @_;

    # no options was provided, so doing nothing.
    if (!$params{path} && !$params{content} && !$params{overwrite}) {
        return 1;
    }
    # if only overwrite checkbox provided
    if ($params{overwrite} && !$params{content}) {
        $self->bail_out(q|"Overwrite deployment plan?" flag should be used along with "Deployment plan path" and "Deployment plan content" parameters.|);
    }
    # deployment plan content was provided. No path was provided.
    if ($params{content} && !$params{path}) {
        $self->bail_out("Missing Deployment Path parameter. It is required, when Deployment Plan Content is provided.");
    }
    # if deployment plan file is empty and no content provided
    if ($params{path} && !$params{content} && !-s $params{path}) {
        $self->bail_out("Deployment plan $params{path} is empty.");
    }
    # deployment plan already exists and it is a directory.
    if (-e $params{path} && -d $params{path}) {
        $self->bail_out("$params{path} exists and it is a directory. Full path to the file is expected.");
    }
    # no overwrite flag was provided, file exists and not empty
    if (!$params{overwrite} && -e $params{path} && -s $params{path} && $params{content}) {
        $self->bail_out(qq|File $params{path} is already exists and not empty. Can't overwrite it without "Overwrite deployment plan?" flag enabled.|);
    }
    # if only path present, but no content, we don't need to write.
    if ($params{path} && !$params{content}) {
        return 1;
    }
    open(my $fh, '>:encoding(UTF-8)', $params{path});
    print $fh $params{content};
    close $fh;
    return 1;
}


sub process_response {
    my ($self, $result) = @_;

    if (!exists $result->{stdout} || !exists $result->{stderr} || !exists $result->{code}) {
        $self->bail_out("Unknown error occured");
    }

    $self->out(1, "EXIT_CODE: ", $result->{code}, "\n");
    $self->out(1, "STDOUT: ", $result->{stdout}, "\n");
    $self->out(1, "STDERR: ", $result->{stderr}, "\n");
    $self->out(1, "DONE\n");
    # result code is != 0, so, it's an error
    if ($result->{code} != 0) {
        for my $where (qw/stdout stderr/) {
            if ($result->{$where} =~ m/WLSTException:\s(.+)$/is) {
                $self->bail_out($1);
            }
            if ($result->{$where} =~ m/ERROR:(.+)$/s) {
                $self->bail_out($1);
            }
        }
        $self->error();
        return;
    }
    my @matches = $result->{stdout} =~ m/WARNING:(.+?)$/gm;
    my %seen = ();
    @matches = grep {!$seen{$_}++} @matches;
    if (@matches) {
        $self->warning( join("\n", @matches));
        return;
    }
    $self->success();
    return;
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
    # augmenting scipt path with random numbers if parallel exec is enabled;
    if (parallel_exec_support) {
        my $rnd = gen_random_numbers(42);
        $rnd = '_' . $rnd;
        $params{script_path} =~ s/(\.[\w]+?)$/$rnd$1/s;
        $self->out(1, "Script path: ", $params{script_path});
    }
    if ($params{script_content} && -e $params{script_path}) {
        !$self->dryrun() && croak "Script file $params{script_path} already exists";
    }

    if (!$self->dryrun() && $params{script_content}) {
        open FH, '>', $params{script_path};
        print FH $params{script_content};
        close FH;
    }

    my $command = $self->esc_args($params{shell}) . ' ';

    if ($params{options}) {
        $command .= $params{options} . ' ';
    }

    my $script_path = $params{script_path};
    $script_path = $self->esc_args($script_path);
    $command .= $script_path;
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


# extending safe_cmd from the base class
sub safe_cmd {
    my ($wl, $command) = @_;

    $command =~ s/-password.+?\s/-password *** /s;

    return $command;
}


sub gen_random_numbers {
    my ($mod) = @_;

    my $rand = rand($mod);
    $rand =~ s/\.//s;
    return $rand;
}


sub render_template_from_property {
    my ($self, $template_name, $params) = @_;

    $params ||= {};

    my $preamble_params = {
        enable_exclusive_sessions => 0,
        enable_named_sessions => 0,
        debug_level => $self->{_credentials}->{debug_level}
    };

    if ($self->{_credentials}->{enable_named_sessions}) {
        $preamble_params->{enable_named_sessions} = 1;
    }

    $params->{preamble} = $self->SUPER::render_template_from_property('/myProject/jython/preamble.jython', $preamble_params);
    # $params->{preamble} = $self->get_param('/myProject/jython/preamble.jython');
    return $self->SUPER::render_template_from_property($template_name, $params);
}


1;

