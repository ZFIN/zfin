package org.zfin.mapping;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DiscriminatorFormula;
import org.zfin.infrastructure.ActiveSource;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.infrastructure.ZdbID;
import org.zfin.profile.Company;
import org.zfin.profile.Person;

import java.util.Date;
import java.util.Map;

@Entity
@Table(name = "panels")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorFormula(
        "CASE ptype " +
        "WHEN 'Radiation Hybrid' THEN 'Radiation' " +
        "ELSE 'Meiotic  ' " +
        "END"
)
@Getter
@Setter
public abstract class Panel implements EntityZdbID {

    @Id
    @Column(name = "zdb_id")
    private String zdbID;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "abbrev", nullable = false)
    private String abbreviation;

    @Column(name = "ptype")
    private String type;

    @Column(name = "mappanel_comments")
    private String comments;

    @Column(name = "source", nullable = false)
    private String sourceID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source", insertable = false, updatable = false)
    private Person sourcePerson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source", insertable = false, updatable = false)
    private Company sourceCompany;

    @Column(name = "disp_order", nullable = false)
    private long displayOrder;

    @Column(name = "panel_date", nullable = false)
    private Date date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producer", insertable = false, updatable = false)
    private Person producer;

    public Map<String, Map<String, Long>> getChromosomePanelCountMap() {
        return MappingService.getChromosomePanelCountMap(this);
    }

    public ZdbID getSource(){
        if(ActiveSource.isValidActiveData(sourceID, ActiveSource.Type.PERS))
            return sourcePerson;
        else
            return sourceCompany;
    }

    public Map<String, Long> getPanelMarkerCountMap() {
        return MappingService.getStatisticMap(this);
    }

    public long getPanelMarkerCount() {
        return MappingService.getTotalNumberOfMarker(this);
    }


    @Override
    public String getAbbreviationOrder() {
        return name;
    }

    @Override
    public String getEntityType() {
        return "Panel";
    }

    @Override
    public String getEntityName() {
        return name;
    }

}
