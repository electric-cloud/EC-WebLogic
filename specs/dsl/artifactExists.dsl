def projName = args.projectName
def resource = args.resourceName ?: 'local'
def artifactName = args.artifactName
def procedureName = 'ArtifactExists'

project projName, {
    procedure procedureName, {
        resourceName = resource

        step 'RunProcedure', {
            shell = 'ec-perl'
            command = '''
use strict;
use warnings;

use ElectricCommander;
my $ec = ElectricCommander->new();
use Data::Dumper;

my $name = '$[artifactName]';

$ec->abortOnError(0);
my $path = $ec->getArtifact($name);

# Message will appear only on error
if (my $message = $path->findvalue('//error/message')){
  print "ERROR: $message\n";
}
else {
  print "Artifact '$name' exists in repository\n";
}


exit 0;

            '''
        }

        formalParameter 'artifactName', defaultValue: artifactName, {
            type = "textarea"
        }

        // Custom properties
        code = ''
        text = ''
    }


}



