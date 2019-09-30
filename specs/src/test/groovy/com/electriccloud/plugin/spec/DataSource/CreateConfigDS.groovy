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
    static configName = [
            originalName: "config",
            correct     : "CreateConfigurationTest",
            incorrect   : "//",
            empty       : "",
    ]

    static debugLevel = [
            originalName: "debug_level",
            info        : "1",
            debug       : "2",
            trace       : "3",
            incorrect   : "-1",
            empty       : "",
    ]

    static weblogicUrl = [
            originalName: "weblogic_url",
            correct     : WebLogicHelper.getEndpoint(),
            incorrect   : "t3:/incorrectHost:1122",
            empty       : "",
    ]
    static wlstPath = [
            originalName: "wlst_path",
            correct     : WebLogicHelper.getWlstPath(),
            incorrect   : "",
            empty       : "",
    ]


    static credential = [
            originalName: "credential",
            correct     : "credential",
            incorrect   : "incorrectCredential",
            empty       : "",
    ]

    static enableNamedSessions = checkBox

    static javaHome = [
            originalName: "java_home",
            correct     : "",
            incorrect   : "",
            empty       : "",
    ]
    static javaVendor = [
            originalName: "java_vendor",
            correct     : "",
            incorrect   : "",
            empty       : "",
    ]
    static mwHome = [
            originalName: "mw_home",
            correct     : "",
            incorrect   : "",
            empty       : "",
    ]
    static testConnectionRes = [
            originalName: "test_connection_res",
            correct     : WebLogicHelper.getResourceName(),
            incorrect   : "",
            empty       : "",
    ]
    static testConnection = checkBox
}
