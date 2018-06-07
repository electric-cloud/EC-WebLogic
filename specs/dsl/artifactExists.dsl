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

$ec->abortOnError(0);
my $path = $ec->getArtifact(\'$[artifactName]\');

print $path->findvalue('//message');

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



