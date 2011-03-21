# -------------------------------------------------------------------------
   # File
   #    startApp.pl
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
   $|=1;
   
   # -------------------------------------------------------------------------
   # Constants
   # -------------------------------------------------------------------------
   use constant {
       SUCCESS => 0,
       ERROR   => 1,
       
       PLUGIN_NAME => 'EC-WebLogic',
       MAIN_CLASS => 'weblogic.Deployer',
       CREDENTIAL_ID => 'credential',
       SEPARATOR_CHAR => ';',
       WIN_IDENTIFIER => 'MSWin32',
       STOP_COMMAND => '-stop',
       DEFAULT_JAVA_EXEC => 'java',
   	
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
  
  $::gAppName = trim(q($[appname]));
  $::gGracefulMode = trim(q($[gracefulmode]));
  $::gJavaPath = trim(q($[javapath]));
  $::gJavaParams = trim(q($[javaparams]));
  $::gConfigurationName = "$[configname]";
  $::gAdditionalOptions = "$[additionalcommands]";
  $::gEnvScriptPath = trim(q($[envscriptpath]));
  
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
    my $actualOperativeSystem = $^O;
    my %configuration;
    my $envLine = '';
    
    if($::gEnvScriptPath ne ''){
        if($^O eq WIN_IDENTIFIER){
           $envLine = '"'.$::gEnvScriptPath . '"';
        }else{
           $envLine = '. "'.$::gEnvScriptPath . '"';
        }
    }
    
    if($::gConfigurationName ne ''){
        %configuration = getConfiguration($::gConfigurationName);
    }
    
    if($::gJavaPath ne DEFAULT_JAVA_EXEC){
         push(@args, '"'.$::gJavaPath.'"');
    }else{
         push(@args, $::gJavaPath);
    }
    
    if($::gJavaParams && $::gJavaParams ne '') {
        push(@args, $::gJavaParams);
    }
    
    #Setting java main class to execute
    push(@args, MAIN_CLASS);
    
    #inject config...
    if(%configuration){
        
        if($configuration{'weblogic_url'} ne ''){
            push(@args, '-adminurl ' . $configuration{'weblogic_url'});
        }
        
        if($configuration{'user'} ne ''){
            push(@args, '-username ' . $configuration{'user'});
        }
        
        if($configuration{'password'} ne ''){
            push(@args, '-password ' . $configuration{'password'});
        }
    }
    
    #setting the stop command
    push(@args, STOP_COMMAND);
    
    if($::gAppName && $::gAppName ne '') {
        push(@args, '-name ' . $::gAppName);
    }
    
    if($::gGracefulMode && $::gGracefulMode ne '') {
        push(@args, '-graceful');
    }
    
    if($::gAdditionalOptions && $::gAdditionalOptions ne '') {
        push(@args, $::gAdditionalOptions);
    }
    
    my $cmdLine = createCommandLine(\@args);
    $props{'stopAppLine'} = $cmdLine;
    setProperties(\%props);
    
    if($envLine ne ''){
        system($envLine);
    }
    
    system($cmdLine);

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
  
  sub fixPath($){
   
     my ($absPath) = @_;
     
     my $separator;
     
     if($absPath && $absPath ne ''){
      
        if((substr($absPath, length($absPath)-1,1) eq '\\') ||
         substr($absPath, length($absPath)-1,1) eq '/'){
          
            return $absPath;
          
         }
     
         if($absPath =~ m/.*\/.+/){
         
             $separator = '/';
         
         }elsif($absPath =~ m/.+\\.+/) {
       
             $separator = "\\";
      
         }else{
             return '';
         }

         my $fixedPath = $absPath . $separator;
     
         return $fixedPath;
         
     }else{
      
         return '';
      
     }
     
     
   
  }
  
  
  main();
   
  1;
