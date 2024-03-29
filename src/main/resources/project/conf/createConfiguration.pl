## === createConfiguration template ===
# Please do not edit this file
use ElectricCommander;
use ElectricCommander::PropDB;
use JSON;
use Data::Dumper;

use constant {
    SUCCESS => 0,
    ERROR   => 1,
};

## get an EC object
my $ec = new ElectricCommander();

my $projName = '$[/myProject/name]';
my $configPropertySheet;
eval {
    $configPropertySheet = $ec->getPropertyValue('/myProject/ec_configPropertySheet');
    1;
} or do {
    $configPropertySheet = 'weblogic_cfgs';
    #    $configPropertySheet = 'ec_plugin_cfgs';
};

eval {
    createConfigurationPropertySheet($configPropertySheet);
    1;
} or do {
    my $err = $@;
    print $err;
    rollback($configPropertySheet, $err);
    $ec->setProperty("/myJob/configError", $err);
    exit 1;
};

my $steps     = [];
my $stepsJSON = eval {$ec->getPropertyValue("/projects/$projName/procedures/CreateConfiguration/ec_stepsWithAttachedCredentials")};
if ($stepsJSON) {
    $steps = decode_json($stepsJSON);
}

my $configName = '$[config]';

eval {
    my $opts = getActualParameters();

    for my $param (
        $ec->getFormalParameters({
                projectName   => $projName,
                procedureName => 'CreateConfiguration',
            }
        )->findnodes('//formalParameter')
    ) {
        my $type = $param->findvalue('type') . '';
        if ($type eq 'credential') {
            my $required       = $param->findvalue('required') . '';
            my $fieldName      = $param->findvalue('formalParameterName') . '';
            my $credentialName = $opts->{$fieldName};

            eval {
                my $createEmpty = 0;
                if (!$credentialName) {
                    # next;
                    $createEmpty    = 1;
                    $credentialName = $fieldName;
                }
                my $ref = createAndAttachCredential($credentialName, $configName, $configPropertySheet, $steps, $createEmpty);
                1;
            } or do {
                my $err = $@;
                if ($required) {
                    die $err;
                }
                else {
                    print "Failed to create credential $credentialName: $err\n";
                }
            };
        } ## end if ($type eq 'credential')
    } ## end for my $param ($ec->getFormalParameters...)
    1;
} or do {
    my $err = $@;
    print $err;
    rollback($configPropertySheet, $err);
    $ec->setProperty("/myJob/configError", $err);
    exit 1;
};

sub createAndAttachCredential {
    my ($credName, $configName, $configPropertySheet, $steps, $createEmpty) = @_;

    # if ($createEmpty) {
    #     print "CREATING EMPTY CREDENTIAL '$projName' : '$credName'\n";
    #     $ec->createCredential($projName, $credName, '', '');
    # }

    my $xpath;
    my $errors;
    my $clientID     = '';
    my $clientSecret = '';
    unless ($createEmpty) {
        $xpath        = $ec->getFullCredential($credName);
        $errors       = $ec->checkAllErrors($xpath);
        $clientID     = $xpath->findvalue("//userName");
        $clientSecret = $xpath->findvalue("//password");
    }

    my ($existingCredRef, $existingCredProjectName, $existingCredName);
    eval {
        $existingCredRef  = $xpath->findvalue("//credentialReference")->string_value();
        $existingCredName = $xpath->findvalue("//credentialName")->string_value();
        # here we have the credential that has been passed as a reference.
        $existingCredProjectName = $1 if $existingCredRef =~ m|\/projects\/(.*?)\/|s;
        # now we have all three of them. And if these values are defined - we have
        # existing credential that we just need to attach instead of create.
    } unless $createEmpty;
    my $projName = '$[/myProject/projectName]';

    my $credObjectName = $credName eq 'credential' ? $configName : "${configName}_${credName}";
    # die $credObjectName;
    my $configPath = "/projects/$projName/$configPropertySheet/$configName/$credName";
    if (!$existingCredRef) {
        print "No existing credential reference... Creating...\n";
        # Create credential
        $ec->deleteCredential($projName, $credObjectName);
        $xpath = $ec->createCredential($projName, $credObjectName, $clientID, $clientSecret);
        $errors .= $ec->checkAllErrors($xpath);

        # Give config the credential's real name

        $xpath = $ec->setProperty($configPath, $credObjectName);
        $errors .= $ec->checkAllErrors($xpath);

        # Give job launcher full permissions on the credential
        my $user = '$[/myJob/launchedByUser]';
        $xpath = $ec->createAclEntry(
            "user", $user, {
                projectName                => $projName,
                credentialName             => $credObjectName,
                readPrivilege              => 'allow',
                modifyPrivilege            => 'allow',
                executePrivilege           => 'allow',
                changePermissionsPrivilege => 'allow'
            }
        );
        $errors .= $ec->checkAllErrors($xpath);
    } ## end if (!$existingCredRef)
        # Attach credential to steps that will need it
    for my $step (@$steps) {
        if ($existingCredRef) {
            $credObjectName = $existingCredRef;
        }
        print "Attaching credential to procedure " . $step->{procedureName} . " at step " . $step->{stepName} . "\n";
        my $apath = $ec->attachCredential(
            $projName,
            $credObjectName, {
                procedureName => $step->{procedureName},
                stepName      => $step->{stepName}
            }
        );
        $errors .= $ec->checkAllErrors($apath);
    }

    if ("$errors" ne "") {
        # Cleanup the partially created configuration we just created
        $ec->deleteProperty($configPath);
        $ec->deleteCredential($projName, $credObjectName);
        my $errMsg = "Error creating configuration credential: " . $errors;
        $ec->setProperty("/myJob/configError", $errMsg);
        die $errMsg;
    }
} ## end sub createAndAttachCredential

