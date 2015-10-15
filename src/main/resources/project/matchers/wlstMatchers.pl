#
#  Copyright 2015 Electric Cloud, Inc.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#


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

