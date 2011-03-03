
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
);

