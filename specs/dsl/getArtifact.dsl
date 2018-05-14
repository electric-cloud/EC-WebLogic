def url = 'https://tomcat.apache.org/tomcat-6.0-doc/appdev/sample/sample.war'

def artifactDir = '/tmp'
def artifactFile = 'sample.war'
def artifactPath = artifactDir + '/' +artifactFile
def artifactName = 'weblogic:sample'




project projName, {

    procedure 'Download', {
        step 'Download', {
            command = '''
use strict;
use warnings;
use Archive::Zip;
use LWP::UserAgent;
use Data::Dumper;
use Cwd qw(chdir);
use File::Basename;

my $ua = LWP::UserAgent->new;
my $url = \'$[url]\';

my $request = HTTP::Request->new(GET => $url);
print Dumper $request;

my $path = \'$[artifactPath]\';
my $dir = dirname($path);
chdir($dir);

if (-f $path) {
    unlink $path;
}
print "$path\\n";
open my $fh, \">$path\" or die $!;
binmode($fh);
my $size = 0;
my $response = $ua->request($request, sub {
    my ($bytes, $res) = @_;

    $size += length($bytes);

    if ($res->is_success) {
        print $fh $bytes;
    }
    else {
        print $res->code;
        exit -1;
    }
});

close $fh;

            '''
            shell = 'ec-perl'
        }
        formalParameter 'url', defaultValue: '', {
            type = "textarea"
        }

        formalParameter 'artifactPath', defaultValue: '', {
            type = 'entry'
        }
    }

}


pipeline 'Download and Publish Artifact', {
  description = ''
  enabled = '1'
  projectName = projName
  type = null

  formalParameter 'ec_stagesToRun', defaultValue: null, {
    expansionDeferred = '1'
    label = null
    orderIndex = null
    required = '0'
    type = null
  }

  stage 'Stage 1', {
    condition = null
    parallelToPrevious = null
    pipelineName = 'Download and Publish Artifact'
    precondition = null
    projectName = projName

    gate 'PRE', {
      condition = null
      precondition = null
      projectName = projName
    }

    gate 'POST', {
      condition = null
      precondition = null
      projectName = projName
    }

    task 'Download Sample.war', {
      description = ''
      actualParameter = [
        'artifactPath': artifactPath,
        'url': url,
      ]
      advancedMode = '0'
      condition = null
      deployerExpression = null
      deployerRunType = null
      enabled = '1'
      environmentName = null
      environmentProjectName = null
      environmentTemplateName = null
      environmentTemplateProjectName = null
      errorHandling = 'stopOnError'
      gateCondition = null
      gateType = null
      groupName = null
      insertRollingDeployManualStep = '0'
      instruction = null
      notificationTemplate = null
      parallelToPrevious = null
      precondition = null
      projectName = projName
      rollingDeployEnabled = null
      rollingDeployManualStepCondition = null
      skippable = '0'
      snapshotName = null
      startTime = null
      subapplication = null
      subpluginKey = null
      subprocedure = 'Download'
      subprocess = null
      subproject = projName
      subworkflowDefinition = null
      subworkflowStartingState = null
      taskType = 'PROCEDURE'
    }

    task 'Deploy Artifact', {
      description = ''
      actualParameter = [
        'artifactName': artifactName,
        'artifactVersionVersion': '1.0.0-$[/increment /server/ec_counters/artifactCounter]',
        'compress': '0',
        'followSymlinks': '1',
        'fromLocation': artifactDir,
        'includePatterns': artifactFile,
        'repositoryName': 'default',
      ]
      advancedMode = '0'
      condition = null
      deployerExpression = null
      deployerRunType = null
      enabled = '1'
      environmentName = null
      environmentProjectName = null
      environmentTemplateName = null
      environmentTemplateProjectName = null
      errorHandling = 'stopOnError'
      gateCondition = null
      gateType = null
      groupName = null
      insertRollingDeployManualStep = '0'
      instruction = null
      notificationTemplate = null
      parallelToPrevious = null
      precondition = null
      projectName = projName
      rollingDeployEnabled = null
      rollingDeployManualStepCondition = null
      skippable = '0'
      snapshotName = null
      startTime = null
      subapplication = null
      subpluginKey = 'EC-Artifact'
      subprocedure = 'Publish'
      subprocess = null
      subproject = null
      subworkflowDefinition = null
      subworkflowStartingState = null
      taskType = 'PLUGIN'
    }
  }

  // Custom properties

  property 'ec_counters', {

    // Custom properties
    pipelineCounter = '5'
  }
}