package org.zfin.orthology;

import java.io.Serializable;

/**
 * User: giles
 * Date: Jul 26, 2006
 */

/**
 * Business object for holding accession information for a gene.
 */
public class AccessionItem  implements Serializable {
    private String name;
    private String number;
    private String url;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getNumber() {
        return number;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
