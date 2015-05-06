=head1 NAME

EC::Plugin::Core

=head1 DESCRIPTION

Toolkit for plugin development. Contains subroutines for system calls, OS detection, debug, ETC, and others.

=over

=cut

package EC::Plugin::Core;
use strict;
use warnings;
use subs qw/is_win/;

use Carp;
use IPC::Open3;
use IO::Select;
use Symbol qw/gensym/;


=item B<new>

Constructor. Parameters:

debug_level => 0 .. 6

=cut

sub new {
    my ($class, %params) = @_;

    my $self = {};
    before_init_hook(@_);
    if ($params{debug_level}) {
        $self->{_init}->{debug_level} = $params{debug_level}
    }
    if ($params{result_folder}) {
        $self->{_init}->{result_folder} = $params{result_folder};
    }
    if ($params{ec}) {
        $self->{_ec} = $params{ec};
    }
    if ($params{dryrun}) {
        $self->{dryrun} = 1;
    }
    if ($params{project_name}) {
        $self->{project_name} = $params{project_name};
    }

    $self->{_init}->{debug_level} ||= 1;

    bless $self, $class;
    $self->after_init_hook(%params);
    return $self;
}


sub ec {
    my ($self) = @_;

    if (!$self->{_ec}) {
        require ElectricCommander;
        import ElectricCommander;
        $self->{_ec} = ElectricCommander->new();
    }
    return $self->{_ec};
}


sub set_property {
    my ($self, $key, $value) = @_;

    $self->ec()->setProperty("/myCall/$key", $value);
}


sub success {
    my ($self) = @_;

    return $self->_set_outcome('success');
}


=item B<error>

Sets outcome step status to error.

    $jboss->error();

=cut

sub error {
    my ($self) = @_;

    return $self->_set_outcome('error');
}


=item B<warning>

Sets outcome step status to warning.

    $jboss->waring();

=cut

sub warning {
    my ($self) = @_;

    return $self->_set_outcome('warning');
}


=item B<_set_outcome>

Sets outcome status to desired status.

    $jboss->_set_outcome('aborted');

=cut

sub _set_outcome {
    my ($self, $status) = @_;

    $self->ec()->setProperty('/myJobStep/outcome', $status);
}


=item B<bail_out>

Terminating execution immediately with error message.

    $jboss->bail_out("Something was VERY wrong");

=cut

sub bail_out {
    my ($self, @msg) = @_;

    my $msg = join '', @msg;

    $msg ||= 'Bailed out.';
    $msg .= "\n";

    $self->error();
    $self->set_property(summary => $msg);
    exit 1;
}


sub before_init_hook {
    1;
}


sub after_init_hook {
    1;
}


sub run_command {
    my ($self, @cmd) = @_;

    my $retval = {
        code => 0,
        stdout => '',
        stderr => ''
    };

    if (is_win) {
        $retval =  $self->_syscall_win32(@cmd);
    }
    else {
        $retval =  $self->_syscall(@cmd);
    }
    return $retval;
}

sub get_credentials {
    my ($self, $config_name, $cfgs_path) = @_;

    print "Running it\n";
    if ($self->{_credentials}) {
        return $self->{_credentials};
    }
    if (!$config_name && !$self->{config_name}) {
        croak "Configuration doesn't exist";
    }

    my $ec = $self->ec();
    $config_name ||= $self->{config_name};
    my $project = $self->{project_name};

    my $pattern = sprintf '/projects/%s/' . $cfgs_path, $project;
    my $plugin_configs;
    eval {
        $plugin_configs = ElectricCommander::PropDB->new($ec, $pattern);
        1;
    } or do {
        $self->out("Can't access credentials.");
        # bailing out if can't access credendials.
        $self->bail_out("Can't access credentials.");
    };

    my %config_row;
    eval {
        %config_row = $plugin_configs->getRow($config_name);
        1;
    } or do {
        $self->out("Configuration $config_name doesn't exist.");
        # bailing out if configuration specified doesn't exist.
        $self->bail_out("Configuration $config_name doesn't exist.");
    };

    unless (%config_row) {
        croak "Configuration doesn't exist";
    }

    my $retval = {};

    my $xpath = $ec->getFullCredential($config_row{credential});
    $retval->{user} = '' . $xpath->findvalue("//userName");
    $retval->{password} = '' . $xpath->findvalue("//password");
    $retval->{java_home} = '' . $config_row{java_home};
    $retval->{weblogic_url} = '' . $config_row{weblogic_url};

    return $retval;

}


sub _syscall {
    my ($self, @command) = @_;

    my $command = join '', @command;
    unless ($command) {
        croak  "Missing command";
    }
    my ($infh, $outfh, $errfh, $pid, $exit_code);
    $errfh = gensym();
    eval {
        $pid = open3($infh, $outfh, $errfh, $command);
        waitpid($pid, 0);
        $exit_code = $? >> 8;
        1;
    } or do {
        # croak "Error occured during command execution: $@";
        return {
            code => -1,
            stderr => $@,
            stdout => '',
        };
    };

    my $retval = {
        code => $exit_code,
        stderr => '',
        stdout => '',
    };
    my $sel = IO::Select->new();
    $sel->add($outfh, $errfh);

    while(my @ready = $sel->can_read) { # read ready
        foreach my $fh (@ready) {
            my $line = <$fh>; # read one line from this fh
            if (not defined $line) {
                $sel->remove($fh);
                next;
            }
            if ($fh == $outfh) {
                $retval->{stdout} .= $line;
            }
            elsif ($fh == $errfh) {
                $retval->{stderr} .= $line;
            }

            if (eof $fh) {
                $sel->remove($fh);
            }
        }
    }
    return $retval;
}


