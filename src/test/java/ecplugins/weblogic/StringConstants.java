package test.java.ecplugins.weblogic;
/*
   Copyright 2015 Electric Cloud, Inc.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */


public class StringConstants {

	final static String COMMANDER_SERVER = "COMMANDER_SERVER";
	//final static String PLUGIN_VERSION = "3.2.0.0";
	final static String PLUGIN_VERSION = System.getProperty("PLUGIN_VERSION");
	final static String PYTHON_VERSION = "2.0.6.96169";
	final static String COMMANDER_USER = "COMMANDER_USER";
	final static String COMMANDER_PASSWORD = "COMMANDER_PASSWORD";
	final static String WEBLOGIC_AGENT_IP = "WEBLOGIC_AGENT_IP";
	final static String EC_AGENT_PORT = "7800";
	final static String WORKSPACE_NAME = "UTWorkspace";
	final static String WEBLOGIC_AGENT_RESOURCE_NAME = "weblogicAgent";
	final static String RAKE_FILE_PATH = "rakefile_path";
	final static String FILE_PATH = "file_path";
	final static String WEBLOGIC_USERNAME = "WEBLOGIC_USERNAME";
	final static String WEBLOGIC_PASSWORD = "WEBLOGIC_PASSWORD";
	final static String WEBLOGIC_URL = "WEBLOGIC_URL";
	final static long jobTimeoutMillis = 5 * 60 * 1000;
}
