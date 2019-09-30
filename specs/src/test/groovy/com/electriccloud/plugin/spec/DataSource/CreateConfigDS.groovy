package com.electriccloud.plugin.spec.DataSource
import com.electriccloud.plugin.spec.WebLogicHelper

class CreateConfigDS {
    //common fields
    static def testRailData = [
            contentPrefix : "Create new Procedure\n" +
                    "Add ${GetChangeTasks.procedureName} Procedure from ${PluginTestHelper.pluginName} Plugin\n" +
                    "With Values\n" +
                    "New Procedure Windows Opened\n" +
                    "Fill Parameters:\n",
            contentSuffix : "",
            expectedPrefix: "",
            expectedSuffix: "",
    ]

    static def resultPropertyBase = '/myJob'

    static def checkBox = [
            unchecked: 0,
            checked  : 1,
    ]

    static def resourceName = [
            originalName: "resourceName",
            local       : "local",
            weblogic    : getResourceName(),
            incorrect   : "incorrectWebLogicResource",
            empty       : "",
    ]

    static def expectedOutcome = [
            success: "success",
            error  : "error",
            warning: "warning",
    ]


    //required fields
    static def configName = [
            originalName: "config",
            correct     : "CreateConfigurationTest",
            incorrect   : "//",
            empty       : "",
    ]

    static def debugLevel = [
            originalName: "debug_level",
            info        : "1",
            debug       : "2",
            trace       : "3",
            incorrect   : "-1",
            empty       : "",
    ]

    static def weblogicUrl = [
            originalName: "weblogic_url",
            correct     : WebLogicHelper.getEndpoint(),
            incorrect   : "t3:/incorrectHost:1122",
            empty       : "",
    ]
    static def wlstPath = [
            originalName: "wlst_path",
            correct     : WebLogicHelper.getWlstPath(),
            incorrect   : "",
            empty       : "",
    ]


    static def credential = [
            originalName: "credential",
            correct     : "credential",
            incorrect   : "incorrectCredential",
            empty       : "",
    ]

    static def enableNamedSessions = checkBox

    static def javaHome = [
            originalName: "java_home",
            correct     : "",
            incorrect   : "",
            empty       : "",
    ]
    static def javaVendor = [
            originalName: "java_vendor",
            correct     : "",
            incorrect   : "",
            empty       : "",
    ]
    static def mwHome = [
            originalName: "mw_home",
            correct     : "",
            incorrect   : "",
            empty       : "",
    ]
    static def testConnectionRes = [
            originalName: "test_connection_res",
            correct     : WebLogicHelper.getResourceName(),
            incorrect   : "",
            empty       : "",
    ]
    static def testConnection = checkBox

    static def we
}
