# -------------------------------------------------------------------------
# File
#    startNodeManager.pl
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
    startNodeManager();
  }
  
  
  
    ########################################################################
  # startNodeManager - start the node manager
  #
  # Arguments:
  #   -weblogic start node manager script: absolute path to node manager script
  #
  # Returns:
  #   none
  #
  ########################################################################
  sub startNodeManager(){ 
      
	my ($SCRIPT) = $::gScriptLocation;
	
    my $ec = new ElectricCommander();
    $ec->abortOnError(0);
	
	my $operatingSystem = $^O;
    my %props;
	my @systemcall;
    
	my $LOGNAMEBASE = "StartNodeManager";
	  
	if($operatingSystem eq WIN_IDENTIFIER) {
		my $commandline = BSLASH . BSLASH . BSLASH . DQUOTE . $SCRIPT . BSLASH . BSLASH . BSLASH . DQUOTE;
		my $logfile = $LOGNAMEBASE . "\." . $ENV{'COMMANDER_JOBSTEPID'} . ".log";
		my $errfile = $LOGNAMEBASE . "\." . $ENV{'COMMANDER_JOBSTEPID'} . ".err";
		$commandline = SQUOTE . $commandline . " 1>" . $logfile . " 2>" . $errfile . SQUOTE;
		$commandline = "exec(" . $commandline . ");";
		$commandline = DQUOTE . $commandline . DQUOTE;
		@systemcall = ("ecdaemon", "--", "ec-perl", "-e", $commandline);
	  
	} else {
		@systemcall = ($SCRIPT . ' &');	  
	}
	
	my $cmdLine = createCommandLine(\@systemcall);
	
	system($cmdLine);
	
	$ec->setProperty("/myJob/jobID", $ENV{'COMMANDER_JOBSTEPID'});
	$ec->setProperty("/myJob/logType", "StartNodeManager");
	$ec->setProperty("/myJob/logExtention", ".err");
	
    $props{'startNodeManagerLine'} = $cmdLine;
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
  
  main();
   
  1;
  
