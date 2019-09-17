use strict;
use warnings;
use ElectricCommander;
use ElectricCommander::PropDB;
use ElectricCommander::PropMod;
use Carp qw( carp croak );
use Data::Dumper;

#*****************************************************************************
use constant {
    SUCCESS => 0,
    ERROR   => 1,
};

use constant {
    LEVEL_ERROR => -1,
    LEVEL_INFO => 0,
    LEVEL_DEBUG => 1,
    LEVEL_TRACE => 2,
};

#*****************************************************************************
my $ec = ElectricCommander->new();
$ec->abortOnError(0);

my $projName   = '$[/myProject/projectName]';
my $pluginName = '@PLUGIN_NAME@';
my $pluginKey  = '@PLUGIN_KEY@';

my $configName = '$[/myJob/config]';

my $weblogic_url          = '$[weblogic_url]';
my $wlst_path             = '$[wlst_path]';
my $java_home             = '$[java_home]';
my $java_vendor           = '$[java_vendor]';
my $mw_home               = '$[mw_home]';
my $credential            = '$[credential]';
my $enable_named_sessions = '$[enable_named_sessions]';
my $debug_level           = '$[debug_level]';

ElectricCommander::PropMod::loadPerlCodeFromProperty($ec, '/myProject/EC::Plugin::Core');
ElectricCommander::PropMod::loadPerlCodeFromProperty($ec, '/myProject/EC::WebLogic');

my $wl = EC::WebLogic->new(
    project_name => $projName,
    plugin_name  => $pluginName,
    plugin_key   => $pluginKey
);

my $cred_xpath = $ec->getFullCredential($credential);
my $username   = $cred_xpath->findvalue("//userName");
my $password   = $cred_xpath->findvalue("//password");

$wl->logger->level($debug_level);
$wl->debug_level($debug_level+1);

# $wl->logger->debug(Dumper(['#001', ''.$username, ''.$password]));
# $wl->logger->debug(Dumper(['#002', $projName, $pluginName, $pluginKey, $configName]));
# $wl->logger->debug(Dumper(['#003', $weblogic_url, $wlst_path, $java_home, $java_vendor, $mw_home, $credential, $enable_named_sessions]));

if ($java_home) {
    $ENV{JAVA_HOME} = $java_home;
    $wl->out(LEVEL_INFO, "JAVA_HOME was set to '$java_home'");
}

if ($java_vendor) {
    $ENV{JAVA_VENDOR} = $java_vendor;
    $wl->out(LEVEL_INFO, "JAVA_VENDOR was set to '$java_vendor'");
}

if ($mw_home) {
    $ENV{MW_HOME} = $mw_home;
    $wl->out(LEVEL_INFO, "MW_HOME was set to $mw_home");
}

my $script = $ENV{COMMANDER_WORKSPACE} . '/do_ls';

open FH, '>', $script;
print FH "connect('$username','$password','$weblogic_url'); ls(); disconnect()\n";
close FH;

my $cmd = qq{$wlst_path $script};

my $result = $wl->run_command($cmd);

my ($code, $stdout, $stderr) = @{$result}{qw(code stdout stderr)};

$wl->out(LEVEL_INFO, 'EXIT_CODE: ', $code);
$wl->out(LEVEL_INFO, 'STDOUT: ', $stdout) if ($stdout ne '');
$wl->out(LEVEL_INFO, 'STDERR: ', $stderr) if ($stderr ne '');

if ($code) {
    $ec->setProperty("/myJob/configError", "Connection to the Weblogic instance failed: $code");
    exit ERROR;
}

$wl->logger->info("Successfully connected to the Weblogic instance.");

exit SUCCESS;
#*****************************************************************************
