# -------------------------------------------------------------------------
# File
#    stopAdministrationServer.pl
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
       WIN_IDENTIFIER => 'MSWin32',
     
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
  
  $::gScriptLocation = trim(q($[scriptlocation]));
   
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
    
    my $ec = new ElectricCommander();
    $ec->abortOnError(0);
    
    my $fixedLocation = $::gScriptLocation;
    
    push(@args, '"'.$fixedLocation.'"');

    my $cmdLine = createCommandLine(\@args);
    $props{'stopAdminServerLine'} = $cmdLine;
    setProperties(\%props);
    
    #execute command
    my $content = `$cmdLine`;
    
    #print log
    print "$content\n";
    
    #evaluates if exit was successful to mark it as a success or fail the step
    if($? == SUCCESS){
     
        $ec->setProperty("/myJobStep/outcome", 'success');
        
        #set any additional error or warning conditions here
        #there may be cases in which an error occurs and the exit code is 0.
        #we want to set to correct outcome for the running step
#        if($content =~ m/WSVR0028I:/){
#            #license expired warning
#            $ec->setProperty("/myJobStep/outcome", 'warning');
#        }
        
    }else{
        $ec->setProperty("/myJobStep/outcome", 'error');
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
  
  main();
   
  1;
  
