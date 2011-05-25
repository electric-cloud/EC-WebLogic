
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
   id =>        "serverShutdownState",
   pattern =>          q{Server State: (.+)},
   action =>           q{
    
              my $description = '';
              
              if($1 eq 'RUNNING'){
              
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

);

