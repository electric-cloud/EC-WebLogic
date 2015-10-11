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
   id =>        "serverState",
   pattern =>          q{Server State: (.+)},
   action =>           q{
    
              if($1 =~ m/(RUNNING|running)/{
              
                   my $description = "Server is started";
              
              }else{
                   my $description = "Server is not started, actual status is $1\n";
              }
              
              
                              
              setProperty("summary", $description);
    
   },
  },
  
  {
   id =>        "adminServerStarted",
   pattern =>          q{Successfully connected to Admin Server '(.+)'},
   action =>           q{
    
             my $description = ((defined $::gProperties{"summary"}) ? 
                    $::gProperties{"summary"} : '');
                    
              $description .= "Admin Server $1 successfully started";
                              
              setProperty("summary", $description . "\n");
    
   },
  },
  
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

