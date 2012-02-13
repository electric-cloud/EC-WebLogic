
push (@::gMatchers,

  {
   id =>        "serverFailed",
   pattern =>          q{.*BEA-000362.*},
   action =>           q{
    
              my $description = ((defined $::gProperties{"summary"}) ? 
                    $::gProperties{"summary"} : '');
                    
              $description .= "Server failed starting.";
                              
              setProperty("summary", $description . "\n");
    
   },
  },
  
  {
   id =>        "serverState",
   pattern =>          q{Server State: (.+)},
   action =>           q{
    
              if($1 =~ m/(RUNNING|running)/{
              
                   my $description = "Server is started";
              
              }else{
                   my $description = "Server is not started, actual status is $1\n";
              }
              
              
                              
              setProperty("summary", $description);
    
   },
  },
  
  {
   id =>        "adminServerStarted",
   pattern =>          q{Successfully connected to Admin Server '(.+)'},
   action =>           q{
    
             my $description = ((defined $::gProperties{"summary"}) ? 
                    $::gProperties{"summary"} : '');
                    
              $description .= "Admin Server $1 successfully started";
                              
              setProperty("summary", $description . "\n");
    
   },
  },
  
);

