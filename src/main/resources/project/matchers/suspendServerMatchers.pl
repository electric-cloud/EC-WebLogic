
push (@::gMatchers,
  
  {
   id =>        "suspendSucces",
   pattern =>          q{Server (.+) suspended successfully},
   action =>           q{&addSimpleError("Server suspended successfully", "success");},
  },

  {
   id =>        "error1",
   pattern =>          q{in suspend},
   action =>           q{&addSimpleError("Error: server to suspend not founded", "error");},
  },
  
  {
   id =>        "error2",
   pattern =>          q{trying to set illegal state, present state ADMIN},
   action =>           q{&addSimpleError("Error: server is already suspended", "error");},
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