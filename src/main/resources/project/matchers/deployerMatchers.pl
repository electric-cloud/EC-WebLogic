
push (@::gMatchers,
  
  {
   id =>        "connectionError",
   pattern =>          q{(.*)javax.naming.CommunicationException(.+)|(.*)java.net.ConnectException(.+)},
   action =>           q{
    
              my $description = ((defined $::gProperties{"summary"}) ? 
                    $::gProperties{"summary"} : '');
                    
              $description .= "Could not connect to the specified URL.";
                              
              setProperty("summary", $description . "\n");
    
   },  
  },
  
  {
   id =>        "noAppForStopOperation",
   pattern =>          q{^\[Deployer:149001\](.+)},
   action =>           q{
    
              my $description = ((defined $::gProperties{"summary"}) ? 
                    $::gProperties{"summary"} : '');
                    
              $description .= "$1";
                              
              setProperty("summary", $description . "\n");
    
   },  
  },

  {
   id =>        "state",
   pattern =>          q{Target state: (.+)},
   action =>           q{
    
              my $description = ((defined $::gProperties{"summary"}) ? 
                    $::gProperties{"summary"} : '');
                    
              $description .= "$1";
                              
              setProperty("summary", $description . "\n");
    
   },  
  },  
  
  

);

