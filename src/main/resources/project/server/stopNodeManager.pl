# -------------------------------------------------------------------------
   # File
   #    stopNodeManager.pl
   #
   # Dependencies
   #    None
   #
   # Template Version
   #    1.0
   #
   # Date
   #    02/16/2012
   #
   # Engineer
   #    Rafael Sanchez
   #
   # Copyright (c) 2012 Electric Cloud, Inc.
   # All rights reserved
   # -------------------------------------------------------------------------
   
   
   # -------------------------------------------------------------------------
   # Includes
   # -------------------------------------------------------------------------
   use ElectricCommander;
   use ElectricCommander::PropDB;
   use warnings;
   use strict;
   $|=1;
   
   # -------------------------------------------------------------------------
   # Constants
   # -------------------------------------------------------------------------
   use constant {
       SUCCESS => 0,
       ERROR   => 1,
       WIN_IDENTIFIER => 'MSWin32',
       
       SQUOTE => q{'},
       DQUOTE => q{"},
       BSLASH => q{\\},
	   
       EXECUTABLE => 'wlst.cmd',
       CREDENTIAL_ID => 'credential',
       DEFAULT_GENERATED_CONNECT_SCRIPT_FILENAME => "generated_script_connect_$[jobStepId].jython",
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
  
  $::gConfigurationName = trim(q($[configname]));
  $::gHostName = trim(q($[hostName]));
  $::gPort = trim(q($[port]));
  $::gDomainName = trim(q($[domainName]));
  $::gDomainPath = trim(q($[domainPath]));
  $::gWLSTAbsPath = trim(q($[wlstabspath]));
  $::gNMType = trim(q($[nmType]));
  
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
	stopNodeManager();
  }
  
  sub stopNodeManager() {
      
    my $ec = new ElectricCommander();
    $ec->abortOnError(0);
	    
    my %configuration;
    my %props;
	
	my @systemcall;
    
	my $LOGNAMEBASE = "StopNodeManager";
	
    my $user = '';
    my $pass = '';
    my $connectScript = '';    
    my $fixedLocation = $::gWLSTAbsPath;
	
	my $operatingSystem = $^O;
    
    if($::gConfigurationName ne ''){
        %configuration = getConfiguration($::gConfigurationName);
    }	 	
    
    #inject config...
    if(%configuration){
                
        if($configuration{'password'} ne ''){
            $pass = $configuration{'password'};
        }
        
        if($configuration{'user'} ne ''){
            $user = $configuration{'user'};
        }
    }else{
        print "Unexpected error: Could not retrieve info from the configuration hash\n";
        exit ERROR;
    }
	
	#jython script to connect and the stop the node manager
	$connectScript = "state = \"Successfully stopped the Node Manager.\"\n
try:\n
    nmConnect('$user', '$pass', '$::gHostName', '$::gPort', '$::gDomainName', '$::gDomainPath', '$::gNMType')\n

except WLSTException:\n
    state = \"Error stopping to Node Manager.\"\n

else:\n
    stopNodeManager()\n";
		
	#save the jython script
	open (MYFILE, '>>' . DEFAULT_GENERATED_CONNECT_SCRIPT_FILENAME);
	print MYFILE "$connectScript";
	close (MYFILE);
	  
	if($operatingSystem eq WIN_IDENTIFIER) {
		my $commandline = BSLASH . BSLASH . BSLASH . DQUOTE . $fixedLocation . " " . '"' . DEFAULT_GENERATED_CONNECT_SCRIPT_FILENAME . '"' . " " . BSLASH . BSLASH . BSLASH . DQUOTE;		
		my $logfile = $LOGNAMEBASE . "\." . $ENV{'COMMANDER_JOBSTEPID'} . ".log";
		my $errfile = $LOGNAMEBASE . "\." . $ENV{'COMMANDER_JOBSTEPID'} . ".err";
		$commandline = SQUOTE . $commandline . " 1>" . $logfile . " 2>" . $errfile . SQUOTE;
		$commandline = "exec(" . $commandline . ");";
		$commandline = DQUOTE . $commandline . DQUOTE;
		@systemcall = ("ecdaemon", "--", "ec-perl", "-e", $commandline);
	  
	} else {
		@systemcall = ($fixedLocation . " " . '"' . DEFAULT_GENERATED_CONNECT_SCRIPT_FILENAME . '"' . ' &');	  
	}

	my $cmdLine = createCommandLine(\@systemcall);
	
	system($cmdLine);
	
	$ec->setProperty("/myJob/jobID", $ENV{'COMMANDER_JOBSTEPID'});
	$ec->setProperty("/myJob/logType", "StopNodeManager");
	$ec->setProperty("/myJob/logExtention", ".log");
	
    $props{'stopNodeManagerLine'} = $cmdLine;
    setProperties(\%props);  
	
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
  main();
  1;
