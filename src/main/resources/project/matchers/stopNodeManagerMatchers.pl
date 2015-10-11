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

