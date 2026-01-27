package org.zfin.mapping;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "panel_count")
@Getter
@Setter
public class PanelCount implements Serializable {

    @EmbeddedId
    private PanelCountId id;

    @Column(name = "panelcnt_count", nullable = false)
    private long count;

    // Convenience methods to access embedded ID fields
    public Panel getPanel() {
        return id != null ? id.getPanel() : null;
    }

    public void setPanel(Panel panel) {
        if (id == null) {
            id = new PanelCountId();
        }
        id.setPanel(panel);
    }

    public String getMarkerType() {
        return id != null ? id.getMarkerType() : null;
    }

    public void setMarkerType(String markerType) {
        if (id == null) {
            id = new PanelCountId();
        }
        id.setMarkerType(markerType);
    }

    public String getLg() {
        return id != null ? id.getLg() : null;
    }

    public void setLg(String lg) {
        if (id == null) {
            id = new PanelCountId();
        }
        id.setLg(lg);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PanelCount that = (PanelCount) o;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
