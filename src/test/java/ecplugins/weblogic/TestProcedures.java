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


import static org.hamcrest.Matchers.equalTo;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

public class TestProcedures {

	private static String resourceName = StringConstants.WEBLOGIC_AGENT_RESOURCE_NAME;
	private static Boolean credentialRequired = false;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// Actions is a HashMap having primary key as procedure to run and
		// secondary key as property name
		Properties props = TestUtils.getProperties();
		ConfigurationsParser.configurationParser();
		TestUtils.createCommanderWorkspace(StringConstants.WORKSPACE_NAME);
		TestUtils.createCommanderResource(StringConstants.WEBLOGIC_AGENT_RESOURCE_NAME, StringConstants.WORKSPACE_NAME,
				props.getProperty(StringConstants.WEBLOGIC_AGENT_IP));
		System.out.println("Starting unit tests");
	}

	@ClassRule
	public static ErrorCollector errorCollector = new ErrorCollector();

	@Test
	public void test() throws Exception {

		for (ProcedureNames procedures : ProcedureNames.values()) {
			JSONObject jsonObject = createJsonObject(procedures.getProcedures());
			if (jsonObject == null)
				continue;

			addActualParameters(procedures.getProcedures(), jsonObject);

			TestUtils.setResourceAndWorkspace(resourceName, StringConstants.WORKSPACE_NAME,
					"EC-Weblogic-" + StringConstants.PLUGIN_VERSION);

			String jobId = TestUtils.callRunProcedure(jsonObject);
			String response = TestUtils.waitForJob(jobId, StringConstants.jobTimeoutMillis);
			// Add the job status to the collector.
			// All the failures will be printed displayed in the end.
			errorCollector.checkThat("Job completed with errors for " + procedures.getProcedures(), response,
					equalTo("success"));

			if (response.equals("success"))
				System.out.println(
						"JobId:" + jobId + ", Completed Unit Test Successfully for " + procedures.getProcedures());
			else if (jobId != null && !jobId.isEmpty())
				System.out.println("JobId:" + jobId + ", Unit test failed for  " + procedures.getProcedures());
		}

	}

	private JSONObject createJsonObject(String procedureName) throws JSONException {
		JSONObject jsonObject = new JSONObject();

		jsonObject.put("projectName", "EC-Weblogic-" + StringConstants.PLUGIN_VERSION);

		if (!ConfigurationsParser.actions.containsKey(procedureName)) {
			System.out.println("Configurations not present for the test" + procedureName);
			return null;
		}
		jsonObject.put("procedureName", procedureName);
		return jsonObject;
	}

	private void addActualParameters(String procedureName, JSONObject jsonObject) throws Exception {
		// This HashMap is populated by reading configurations.json file

		for (Map.Entry<String, HashMap<String, String>> objectCursor : ConfigurationsParser.actions.get(procedureName)
				.entrySet()) {

			// Every run will be new job
			JSONArray actualParameterArray = new JSONArray();

			for (Map.Entry<String, String> propertyCursor : objectCursor.getValue().entrySet()) {

				// Get each Run's data and iterate over it to populate
				// parameter array
				if (propertyCursor != null && !propertyCursor.getValue().isEmpty()) {
					
					if(propertyCursor.getKey().contains("credentials"))
					{
						credentialRequired = true;

			             actualParameterArray.put(new JSONObject().put("actualParameterName", propertyCursor.getKey())
			                     .put("value", "web_credentials"));
					}
					else
					{	
						actualParameterArray.put(new JSONObject().put("value", propertyCursor.getValue())
								.put("actualParameterName", propertyCursor.getKey()));
					}
				}
			}
			jsonObject.put("actualParameter", actualParameterArray);
			if(credentialRequired)
			{

	             JSONArray credentialArray = new JSONArray();

	             credentialArray.put(new JSONObject()
	                     .put("credentialName", "web_credentials")
	                     .put("userName", TestUtils.props.getProperty(StringConstants.WEBLOGIC_USERNAME))
	                     .put("password", TestUtils.props.getProperty(StringConstants.WEBLOGIC_PASSWORD)));

	             jsonObject.put("credential", credentialArray);
			}
		}
	}
}
