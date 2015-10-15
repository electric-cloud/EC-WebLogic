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

	####################################################################################
	# Read Logs
	#
	#
	####################################################################################

	# -------------------------------------------------------------------------
	# Includes
	# -------------------------------------------------------------------------

	use ElectricCommander;
	use warnings;
	use strict;
	use Cwd;
	use File::Spec;
	use ElectricCommander::PropDB;
	$|=1;

    my $ec = new ElectricCommander;
	
	$::gJobID = ($ec->getProperty("jobID"))->findvalue("//value"); 
	$::gLogType = ($ec->getProperty("logType"))->findvalue("//value"); 
	$::gLogExt = ($ec->getProperty("logExtention"))->findvalue("//value"); 
	$::gMaxElapsedTime = ($ec->getProperty("maxelapsedtime"))->findvalue("//value");

	sub main() {
		$ec->abortOnError(0);
		
		my $LOGNAMEBASE = $::gLogType;
		
		if ($::gMaxElapsedTime ne '') {		
			sleep $::gMaxElapsedTime;
		}
		
		my $logfile = $LOGNAMEBASE . "\." . $::gJobID . $::gLogExt;
				
		open(FILE, $logfile) || die("Could not open file!");
		my(@fcont) = <FILE>;
		close (FILE);
		
		print @fcont;
		
		my $content = join('', @fcont); 
		
		if($content =~ m/Exception (.+)/) {       
			$ec->setProperty("/myJobStep/outcome", 'error');         
		} elsif ($content =~ m/Successfully (.+)/) {       
			$ec->setProperty("/myJobStep/outcome", 'success');         
		} elsif ($content =~ m/started on port (.+)/) {       
			$ec->setProperty("/myJobStep/outcome", 'success');         
		} elsif ($content =~ m/Fatal error (.+)/) {       
			$ec->setProperty("/myJobStep/outcome", 'error');         
		} else {
			$ec->setProperty("/myJobStep/outcome", 'warning');         
		}
	}
	main();

	1;

