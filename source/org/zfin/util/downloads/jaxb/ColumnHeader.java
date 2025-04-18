package org.zfin.util.downloads.jaxb;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlValue;

/**
 *
 */
@XmlRootElement(name = "Column")
public class ColumnHeader {

    private String column;
    private int id;
    private boolean unique;

    public String getColumn() {
        return column;
    }

    @XmlValue()
    public void setColumn(String column) {
        this.column = column;
    }

    @XmlAttribute(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @XmlAttribute(name = "unique")
    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    @Override
    public String toString() {
        return "ColumnHeader{" +
                "column='" + column + '\'' +
                ", id=" + id +
                ", unique=" + unique +
                '}';
    }
}
