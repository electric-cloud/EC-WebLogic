# -------------------------------------------------------------------------
   # File
   #    startManagedServer.pl
   #
   # Dependencies
   #    None
   #
   # Template Version
   #    1.0
   #
   # Date
   #    11/05/2010
   #
   # Engineer
   #    Alonso Blanco
   #
   # Copyright (c) 2011 Electric Cloud, Inc.
   # All rights reserved
   # -------------------------------------------------------------------------
   
   
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
       
       PLUGIN_NAME => 'EC-WebLogic',
       WIN_IDENTIFIER => 'MSWin32',
       CREDENTIAL_ID => 'credential',
       SERVER_RUNNING_STATE => 'RUNNING',
       
       SQUOTE => q{'},
       DQUOTE => q{"},
       BSLASH => q{\\},
       
       DEFAULT_ADMIN_SERVER_NAME => 'AdminServer',
       SERVER_NOT_RUNNING_STATE => 'NOT_RUNNING',
       SERVER_UNEXPECTED_RESPONSE => 'unexpectedresponse',
       
       SERVER_RUNNING_STATE => 'RUNNING',
   	
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
      
    # create args array
    my @args = ();
    my %props;
    
    my $fixedLocation = $::gScriptLocation;
    
    my %configuration;
    
    my $user = '';
    my $password = '';
    my $serverName = '';
    my $urlName = '';
    
    if($::gConfigurationName ne ''){
        %configuration = getConfiguration($::gConfigurationName);
    }

    my $cmdLineParams = '';
    
    # if target: add to command string
    if($::gInstanceName && $::gInstanceName ne '') {
        $cmdLineParams .= ' ' . $::gInstanceName . ' ';
        $serverName = $::gInstanceName;
    }    
    
    #inject config...
    if(%configuration){
        
        if($configuration{'weblogic_url'} ne ''){
            $cmdLineParams .= ' ' . $configuration{'weblogic_url'} . ' ';
            $urlName = $configuration{'weblogic_url'};
        }
        
        if($configuration{'user'} ne ''){
            $cmdLineParams .= ' -Dweblogic.management.username=' . $configuration{'user'} . ' ';
            $user = $configuration{'user'};
        }
        
        if($configuration{'password'} ne ''){
            $cmdLineParams .= ' -Dweblogic.management.password=' . $configuration{'password'} . ' ';
            $password = $configuration{'password'};
        }
     
    }else{
        print "Unexpected error: Could not retrieve info from the configuration hash\n";
        exit ERROR;
    }

    #start managed server using ecdaemon
    startServer($::gScriptLocation);
    
    #almost none of the log is captured by ecdaemon, so a check is needed
    #to verify the RUNNING state on it
    verifyServerIsStarted($serverName, $urlName, $user, $password);

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
  
  ########################################################################
  # registerReports - creates a link for registering the generated report
  # in the job step detail
  #
  # Arguments:
  #   -reportFilename: name of the archive which will be linked to the job detail
  #   -reportName: name which will be given to the generated linked report
  #
  # Returns:
  #   none
  #
  ########################################################################
  sub registerReports($){
      
      my ($reportFilename, $reportName) = @_;
      
      if($reportFilename && $reportFilename ne ''){
          
          # get an EC object
          my $ec = new ElectricCommander();
          $ec->abortOnError(0);
          
          $ec->setProperty("/myJob/artifactsDirectory", '');
                  
          $ec->setProperty("/myJob/report-urls/" . $reportName, 
             "jobSteps/$[jobStepId]/" . $reportFilename);
              
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
          if($c ne CREDENTIAL_ID){
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
      
      if($operatingSystem eq WIN_IDENTIFIER) {
       
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
      $props{'startManagedServerLine'} = $cmdLine;
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
   
      my ($serverName, $managedServerURL, $user, $password)= @_;
      
      # create args array
      my @args = ();
      my %props;
      
      my $ec = new ElectricCommander();
      $ec->abortOnError(0);
      
      push(@args, '"'.$::gWLSTAbsPath.'"');

      #embedding jython code in the following scalar var
      my $fileContent = "
state = \"\"
x = 1
runningVariableIndicator = \"RUNNING\"

while state != runningVariableIndicator:
    
    print \"Checking state...\"
    
    try:
        connect('$user','$password','$managedServerURL')
        
    except WLSTException:
    
        state = \"NO_SERVER_FOUND\"
    
    else:
        cd(\"Servers/$serverName\")
        state = cmo.lookupServerLifeCycleRuntime().getState()
        
    x = x + 1
    
domainRuntime()
print \"Server State: $serverName is running\"";

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
      print "$content\n";
      
      print "RESULT\n";
      #evaluates if exit was successful to mark it as a success or fail the step
      if($? == SUCCESS){
       
          #set any additional error or warning conditions here
          #there may be cases in which an error occurs and the exit code is 0.
          #we want to set to correct outcome for the running step
          
          #verifying server actual state
          if($content =~ m/Server State: (.+)/){
           
              if($1 =~ m/(RUNNING|running|Successfully connected to managed Server)/){
                  
                  
                  #server is running
                  print "------------------------------------\n";
                  print "Server $serverName is up and running\n";
                  print "------------------------------------\n";
                  $ec->setProperty("/myJobStep/outcome", 'success');
                  
              }else{
                  
                  #server is not running
                  print "----------------------------------------\n";
                  print "Server is not started, it is in $1 state\n";
                  print "----------------------------------------\n";
                  $ec->setProperty("/myJobStep/outcome", 'error');
                  
              }
              
          }else{
           
               #Server may not be running, stats could not be read from the log.
               #A warning is signaled.
               print "-------------------------------------------------------\n";
               print "Server may not be started, could not check actual state\n";
               print "-------------------------------------------------------\n";
               $ec->setProperty("/myJobStep/outcome", 'warning');
           
          }
          
      }else{
       
          #server is not running
          print "-------------------------------------------------------------------\n";
          print "An unexpected error occurred and server may not be started, please check the log for more details\n";
          print "-------------------------------------------------------------------\n";
          $ec->setProperty("/myJobStep/outcome", 'error');
       
      }
   
  }
  
  main();
   
  1;
