
push (@::gMatchers,
  
  {
   id =>        "serverRunningState",
   pattern =>          q{Server State: (.+)},
   action =>           q{
    
              my $description = '';
              
              if($1 =~ m/(RUNNING|running)/){
              
                   $description = "Server is started";
              
              }else{
                   $description = "Server is not started, actual status is $1\n";
              }
              
              setProperty("summary", $description);
    
   },
  },

  {
   id =>        "adminServerStopped",
   pattern =>          q{(.*)Disconnected from weblogic server: (.+)},
   action =>           q{
    
              my $description = "Admin Server $2 was stopped successfully\n";
              
              
              setProperty("summary", $description);
    
   },
  },
  
  {
   id =>        "error1",
   pattern =>          q{(Exception|Problem invoking WLST)},
   action =>           q{
    
              my $description = "An unexpected error has occurred, please check the log for more details\n";
              
              
              setProperty("summary", $description);
    
   },
  },
  
  {
   id =>        "successConnectingManagedServer",
   pattern =>          q{Successfully connected to managed Server (.+) that belongs to domain (.+)},
   action =>           q{
    
              my $description = "Successfully connected to managed Server $1";
              
              
              setProperty("summary", $description);
    
   },
  },
  
  {
   id =>        "adminServerStarted",
   pattern =>          q{Successfully connected to Admin Server '(.+)'.*},
   action =>           q{
    
             my $description= "Admin Server $1 successfully started";
                              
              setProperty("summary", $description . "\n");
    
   },
  },
  
);

