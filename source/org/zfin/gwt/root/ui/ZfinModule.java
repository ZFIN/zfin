package org.zfin.gwt.root.ui;

/**
 * Convenience class in root.ui package
 */
public class ZfinModule {

    private String tabName;
    private String moduleName;


    public ZfinModule(String tabName, String moduleName) {
        this.tabName = tabName;
        this.moduleName = moduleName;
    }

    public String getTabName() {
        return tabName;
    }

    public String getModuleName() {
        return moduleName;
    }
}
