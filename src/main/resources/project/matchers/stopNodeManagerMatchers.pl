
push (@::gMatchers,
  
  {
   id =>        "stopError",
   pattern =>          q{Exception (.+)},
   action =>           q{
    
              my $description = 'Error stopping to Node Manager';
                                                  
              setProperty("summary", $description . "\n");
    
   },
  },


  {
   id =>        "stopSuccess",
   pattern =>          q{Successfully (.+)},
   action =>           q{
    
              my $description = 'Node Manager successfully stopped';
                                                  
              setProperty("summary", $description . "\n");
    
   },
  },
  
  {
   id =>        "stopWarning",
   pattern =>          q{Warning: cannot assure if Node Manager was stopped},
   action =>           q{
    
              my $description = 'Cannot assure if Node Manager was stopped.';
                                                  
              setProperty("summary", $description . "\n");
    
   },
  },

);

