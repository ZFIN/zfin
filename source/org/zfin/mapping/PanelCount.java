package org.zfin.mapping;

import java.io.Serializable;

public class PanelCount implements Serializable {

    private Panel panel;
    private String markerType;
    private String lg;
    private long count;

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public String getLg() {
        return lg;
    }

    public void setLg(String lg) {
        this.lg = lg;
    }

    public String getMarkerType() {
        return markerType;
    }

    public void setMarkerType(String markerType) {
        this.markerType = markerType;
    }

    public Panel getPanel() {
        return panel;
    }

    public void setPanel(Panel panel) {
        this.panel = panel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PanelCount that = (PanelCount) o;

        if (lg != that.lg) return false;
        if (!markerType.equals(that.markerType)) return false;
        if (!panel.equals(that.panel)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = panel.hashCode();
        result = 31 * result + markerType.hashCode();
        result = 31 * result + lg.hashCode();
        return result;
    }
}
