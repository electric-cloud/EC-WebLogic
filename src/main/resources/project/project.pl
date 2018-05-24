use strict;
use warnings;
use XML::XPath;
use XML::XPath::XMLParser;
use Data::Dumper;
use YAML qw(Dump Load);
use File::Spec;
use File::Basename qw(dirname);
use XML::Tidy;

# my $mode = 'dump';
my $mode = 'restore';

my $project_xml_path = $ARGV[0] || File::Spec->catfile(dirname($0), 'project.xml');
my $manifest_path = $ARGV[1] || File::Spec->catfile(dirname($0), 'manifest.xml');
my $procedure_name = 'DeleteConnectionFactory';

my $proj = {procedures => []};
my $project = XML::XPath->new(filename => $project_xml_path);
my $manifest = XML::XPath->new(filename => $manifest_path);

if  ($mode eq 'dump') {
    my $count = 5;
    my $index = 0;
    for my $node ($manifest->findnodes('//file')) {
        # next if $index > $count;
        my $path = $node->findvalue('path')->string_value;
        my $xpath = $node->findvalue('xpath')->string_value;
        print "$xpath: $path\n";
        $project->setNodeText($xpath, "Path: $path");
        $index ++;
    }

    print "Processed manifest\n";

    for my $proc ($project->findnodes('//project/procedure')) {
        my $procedure = read_procedure($proc);
        print Dumper $procedure;
        my $props = load_properties($project, '//project/');
        push @{$proj->{procedures}}, $procedure;
        $proj->{properties} = $props;
    }

    my $yaml = Dump($proj);
    open my $fh, ">".File::Spec->catfile(dirname($0), 'project.yaml') or die $!;
    print $fh $yaml;
    close $fh;
    print "Saved project\n";
}
else {
    $project->setNodeText('//project/description', 'test');

    open my $fh, File::Spec->catfile(dirname($0), 'project.yaml') or die $!;
    my $content = join('', <$fh>);
    close $fh;
    my $yaml = Load($content);

    my $proj = XML::XPath::Node::Element->new('project');
    my $props = make_property_sheet_node($yaml->{properties});
    $proj->appendChild($props);

    my $maps = {};
    my $manifest = {};

    for my $procedure (@{$yaml->{procedures}}) {
        # my $prefix = '//procedure/[procedureName="' + $procedure->{name} . '"]';
        # next if $procedure->{procedureName} =~ /Configuration/;
        my $xml_form = $procedure->{properties}->{ec_parameterForm}->{value};
        my $props = $procedure->{properties};
        $props->{ec_parameterForm} = {value => '', expandable => '1'};
        $xml_form =~ s/Path:\s*//;
        $manifest->{$procedure->{procedureName}}->{form} = $xml_form;

        my $procedure_node = make_node($procedure, 'procedure');
        if ($xml_form) {
            my $xpath = XML::XPath->new(filename =>  File::Spec->catfile(dirname($0), $xml_form));
            for my $e ($xpath->findnodes('//formElement')) {
                my $name = $e->findvalue('property');
                $props->{ec_customEditorData}->{parameters}->{$name}->{formType} = {value => 'standard', expandable => 1};
            }
        }
        $procedure_node->appendChild(make_property_sheet_node($props));

        if ($xml_form) {
            my @formal_params = make_formal_parameters($xml_form);
            $procedure_node->appendChild($_) for @formal_params;
        }

        for my $step (@{$procedure->{steps}}) {
            my $path = $step->{command};
            $step->{command} = '' if $step->{command} =~ /Path/;
            $path =~ /Path:\s*/;
            $manifest->{$procedure->{procedureName}}->{step} = $path;
            my $node = make_node($step, 'step');
            $procedure_node->appendChild($node);
        }

        # my @result = ();

        # my $tidy = XML::Tidy->new(xml => $procedure_node->toString);
        # $tidy->tidy;
        # die $tidy->toString;
        my $name = $procedure->{procedureName};
        my $old_node = $project->find('//project/procedure[procedureName="' . $name . '"]');
        # $old_node = $procedure_node;
        # $project->removeChild($old_node);
        # $project->appendChild($procedure_node);
        $proj->appendChild($procedure_node);
    }
    if (0) {
        my $paths = get_paths('//project/', $proj);
        print Dumper $paths;
        for my $xpath (keys %$paths) {
            my $value = $paths->{$xpath};
            my $old = $project->findvalue($xpath)->string_value;
            $old ||= '';
            $value ||= '';
            print "Old: $old, new: $value\n";
            if ($old ne $value) {
                $project->setNodeText($xpath, $value);
            }
        }
        print "Updated\n";

        # $project->setNodeText('/project/test/node', 'test');
        for my $n ($project->findnodes('//project/procedure')) {
            die $n;
        }
    }

    my $tidy = XML::Tidy->new(xml => $proj->toString);
    $tidy->tidy;


    open my $fh, '>' . File::Spec->catfile(dirname($0), 'project1.xml') or die $!;
    print $fh $tidy->toString;
    close $fh;

    for my $node ($proj->findnodes('//procedure[procedureName="' . $procedure_name . '"]')) {
        my $procedure_xml = tidy($node->toString);
        print $procedure_xml;

        my $proj_path = File::Spec->catfile(dirname($0), 'project.xml');
        my $manifest_path = File::Spec->catfile(dirname($0), 'manifest.xml');

        open my $fh, $proj_path or die $!;
        my $content = join('', <$fh>);
        close $fh;
        print "\n\n\n\n";

        $procedure_xml =~ s/\Q<?xml version="1.0" encoding="utf-8"?>//xms;
        my $parts = $manifest->{$procedure_name};
        my $code = $parts->{step};
        my $form = $parts->{form};


        open my $fh, $manifest_path or die $!;
        my $manifest_content = join('', <$fh>);
        close $fh;

        if ($manifest !~ /\Q$form/xms) {
            my $manifest_form = qq{
    <file>
        <path>$form</path>
        <xpath>//procedure[procedureName="$procedure_name"]/propertySheet/property[propertyName="ec_parameterForm"]/value</xpath>
    </file>};
            $manifest =~ s/<\/fileset>/$manifest_form\n<\/fileset>/;
        }



        my $replaced = 0;
        while($content =~ /(<procedure>.*?<\/procedure>)/gmxsc) {
            my $proc = $1;
            if ($proc =~ /<procedureName>$procedure_name/xms) {
                $content =~ s/\Q$proc\E/$procedure_xml/xms;
                print "Found procedure\n";
                open my $fh, ">$proj_path" or die $!;
                print $fh $content;
                close $fh;
                print "Replaced procedure $procedure_name\n";
                $replaced = 1;
                last;
            }
        }

        if (!$replaced) {
            $content =~ s/<\/project>/$procedure_xml\n<\/project>/xms;
            open my $fh, ">$proj_path" or die $!;
            print $fh $content;
            close $fh;
            print "Added procedure $procedure_name\n";
        }
    }
}

