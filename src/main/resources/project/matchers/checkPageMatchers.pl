
push (@::gMatchers,
  
  {
   id =>        "testResult",
   pattern =>          q{RESULT: (.+)},
   action =>           q{
    
              my $description = ((defined $::gProperties{"summary"}) ? 
                    $::gProperties{"summary"} : '');
                    
              $description .= "$1";
                              
              setProperty("summary", $description . "\n");
    
   },
  },

);

