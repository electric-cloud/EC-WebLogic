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
   id =>        "resumeSucces1",
   pattern =>          q{is assigned to variable},
   action =>           q{&addSimpleError("Server resumed successfully", "success");},
  },

  {
   id =>        "resumeSucces2",
   pattern =>          q{resumed successfully},
   action =>           q{&addSimpleError("Server resumed successfully", "success");},
  },

  {
   id =>        "error1",
   pattern =>          q{in resume},
   action =>           q{&addSimpleError("Error: server to resume not founded", "error");},
  },
  
  {
   id =>        "error2",
   pattern =>          q{trying to set illegal state, present state},
   action =>           q{&addSimpleError("Error: server is not in suspended state", "error");},
  },
  
  {
   id =>        "error3",
   pattern =>          q{No such file or directory},
   action =>           q{&addSimpleError("Error: can not find WLST", "error");},
  },
  
  {
   id =>        "error4",
   pattern =>          q{failed to be authenticated.},
   action =>           q{&addSimpleError("Error: failed to be authenticated", "error");},
  },  
  {
   id =>        "error5",
   pattern =>          q{Destination unreachable},
   action =>           q{&addSimpleError("Error: Destination unreachable. Check the provided URL", "error");},
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