sub tidy {
    my ($string) = @_;
    my $tidy = XML::Tidy->new(xml => $string);
    $tidy->tidy;
    return $tidy->toString;
}

sub get_paths {
    my ($prefix, $source) = @_;

    my $paths = {};
    print "$prefix\n";
    for my $node ($source->getChildNodes()) {
        if ($node->isa('XML::XPath::Node::Element')) {
            my $prefix_updated = $prefix;
            if ($node->getName() eq 'procedure') {
                $prefix_updated .= 'procedure[procedureName="' . $node->findvalue('procedureName')->string_value . '"]/';
            }
            elsif ($node->getName() eq 'step' ) {
                $prefix_updated .= qq{step[stepName="@{[$node->findvalue('stepName')->string_value]}"]/};
            }
            elsif ($node->getName() eq 'formalParameter') {
                $prefix_updated .= qq{formalParameter[formalParameterName="@{[$node->findvalue('formalParameterName')->string_value]}"]/};
            }
            else {
                $prefix_updated .= $node->getName();
            }
            my $p = get_paths($prefix_updated, $node);
            $paths = {%$p, %$paths};
        }
        elsif ($node->isa('XML::XPath::Node::Text')) {
            my $value = $node->string_value;
            $paths->{$prefix} = $value;
        }
        else {
            die $node;
        }
    }
    return $paths;
}

sub make_editor_form_data {
    my ($xml_form) = @_;

    my @result = ();
    my $props = {};
    my $xpath = XML::XPath->new(filename =>  File::Spec->catfile(dirname($0), $xml_form));
    for my $e ($xpath->findnodes('//formElement')) {
        my $name = $e->findvalue('property');
        $props->{ec_customEditorData}->{parameters}->{$name}->{formType} = {value => 'standard', expandable => 1};

        # my $a = make_node({propertyName => 'formType', expandable => 1, value => 'standard'}, 'property');
        # my $sheet = XML::XPath::Node::Element->new('propertySheet');
        # $sheet->appendChild($a);
        # my $parent_prop = XML::XPath::Node::Element->new('property');
        # my $name = XML::XPath::Node::Element->new('propertyName');
        # my $text = XML::XPath::Node::Text->new($name);
        # $name->appendChild($text);
        # $parent_prop->appendChild($name);
        # $parent_prop->appendChild($sheet);
    }

    print Dumper $props;
    my $node = make_property_sheet_node($props);

    die Dumper $node->toString;
}

