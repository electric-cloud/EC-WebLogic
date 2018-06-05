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

$ec->abortOnError(0);

eval {
  $ec->getArtifact(\'$[artifactName]\');
  print "Artifact exists";
} or do {
  print "Not exists";
};

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



