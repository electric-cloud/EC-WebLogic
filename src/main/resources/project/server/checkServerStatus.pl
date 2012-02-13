# -------------------------------------------------------------------------
   # File
   #    checkServerStatus.pl
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
   use LWP::UserAgent;
   use HTTP::Request;
   use warnings;
   use strict;
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
       CREDENTIAL_ID => 'credential',
       
       SERVER_RUNNING_STATE => 'RUNNING',
       SERVER_NOT_RUNNING_STATE => 'NOT_RUNNING',
       SERVER_UNEXPECTED_RESPONSE => 'unexpectedresponse',
       
       GENERATE_REPORT => 1,
       DO_NOT_GENERATE_REPORT => 0,
       
   
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
  #########################################################################
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
  
  $::gConfigName = "$[configname]";
  $::gMaxElapsedTime = "$[maxelapsedtime]";
  $::gSuccessCriteria = "$[successcriteria]";
  $::gIntervalWaitTime = 10;
  $::gServerInstanceName = "$[instancename]";
  $::gWLSTAbsPath = trim(q($[wlstabspath]));
  
  # -------------------------------------------------------------------------
  # Main functions
  # -------------------------------------------------------------------------
  
  
  ########################################################################
  # main - contains the whole process to be done by the plugin
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
    my %props;
    
    my $url = '';
    my $user = '';
    my $pass = '';
    my %configuration;
    
    my $elapsedTime = 0;
    my $startTimeStamp = time;
    
    #checks if max elapsed time is default
    if($::gMaxElapsedTime eq ''){
        $::gMaxElapsedTime = 0;
    }

    #check elapsed time is not negative
    if($::gMaxElapsedTime < 0){
     
        print 'Elapsed time cannot be a negative number. Enter a number greater or equal than zero.';
        exit ERROR;
        
    }
    
    #getting all info from the configuration, url, user and pass
    if($::gConfigName ne ''){
     
        #retrieve configuration hash
        %configuration = getConfiguration($::gConfigName);
        
        #insert into params the respective values by reference
        getDataFromConfig(\%configuration, \$url, \$user, \$pass);
        
    }

    #setting variables for iterating
    my $retries = 0;
    my $attempts = 0;
    my $continueFlag = 0;
    my $successCriteriaReached = FALSE;
    
    do{
        
        $attempts++;
        print "------------\nATTEMPT $attempts\n";
        
        #first attempt will always be done, no need to be forced to sleep
        if($retries > 0){
           
           my $testtimestart = time;
           
           print "Waiting $::gIntervalWaitTime seconds before starting Attempt #$attempts...\n\n";
           
           #sleeping process during N seconds
           sleep $::gIntervalWaitTime;
           
        }
        
        #check the status of the server in a round
        my $obtainedResult = checkServerStatus(
            $::gServerInstanceName, $url, $user, $pass);
        
        #does the expected criteria match the obtained criteria?
        if($::gSuccessCriteria eq $obtainedResult){
            $successCriteriaReached = TRUE;
        }else{
            $successCriteriaReached = FALSE;
        }
        
        print "\nCriteria reached: ";
        
        if($successCriteriaReached == TRUE){
         
            print "True\n";
         
        }else{
           
            print "False\n";
            
        }
        
        $elapsedTime = time - $startTimeStamp;
        print "Elapsed time so far: $elapsedTime seconds\n";
        $retries++;
        
        #evaluate if loop has to be continued
        $continueFlag = keepChecking($successCriteriaReached, $elapsedTime);

        print "------------\n\n";
     
    }while($continueFlag == TRUE);
    
    #print stats
    print "\n---------------------------------\n";
    print "URL: $url\n";
    print "Attempts of connecting to the server: $attempts\n";
    print "Total elapsed time: $elapsedTime seconds";
    
    determineFinalResult($successCriteriaReached);
    
    print "---------------------------------\n";
    
    $props{'url'} = $url;
    
    setProperties(\%props);
    
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
          print "Error: Configuration '$configName' doesn't exist\n";
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
      if($::gMaxElapsedTime == 0 || $successCriteriaReached == TRUE){
       
          $continueFlag = FALSE;
       
      }elsif($elapsedTime < $::gMaxElapsedTime && $successCriteriaReached == FALSE){
         
          $continueFlag = TRUE;
       
      }elsif($elapsedTime >= $::gMaxElapsedTime){
       
          $continueFlag = FALSE;
         
      }
      #print "max time $::gMaxElapsedTime continue flag $continueFlag";
      
      return $continueFlag;
   
  }
  
  #########################################################################
  # determineFinalResult - prints the final obtained result and sets the step's
  #                              outcome
  #
  # Arguments:
  #   -successCriteriaReached: indicates if the selected success criteria by
  #          the user matches the criteria so far.
  #
  # Returns:
  #   none
  #
  #########################################################################
  sub determineFinalResult($){
   
      my ($successCriteriaReached) = @_;
      
      # get an EC object
      my $ec = new ElectricCommander();
      $ec->abortOnError(0);
      
      if($successCriteriaReached == TRUE){
          
          if($::gSuccessCriteria eq SERVER_RUNNING_STATE){
           
              print "\nRESULT: Criteria reached, server is running\n";
                 
          }else{
           
              print "\nRESULT: Criteria reached, server is not running\n";
                 
          }
          $ec->setProperty("/myJobStep/outcome", 'success');
          
      }else{
       
          if($::gSuccessCriteria eq SERVER_NOT_RUNNING_STATE){
           
              print "\nRESULT: Criteria not reached, server is running, check log for more details\n";
                 
          }else{
           
              print "\nRESULT: Criteria not reached, server is not running, check log for more details\n";
                 
          }
          
          $ec->setProperty("/myJobStep/outcome", 'error');
          
      }
   
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
        
        if($configuration->{'weblogic_url'} && $configuration->{'weblogic_url'} ne ''){
            ${$url} = $configuration->{'weblogic_url'};
        }else{
            print "Error: Could not get URL from configuration '$::gConfigName'\n";
            exit ERROR;
        }
        
        if($configuration->{'user'} && $configuration->{'user'} ne ''){
            ${$user} = $configuration->{'user'};
        }else{
            #print "Error: Could not get user from configuration '$::gConfigName'\n";
            #exit ERROR;
        }
        
        if($configuration->{'password'} && $configuration->{'password'} ne ''){
            ${$pass} = $configuration->{'password'};
        }else{
            #print "Error: Could not get password from configuration $::gConfigName'\n";
            #exit ERROR;
        }
   
  }
  
  sub checkServerStatus($){
   
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
      if($? == SUCCESS){
       
          $ec->setProperty("/myJobStep/outcome", 'success');
          
          #set any additional error or warning conditions here
          #there may be cases in which an error occurs and the exit code is 0.
          #we want to set to correct outcome for the running step
          
          #verifying server actual state
          if($content =~ m/Server State: (.+)/){
           
              if($1 eq SERVER_RUNNING_STATE){
                  
                  #server is running
                  print "Server $serverName is up and running\n";
                  $obtainedResult = SERVER_RUNNING_STATE;
                  
              }else{
                  
                  #server is not running
                  print "Server is not started, it is in $1 state\n";
                  $obtainedResult = SERVER_NOT_RUNNING_STATE;
                  
              }
              
          }
          
      }else{
       
          #server is not running
          print "-------------------------------------------------------------------\n";
          print "An unexpected error occurred, please check the log for more details\n";
          print "-------------------------------------------------------------------\n";
          $obtainedResult = SERVER_UNEXPECTED_RESPONSE;
          
       
      }
      
      return $obtainedResult;
   
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
  
  main();
   
  1;
