package org.zfin.mapping;

import org.zfin.infrastructure.ActiveSource;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.infrastructure.ZdbID;
import org.zfin.profile.Company;
import org.zfin.profile.Person;

import javax.persistence.Entity;
import java.util.Date;
import java.util.Map;

public abstract class Panel implements EntityZdbID {

    private String zdbID;
    private String name;
    private String abbreviation;
    private String type;
    private String comments;
    private String sourceID;
    private Person sourcePerson;
    private Company sourceCompany;
    private long displayOrder;
    private Date date;
    private Person producer;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(long displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Person getProducer() {
        return producer;
    }

    public void setProducer(Person producer) {
        this.producer = producer;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Map<String, Map<String, Long>> getChromosomePanelCountMap() {
        return MappingService.getChromosomePanelCountMap(this);
    }

    public String getSourceID() {
        return sourceID;
    }

    public void setSourceID(String source) {
        this.sourceID = source;
    }

    public ZdbID getSource(){
        if(ActiveSource.isValidActiveData(sourceID, ActiveSource.Type.PERS))
            return sourcePerson;
        else
            return sourceCompany;
    }

    public Person getSourcePerson() {
        return sourcePerson;
    }

    public void setSourcePerson(Person sourcePerson) {
        this.sourcePerson = sourcePerson;
    }

    public Company getSourceCompany() {
        return sourceCompany;
    }

    public void setSourceCompany(Company sourceCompany) {
        this.sourceCompany = sourceCompany;
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
