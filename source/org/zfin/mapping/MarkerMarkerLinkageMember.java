package org.zfin.mapping;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostLoad;
import lombok.Getter;
import lombok.Setter;
import org.zfin.infrastructure.ZdbID;
import org.zfin.marker.Marker;

@Entity
@DiscriminatorValue("MarkMark")
@Getter
@Setter
public class MarkerMarkerLinkageMember extends LinkageMember {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lms_member_1_zdb_id")
    private Marker marker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lms_member_2_zdb_id")
    private Marker pairedMarker;

    @PostLoad
    private void initTransientFields() {
        entityOne = marker;
        entityTwo = pairedMarker;
    }

    public void setPairedMarker(Marker pairedMarker) {
        this.pairedMarker = pairedMarker;
        entityOne = marker;
        entityTwo = pairedMarker;
    }

    @Override
    public ZdbID getLinkedMember() {
        return pairedMarker;
    }

    @Override
    public LinkageMember getInverseMember() {
        MarkerMarkerLinkageMember inverse;
        try {
            inverse = (MarkerMarkerLinkageMember) this.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
        inverse.setMarker(pairedMarker);
        inverse.setPairedMarker(marker);
        return inverse;
    }
}
