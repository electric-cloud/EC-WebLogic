# WARNING
# Do not edit this file manually. Your changes will be overwritten with next FlowPDF update.
# WARNING

package FlowPDF;

=head1 NAME

FlowPDF

=head1 AUTHOR

CloudBees

=head1 DESCRIPTION

FlowPDF is an Flow Plugin Development Framework.

This tool has been created to make plugin development easier.

To use it one should extend this class and define pluginInfo which should return a hash reference with following fields:

=over 4

=item * B<pluginName>

A name of the plugin. @PLUGIN_KEY@ could be used to be replaced with plugin name during plugin build-time.

=item * B<pluginVersion>

A version of the plugin. @PLUGIN_VERSION@ could be used to be replaced with version during build-time.

=item * B<configFields>

An array reference, that represents fields that would be used by plugin as a reference to plugin configurations.
For example, one could use ['config', 'configName'] to say that config name could be found in these parameter values.

B<IMPORTANT> This list will be used from left to right, so, in example above configName will be used only if there is no procedure parameter with name 'config'.

=item * B<configLocations>

An array reference with locations of plugin configurations. In all new plugins it will be set to ['ec_plugin_cfgs']. Precedence is the same, as in configFields.

=back

=head1 SYNOPSIS

Example of a plugin main class:

%%%LANG=perl%%%

    package EC::Plugin::NewRest;
    use strict;
    use warnings;
    use base qw/FlowPDF/;
    # Service function that is being used to set some metadata for a plugin.
    sub pluginInfo {
        return {
            pluginName    => '@PLUGIN_KEY@',
            pluginVersion => '@PLUGIN_VERSION@',
            configFields  => ['config'],
            configLocations => ['ec_plugin_cfgs']
        };
    }
    sub step_do_something {
        my ($pluginObject) = @_;
        my $context = $pluginObject->newContext();
        # This will show where we are. It could be procedure, pipeline or schedule
        print "Current context is: ", $context->getRunContext(), "\n";
        # This will get a step parameters.
        # $params now will be an L<FlowPDF::StepParameters> object.
        my $params = $context->getStepParameters();
        # This gets $headers parameter that is being stored under request_headers field of procedure.
        # To get value of this parameter one should 1. get parameter object 2. get a value if it is defined
        my $headers = $params->getParameter('request_headers');
        # This will return a config values for current procedure including credentials.
        # For configuration lookup see section above.
        my $configValues = $context->getConfigValues();
        # This creates a step result object, which handles actions that should be done during or after step execution
        my $stepResult = $context->newStepResult();
        # schedule setting a job step outcome to warning
        $stepResult->setJobStepOutcome('warning');
        # schedule setting a whole job summary:
        $stepResult->setJobSummary("See, this is a whole job summary");
        # schedule setting a current jobstep summary
        $stepResult->setJobStepSummary('And this is a job step summary');
        # abd, finally, apply all scheduled settings.
        $stepResult->apply();
    }

%%%LANG%%%

=head1 METHODS

=cut

use base qw/FlowPDF::BaseClass2/;
use FlowPDF::Types;

__PACKAGE__->defineClass({
    pluginName          => FlowPDF::Types::Scalar(),
    pluginVersion       => FlowPDF::Types::Scalar(),
    configFields        => FlowPDF::Types::ArrayrefOf(FlowPDF::Types::Scalar()),
    configLocations     => FlowPDF::Types::ArrayrefOf(FlowPDF::Types::Scalar()),
    contextFactory      => FlowPDF::Types::Reference('FlowPDF::ContextFactory'),
    pluginValues        => FlowPDF::Types::Reference('HASH'),
    contextObject       => FlowPDF::Types::Reference('FlowPDF::Context'),
    defaultConfigValues => FlowPDF::Types::Reference('HASH')
});

use strict;
use warnings;

use Carp;
use Data::Dumper;
use Try::Tiny;

use FlowPDF::Service::Bootstrap;
use FlowPDF::ContextFactory;
use FlowPDF::ComponentManager;
use FlowPDF::Helpers qw/inArray/;
use FlowPDF::Log;
use FlowPDF::Log::FW;

use FlowPDF::Exception::MissingFunctionDefinition;
use FlowPDF::Exception::MissingFunctionArgument;

our $VERSION = '1.3.5';

# We need to do an autoflush for STDOUT and STDERR to not mess up output streams.
# $| is a local variable for currently selected file descriptor.
# we're selecting STDOUT and enabling autoflush, and the same thing is being performed for STDERR.
BEGIN {
    select (STDERR);
    $| = 1;
    select (STDOUT);
    $| = 1;
};

sub classDefinition {
    return {

    };
}

=head2 newContext()

=head3 Description

Creates L<FlowPDF::Context> object. Does not require any additional parameters.

Please, note, that this function always creates a new context object.

If you want to use already existing context object, consider to use a getContext() method.

=head3 Parameters

=over 4

=item None

=back

=head3 Returns

=over 4

=item L<FlowPDF::Context>

=back

%%%LANG=perl%%%

    my $context = $pluginObject->newContext();

%%%LANG%%%

=cut


sub newContext {
    my ($pluginObject) = @_;

    my $context = $pluginObject->getContextFactory()->newContext($pluginObject);
    return $context;
}


=head2 getContext()

=head3 Description

This method returns already created L<FlowPDF::Context> object. Does not require any additional parameters.

If this method is being used first time, it creates new context object and returns it. Each next call will return exactly this object.

=head3 Parameters

=over 4

=item None

=back

=head3 Returns

=over 4

=item L<FlowPDF::Context>

=back

