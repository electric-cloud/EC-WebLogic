
push (@::gMatchers,
  {
   id =>        "appInstalled",
   pattern =>          q{^ADMA5013I(.+)},
   action =>           q{
    
              my $description = ((defined $::gProperties{"summary"}) ? 
                    $::gProperties{"summary"} : '');
                    
              $description .= "Application installed successfully.";
                              
              setProperty("summary", $description . "\n");
    
   },
  },
  
  {
   id =>        "appNameExists",
   pattern =>          q{(.*)WASX7279E(.+)},
   action =>           q{
    
              my $description = ((defined $::gProperties{"summary"}) ? 
                    $::gProperties{"summary"} : '');
                    
              $description .= "Install failure, application name already exists.";
                              
              setProperty("summary", $description . "\n");
    
   },
  },
  
  {
   id =>        "scriptError",
   pattern =>          q{^Problem invoking WLST(.+)},
   action =>           q{
    
              my $description = ((defined $::gProperties{"summary"}) ? 
                    $::gProperties{"summary"} : '');
                    
              $description .= "Error detected while executing WLST.";
                              
              setProperty("summary", $description . "\n");
    
   },
  },
  
  {
   id =>        "appUninstalled",
   pattern =>          q{^ADMA5106I(.+)},
   action =>           q{
    
              my $description = ((defined $::gProperties{"summary"}) ? 
                    $::gProperties{"summary"} : '');
                    
              $description .= "Application uninstalled successfully.";
                              
              setProperty("summary", $description . "\n");
    
   },  
  },
  
  {
   id =>        "unvalidCredentials",
   pattern =>          q{^ADMN0022E(.+)},
   action =>           q{
    
              my $description = ((defined $::gProperties{"summary"}) ? 
                    $::gProperties{"summary"} : '');
                    
              $description .= "Authentification error, unvalid or empty credentials.";
                              
              setProperty("summary", $description . "\n");
    
   },  
  },

);

