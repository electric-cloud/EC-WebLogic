#
#  Copyright 2021 Electric Cloud, Inc.
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

# Here we are loading PDK. We need to load it in the begin.
# Do not modify it if you don't understand how perl phases are working.
# Thanks.

BEGIN {
    require ElectricCommander;
    import ElectricCommander;
    my $ec = ElectricCommander->new();

    my @locations = (
        '/myProject/pdk/',
        # '/myProject/perl/core/lib/',
        # '/myProject/perl/lib/'
    );
    my $display;
    my $pdk_loader = sub {
        my ($self, $target) = @_;

        $display = '[EC]@PLUGIN_KEY@-@PLUGIN_VERSION@/' . $target;
        # Undo perl'd require transformation
        # Retrieving framework part and lib part.
        my $code;
        for my $prefix (@locations) {
            my $prop = $target;
            # $prop =~ s#\.pm$##;

            $prop = "$prefix$prop";
            $code = eval {$ec->getProperty("$prop")->findvalue('//value')->string_value;};
            last if $code;
        }
        return unless $code; # let other module paths try ;)

        # Prepend comment for correct error attribution
        $code = qq{# line 1 "$display"\n$code};

        # We must return a file in perl < 5.10, in 5.10+ just return \$code
        #    would suffice.
        open my $fd, "<", \$code
            or die "Redirect failed when loading $target from $display";

        return $fd;
    };

    push @INC, $pdk_loader;
} ## end BEGIN

use strict;
use warnings;

no warnings qw/redefine/;

use subs qw/parallel_exec_support/;

use Data::Dumper;
use ElectricCommander;
use Carp;
use Try::Tiny;
use File::Spec;

use base 'EC::Plugin::Core';

use FlowPDF;
use FlowPDF::ContextFactory;
use FlowPDF::Log;
# use FlowPDF::Log::FW;
use FlowPDF::ComponentManager;

our $ENABLE_PARALLEL_EXEC_SUPPORT = 1;

use constant {
    SUCCESS => 0,
    ERROR   => 1,
};

#*****************************************************************************
sub flowpdf {
    my ($self) = @_;

    if (!$self->{flowpdf}) {
        print "DEBUG: Creating flowpdf object...\n";

        my $procedureName = $self->ec()->getProperty('/myProcedure/procedureName')->findvalue('//value')->string_value();
        my $stepName = $self->ec()->getProperty('/myJobStep/stepName')->findvalue('//value')->string_value();
        my $pluginName = '@PLUGIN_KEY@';
        my $pluginVersion = '@PLUGIN_VERSION@';

        *FlowPDF::pluginInfo = sub {
            return {
                pluginName          => $pluginName,
                pluginVersion       => $pluginVersion,
                config_fields       => [ 'config_name', 'configuration_name', 'config' ],
                config_locations    => [ 'weblogic_cfgs', 'ec_plugin_cfgs' ],
                defaultConfigValues => { 'auth_type' => 'basic' }
            }
        };

        $self->{flowpdf} = FlowPDF->new({
            pluginName      => $pluginName,
            pluginVersion   => $pluginVersion,
            configFields    => [ 'config_name', 'configuration_name', 'config' ],
            configLocations => [ 'weblogic_cfgs', 'ec_plugin_cfgs' ],
            # defaultConfigValues => {
            # 'auth_type' => 'basic'
            # },
            contextFactory  => FlowPDF::ContextFactory->new({
                procedureName => $procedureName,
                stepName      => $stepName
            }
            )
        });
        # $self->{flowpdf}->showEnvironmentInfo();
    } ## end if (!$self->{flowpdf})

    return $self->{flowpdf};
} ## end sub flowpdf

#*****************************************************************************

=head2 loadConfiguration

  Title    : loadConfiguration
  Usage    : $self->loadConfiguration();
  Function : Retrieves configuration content and set class atributes
  Returns  : none
  Args     : named arguments:
           : none
           :

=cut

#-----------------------------------------------------------------------------
sub loadConfiguration {
    my ($self, $params) = @_;

    my $context = $self->flowpdf()->getContext();
    my $cfg = {};
    try {
        $cfg = $context->getConfigValuesAsHashref($params);
    }
    catch {
        my $e = $_;
        unless (ref($e)) {
            $context->bailOut($e);
        }

        if ($e->isa('FlowPDF::Exception::ConfigDoesNotExist')) {
            print "Can't get config: Config does not exist.\n";
        }
        else {
            $context->bailOut($e->getMessage());
        }
    };

    # for my $k (keys %{$cfg}) {
    #     $self->{$k} = $cfg->{$k};
    # }

    return $cfg;
} ## end sub loadConfiguration

#*****************************************************************************
sub cli {
    my ($self) = @_;
    if (!$self->{cli}) {
        $self->{cli} = FlowPDF::ComponentManager->loadComponent('FlowPDF::Component::CLI', {
            workingDirectory => $ENV{COMMANDER_WORKSPACE}
        });
    }

    return $self->{cli};
}

#*****************************************************************************
sub testConnection {
    my ($self, $cfg) = @_;

    $cfg ||= $self->loadConfiguration();
    # my $cfg = {
    #     debug_level           => '2',
    #     enable_named_sessions => '1',
    #     java_home             => '',
    #     java_vendor           => '',
    #     mw_home               => '',
    #     password              => 'weblogic2',
    #     weblogic_url          => 't3://127.0.0.1:7001',
    #     wlst_path             => '/u01/oracle/oracle_common/common/bin/wlst.sh',
    #     user                  => 'weblogic',
    # };

    if (defined($cfg->{debug_level})) {
        FlowPDF::Log::setLogLevel($cfg->{debug_level});
    }

    if ($cfg->{java_home}) {
        $ENV{JAVA_HOME} = $cfg->{java_home};
        logInfo("JAVA_HOME was set to '$cfg->{java_home}'");
    }

    if ($cfg->{java_vendor}) {
        $ENV{JAVA_VENDOR} = $cfg->{java_vendor};
        logInfo("JAVA_VENDOR was set to '$cfg->{java_vendor}'");
    }

    if ($cfg->{mw_home}) {
        $ENV{MW_HOME} = $cfg->{mw_home};
        logInfo("MW_HOME was set to '$cfg->{mw_home}'");
    }

    my $script = File::Spec->catfile($ENV{COMMANDER_WORKSPACE}, 'do_ls');

    open FH, '>', $script;
    print FH "connect('$cfg->{user}','$cfg->{password}','$cfg->{weblogic_url}'); ls(); disconnect()\n";
    close FH;

    my $cli = $self->cli();
    my $command = $cli->newCommand($cfg->{wlst_path}, [ $script ]);
    my $result = eval {$cli->runCommand($command)};

    my $evalError = $@;
    my $code = 0;
    my $stdout = "";
    my $stderr = "";
    my $errmsg = "";
    if ($result) {
        $code = $result->getCode();
        $stdout = $result->getStdout();
        $stderr = $result->getStderr();
        $errmsg = $result->getErrmsg();
    }

    if ($evalError) {
        if ($errmsg) {
            $errmsg .= "\n$evalError";
        }
        else {
            $errmsg = $evalError;
        }

        $code ||= 1;
    }

    logInfo('STDOUT', $stdout) if ($stdout);
    logInfo('STDERR: ', $stderr) if ($stderr);
    logInfo('ERRMSG: ', $errmsg) if ($errmsg);
    logInfo('EXIT_CODE: ', $code);

    if ($code) {
        $errmsg ||= $stderr || $stdout;
        $errmsg =~ s/^(.+?)WLSTException:\s+//s;
        $errmsg =~ s/^(.+?)(?:nested exception is:.*)$/$1/s;
        $errmsg =~ s/^(.+?)(?:Use dumpStack.*)$/$1/s;

        $self->configurationErrorWithSuggestions($errmsg);

        return ERROR;
    }

    return SUCCESS;
}

#*****************************************************************************
sub parallel_exec_support {
    my ($p) = @_;

    if (defined $p) {
        $ENABLE_PARALLEL_EXEC_SUPPORT = $p;
    }
    return $ENABLE_PARALLEL_EXEC_SUPPORT;
}

#*****************************************************************************
sub out {
    my ($self, $level, @message) = @_;

    if ($self->{_credentials}->{password}) {
        my $password = $self->{_credentials}->{password};
        $password = quotemeta($password);
        map {s/$password/********/gms;
            $_} @message;
    }
    $level ||= 1;
    return $self->SUPER::out($level, @message);
}

#*****************************************************************************
sub after_init_hook {
    my ($self, %params) = @_;

    $self->{plugin_name} = 'EC-WebLogic';
    $self->{_credentials} = {};
    my $dryrun = 0;

    if ($self->{plugin_key}) {
        eval {$dryrun = $self->ec()->getProperty("/plugins/$self->{plugin_key}/project/dryrun")->findvalue('//value')->string_value();};
    }
    if ($dryrun) {
        $self->dbg("Dryrun enabled");
        $self->dryrun(1);
    }
    print 'Using plugin @PLUGIN_NAME@' . "\n";
    my $version = $self->ec->getVersions()->findvalue('//version')->string_value;
    print "EF Server Version: $version\n";
    my $perlLibraryVersion = $ElectricCommander::VERSION;
    print "Perl Library Version: $perlLibraryVersion\n";
} ## end sub after_init_hook

#*****************************************************************************
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

#*****************************************************************************
sub get_credentials {
    my ($self, $config_name) = @_;

    $config_name ||= 'configname';

    my $cfg = $self->loadConfiguration({configName => $config_name});

    if (defined $cfg->{debug_level}) {
        my $level = $cfg->{debug_level} ? int($cfg->{debug_level}) : 0;
        $self->debug_level($level + 1);
        $self->logger->level($level);
        FlowPDF::Log::setLogLevel($cfg->{debug_level});
        $self->out(3, "Debug level set to ", $self->debug_level())
    }

    if ($cfg->{java_home}) {
        $ENV{JAVA_HOME} = $cfg->{java_home};
        $self->out(10, "JAVA_HOME was set to $cfg->{java_home}");
    }
    if ($cfg->{java_vendor}) {
        $ENV{JAVA_VENDOR} = $cfg->{java_vendor};
        $self->out(10, "JAVA_VENDOR was set to $cfg->{java_vendor}");
    }
    if ($cfg->{mw_home}) {
        $ENV{MW_HOME} = $cfg->{mw_home};
        $self->out(10, "MW_HOME was set to $cfg->{mw_home}");
    }

    $self->{_credentials} = $cfg;

    return $cfg;
}

#*****************************************************************************
sub get_credentials_old {
    my ($self, $config_name) = @_;

    $config_name ||= 'configname';

    my $cred = $self->SUPER::get_credentials(
        $config_name => {
            userName                  => 'user',
            password                  => 'password',
            java_home                 => 'java_home',
            java_vendor               => 'java_vendor',
            mw_home                   => 'mw_home',
            weblogic_url              => 'weblogic_url',
            debug_level               => 'debug_level',
            enable_exclusive_sessions => 'enable_exclusive_sessions',
            enable_named_sessions     => 'enable_named_sessions',
            wlst_path                 => 'wlst_path',
        },
        'weblogic_cfgs'
    );

    if (defined $cred->{debug_level}) {
        my $level = $cred->{debug_level} ? int($cred->{debug_level}) : 0;
        $self->debug_level($level + 1);
        $self->logger->level($level);
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
} ## end sub get_credentials

#*****************************************************************************
sub get_common_credentials {
    my ($self, $cred_name) = @_;

    my $xpath = $self->ec()->getFullCredential($cred_name, { jobStepId => $ENV{COMMANDER_JOBSTEPID} });
    if (!defined $xpath) {
        $self->error("Can't find common credential", $cred_name);
    }

    my $credentials = {
        user     => $xpath->findvalue('//credential/userName') . '',
        password => $xpath->findvalue('//credential/password') . ''
    };

    return $credentials;
}

#*****************************************************************************
sub _unused_anywhere_get_step_credential {
    my ($self, $cred_name) = @_;

    return {} unless $cred_name;

    my $xpath = $self->ec->getFullCredential($cred_name);
    my $user_name = $xpath->findvalue('//userName')->string_value;
    my $password = $xpath->findvalue('//password')->string_value;

    return { userName => $user_name, password => $password };
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
} ## end sub write_deployment_plan

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

    my $restartFlagName = "WebLogicServerRestartRequired";
    my $restart = $result->{stdout} =~ m/that require server re-start/;
    $self->ec->setProperty('/myJob/' . $restartFlagName, ($restart ? 'true' : 'false'));
    my $summary = '';

    if ($result->{stdout} =~ m/SUMMARY:\s*(.+)/) {
        $summary = $1;
    }

    $self->set_output_parameter($restartFlagName, ($restart ? '1' : '0'));

    if ($restart) {
        $summary .= "\n" if $summary;
        $summary .= "Server restart is required";
        $self->out(1, "WebLogic Server restart is required");
    }

    my @matches = $result->{stdout} =~ m/WARNING:(.+?)$/gm;
    my %seen = ();
    @matches = grep {!$seen{$_}++} @matches;
    if (@matches) {
        $self->warning(join("\n", @matches));
    }
    else {
        $self->success($summary);
    }

    return;
} ## end sub process_response

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

    my $check = $self->dryrun() ? { ok => 1 } : $self->check_executable($params{shell});

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
} ## end sub execute_jython_script

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
    my ($self, $template_name, $params, %options) = @_;

    $params ||= {};

    my $preamble_params = {
        enable_exclusive_sessions => 0,
        enable_named_sessions     => 0,
    };

    if ($self->{_credentials}->{enable_named_sessions}) {
        $preamble_params->{enable_named_sessions} = 1;
    }

    if ($self->{_credentials}->{debug_level}) {
        $preamble_params->{debug_level} = $self->{_credentials}->{debug_level};
    }

    $params->{preamble} = $self->SUPER::render_template_from_property('/myProject/jython/preamble.jython', $preamble_params);
    # $params->{preamble} = $self->get_param('/myProject/jython/preamble.jython');
    return $self->SUPER::render_template_from_property($template_name, $params, %options);
} ## end sub render_template_from_property

