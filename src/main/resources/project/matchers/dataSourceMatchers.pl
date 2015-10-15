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
   id =>        "createSucces",
   pattern =>          q{Datasource created successfully},
   action =>           q{&addSimpleError("Datasource created successfully", "success");},
  },

  {
   id =>        "deleteSucces",
   pattern =>          q{Datasource deleted successfully},
   action =>           q{&addSimpleError("Datasource deleted successfully", "success");},
  },

  {
   id =>        "error1",
   pattern =>          q{(Cannot load driver class)},
   action =>           q{&addSimpleError("Error: cannot load driver class", "error");},
  },
  
  {
   id =>        "error2",
   pattern =>          q{Bean already exists},
   action =>           q{&addSimpleError("Error: Datasource already exist", "error");},
  },
  
  {
   id =>        "error3",
   pattern =>          q{Communications link failure due to underlying exception},
   action =>           q{&addSimpleError("Error: can not connect. Please check the URL and the database name", "error");},
  },
  
  {
   id =>        "error4",
   pattern =>          q{Could not find the Datasource},
   action =>           q{&addSimpleError("Error: could not find the Datasource", "error");},
  },  
  
  {
   id =>        "error5",
   pattern =>          q{failed to be authenticated.},
   action =>           q{&addSimpleError("Error: failed to be authenticated", "error");},
  },  
  
  {
   id =>        "error6",
   pattern =>          q{Destination unreachable},
   action =>           q{&addSimpleError("Error: Destination unreachable. Check the provided URL", "error");},
  },

  {
   id =>        "error7",
   pattern =>          q{This Exception occurred},
   action =>           q{&addSimpleError("Error: Could not create and/or configure the datasource properly. Please check logs for details", "error");},
  },

);


sub addSimpleError {
    my ($customError, $type) = @_;	
    my $ec = new ElectricCommander();
    $ec->abortOnError(0);
	
	setProperty("summary", $customError);
	if ($type eq "success") {
		$ec->setProperty("/myJobStep/outcome", 'success');
	} elsif ($type eq "error") {
		$ec->setProperty("/myJobStep/outcome", 'error');
	} else {
		$ec->setProperty("/myJobStep/outcome", 'warning');
	}
}