sub rollback {
    my ($configPropertySheet, $error) = @_;

    if ($error !~ /already exists/) {
        my $configName = '$[config]';
        $ec->deleteProperty("/myProject/$configPropertySheet/$configName");
    }
}

sub getActualParameters {
    my $x       = $ec->getJobDetails($ENV{COMMANDER_JOBID});
    my $nodeset = $x->find('//actualParameter');
    my $opts;

    foreach my $node ($nodeset->get_nodelist) {
        my $parm = $node->findvalue('actualParameterName');
        if ($parm =~ m/credential$/s) {
            my $value = $node->findvalue('value');
            unless ($value) {
                # return undef;
                # $opts->{$parm} = $parm;
                # no cred ref here, we can next;
                next;
            }
            my $credRef = getCredentialReference($parm);
            if ($credRef) {
                my $key = $parm . '_ref';
                $opts->{$key} = $credRef;
            }
        }
        my $val = $node->findvalue('value');
        $opts->{$parm} = "$val";
    }
    return $opts;
} ## end sub getActualParameters

sub getCredentialReference {
    my ($credName) = @_;

    if (!$credName) {
        die "Cred name is mandatory to get credential reference...\n";
    }
    my $xpath           = $ec->getFullCredential($credName);
    my $existingCredRef = undef;
    eval {$existingCredRef = $xpath->findvalue("//credentialReference")->string_value();};

    return $existingCredRef;
}

sub createConfigurationPropertySheet {
    my ($configPropertySheet) = @_;

    ## load option list from procedure parameters
    my $ec = ElectricCommander->new;
    $ec->abortOnError(0);
    my $x       = $ec->getJobDetails($ENV{COMMANDER_JOBID});
    my $nodeset = $x->find('//actualParameter');
    my $opts    = getActualParameters();

    print "Got actual parameters\n";
    if (!defined $opts->{config} || "$opts->{config}" eq "") {
        die "config parameter must exist and be non-blank\n";
    }

    # check to see if a config with this name already exists before we do anything else
    my $xpath    = $ec->getProperty("/myProject/$configPropertySheet/$opts->{config}");
    my $property = $xpath->findvalue("//response/property/propertyName");

    if (defined $property && "$property" ne "") {
        my $errMsg = "A configuration named '$opts->{config}' already exists.";
        $ec->setProperty("/myJob/configError", $errMsg);
        die $errMsg;
    }

    my $table_path = "/myProject/$configPropertySheet";
    my $cfg        = new ElectricCommander::PropDB($ec, $table_path);

    # add all the options as properties
    foreach my $key (keys %{$opts}) {
        if ("$key" eq "config" || $key =~ m/_?credential_ref$/s) {
            next;
        }

        my $t = $opts->{"$key" . "_ref"};
        if ("$key" =~ m/credential$/s && $t) {
            $opts->{$key} = $t;
        }
        setColAdvanced($ec, $table_path, "$opts->{config}", "$key", "$opts->{$key}");
        # $cfg->setCol("$opts->{config}", "$key", "$opts->{$key}");
    }
} ## end sub createConfigurationPropertySheet

sub gen_credential_name {
    my ($config_name, $credential_name) = @_;

    if ($credential_name eq 'credential') {
        return $config_name;
    }

    return $config_name . '_' . $credential_name;
}

sub setColAdvanced {
    my ($ec, $path, $row, $col, $value) = @_;

    my $full_path = $path . "/$row/$col";
    $ec->setProperty($full_path, $value, {expandable => 0});
    return $value;
}

## === createConfiguration template ends ===
## === createConfiguration ends ===
# Please do not edit this file
