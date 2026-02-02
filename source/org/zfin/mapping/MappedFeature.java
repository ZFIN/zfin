package org.zfin.mapping;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import org.zfin.feature.Feature;

@Entity
@DiscriminatorValue("Feat")
@Getter
@Setter
public class MappedFeature extends MappedMarker {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marker_id", insertable = false, updatable = false)
    private Feature feature;

    public int compareTo(Object o) {
        if (o == null) {
            return -1;
        } else if (!(o instanceof MappedFeature)) {
            return o.toString().compareTo(toString());
        }
        // both MappedMarker
        else {
            MappedFeature mappedMarker = (MappedFeature) o;
            if (!lg.equalsIgnoreCase(mappedMarker.getLg())) {
                return lg.toLowerCase().compareTo(mappedMarker.getLg().toLowerCase());
            } else {
                return feature.compareTo(mappedMarker.getFeature());
            }
        }
    }

    @Override
    public String getEntityID() {
        return feature.getZdbID();
    }

    @Override
    public String getEntityAbbreviation() {
        return feature.getAbbreviation();
    }
}
