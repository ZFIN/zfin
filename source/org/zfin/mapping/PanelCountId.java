package org.zfin.mapping;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
public class PanelCountId implements Serializable {

    @Column(name = "panelcnt_chromosome")
    private String lg;

    @Column(name = "panelcnt_mrkr_type")
    private String markerType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "panelcnt_panel_zdb_id")
    private Panel panel;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PanelCountId that = (PanelCountId) o;
        return Objects.equals(lg, that.lg) &&
                Objects.equals(markerType, that.markerType) &&
                Objects.equals(panel != null ? panel.getZdbID() : null,
                        that.panel != null ? that.panel.getZdbID() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lg, markerType, panel != null ? panel.getZdbID() : null);
    }
}
