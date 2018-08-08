package EC::WebLogicDiscover;

use strict;
use warnings;
use ElectricCommander;
use ElectricCommander::ArtifactManagement;
use File::Spec;
use Data::Dumper;
use File::Path qw(mkpath);
use XML::Simple qw(XMLin);
use JSON;

use constant {
    TIER_NAME => 'WebLogic',
    DEPLOY => 'Deploy',
};

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
    # $self->wl->logger->debug($template);

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
    my $structure = XMLin($xml, SuppressEmpty => 1, ForceArray => ['AppDeployment',
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
    $self->create_application($structure);
    $self->generate_reports($structure);
}

sub create_application {
    my ($self, $data) = @_;

    my $project_name = $self->{params}->{appProjName};
    my $app_name = $self->{params}->{appName};

    unless($self->{params}->{appProjName} && $self->{params}->{appName}) {
        $self->wl->logger->warning("Application name and Application Project Name should be provided for the Application generation");
        return;
    }
    unless($self->{params}->{objectNames}) {
        $self->wl->logger->warning("Object Names should be provided for the Application generation, the Application will not be created");
        return;
    }

    $self->_create_app();

    $self->ec->createApplicationTier({
        applicationTierName => TIER_NAME,
        projectName => $project_name,
        applicationName => $app_name,
    });
    my $step_name = $self->_create_deploy_process($data);
    $self->_create_components($data, $step_name);
    $self->_create_tier_map;
}

sub _create_tier_map {
    my ($self) = @_;

    my $project_name = $self->{params}->{appProjName};
    my $app_name = $self->{params}->{appName};
    my $env_name = $self->{params}->{envName};
    my $env_project_name = $self->{params}->{envProjectName};

    return unless ($env_name && $env_project_name);

    my $tier_map_name = "$app_name " . TIER_NAME . " Tier Map";
    $self->ec->createTierMap({
        tierMapName => $tier_map_name,
        applicationName => $app_name,
        projectName => $project_name,
        environmentName => $env_name,
        environmentProjectName => $env_project_name
    });

    $self->ec->createTierMapping({
        tierMappingName => "Tier Mapping $app_name $env_name",
        tierMapName => $tier_map_name,
        applicationName => $app_name,
        projectName => $project_name,
        environmentName => $env_name,
        environmentProjectName => $env_project_name,
        applicationTierName => TIER_NAME,
        environmentTierName => TIER_NAME
    });

  # tierMap 'fbde699f-9a48-11e8-bd50-6265ff6e656c', {
  #   applicationName = 'wlApp'
  #   environmentName = 'wlEnv'
  #   environmentProjectName = 'WebLogic Discovered'
  #   projectName = 'WebLogic Discovered'

  #   tierMapping 'fc71a89f-9a48-11e8-9ae9-6265ff6e656c', {
  #     applicationTierName = 'WebLogic'
  #     environmentTierName = 'WebLogic'
  #     tierMapName = 'fbde699f-9a48-11e8-bd50-6265ff6e656c'
  #   }
  # }
}


sub _create_deploy_process {
    my ($self, $data) = @_;

    my $project_name = $self->{params}->{appProjName};
    my $app_name = $self->{params}->{appName};
    $self->ec->createProcess({
        projectName => $project_name,
        applicationName => $app_name,
        processType => 'DEPLOY',
        processName => DEPLOY
    });

    my $prev_step_name = '';
    my $step_name;

    my @types = qw(Cluster
        Server
        JMSServer
        JMSResource
        SubDeployment
        Queue
        Topic
        ConnectionFactory
        Datasource
        User
        Group
    );

    for my $type (@types) {
        next if $type =~ /AppDeployment|Library/;
        for my $object (@{$data->{$type}}) {
            my $name = $object->{name};
            next unless $self->_is_requested($type, $object);

            $self->wl->logger->info("Creating process step for $type ", $object);

            $step_name = $self->create_process_step($type, $object, $data, $prev_step_name);
            $prev_step_name = $step_name;
        }
    }
    return $step_name;
}

sub _create_credential {
    my ($self, $type, $object) = @_;

    my $cred_name;
    my $username;
    my $params = {};
    if ($type eq 'Datasource') {
        $cred_name = "Datasource $object->{name}";
    }
    elsif ($type eq 'User') {
        $cred_name = "User $object->{name}";
        $username = $object->{name};
    }
    return unless $cred_name;

    $self->ec->createCredential({
        projectName => $self->{params}->{appProjName},
        credentialName => $cred_name,
        userName => $username,
    });
    $self->send_warning("Credential $cred_name requires password");

    return $cred_name;
}

sub send_warning {
    my ($self, $warning) = @_;

    $self->wl->logger->warning($warning);
    push @{$self->{warnings}}, $warning;
}

sub create_jms_module_step {
    my ($self, $object, $data, $prev_step_name) = @_;

    my $jms_module_name = $object->{jmsModuleName};
    my $step_name;
    my ($jms_module) = grep { $_->{name} eq $jms_module_name } @{$data->{JMSResource}};
    if ($jms_module) {
        eval {
            $step_name = $self->create_process_step('JMSResource', $jms_module, $data, $prev_step_name);
        };
    }
    return $step_name;
}

sub is_jms_type {
    my ($type) = @_;
    return $type =~ /Queue|Topic|ConnectionFactory|SubDeployment/;
}

sub create_process_step {
    my ($self, $type, $object, $data, $prev_step_name) = @_;

    my $params = {};
    my $cred_name = '';
    my $procedure_name = '';
    my $project_name = $self->{params}->{appProjName};
    my $process_step_name = '';

    if (is_jms_type($type)) {
        my $jms_module_step = $self->create_jms_module_step($object, $data, $prev_step_name);
        $prev_step_name = $jms_module_step if $jms_module_step;
    }

    if ($type eq 'Datasource') {
#                                   {
#                             'targets' => 'AdminServer',
#                             'jndiName' => 'examples-dataSource-demoPool',
#                             'name' => 'examples-demo',
#                             'url' => 'jdbc:derby://localhost:1527/examples;create=true',
#                             'driverName' => 'org.apache.derby.jdbc.ClientDriver',
#                             'driverProperties' => 'user=examples
                                # DatabaseName=examples;create=true'
        $cred_name = $self->_create_credential($type, $object);
        $procedure_name = 'CreateOrUpdateDatasource';
        $process_step_name = "Create Datasouce $object->{name}";
        $params = {
            ecp_weblogic_dataSourceCredentials => "/projects/$project_name/$cred_name",
            configname => $self->_get_config_name,
            ecp_weblogic_dataSourceName => $object->{name},
            ecp_weblogic_dataSourceDriverClass => $object->{driverName},
            ecp_weblogic_databaseUrl => $object->{url},
            ecp_weblogic_jndiName => $object->{jndiName},
            ecp_weblogic_databaseName => $object->{databaseName},
            ecp_weblogic_driverProperties => $object->{driverProperties},
            ecp_weblogic_targets => $object->{targets},
        }
    }
    elsif ($type eq 'JMSResource') {
        $process_step_name = "Create JMS Module $object->{name}";
        $params = {
            configname => $self->_get_config_name,
            ecp_weblogic_jms_module_name => $object->{name},
            ecp_weblogic_target_list => $object->{targets},
        };
        $procedure_name = 'CreateOrUpdateJMSModule';
    }
    elsif ($type eq 'Queue') {
        $process_step_name = "Create JMS Queue $object->{name}";
        my ($targets, $jms_targets) = $self->_get_subdeployment_targets(
            $object->{subdeploymentName},
            $object->{jmsModuleName},
            $data
        );
        $params = {
            configname => $self->_get_config_name,
            ecp_weblogic_jms_queue_name => $object->{name},
            ecp_weblogic_jms_module_name => $object->{jmsModuleName},
            ecp_weblogic_jndi_name => $object->{jndiName},
            ecp_weblogic_target_jms_server => $jms_targets,
            ecp_weblogic_subdeployment_name => $object->{subdeploymentName},
        };
        $procedure_name = 'CreateOrUpdateJMSQueue';
    }
    elsif ($type eq 'Topic') {
        $process_step_name = "Create JMS Topic $object->{name}";
        my ($targets, $jms_targets) = $self->_get_subdeployment_targets($object->{subdeploymentName}, $object->{jmsModuleName}, $data);
        $params = {
            configname => $self->_get_config_name,
            ecp_weblogic_jms_topic_name => $object->{name},
            ecp_weblogic_jms_module_name => $object->{jmsModuleName},
            ecp_weblogic_jndi_name => $object->{jndiName},
            ecp_weblogic_target_jms_server => $jms_targets,
            ecp_weblogic_subdeployment_name => $object->{subdeploymentName},
        };
        $procedure_name = 'CreateOrUpdateJMSTopic';
    }
    elsif ($type eq 'SubDeployment') {
        $process_step_name = "Create SubDeployment $object->{name}";
        $params = {
            configname => $self->_get_config_name,
            ecp_weblogic_subdeployment_name => $object->{name},
            ecp_weblogic_jms_module_name => $object->{jmsModuleName},
            ecp_weblogic_subdeployment_target_list => $object->{targets},
        };
        $procedure_name = 'CreateOrUpdateJMSModuleSubdeployment';
    }
    elsif ($type eq 'JMSServer') {
        $process_step_name = "Create JMS Server $object->{name}";
        $params = {
            configname => $self->_get_config_name,
            ecp_weblogic_jms_server_name => $object->{name},
            ecp_weblogic_target => $object->{targets},
        };
        $procedure_name = 'CreateOrUpdateJMSServer';
    }
    elsif ($type eq 'ConnectionFactory') {
        $process_step_name = "Create Connection Factory $object->{name}";
        my $default_targeting = $object->{defaultTargeting} && $object->{defaultTargeting} =~ /1/;
        my $subdeployment_name = $default_targeting ? '' : $object->{subdeploymentName};
        my $wl_targets = '';
        my $jms_targets = '';
        unless($default_targeting) {
            $wl_targets = $object->{targets};
            $jms_targets = $object->{jmsTargets};
        }
        $params = {
            configname => $self->_get_config_name,
            cf_name => $object->{name},
            jms_module_name => $object->{jmsModuleName},
            cf_sharing_policy => $object->{SubscriptionSharingPolicy},
            cf_client_id_policy => $object->{ClientIdPolicy},
            jndi_name => $object->{jndiName},
            cf_xa_enabled => $object->{XAConnectionFactoryEnabled},
            cf_max_messages_per_session => $object->{MessagesMaximum},
            subdeployment_name => $subdeployment_name,
            wls_instance_list => $wl_targets,
            jms_server_list => $jms_targets,
        };
        $procedure_name = 'CreateOrUpdateConnectionFactory';
    }
    elsif ($type eq 'Server') {
        $process_step_name = "Create Managed Server $object->{name}";
        $params = {
            configname => $self->_get_config_name,
            server_name => $object->{name},
            listen_address => $object->{listenAddress},
            listen_port => $object->{listenPort},
        };
        $procedure_name = 'CreateManagedServer';
    }
    elsif ($type eq 'Cluster') {
        $process_step_name = "Create Cluster $object->{name}";
        $params = {
            configname => $self->_get_config_name,
            cluster_name => $object->{name},
            multicast_address => $object->{multicastAddress},
            multicast_port => $object->{multicastPort},
        };
        $procedure_name = 'CreateCluster';
    }
    elsif ($type eq 'User') {
        $cred_name = $self->_create_credential($type, $object);
        $process_step_name = "Create user $object->{name}";
        $params = {
            configname => $self->_get_config_name,
            domain_name => $object->{domainName},
            realm_name => $object->{realmName},
            user_credentials => "/projects/$project_name/$cred_name",
        };
        $procedure_name = 'CreateUser';
    }
    elsif ($type eq 'Group') {
        $process_step_name = "Create Group $object->{name}";
        $params = {
            configname => $self->_get_config_name,
            domain_name => $object->{domainName},
            realm_name => $object->{realmName},
            group_name => $object->{name}
        };
        $procedure_name = 'CreateGroup';
    }

    return unless $process_step_name;
    $self->wl->logger->info("Process step params", $params);

    $self->ec->createProcessStep({
        projectName => $self->{params}->{appProjName},
        applicationName => $self->{params}->{appName},
        processName => DEPLOY,
        processStepName => $process_step_name,
        actualParameter => actual_parameter($params),
        subproject => '/plugins/EC-WebLogic/project',
        subprocedure => $procedure_name,
        processStepType => 'plugin',
        applicationTierName => TIER_NAME
    });
    if ($cred_name) {
        # Credentials need to be attached to the step
        $self->ec->attachCredential({
            projectName => $self->{params}->{appProjName},
            applicationName => $self->{params}->{appName},
            processName => DEPLOY,
            credentialName => $cred_name,
            processStepName => $process_step_name
        });
    }
    if ($process_step_name && $prev_step_name) {
        # Step order
        $self->ec->createProcessDependency({
            projectName => $self->{params}->{appProjName},
            applicationName => $self->{params}->{appName},
            processName => DEPLOY,
            processStepName => $prev_step_name,
            targetProcessStepName => $process_step_name,
        });
    }

    return $process_step_name;
}

sub _get_subdeployment_targets {
    my ($self, $sub_name, $jms_module_name, $data) = @_;

    my ($sub) = grep { $_->{name} eq $sub_name && $_->{jmsModuleName} eq $jms_module_name } @{$data->{SubDeployment}};
    unless($sub) {
        die "Cannot find SubDeployment $sub_name";
    }
    return ($sub->{targets}, $sub->{jmsTargets});
}

sub _publish_artifact {
    my ($self, $dep) = @_;

    my $am = ElectricCommander::ArtifactManagement->new($self->ec);
    my $artifact;
    eval {
        my ($volume, $directories, $file) = File::Spec->splitpath($dep->{sourcePath});
        my $artifact_version = $am->publish({
            groupId => "weblogic.discovered",
            artifactKey => $dep->{name},
            version => '1.0.0',
            includePatterns => $file,
            fromDirectory => File::Spec->catpath($volume, $directories),
            description => 'Automatically published during WebLogic Discovery',
        });
        $artifact = {
            file => $file,
            artifactName => "weblogic.discovered:$dep->{name}",
            version => '1.0.0'
        };
    } or do {
        $self->wl->logger->warning("Failed to publish artifact: $@");
    };

    return $artifact;
}

sub _create_component {
    my ($self, $dep, $data, $prev_step_name, $is_library) = @_;

    my $name = $dep->{name};
    # Names with version, like mysql-connector-java-8#4.2@8.0.12'
    $name =~ s/\#.+//;
    my $project_name = $self->{params}->{appProjName};
    my $app_name = $self->{params}->{appName};

    $self->ec->createComponent({
        applicationName => $app_name,
        projectName => $project_name,
        pluginKey => 'EC-Artifact',
        componentName => $name
    });

    my $artifact = $self->_publish_artifact($dep);

    my $artifact_data = {
        artifactName => "weblogic.discovered:$name",
        artifactVersionLocationProperty => '/myJob/retrievedArtifactVersions/' .$name,
        overwrite => 'update',
        pluginProcedure => 'Retrieve',
        pluginProjectName => 'EC-Artifact',
        retrieveToDirectory => 'deploy',
        versionRange => ''
    };

    for my $property (keys %$artifact_data) {
        $self->ec->setProperty("ec_content_details/$property", $artifact_data->{$property}, {
            applicationName => $app_name,
            projectName => $project_name,
            componentName => $name
        });
    }

    $self->ec->addComponentToApplicationTier({
        projectName => $project_name,
        applicationName => $app_name,
        componentName => $name,
        applicationTierName => TIER_NAME,
    });
    $self->ec->createProcess({
        componentApplicationName => $app_name,
        projectName => $project_name,
        componentName => $name,
        processType => 'DEPLOY',
        processName => DEPLOY
    });
    $self->wl->logger->info("Creating Deploy step for application", $dep);
    # TODO
    # Version, stage, component
    # TODO retrieve
    my $path;
    if ($artifact) {
        $path = File::Spec->catfile('deploy', $artifact->{file});
    }
    else {
        $path = $dep->{sourcePath};
    }
    $self->ec->createProcessStep({
        componentApplicationName => $app_name,
        projectName => $project_name,
        processName => DEPLOY,
        componentName => $name,
        processStepName => 'Deploy App',
        subproject => '/plugins/@PLUGIN_KEY@/project',
        subprocedure => 'DeployApp',
        actualParameter => actual_parameter({
            configname => $self->_get_config_name,
            wlstabspath => $self->_get_wlst_path,
            appname => $name,
            is_library => $is_library,
            apppath => $path,
            targets => $dep->{targets},
            version_identifier => $dep->{versionIdentifier}
        })
    });

    my $step_name = "Deploy App $name";

    # Adding references to the main deploy process
    $self->ec->createProcessStep({
        projectName => $project_name,
        processName => DEPLOY,
        processStepName => $step_name,
        subcomponent => $name,
        subcomponentProcess => DEPLOY,
        subcomponentApplicationName => $app_name,
        applicationTierName => TIER_NAME,
        applicationName => $app_name
    });
    $self->ec->createProcessDependency({
        projectName => $project_name,
        applicationName => $app_name,
        processName => DEPLOY,
        processStepName => $prev_step_name,
        targetProcessStepName => $step_name,
    });

    return $step_name;
}

sub _create_components {
    my ($self, $data, $prev_step_name) = @_;

    my $project_name = $self->{params}->{appProjName};
    my $app_name = $self->{params}->{appName};
    for my $dep (@{$data->{AppDeployment}}) {
        next unless $self->_is_requested('AppDeployment', $dep);
        $prev_step_name = $self->_create_component($dep, $data, $prev_step_name, 0);
    }

    for my $dep (@{$data->{Library}}) {
        next unless $self->_is_requested('Library', $dep);
        $prev_step_name = $self->_create_component($dep, $data, $prev_step_name, 1);
    }
}

sub actual_parameter {
    my ($hashref) = @_;
    my @list = map { { actualParameterName => $_, value => $hashref->{$_} }} keys %$hashref;
    return \@list;
}

sub _is_requested {
    my ($self, $req_type, $object) = @_;

    my $names = $self->{params}->{objectNames};
    unless($self->{filter}) {
        my $objects = {};
        for my $line (split /\n/ => $names) {
            next unless $line;
            my ($type, $name) = split(/\s*:\s*/, $line, 2);
            $objects->{$type}->{$name} = 1;
        }
        $self->{filter} = $objects;
    }

    my $req_name = $object->{name};
    if ($req_type =~ /Queue|Topic|ConnectionFactory|SubDeployment/) {
        $req_name = $object->{jmsModuleName} . ':' . $req_name;
    }
    return $self->{filter}->{$req_type}->{$req_name};
}


sub _create_app {
    my ($self) = @_;

    unless($self->{params}->{appProjName} && $self->{params}->{appName}) {
        $self->wl->logger->warning("Application name and Application Project Name should be provided for the Application generation");
        return;
    }
    my $project_name = $self->{params}->{appProjName};
    my $app_name = $self->{params}->{appName};

    $self->ec->abortOnError(0);
    $self->ec->createProject({projectName => $project_name});
    $self->ec->abortOnError(1);
    $self->ec->createApplication({applicationName => $app_name, projectName => $project_name})
}


sub generate_reports {
    my ($self, $discovered_data) = @_;

    $self->_generate_csv($discovered_data);
    $self->_generate_html($discovered_data);
    my $json = encode_json($discovered_data);
    $self->ec->setProperty('/myJob/discoveredResources', $json);
}

sub _generate_html {
    my ($self, $data) = @_;

    my $render_params = $data;
    my $jms_resources = $data->{JMSResource};

    my $jms_structure = [];

    for my $jms (@$jms_resources) {
        my $name = $jms->{name};
        my $find_targets = sub {
            my ($object) = @_;
            if ($object->{targets}) {
                return $object->{targets};
            }
            if ($object->{defaultTargeting}) {
                return $jms->{targets};
            }
            if ($object->{subdeploymentName}) {
                my ($subdeployment) = grep { $_->{name} eq $object->{subdeploymentName}} @{$data->{SubDeployment}};
                my $wl_targets = $subdeployment->{targets} ||= '';
                my $jms_servers = $subdeployment->{jmsTargets} ||= '';
                my @all_targets = split(/\s*,\s*/, $jms_servers . ',' . $wl_targets);
                return join(', ', @all_targets);
            }
            return '';
        };

        for my $type (qw/ConnectionFactory Queue Topic SubDeployment/) {
            my @objects = grep { $_->{jmsModuleName} eq $name } @{$data->{$type}};
            for my $object (@objects) {
                $object->{targets} = $find_targets->($object);
            }
            $jms->{$type} = \@objects;
        }
        push @$jms_structure, $jms;
    }

    $render_params->{jms} = $jms_structure;

    my $template_path = "/myProject/templates/discovered_resources.html";
    my $report = $self->wl->render_template_from_property(
        $template_path,
        $render_params,
        mt => 1
    );
    $self->_attach_report("Discovered Resources.html", $report);
}

sub _generate_csv {
    my ($self, $data) = @_;

    my @short = ();
    my @long = ();
    for my $type (keys %$data) {
        for my $object (@{$data->{$type}}) {
            my $name = $object->{name};
            if (is_jms_type($type)) {
                $name = $object->{jmsModuleName} . ':' . $name;
            }
            my $summary = $self->_get_resource_summary($type, $object);
            push @short, "$type: $name";
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

    my $res_name = $self->ec->getProperty('/myJobStep/resourceName')->findvalue('//value')->string_value;
    $self->ec->addResourceToEnvironmentTier({
        projectName => $proj_name,
        environmentName => $env_name,
        environmentTierName => 'WebLogic',
        resourceName => $res_name
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
    my $config_name = $self->_get_config_name;
    my $config_location = $self->ec->getProperty('/plugins/@PLUGIN_KEY@/project/ec_config/configLocation')->findvalue('//value')->string_value;
    my $exists = 0;
    eval {
        $self->ec->getProperty("/plugins/@PLUGIN_KEY@/project/$config_location/$config_name");
        $exists = 1;
    } or do {
        # Config does not exist
    };
    if ($exists) {
        $self->wl->logger->info("Configuration $config_name exists");
        return;
    }

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

    my $res_name = $self->ec->getProperty('/myJobStep/resourceName')->findvalue('//value')->string_value;
    my $config_name = "WebLogic Config $res_name";
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
    unless($self->{wlst_path}) {
        $self->{wlst_path} = $self->_find_wlst;
    }
    return $self->{wlst_path};
}

sub _find_wlst {
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
