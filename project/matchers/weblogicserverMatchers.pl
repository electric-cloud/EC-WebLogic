
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

);

