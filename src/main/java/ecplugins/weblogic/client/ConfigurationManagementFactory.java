
// ConfigurationManagementFactory.java --
//
// ConfigurationManagementFactory.java is part of ElectricCommander.
//
// Copyright (c) 2005-2012 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.weblogic.client;

import ecinternal.client.InternalComponentBaseFactory;
import ecinternal.client.InternalFormBase;
import ecinternal.client.PropertySheetEditor;

import com.electriccloud.commander.gwt.client.BrowserContext;
import com.electriccloud.commander.gwt.client.Component;
import com.electriccloud.commander.gwt.client.ComponentContext;
import org.jetbrains.annotations.NotNull;

import ecplugins.weblogic.client.EditConfigPropertySheetEditor;
import static com.electriccloud.commander.gwt.client.util.CommanderUrlBuilder.createPageUrl;

public class ConfigurationManagementFactory
    extends InternalComponentBaseFactory
{

    //~ Methods ----------------------------------------------------------------

    @NotNull
    @Override public Component createComponent(ComponentContext jso)
    {
        String    panel     = jso.getParameter("panel");
        Component component;

        if ("create".equals(panel)) {
            component = new CreateConfiguration();
        }
        else if ("edit".equals(panel)) {
            String configName    = BrowserContext.getInstance()
                                                 .getGetParameter("configName");
            String projectName = "/plugins/" + getPluginName() + "/project";
            String propSheetPath = projectName + "/weblogic_cfgs/" + configName;

            String formXmlPath   = "/plugins/" + getPluginName()
                    + "/project/ui_forms/WebLogicEditConfigForm";

            component = new EditConfigPropertySheetEditor("ecgc",
                    "Edit WebLogic Configuration", configName, propSheetPath,
                    formXmlPath, projectName, getPluginName()
            );

            ((InternalFormBase) component).setDefaultRedirectToUrl(
                createPageUrl(getPluginName(), "configurations").buildString());
        }
        else {

            // Default panel is "list"
            component = new ConfigurationList();
        }

        return component;
    }
}