sub get_wlst_path {
    my ($self, $params, $cred) = @_;

    my $retval = '';
    if ($params) {
        $retval = $params->{wlstabspath} || $params->{wlst_abs_path};
    }
    else {
        $retval = eval {$self->ec->getProperty('wlstabspath')->findvalue('//value')->string_value};
    }
    return $retval if $retval;
    unless ($cred) {
        $cred = $self->get_credentials;
    }
    $retval = $cred->{wlst_path};
    return $retval if $retval;

    $self->bail_out("WLST Path was not provided");
}

sub run_jython_step {
    my ($self) = @_;

    my $params = $self->get_step_parameters;
    my $config = $self->get_credentials($params->{configname});
    if ($config->{java_home}) {
        $self->out(1, 'JAVA_HOME was provided');
    }
    my $render_params = {
        username     => $config->{user},
        password     => $config->{password},
        weblogic_url => $config->{weblogic_url},
        admin_url    => $config->{weblogic_url},
    };
    $render_params = { %$params, %$render_params };
    my $wlst_path = $self->get_wlst_path($params, $config);

    my $step_name = $self->ec->getProperty('/myStep/name')->findvalue('//value')->string_value;
    my $replace = sub {
        my ($letter) = @_;
        return '_' . lc($letter)
    };
    $step_name = lcfirst($step_name);
    $step_name =~ s/([A-Z])/$replace->($1)/ge;
    my $template_path = "/myProject/jython/$step_name.jython";
    my $template = $self->render_template_from_property($template_path, $render_params);
    $self->out(2, "Generated script:\n", numbered_lines($template));

    my $res = $self->execute_jython_script(
        shell          => $wlst_path,
        script_path    => File::Spec->catfile($ENV{COMMANDER_WORKSPACE}, 'exec.jython'),
        script_content => $template,
    );
    $self->process_response($res);
} ## end sub run_jython_step

