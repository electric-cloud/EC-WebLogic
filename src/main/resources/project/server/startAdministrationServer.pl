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

   
# -------------------------------------------------------------------------
# Includes
# -------------------------------------------------------------------------
use ElectricCommander;
use ElectricCommander::PropDB;
use warnings;
use strict;
use Data::Dumper;
$|=1;
   
# -------------------------------------------------------------------------
# Constants
# -------------------------------------------------------------------------
use constant {
    SUCCESS => 0,
    ERROR   => 1,
    TRUE => 1,
    FALSE => 0,
    PLUGIN_NAME => 'EC-WebLogic',
    WIN_IDENTIFIER => 'MSWin32',
    CREDENTIAL_ID => 'credential',
    SQUOTE => q{'},
    DQUOTE => q{"},
    BSLASH => q{\\},
    DEFAULT_ADMIN_SERVER_NAME => 'AdminServer',
    SERVER_RUNNING_STATE => 'RUNNING',
    SERVER_NOT_RUNNING_STATE => 'NOT_RUNNING',
    SERVER_UNEXPECTED_RESPONSE => 'unexpectedresponse',
};

########################################################################
# trim - deletes blank spaces before and after the entered value in 
# the argument
#
# Arguments:
#   -untrimmedString: string that will be trimmed
#
# Returns:
#   trimmed string
#
########################################################################  
sub trim($) {
    my ($untrimmedString) = @_;

    my $string = $untrimmedString;
    #removes leading spaces
    $string =~ s/^\s+//;
    #removes trailing spaces
    $string =~ s/\s+$//;
    #returns trimmed string
    return $string;
}

# -------------------------------------------------------------------------
# Variables
# -------------------------------------------------------------------------

$::gInstanceName = trim(q($[admininstancename]));
$::gScriptLocation = trim(q($[scriptlocation]));
$::gConfigurationName = trim(q($[configname]));
$::gWLSTAbsPath = trim(q($[wlstabspath]));
$::gMaxElapsedTime = "$[maxelapsedtime]";
$::gIntervalWaitTime = 10;
$::gSuccessCriteria = SERVER_RUNNING_STATE;

# -------------------------------------------------------------------------
# Main functions
# -------------------------------------------------------------------------

########################################################################
# main - contains the whole process to be done by the plugin, it builds 
#        the command line, sets the properties and the working directory
#
# Arguments:
#   none
#
# Returns:
#   none
#
########################################################################
sub main() {
    # get an EC object
    my $ec = new ElectricCommander();
    $ec->abortOnError(0);

    # create args array
    my @args = ();
    my %props;

    my $fixedLocation = $::gScriptLocation;
    my %configuration;

    my $user = '';
    my $pass = '';
    my $serverName = '';
    my $url = '';
    my $elapsedTime = 0;
    my $startTimeStamp = time;

    if ($::gConfigurationName ne '') {
        %configuration = getConfiguration($::gConfigurationName);
    }

    my $cmdLineParams = '';
    # if target: add to command string
    if ($::gInstanceName && $::gInstanceName ne '') {
        $cmdLineParams .= ' ' . $::gInstanceName . ' ';
        $serverName = $::gInstanceName;
    }

    #inject config...
    if (%configuration) {
        if ($configuration{'weblogic_url'} ne '') {
            $cmdLineParams .= ' ' . $configuration{'weblogic_url'} . ' ';
            $url = $configuration{'weblogic_url'};
        }

        if ($configuration{'user'} ne '') {
            $cmdLineParams .= ' -Dweblogic.management.username=' . $configuration{'user'} . ' ';
            $user = $configuration{'user'};
        }

        if ($configuration{'password'} ne '') {
            $cmdLineParams .= ' -Dweblogic.management.password=' . $configuration{'password'} . ' ';
            $pass = $configuration{'password'};
        }

    } else {
        print "Unexpected error: Could not retrieve info from the configuration hash\n";
        exit ERROR;
    }

    #start managed server using ecdaemon
    startServer($::gScriptLocation);

    sleep 15;
    #checks if max elapsed time is default
    if ($::gMaxElapsedTime eq '') {
        $::gMaxElapsedTime = 0;
    }

    #check elapsed time is not negative
    if ($::gMaxElapsedTime !~ m/^\d+$/s) {
        print 'Elapsed time should be a positive integer.';
        exit ERROR;
    }

    #getting all info from the configuration, url, user and pass
    if ($::gConfigurationName ne '') {
        #retrieve configuration hash
        %configuration = getConfiguration($::gConfigurationName);
        #insert into params the respective values by reference
        getDataFromConfig(\%configuration, \$url, \$user, \$pass);
    }

    #setting variables for iterating
    my $retries = 0;
    my $attempts = 0;
    my $continueFlag = 0;
    my $successCriteriaReached = FALSE;
    do {
        $attempts++;
        print "------------\nATTEMPT $attempts\n";
        #first attempt will always be done, no need to be forced to sleep
        if ($retries > 0) {
            my $testtimestart = time;
            print "Waiting $::gIntervalWaitTime seconds before starting Attempt #$attempts...\n\n";
            #sleeping process during N seconds
            sleep $::gIntervalWaitTime;
        }
        #check the status of the server in a round
        my $obtainedResult = verifyServerIsStarted($::gInstanceName, $url, $user, $pass);
        #does the expected criteria match the obtained criteria?
        if ($::gSuccessCriteria eq $obtainedResult) {
            $successCriteriaReached = TRUE;
        } else {
            $successCriteriaReached = FALSE;
        }

        print "\nCriteria reached: ";
        if ($successCriteriaReached == TRUE) {
            print "True\n";
        } else {
            print "False\n";
        }
        $elapsedTime = time - $startTimeStamp;
        print "Elapsed time so far: $elapsedTime seconds\n";
        $retries++;
        #evaluate if loop has to be continued
        $continueFlag = keepChecking($successCriteriaReached, $elapsedTime);

        print "------------\n\n";
    } while ($continueFlag == TRUE);

    #print stats
    print "\n---------------------------------\n";
    print "URL: $url\n";
    print "Attempts of connecting to the server: $attempts\n";
    print "Total elapsed time: $elapsedTime seconds";
    print "---------------------------------\n";

    $props{'url'} = $url;

    setProperties(\%props);
}
########################################################################
# keepChecking - determines if analysis must be continued or aborted
#
# Arguments:
#   -successCriteriaReached: indicates if the selected success criteria by
#          the user matches the criteria so far.
#   -elapsedTime: current analysis' elapsed time
#
# Returns:
#   -continueFlag: determines if process must continued or terminated
#                      (1 => continued. 0 => terminated)
#
#########################################################################  
sub keepChecking($){
    my ($successCriteriaReached, $elapsedTime) = @_;
    my $continueFlag;
    #If entered max elapsed time is default or criteria is reached, 
    # evaluation is done.
    #If current elapsed time is lower than the maximum established 
    # by the user and criteria has not been reached, evaluation 
    # shall continue.
    #If current elapsed is equal or greater, than the maximum permitted. The
    # evaluation must be terminated.
    if ($::gMaxElapsedTime == 0 || $successCriteriaReached == TRUE) {
        $continueFlag = FALSE;
    } elsif ($elapsedTime < $::gMaxElapsedTime && $successCriteriaReached == FALSE) {
        $continueFlag = TRUE;
    } elsif ($elapsedTime >= $::gMaxElapsedTime) {
        $continueFlag = FALSE;
    }
    #print "max time $::gMaxElapsedTime continue flag $continueFlag";
    return $continueFlag;
}

#########################################################################
# getDataFromConfig - gets the data required from the config for this procedure
#                        and pass it by reference to the actual function's
#                        parameters.
#
# Arguments:
#   -configuration: hash containing the data from the config
#   -url: parameter that will receive the value of the URL, must be passed
#              as reference.
#   -user: config's user whose value is set in this function, must be passed
#              as reference.
#   -pass: config's password whose value is set in this function, must be passed
#              as reference.
#
# Returns:
#   none
#
#########################################################################  
sub getDataFromConfig($){
    my($configuration, $url, $user, $pass) = @_;
    if ($configuration->{'weblogic_url'} && $configuration->{'weblogic_url'} ne '') {
        ${$url} = $configuration->{'weblogic_url'};
    } else {
        print "Error: Could not get URL from configuration '$::gConfigurationName'\n";
        exit ERROR;
    }
    if ($configuration->{'user'} && $configuration->{'user'} ne '') {
        ${$user} = $configuration->{'user'};
    } else {
        #print "Error: Could not get user from configuration '$::gConfigName'\n";
        #exit ERROR;
    }
    if ($configuration->{'password'} && $configuration->{'password'} ne '') {
        ${$pass} = $configuration->{'password'};
    } else {
        #print "Error: Could not get password from configuration $::gConfigName'\n";
        #exit ERROR;
    }
}
########################################################################
# createCommandLine - creates the command line for the invocation
# of the program to be executed.
#
# Arguments:
#   -arr: array containing the command name (must be the first element) 
#         and the arguments entered by the user in the UI
#
# Returns:
#   -the command line to be executed by the plugin
#
########################################################################
sub createCommandLine($) {
    my ($arr) = @_;
    my $commandName = @$arr[0];
    my $command = $commandName;
    shift(@$arr);
    foreach my $elem (@$arr) {
        $command .= " $elem";
    }
    return $command;
}


########################################################################
# setProperties - set a group of properties into the Electric Commander
#
# Arguments:
#   -propHash: hash containing the ID and the value of the properties 
#              to be written into the Electric Commander
#
# Returns:
#   none
#
########################################################################
sub setProperties($) {
    my ($propHash) = @_;

    # get an EC object
    my $ec = new ElectricCommander();
    $ec->abortOnError(0);

    foreach my $key (keys % $propHash) {
        my $val = $propHash->{$key};
        $ec->setProperty("/myCall/$key", $val);
    }
}

##########################################################################
# getConfiguration - get the information of the configuration given
#
# Arguments:
#   -configName: name of the configuration to retrieve
#
# Returns:
#   -configToUse: hash containing the configuration information
#
#########################################################################
sub getConfiguration($){
    my ($configName) = @_;

    # get an EC object
    my $ec = new ElectricCommander();
    $ec->abortOnError(0);

    my %configToUse;

    my $proj = "$[/myProject/projectName]";
    my $pluginConfigs = new ElectricCommander::PropDB($ec,"/projects/$proj/weblogic_cfgs");
    my %configRow = $pluginConfigs->getRow($configName);
    # Check if configuration exists
    unless(keys(%configRow)) {
        print "Configuration '$configName' doesn't exist.\n";
        exit ERROR;
    }
    # Get user/password out of credential
    my $xpath = $ec->getFullCredential($configRow{credential});
    $configToUse{'user'} = $xpath->findvalue("//userName");
    $configToUse{'password'} = $xpath->findvalue("//password");
    foreach my $c (keys %configRow) {
        #getting all values except the credential that was read previously
        if ($c ne CREDENTIAL_ID) {
            $configToUse{$c} = $configRow{$c};
        }
    }
    return %configToUse;
}

########################################################################
# startServer - uses ecdaemon for starting a Managed Server
#
# Arguments:
#   -weblogic start managed server script: absolute path to managed server script
#   -server name: name of the instance of the managed server
#   -URL: URL (including protocol and port) of the Admin Server of the domain
#   -user: user of the admin server
#   -password: password of the admin server
#
# Returns:
#   none
#
########################################################################
sub startServer($){
    my ($SCRIPT, $serverName, $adminServerURL, $user, $pass) = @_;

    # $The quote and backslash constants are just a convenient way to represtent literal literal characters so it is obvious
    # in the concatentations. NOTE: BSLASH ends up being a single backslash, it needs to be doubled here so it does not
    # escape the right curly brace.

    my $operatingSystem = $^O;
    print qq{OS: $operatingSystem\n};

    # Ideally, the logs should exist in the step's workspace directory, but because the ecdaemon continues after the step is
    # completed the temporary drive mapping to the workspace is gone by the time we want to write to it. Instead, the log
    # and errors get the JOBSTEPID appended and it goes in the Tomcat root directory.
    my $LOGNAMEBASE = "weblogicstartmanagedserver";

    # If we try quoting in-line to get the final string exactly right, it will be confusing and ugly. Only the last
    # parameter to our outer exec() needs _literal_ single and double quotes inside the string itself, so we build that
    # parameter before the call rather than inside it. Using concatenation here both substitutes the variable values and
    # puts literal quote from the constants in the final value, but keeps any other shell metacharacters from causing
    # trouble.

    my @systemcall;

    my $shellscript = $SCRIPT;
    if ($operatingSystem eq WIN_IDENTIFIER) {
        # Windows has a much more complex execution and quoting problem. First, we cannot just execute under "cmd.exe"
        # because ecdaemon automatically puts quote marks around every parameter passed to it -- but the "/K" and "/C"
        # option to cmd.exe can't have quotes (it sees the option as a parameter not an option to itself). To avoid this, we
        # use "ec-perl -e xxx" to execute a one-line script that we create on the fly. The one-line script is an "exec()"
        # call to our shell script. Unfortunately, each of these wrappers strips away or interprets certain metacharacters
        # -- quotes, embedded spaces, and backslashes in particular. We end up escaping these metacharacters repeatedly so
        # that when it gets to the last level it's a nice simple script call. Most of this was determined by trial and error
        # using the sysinternals procmon tool.
        my $commandline = BSLASH . BSLASH . BSLASH . DQUOTE . $shellscript . BSLASH . BSLASH . BSLASH . DQUOTE;
        my $logfile = $LOGNAMEBASE . "-" . $ENV{'COMMANDER_JOBSTEPID'} . ".log";
        my $errfile = $LOGNAMEBASE . "-" . $ENV{'COMMANDER_JOBSTEPID'} . ".err";
        $commandline = SQUOTE . $commandline . " 1>" . $logfile . " 2>" . $errfile . SQUOTE;
        $commandline = "exec(" . $commandline . ");";
        $commandline = DQUOTE . $commandline . DQUOTE;
        @systemcall = ("ecdaemon", "--", "ec-perl", "-e", $commandline);

    } else {
        # Linux is comparatively simple, just some quotes around the script name in case of embedded spaces.
        # IMPORTANT NOTE: At this time the direct output of the script is lost in Linux, as I have not figured out how to
        # safely redirect it. Nothing shows up in the log file even when I appear to get the redirection correct; I believe
        # the script might be putting the output to /dev/tty directly (or something equally odd). Most of the time, it's not
        # really important since the vital information goes directly to $CATALINA_HOME/logs/catalina.out anyway. It can lose
        # important error messages if the paths are bad, etc. so this will be a JIRA.
        @systemcall = ($shellscript . " &");
    }
    #print "Command Parameters:\n" . Dumper(@systemcall) . "--------------------\n";
    my %props;
    my $cmdLine = createCommandLine(\@systemcall);
    $props{'startAdminServerLine'} = $cmdLine;
    setProperties(\%props);
    print "cmd line: $cmdLine\n";
    system($cmdLine);

}

##########################################################################
# verifyServerIsStarted - verifies if the specified managed server
#                            is running.
#
# Arguments:
#   -ServerName: name of the server instance
#   -URL: Managed Server URL (including protocol and port)
#   -User: user for logging into the admin server
#   -Password: password for logging into the admin server
#
# Returns:
#   none
#
#########################################################################  
sub verifyServerIsStarted($){
    my ($serverName, $urlName, $user, $password)= @_;

    my $obtainedResult = '';
    # create args array
    my @args = ();
    my %props;

    my $ec = new ElectricCommander();
    $ec->abortOnError(0);

    push(@args, '"'.$::gWLSTAbsPath.'"');

    #embedding jython code in the following scalar var
    my $fileContent = "state = \"\"\n
try:\n
    connect('$user','$password','$urlName')\n

except WLSTException:\n

    state = \"NO_SERVER_FOUND\"\n

else:\n

    domainRuntime()\n

    state = cmo.lookupServerLifeCycleRuntime('$serverName').getState()\n

print \"Server State: \" + state\n";

    open (MYFILE, '>>verifyServer.jython');
    print MYFILE "$fileContent";
    close (MYFILE);
    push(@args, '"verifyServer.jython"');

    my $cmdLine = createCommandLine(\@args);

    $props{'wlstLine'} = $cmdLine;
    setProperties(\%props);
    #execute command
    my $content = `$cmdLine`;
    #print log
    print "$content";
    #evaluates if exit was successful to mark it as a success or fail the step
    if ($? == SUCCESS) {
        #set any additional error or warning conditions here
        #there may be cases in which an error occurs and the exit code is 0.
        #we want to set to correct outcome for the running step
        #verifying server actual state
        if ($content =~ m/Server State: (.+)/) {
            if ($1 eq SERVER_RUNNING_STATE) {
                $ec->setProperty("/myJobStep/outcome", 'success');
                #server is running
                print "Server $serverName is up and running\n";
                $obtainedResult = SERVER_RUNNING_STATE;
            } else {
                $ec->setProperty("/myJobStep/outcome", 'error');
                #server is not running
                print "Server is not started, it is in $1 state\n";
                $obtainedResult = SERVER_NOT_RUNNING_STATE;
            }
        } else {
            #Server may not be running, stats could not be read from the log.
            #A warning is signaled.
            print "-------------------------------------------------------\n";
            print "Server may not be started, could not check actual state\n";
            print "-------------------------------------------------------\n";
            $ec->setProperty("/myJobStep/outcome", 'warning');
        }
    } else {
        $ec->setProperty("/myJobStep/outcome", 'error');
        #server is not running
        print "-------------------------------------------------------------------\n";
        print "An unexpected error occurred, please check the log for more details\n";
        print "-------------------------------------------------------------------\n";
        $obtainedResult = SERVER_UNEXPECTED_RESPONSE;
    }
    return $obtainedResult;
}
main();

1;

