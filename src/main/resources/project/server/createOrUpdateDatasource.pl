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

# preamble.pl
$[/myProject/preamble]

my $PROJECT_NAME = '$[/myProject/projectName]';
my $PLUGIN_NAME = '@PLUGIN_NAME@';
my $PLUGIN_KEY  = '@PLUGIN_KEY@';
use Data::Dumper;

$| = 1;

main();

sub main {
      my $wl = EC::WebLogic->new(
          project_name => $PROJECT_NAME,
          plugin_name  => $PLUGIN_NAME,
          plugin_key   => $PLUGIN_KEY
      );
      my $params = $wl->get_params_as_hashref(
          'configname',
          'dsname',
          'target',
          'driverurl',
          'jndiname',
          'driverclass',
          'wlstabspath',
          'ds_credential',
          'ds_initial_poolsize',
          'ds_min_poolsize',
          'ds_max_poolsize',
          'ds_test_table'
      );
      my $cred = $wl->get_credentials( $params->{configname} );
      if ( $cred->{java_home} ) {
          $wl->out( 1, "JAVA_HOME was provided" );
      }

      #TO BE CHANGED TO THE NAME WITH _ _
      $params->{wlstabspath} = $cred->{wlst_path} unless ($params->{wlstabspath});


      if ($params->{ds_credential}) {
          my $ds_cred = $wl->get_step_credential($params->{ds_credential});
          $params->{username} = $ds_cred->{userName};
          $params->{password} = $ds_cred->{password};
      }
      my $render_params = {
          username     => $cred->{user},
          password     => $cred->{password},
          weblogic_url => $cred->{weblogic_url},

          ds_name          => $params->{dsname},
          server_name      => $params->{target},
          ds_jndi_name     => $params->{jndiname},
          ds_driver_class  => $params->{driverclass},
          ds_driver_url    => $params->{driverurl},
          ds_user_name     => $params->{username},
          ds_password      => $params->{password},
          ds_initial_poolsize => $params->{ds_initial_poolsize},
          ds_min_poolsize => $params->{ds_min_poolsize},
          ds_max_poolsize => $params->{ds_max_poolsize},
          ds_test_table => $params->{ds_test_table}
      };
      print Dumper $params;
      my $template_path = '/myProject/jython/create_or_update_datasource.jython';
      my $template =
        $wl->render_template_from_property( $template_path, $render_params );

      $wl->out( 10, "Generated script:\n", $template );
      my $res = $wl->execute_jython_script(
          shell          => $params->{wlstabspath},
          script_path    => $ENV{COMMANDER_WORKSPACE} . '/exec.jython',
          script_content => $template,
      );

      $wl->process_response($res);
      return;
}
