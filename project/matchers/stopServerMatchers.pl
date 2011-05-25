
push (@::gMatchers,
  {
   id =>        "warning",
   pattern =>          q{^Warning:(.+)},
   action =>           q{
    
              my $description = ((defined $::gProperties{"summary"}) ? 
                    $::gProperties{"summary"} : '');
                    
              $description .= "$1";
                              
              setProperty("summary", $description . "\n");
    
   },
  },
  {
   id =>        "serverStopped",
   pattern =>          q{.*Disconnected from weblogic server.*},
   action =>           q{
    
              my $description = ((defined $::gProperties{"summary"}) ? 
                    $::gProperties{"summary"} : '');
                    
              $description .= "Disconnected from weblogic server";
                              
              setProperty("summary", $description . "\n");
    
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
