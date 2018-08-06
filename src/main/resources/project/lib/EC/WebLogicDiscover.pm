package EC::WebLogicDiscover;

use strict;
use warnings;
use ElectricCommander;
use File::Spec;
use Data::Dumper;
use File::Path qw(mkpath);
use XML::Simple qw(XMLin);

sub new {
    my ($class, $params, $wl) = @_;
    my $self = {params => $params, wl => $wl};
    return bless $self, $class;
}

sub wl {
    my ($self) = @_;
    return $self->{wl};
}

sub ensure_resource {
    my ($self) = @_;

    my $resource_name = $self->{params}->{resourceName};
    my $port = $self->{params}->{resPort};
    my $host = $self->{params}->{hostname};
    $port ||= '7800';

    if ($resource_name) {
        eval {
            $self->ec->getResource({resourceName => $resource_name});
            1;
        } or do {
            unless($host) {
                $self->wl->bail_out("Resource $resource_name does not exist and no hostname is provided");
            }
            $self->ec->createResource({
                resourceName => $resource_name,
                port => $port,
                hostName => $host
            });
        };
    }
    else {
        unless($host) {
            $self->wl->bail_out("Either WebLogic resource name or machine hostname should be provided");
        }
        $resource_name = "WebLogic-$host";
        $self->ec->createResource({
            resourceName => $resource_name,
            hostName => $host,
            port => $port
        });
    }

    my $alive = $self->ec->pingResource($resource_name)->findvalue('//agentState/alive')->string_value;
    if ($alive ne '1') {
        $self->wl->bail_out("Resource $resource_name is not alive. Please check the EF Agent.");
    }
    $self->ec->setProperty('/myJob/WLResourceName', $resource_name);
    $self->wl->logger->info("Resource is set up");
}


sub discover_resources {
    my ($self) = @_;

    my $render_params = {
        username     => $self->{params}->{credentialUsername},
        password     => $self->{params}->{credentialPassword},
        weblogic_url => $self->_generate_url,
    };
    my $wlst_path = $self->_get_wlst_path;

    my $template_path = "/myProject/jython/discover.jython";
    my $template = $self->wl->render_template_from_property($template_path, $render_params);
    $self->wl->logger->debug($template);

    my $res = $self->wl->execute_jython_script(
        shell          => $wlst_path,
        script_path    => File::Spec->catfile($ENV{COMMANDER_WORKSPACE}, 'exec.jython'),
        script_content => $template,
    );
    if ($res->{code} != 0) {
        $self->wl->bail_out("Failed to discover WebLogic resources: " . ($res->{stderr} || $res->{stdout}));
    }
    my ($xml) = $res->{stdout} =~ m/DISCOVERED\sRESOURCES:(.+)END\sDISCOVERED\sRESOURCES/s;
    $xml =~ s/^\s+//g;
    $xml =~ s/\s+$//g;
    my $structure = XMLin($xml, ForceArray => ['AppDeployment',
        'Datasource',
        'ConnectionFactory',
        'JMSResource',
        'warning',
        'Queue',
        'Topic',
        'JMSServer',
        'SubDeployment',
        'Library',
        'User',
        'Group',
        'Server',
        'Cluster'
    ], KeyAttr => []);
    $self->wl->logger->info('Discovered resources: ', $structure);
    $self->generate_reports($structure);
}


sub generate_reports {
    my ($self, $discovered_data) = @_;

    $self->_generate_csv($discovered_data);
}

sub _generate_csv {
    my ($self, $data) = @_;

    my @short = ();
    my @long = ();
    for my $type (keys %$data) {
        for my $object (@{$data->{$type}}) {
            my $name = $object->{name};
            if ($type =~ /ConnectionFactory|Queue|Topic|SubDeployment/i) {
                $name = $object->{jmsModuleName} . ':' . $name;
            }
            my $summary = $self->_get_resource_summary($type, $object);
            push @short, "$name=$type";
            push @long, "$name\t$type\t$summary";
        }
    }

    unshift @short, "Objects";
    unshift @long, "Name\tType\tSummary";
    $self->_attach_report('Short List of Objects.csv', join("\n", @short));
    $self->_attach_report('Long List of Objects.csv', join("\n", @long));
}


sub _attach_report {
    my ($self, $report_name, $report) = @_;

    mkpath('artifacts');
    my $path = File::Spec->catfile('artifacts', $report_name);
    open my $fh, ">$path" or die "Cannot open $path for writing: $!";
    print $fh $report;
    close $fh;

    my $link = "/commander/jobSteps/$ENV{COMMANDER_JOBSTEPID}/$report_name";
    $self->ec->setProperty("/myJob/report-urls/$report_name", $link);
}


sub _get_resource_summary {
    my ($self, $type, $object) = @_;

    my $method = "get_${type}_summary";
    if ($self->can($method)) {
        return $self->$method($object);
    }
    else {
        return '';
    }
}

sub get_datasource_summary {
    my ($self, $ds) = @_;
    return "URL: $ds->{url}, Driver Name: $ds->{driverName}";
}