%%%LANG=perl%%%

    my $context = $pluginObject->getContext();

%%%LANG%%%

=cut

sub getContext {
    my ($pluginObject) = @_;

    my $context = $pluginObject->getContextObject();
    if ($context) {
        return $context;
    }
    $context = $pluginObject->getContextFactory()->newContext($pluginObject);
    $pluginObject->setContextObject($context);
    return $context;
}


# this function is here to be a placeholder for real pluginInfo function that is being defined by plugin object.
sub pluginInfo {}

sub showEnvironmentInfo {
    my ($class, $procedureName, $stepName) = @_;
    try {
        require ElectricCommander;
        my $ec = ElectricCommander->new();
        $ec->abortOnError(0);
        my $xpath = $ec->getServerStatus();

        my $version = $xpath->findvalue('//serverVersion/version')->string_value();
        if (!$version) {
            $version = $ElectricCommander::VERSION;
        }
        my $delimiter = '=' x 60;
        my $messageTemplate = qq|$delimiter

Plugin Name            : %s
Plugin Version         : %s
Procedure Name         : %s
Step Name              : %s
CloudBees CD version   : $version
FlowPDF-Perl version   : $VERSION

$delimiter

|;
        my $pluginInfo = $class->pluginInfo();
        my $message = sprintf(
            $messageTemplate,
            $pluginInfo->{pluginName},
            $pluginInfo->{pluginVersion},
            $procedureName,
            $stepName
        );
        logInfo $message;
    };
}


sub runStep {
    my ($class, $procedureName, $stepName, $function) = @_;

    $class->showEnvironmentInfo($procedureName, $stepName);
    fwLogTrace("Running runStep function");

    if (!$class->can($function)) {
        FlowPDF::Exception::MissingFunctionDefinition->new({
            class => $class,
            function => $function
        })->throw();
    }
    if (!$class->can('pluginInfo')) {
        FlowPDF::Exception::MissingFunctionDefinition->new({
            class => $class,
            function => 'pluginInfo'
        })->throw();
    }
    fwLogTrace("Executing pluginInfo()");
    my $pluginInfo = $class->pluginInfo();

    my $flowpdf = $class->new({
        pluginName      => $pluginInfo->{pluginName},
        pluginVersion   => $pluginInfo->{pluginVersion},
        configFields    => $pluginInfo->{configFields},
        configLocations => $pluginInfo->{configLocations},
        contextFactory  => FlowPDF::ContextFactory->new({
            procedureName => $procedureName,
            stepName      => $stepName
        })
    });
    fwLogTrace("Setting default config values.");
    if ($pluginInfo->{defaultConfigValues}) {
        $flowpdf->setDefaultConfigValues($pluginInfo->{defaultConfigValues});
    }
    fwLogTrace("Getting context object.");
    my $context           = $flowpdf->getContext();
    fwLogTrace("Getting runtime parameters.");
    my $runtimeParameters = $context->getRuntimeParameters();
    fwLogTrace("Creating stepResult object");
    my $stepResult        = $context->newStepResult();

    # if pluginvalues has been passed, it will be added to plugin object.
    if ($pluginInfo->{pluginValues}) {
        fwLogTrace("Setting pluginValues");
        $flowpdf->setPluginValues($pluginInfo->{pluginValues});
    }

    # From now control is passed to the user functions.
    my $retval;
    # This handles an exception from plugin code that was not handled for any reason.
    # It is the default handler for exceptions. It will propagate an err
    try {
        $retval = $flowpdf->$function($runtimeParameters, $stepResult);
    } catch {
        my ($e) = @_;

        $stepResult->flush();
        my $fullExceptionString;
        my $briefExceptionString;
        # this handler for FlowPDF::Exception::*
        if (ref($e) && ref($e) =~ m/^FlowPDF::Exception/s) {
            $fullExceptionString = $e->toString();
            $briefExceptionString = $e->toString({dropStackTrace => 1});
        }
        # This for regular die/croak
        else {
            $fullExceptionString = $e;
            $briefExceptionString = $e;
        }
        logErrorDiag($fullExceptionString);
        if ($context->getRunContext() eq 'pipeline') {
            $stepResult->setPipelineSummary('Unhandled Exception:', $briefExceptionString);
        }
        $stepResult->setJobStepOutcome('error');
        $stepResult->setJobStepSummary($briefExceptionString);
    } finally {
        $stepResult->applyIfNotApplied();
    };
    return $retval;
}


=head2 getPluginProjectName()

=head3 Description

This method returns a complete name of your plugin with version as string.

=head3 Parameters

=over 4

=item None

=back

=head3 Returns

=over 4

=item (String) PluginName-PluginVersion;

=back

%%%LANG=perl%%%

    my $pluginProjectName = $pluginObject->getPluginProjectName();

%%%LANG%%%

=cut


sub getPluginProjectName {
    my ($self) = @_;

    my $pluginProjectName = sprintf(
        '%s-%s',
        $self->getPluginName(),
        $self->getPluginVersion()
    );

    return $pluginProjectName;
}

=head1 SEE ALSO

=head2 L<FlowPDF::Context>

=head2 L<FlowPDF::StepResult>

=head2 L<FlowPDF::Config>

=cut


# a private method to get is that field name is a field name for a config.
sub isConfigField {
    my ($self, $field) = @_;

    if (!$field) {
        FlowPDF::Exception::MissingFunctionArgument->new({
            argument => 'field',
            function => 'isConfigField'
        })->throw();
    }

    my $configFields = $self->getConfigFields();
    if (inArray($field, @$configFields)) {
        return 1;
    }

    return 0;
}
1;
