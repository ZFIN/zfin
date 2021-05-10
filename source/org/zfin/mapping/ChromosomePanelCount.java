package org.zfin.mapping;

import java.io.Serializable;
import java.util.List;

public class ChromosomePanelCount implements Serializable {

    private Panel panel;
    private int lg;
    private List<PanelCount> panelCountList;

    public int getLg() {
        return lg;
    }

    public void setLg(int lg) {
        this.lg = lg;
    }

    public Panel getPanel() {
        return panel;
    }

    public void setPanel(Panel panel) {
        this.panel = panel;
    }

    public List<PanelCount> getPanelCountList() {
        return panelCountList;
    }

    public void setPanelCountList(List<PanelCount> panelCountList) {
        this.panelCountList = panelCountList;
    }
}
