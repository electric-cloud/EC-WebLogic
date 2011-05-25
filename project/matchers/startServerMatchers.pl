
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
   id =>        "serverShutdownState",
   pattern =>          q{Server State: (.+)},
   action =>           q{
    
              if($1 eq 'RUNNING'){
              
                   my $description = "Server is started";
              
              }else{
                   my $description = "Server is not started, actual status is $1\n";
              }
              
              
                              
              setProperty("summary", $description);
    
   },
  },
);