sub _syscall_win32 {
    my ($self, @command) = @_;

    my $command = join '', @command;

    my $result_folder = $ENV{COMMANDER_WORKSPACE};
    $command .= qq| 1> "$result_folder/command.stdout" 2> "$result_folder/command.stderr"|;
    if (is_win) {
        $self->dbg("MSWin32 detected");
        $ENV{NOPAUSE} = 1;
    }

    my $pid = system($command);
    my $retval = {
        stdout => '',
        stderr => '',
        code => $? >> 8,
    };

    open my $stderr, "$result_folder/command.stderr" or croak "Can't open stderr: $@";
    open my $stdout, "$result_folder/command.stdout" or croak "Can't open stdout: $@";
    $retval->{stdout} = join '', <$stdout>;
    $retval->{stderr} = join '', <$stderr>;
    close $stdout;
    close $stderr;
    return $retval;
}

sub is_win {
    if ($^O eq 'MSWin32') {
        return 1
    }
    return 0;
}


sub dbg {
    my ($self, @params) = @_;
    return $self->out(1, @params);
}


sub out {
    my ($self, $debug_level, @msg) = @_;


    if (!$self->{_init}->{debug_level}) {
        return 1;
    }
    if ($self->{_init}->{debug_level} < $debug_level) {
        return 1;
    }
    my $msg = join '', @msg;

    $msg =~ s/\s+$//gs;
    $msg .= "\n";
    print $msg;
    return 1;
}

sub render_template {
    my ($self, @params) = @_;

    return EC::MicroTemplate::render(@params);
}


sub get_param {
    my ($self, $param) = @_;

    my $ec = $self->ec();
    my $retval;
    eval {
        $retval = $ec->getProperty($param)->findvalue('//value') . '';
        1;
    } or do {
        $self->dbg("Error '$@' was occured while getting property: $param");
        $retval = undef;
    };

    return $retval;
}


=item B<get_params_as_hashref>

Returns request params as hashref by list of param names.

    my $params = $core->get_params_as_hashref('param1', 'param2', 'param3');
    # $params = {
    #     param1  =>  'value1',
    #     param2  =>  'value2',
    #     param3  =>  'value3'
    # }

=cut

sub get_params_as_hashref {
    my ($self, @params_list) = @_;

    my $retval = {};
    my $ec = $self->ec();
    for my $param_name (@params_list) {
        my $param = $self->get_param($param_name);
        next unless defined $param;
        $retval->{$param_name} = $param;
    }
    return $retval;
}


sub render_template_from_property {
    my ($self, $template_name, $params) = @_;

    if (!$template_name) {
        croak "No template";
    }
    my $template;
    $template = $self->get_param($template_name);
    unless ($template) {
        croak "Template $template_name wasn't found";
    }

    return $self->render_template(text => $template, render_params => $params);
}

1;

package EC::MicroTemplate;

use strict;
use warnings;

use Carp;

our $ANCHOR = ['\[%', '%\]'];

=over

=item B<render>

Returns rendered template. Accepts as parameters file or handle
and variables hashref(key=>value).

=back 

=cut

sub render {
    my (%params) = @_;

    if (!$params{file} && !$params{handle} && !$params{text}) {
        croak "Can't render nothing";
    }

    if ($params{render_params} && ref $params{render_params} ne 'HASH') {
        croak "render_params must be a hashref";
    }

    my $render_params = $params{render_params};

    $render_params->{PERL} = $^X;
    my @template;

    if ($params{file}) {
        @template = _tff($params{file});
    }
    elsif($params{handle}) {
        @template = _tfd($params{handle});
    }
    else {
        @template = split "\n", $params{text};
    }
    local *{EC::MicroTemplate::parse} = sub {
        my $string = shift;
        for my $key (keys %$render_params) {
            next unless $render_params->{$key};
            $string =~ s/$ANCHOR->[0]\s*?$key\s*?$ANCHOR->[1]/$render_params->{$key}/gs;
        }
        my $template = "$ANCHOR->[0].*?$ANCHOR->[1]";
        # print $template;
        $string =~ s/$template//gs;

        return $string;
    };

    @template = map {
        parse($_);
    } @template;
    return join "\n", @template;
}


# template from file
sub _tff {
    my $file = shift;

    unless (-e $file) {
        croak "File $file does not exists.";
    }

    my $fh;

    open $fh, $file or croak "Can't open file $file: $!";
    return _tfd($fh);
}


#template from descriptor
sub _tfd {
    my $glob = shift;

    my @content = <$glob>;
    close $glob;
    return @content;
}


1;

