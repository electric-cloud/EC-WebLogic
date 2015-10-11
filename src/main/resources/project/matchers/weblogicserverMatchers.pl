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
   id =>        "serverRunningState",
   pattern =>          q{Server State: (.+)},
   action =>           q{
    
              my $description = '';
              
              if($1 =~ m/(RUNNING|running)/){
              
                   $description = "Server is started";
              
              }else{
                   $description = "Server is not started, actual status is $1\n";
              }
              
              setProperty("summary", $description);
    
   },
  },

  {
   id =>        "adminServerStopped",
   pattern =>          q{(.*)Disconnected from weblogic server: (.+)},
   action =>           q{
    
              my $description = "Admin Server $2 was stopped successfully\n";
              
              
              setProperty("summary", $description);
    
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
  
  {
   id =>        "successConnectingManagedServer",
   pattern =>          q{Successfully connected to managed Server (.+) that belongs to domain (.+)},
   action =>           q{
    
              my $description = "Successfully connected to managed Server $1";
              
              
              setProperty("summary", $description);
    
   },
  },
  
  {
   id =>        "adminServerStarted",
   pattern =>          q{Successfully connected to Admin Server '(.+)'.*},
   action =>           q{
    
             my $description= "Admin Server $1 successfully started";
                              
              setProperty("summary", $description . "\n");
    
   },
  },
  
);

