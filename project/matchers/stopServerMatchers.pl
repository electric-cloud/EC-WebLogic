
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




);