sub make_property_sheet_node {
    my ($properties) = @_;

    my $sheet = XML::XPath::Node::Element->new('propertySheet');
    for my $key (keys %$properties) {
        my $value = $properties->{$key};
        my $property = XML::XPath::Node::Element->new('property');
        my $prop_name = XML::XPath::Node::Element->new('propertyName');
        my $text = XML::XPath::Node::Text->new($key);
        $prop_name->appendChild($text);
        $property->appendChild($prop_name);

        if (ref $value && ! defined $value->{value}) {
            $property->appendChild(make_property_sheet_node($value));
        }
        elsif (ref $value && defined $value->{value}) {
            my $val = XML::XPath::Node::Element->new('value');
            my $v = $value->{value};
            $val->appendChild(XML::XPath::Node::Text->new($v));
            my $exp = XML::XPath::Node::Element->new('expandable');
            $exp->appendChild(XML::XPath::Node::Text->new($value->{expandable}));
            $property->appendChild($val);
            $property->appendChild($exp);
        }
        $sheet->appendChild($property);
    }
    return $sheet;
}

sub make_formal_parameters {
    my ($xml_form) = @_;

    my @result = ();
    my $xpath = XML::XPath->new(filename =>  File::Spec->catfile(dirname($0), $xml_form));
    for my $element ($xpath->findnodes('//formElement')) {
        my $name = $element->findvalue('property')->string_value;
        my $type = $element->findvalue('type')->string_value;
        my $required = $element->findvalue('required');
        my $doc = $element->findvalue('documentation')->string_value;
        $type = 'entry' if $type =~ /select/;
        my $default = $element->findvalue('value')->string_value;

        my $param = make_node({
            type => $type,
            formalParameterName => $name,
            required => ($required ? 1 : 0),
            defaultValue => ($default ? $default : undef),
            description => $doc
        }, 'formalParameter');

        push @result, $param;
    }
    return @result;
}

sub make_node {
    my ($struct, $name) = @_;

    my $root = XML::XPath::Node::Element->new($name);
    for (sort keys %$struct) {
        my $node = XML::XPath::Node::Element->new($_);
        if (!ref $struct->{$_}) {
            my $t = XML::XPath::Node::Text->new($struct->{$_});
            $node->appendChild($t);
            $root->appendChild($node);
        }
    }
    return $root;
}


sub read_procedure {
    my ($xp) = @_;

    my $proc= {};
    for my $field_name (qw/projectName description procedureName jobNameTemplate/) {
        $proc->{$field_name} = $xp->findvalue($field_name)->string_value;
    }
    $proc->{steps} = [];
    for my $s ($xp->findnodes('step')) {
        my $step = {};
        for (qw/parallel releaseExclusive retries timeLimit timeLimitUnits workingDirectory procedureName projectName workspaceName resourceName stepName shell alwaysRun broadcast command condition description errorHandling exclusive logFileName/) {
            $step->{$_} = $s->findvalue($_)->string_value;
        }
        push @{$proc->{steps}}, $step;
    }
    $proc->{formalParameters} = [];
    # for my $fp ($xp->findnodes('formalParameter')) {
    #     push @{$proc->{formalParameters}}, $fp->findvalue('formalParameterName')->string_value;
    # }
    my $properties = load_properties($xp);
    delete $properties->{ec_customEditorData};
    $proc->{properties} = $properties;
    return $proc;
}


sub load_properties {
    my ($xpath, $prefix) = @_;

    $prefix ||= '';
    my $props = {};
    for my $p ($xpath->findnodes("${prefix}propertySheet/property")) {
        my $name = $p->findvalue('propertyName')->string_value;
        my $value = $p->findvalue('value')->string_value;
        my $expandable = $p->findvalue('expandable')->string_value;

        my $children = load_properties($p);
        if ($children && keys %$children) {
            $props->{$name} = $children;
        }
        else {
            $props->{$name} = {value => $value, expandable => $expandable};
        }
    }
    return $props;
}
# my $xp = XML::XPath->new();

# my $xml_pi = XML::XPath::Node::PI->new('xml', 'version="1.0"');

# my $root = XML::XPath::Node::Element->new('html');

# my $body = XML::XPath::Node::Element->new('body');

# $root->appendChild($body);

# my %camelid_links = (test => {url => "url", description => 'descr'});



# foreach my $item ( keys (%camelid_links) ) {

#     my $link = XML::XPath::Node::Element->new('a');

#     my $href = XML::XPath::Node::Attribute->new('href',

#          $camelid_links{$item}->{url});

#     $link->appendAttribute($href);

#     my $text = XML::XPath::Node::Text->new(

#          $camelid_links{$item}->{description});

#     $link->appendChild($text);

#     $body->appendChild($link);

# }



# print $xml_pi->toString;

# print $root->toString
