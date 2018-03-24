package org.zfin.infrastructure;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "annual_stats")
public class AnnualStats implements Serializable, Comparable<AnnualStats> {

    @Id
    @Column(name = "as_pk_id")
    private int id;

    @Column(name = "as_count")
    private int count;

    @Column(name = "as_section")
    private String section;

    @Column(name = "as_type")
    private String type;

    @Column (name = "as_date")
    private Date date;

    public boolean equals(Object o) {
        if (!(o instanceof AnnualStats))
            return false;

        AnnualStats annualStats = (AnnualStats) o;
        return annualStats.getId() == this.getId();
    }

    public int hashCode() {
        return (getId()+"").hashCode();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int compareTo(AnnualStats anotherStats) {
        return date.compareTo(anotherStats.getDate());
    }
}
