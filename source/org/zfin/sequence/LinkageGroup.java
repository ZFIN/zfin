/**
 *  Class LinkageGroup.
 */
package org.zfin.sequence ; 

public class LinkageGroup implements Comparable<LinkageGroup>{
    private String name;
    private String nameOrder;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameOrder() {
        return nameOrder;
    }

    public void setNameOrder(String nameOrder) {
        this.nameOrder = nameOrder;
    }

    public int compareTo(LinkageGroup o) {
        return this.name.compareTo(o.getName()) ;
    }
}


