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

public enum ProcedureNames {
	
	CREATE_USER("CreateUser"),
	CREATE_GROUP("CreateGroup"),
	ADD_USER_TO_GROUP("AddUserToGroup"),
	REMOVE_USER_FROM_GROUP("RemoveUserFromGroup"),
	CONFIGURE_USER_LOCKOUT_MANAGER("ConfigureUserLockoutManager"),
	UNLOCK_USER_ACCOUNT("UnlockUserAccount"),
	CHANGE_USER_PASSWORD("ChangeUserPassword"),
	DELETE_GROUP("DeleteGroup"),
	DELETE_USER("DeleteUser"),
	CREATE_CLUSTER("CreateCluster"),
	CREATE_MANAGED_SERVER("CreateManagedServer"),
	ADD_SERVER_TO_CLUSTER("AddServerToCluster"),
	DELETE_CLUSTER("DeleteCluster"),
	DELETE_MANAGED_SERVER("DeleteManagedServer");
	
	
	
	private String procedures;
	
	private ProcedureNames(String procedures)
	{
	   this.procedures = procedures;	
	}
	
    public String getProcedures()
    {
    	return this.procedures;
    }
}