sub numbered_lines {
    my ($text) = @_;

    my @lines = split(/\n/, $text);
    my $counter = 1;
    @lines = map {$counter++ . ') ' . $_} @lines;
    return join("\n", @lines);
}

sub get_step_parameters {
    my ($self) = @_;

    my $params = {};
    my $procedure_name = $self->ec->getProperty('/myProcedure/name')->findvalue('//value')->string_value;
    my $xpath = $self->ec->getFormalParameters({ projectName => '@PLUGIN_NAME@', procedureName => $procedure_name });
    for my $param ($xpath->findnodes('//formalParameter')) {
        my $name = $param->findvalue('formalParameterName')->string_value;
        my $value = $self->get_param($name);

        my $name_in_list = $name;
        $name_in_list =~ s/ecp_weblogic_//;
        if ($param->findvalue('type')->string_value eq 'credential') {
            my $cred = $self->ec->getFullCredential($value);
            my $username = $cred->findvalue('//userName')->string_value;
            my $password = $cred->findvalue('//password')->string_value;

            $params->{$name_in_list . 'Username'} = $username;
            $params->{$name_in_list . 'Password'} = $password;
        }
        else {
            $params->{$name_in_list} = EC::Plugin::Core::trim_input($value);
            $self->out(1, qq{Got parameter "$name" with value "$value"\n});
        }
    }
    return $params;
} ## end sub get_step_parameters

#*****************************************************************************
sub configurationErrorWithSuggestions {
    my ($self, $errmsg) = @_;

    my $suggestions = q{Reasons could be due to one or more of the following. Please ensure they are correct and try again.:
1. WebLogic URL - Is your URL complete and reachable?
2. WLST Script Absolute Path  - Is your Path to the Script correct?
3. Test Resource - Is your Test resource correctly wired with CloudBees CD?  Is your Test Resource correctly setup with WebLogic?
4. Credentials - Are your credentials correct? Are you able to use these credentials to log in to WebLogic using its console?
};

    $self->ec->setProperty('/myJob/configError', $errmsg . "\n\n" . $suggestions);
    $self->ec->setProperty('/myJobStep/summary', $errmsg . "\n\n" . $suggestions);

    logErrorDiag("Create Configuration failed.\n\n$errmsg");
    logInfoDiag($suggestions);

    return;
}

#*****************************************************************************
1;

