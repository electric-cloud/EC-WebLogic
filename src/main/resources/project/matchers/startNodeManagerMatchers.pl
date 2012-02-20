
push (@::gMatchers,
  
  
  {
   id =>        "startError",
   pattern =>          q{Exception (.+)},
   action =>           q{
    
              my $description = 'Error starting to Node Manager';
                                                  
              setProperty("summary", $description . "\n");
    
   },
  },


  {
   id =>        "startSuccess",
   pattern =>          q{Successfully (.+)},
   action =>           q{
    
              my $description = 'Node Manager successfully started';
                                                  
              setProperty("summary", $description . "\n");
    
   },
  },
  
  {
   id =>        "startSuccess2",
   pattern =>          q{started on port (.+)},
   action =>           q{
    
              my $description = 'Node Manager successfully started';
                                                  
              setProperty("summary", $description . "\n");
    
   },
  },
  
  {
   id =>        "startWarning",
   pattern =>          q{Fatal error (.+)},
   action =>           q{
    
              my $description = 'Address to start node manager already in use';
                                                  
              setProperty("summary", $description . "\n");
    
   },
  },  
);