sub ensure_environment {
    my ($self) = @_;

    my $proj_name = $self->{params}->{envProjectName};
    my $env_name = $self->{params}->{envName};

    unless($proj_name && $env_name) {
        $self->wl->logger->info("Environement parameters are not provided, skipping Environment creation");
        return;
    }

    $self->ec->abortOnError(0);
    my $xpath = $self->ec->createProject({projectName => $proj_name});
    my $errors = $self->ec->checkAllErrors($xpath);
    $self->ec->createEnvironment({projectName => $proj_name, environmentName => $env_name});
    $errors .= $self->ec->checkAllErrors($xpath);
    $self->ec->createEnvironmentTier({projectName => $proj_name, environmentName => $env_name, environmentTierName => 'WebLogic'});
    $errors .= $self->ec->checkAllErrors($xpath);
    $self->ec->addResourceToEnvironmentTier({
        projectName => $proj_name,
        environmentName => $env_name,
        environmentTierName => 'WebLogic',
        resourceName => '$[resourceName]'
    });
    $errors .= $self->ec->checkAllErrors($xpath);
    if ($errors) {
        $self->wl->logger->info("Environment setup failed: $errors");
    }
    else {
        $self->wl->logger->info("Set environment $env_name up");
    }
    $self->ec->abortOnError(1);
}

sub ec {
    my ($self) = @_;

    unless($self->{ec}) {
        $self->{ec} = ElectricCommander->new;
    }
    return $self->{ec};
}

sub ensure_configuration {
    my ($self) = @_;

    my $url = $self->_generate_url;
    my $wlst_path = $self->_get_wlst_path;
    my $res_name = '$[resourceName]';
    my $config_name = $self->_get_config_name;
    my $config_location = $self->ec->getProperty('/plugins/@PLUGIN_KEY@/project/ec_config/configLocation')->findvalue('//value')->string_value;
    my $exists = 0;
    eval {
        $self->ec->getProperty("/plugins/@PLUGIN_KEY@/project/$config_location/$config_name");
        $exists = 1;
    } or do {
        # Config does not exist
    };
    return if $exists;

    $self->wl->logger->info("URL: $url");
    $self->wl->logger->info("WLST Path: $wlst_path");
    my $actual_parameter = [
        {actualParameterName => 'config', value => $config_name},
        {actualParameterName => 'weblogic_url', value => $url},
        {actualParameterName => 'wlst_path', value => $wlst_path},
        {actualParameterName => 'credential', value => $config_name},
        {actualParameterName => 'debug_level', value => '0'},
    ];

    my $xpath = $self->ec->runProcedure({
        projectName => '/plugins/@PLUGIN_KEY@/project',
        procedureName => 'CreateConfiguration',
        credential => [{
            credentialName => $config_name,
            userName => $self->{params}->{credentialUsername},
            password => $self->{params}->{credentialPassword},
        }],
        actualParameter => $actual_parameter,
    });

    my $job_id = $xpath->findvalue('//jobId')->string_value;
    my $finished = 0;
    while(!$finished) {
        my $status = $self->ec->getJobStatus($job_id)->findvalue('//status')->string_value;
        $self->wl->logger->info("Configuration Job Status: $status");
        $finished = $status eq 'completed';
        sleep(2);
    }

    if ($self->ec->getJobStatus($job_id)->findvalue('//outcome')->string_value ne 'success') {
        $self->wl->bail_out("Configuration Job $job_id failed");
    }

    $self->wl->logger->info("Confuguration $config_name has been created");
}


sub _get_config_name {
    my ($self) = @_;

    my $config_name = "WebLogic Config " . '$[resourceName]';
    return $config_name;
}

sub _generate_url {
    my ($self) = @_;

    my $protocol = $self->{params}->{connectionProtocol} ||= 't3s';
    my $hostname = $self->{params}->{connectionHostname} ||= 'localhost';
    # TODO weblgoic port
    my $url = $protocol . '://' . $hostname . ':7001';
    return $url;
}

sub _get_wlst_path {
    my ($self) = @_;

    my $path = $self->{params}->{wlstPath};
    if ($path && $self->_check_wlst_path($path)) {
        return $path;
    }
    my $oracle_home = $self->{params}->{oracleHome};
    unless($oracle_home) {
        $self->bail_out("Either WLST path or Oracle Home must be provided");
    }

    my $system = $^O =~ /win32/i ? 'windows' : 'unix';
    my @possible_locations = qw(
        wlserver/common/bin/wlst
        oracle_common/common/bin/wlst
    );

    for my $location (@possible_locations) {
        my $extension = $system eq 'windows' ? 'cmd' : 'sh';
        $path = File::Spec->catfile($oracle_home, "$location.$extension");
        $self->wl->logger->info("Looking for WLST in $path...");
        if (-e $path) {
            $self->wl->logger->info("$path exists");
            return $path;
        }
    }
    $self->wl->bail_out("Could not find WLST CLI in $oracle_home");
}


sub _check_wlst_path {
    my ($self, $path) = @_;

    if (File::Spec->file_name_is_absolute($path)) {
        if (! -e $path) {
            $self->wl->bail_out("$path does not exist");
        }
    }
    else {
        my @paths = split(/[;:]/, $ENV{PATH});
        my $found = 0;
        for (@paths) {
            if (-e File::Spec->catfile($_, $path)) {
                $found = 1;
            }
        }
        unless($found) {
            $self->wl->bail_out("$path does not exist");
        }
    }
    return 1;
}

1;
