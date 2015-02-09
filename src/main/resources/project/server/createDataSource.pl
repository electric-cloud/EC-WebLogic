# -------------------------------------------------------------------------
   # File
   #    createDatasource.pl
   #
   # Dependencies
   #    None
   #
   # Template Version
   #    1.0
   #
   # Date
   #    06/05/2012
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

  # get an EC object
  my $ec = new ElectricCommander();
  $ec->abortOnError(0);
	
  $::gConfigurationName = ($ec->getProperty("configname") )->findvalue("//value");
  $::gDSName    = ($ec->getProperty("dsname") )->findvalue("//value");     
  $::gDSDBName    = ($ec->getProperty("dsdbname") )->findvalue("//value");     
  $::gDSTarget    = ($ec->getProperty("target") )->findvalue("//value");      
  $::gDriverURL    = ($ec->getProperty("driverurl") )->findvalue("//value");     
  $::gDSjndiname    = ($ec->getProperty("jndiname") )->findvalue("//value");     
  $::gDSDriverClass    = ($ec->getProperty("driverclass") )->findvalue("//value");     
  $::gDSUserName    = ($ec->getProperty("username") )->findvalue("//value");     
  $::gDSPassword    = ($ec->getProperty("password") )->findvalue("//value");     
  $::gWLSTAbsPath = ($ec->getProperty("wlstabspath") )->findvalue("//value");
   
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
    my %configuration;
    
    my $user = '';
    my $pass = '';
    my $url = '';
	
    if($::gConfigurationName ne ''){
        %configuration = getConfiguration($::gConfigurationName);
    }
    
    #inject config...
    if(%configuration){
        
        if($configuration{'weblogic_url'} ne ''){
            $url = $configuration{'weblogic_url'};
        }
        
        if($configuration{'user'} ne ''){
            $user = $configuration{'user'};
        }
        
        if($configuration{'password'} ne ''){
            $pass = $configuration{'password'};
        }
     
    }else{
        print "Unexpected error: Could not retrieve info from the configuration hash\n";
        exit ERROR;
    }

	my $obtainedResult = createDatasource($::gDSTarget, $url, $user, $pass, $::gDSName, $::gDSDBName, $::gDSjndiname, $::gDSDriverClass, $::gDSUserName, $::gDSPassword, $::gWLSTAbsPath, $::gDriverURL);
	
    $props{'url'} = $url;
    
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
  
  
  ##########################################################################
  # createDatasource - Creates a Datasource.
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
  sub createDatasource($){
      
      my ($serverName, $urlName, $user, $password, $DSName, $DSDBName, $DSjndiname, $DSDriverClass, $DSUserName, $DSPassword, $WLSTAbsPath, $DSdriverURL)= @_;
      
      my $obtainedResult = '';
      
      # create args array
      my @args = ();
      my %props;
      
      my $ec = new ElectricCommander();
      $ec->abortOnError(0);
      
      push(@args, '"'.$WLSTAbsPath.'"');
      
      #embedding jython code in the following scalar var
      my $fileContent = "state = \"\"\n
try:\n
    connect('$user','$password','$urlName')\n
    edit()\n
    startEdit()\n	
    dsName = '$DSName'\n
    dsDatabaseName='$DSDBName'\n
    DatasourceTarget='$serverName'\n
    dsJNDIName='$DSjndiname'\n
    dsDriverName='$DSDriverClass'\n
    dsURL='$DSdriverURL'\n
    dsUserName='$DSUserName'\n
    dsPassword='$DSPassword'\n
    dsTestQuery='SQL SELECT * FROM DUAL'\n
    
    print ''\n
    print 'Creating Datasource: ',dsName,'.'\n
    cmo.createJDBCSystemResource(dsName)\n
    cd('/JDBCSystemResources/' + dsName + '/JDBCResource/' + dsName)\n
    cmo.setName(dsName)\n

    cd('/JDBCSystemResources/' + dsName + '/JDBCResource/' + dsName + '/JDBCDataSourceParams/' + dsName )\n
    set('JNDINames',jarray.array([String(dsJNDIName)], String))\n

    cd('/JDBCSystemResources/' + dsName + '/JDBCResource/' + dsName + '/JDBCDriverParams/' + dsName )\n
    cmo.setUrl(dsURL)\n
    cmo.setDriverName( dsDriverName )\n
    cmo.setPassword(dsPassword)\n

    cd('/JDBCSystemResources/' + dsName + '/JDBCResource/' + dsName + '/JDBCConnectionPoolParams/' + dsName )\n
    cmo.setTestTableName(dsTestQuery)\n
    cd('/JDBCSystemResources/' + dsName + '/JDBCResource/' + dsName + '/JDBCDriverParams/' + dsName + '/Properties/' + dsName )\n
    cmo.createProperty('user')\n
    cd('/JDBCSystemResources/' + dsName + '/JDBCResource/' + dsName + '/JDBCDriverParams/' + dsName + '/Properties/' + dsName + '/Properties/user')\n
    cmo.setValue(dsUserName)\n

    cd('/JDBCSystemResources/' + dsName + '/JDBCResource/' + dsName + '/JDBCDriverParams/' + dsName + '/Properties/' + dsName )\n
    cmo.createProperty('databaseName')\n

    cd('/JDBCSystemResources/' + dsName + '/JDBCResource/' + dsName + '/JDBCDriverParams/' + dsName + '/Properties/' + dsName + '/Properties/databaseName')\n
    cmo.setValue(dsDatabaseName)\n

    cd('/JDBCSystemResources/' + dsName + '/JDBCResource/' + dsName + '/JDBCDataSourceParams/' + dsName )\n
    cmo.setGlobalTransactionsProtocol('OnePhaseCommit')\n

    cd('/SystemResources/' + dsName )\n
    set('Targets',jarray.array([ObjectName('com.bea:Name=' + DatasourceTarget + ',Type=Server')], ObjectName))\n

    print 'Datasource: ',dsName,', has been created Successfully'\n
    print ''\n
	
except WLSTException:\n
    print '***** CANNOT CREATE Datasource !!! Check If the Datasource With the Name : ' , dsName ,' already exists or NOT'\n
else:\n
    save()\n
    activate()\n";

      open (MYFILE, '>>createDatasource.jython');
      print MYFILE "$fileContent";
      close (MYFILE);
      
      push(@args, '"createDatasource.jython"');
     
      
      my $cmdLine = createCommandLine(\@args);
			
			
      $props{'createDatasourceLine'} = $cmdLine;
      setProperties(\%props);
      
      #execute command
      my $content = `$cmdLine`;
	  
	  #remove wrong char and string from the output
      $content =~ s/\.\.\./\./g;
      $content =~ s/\://g;
	  
	  print "$content\n"; 		
            
      return $obtainedResult;   
   
  }
  
  main();
   
  1;
