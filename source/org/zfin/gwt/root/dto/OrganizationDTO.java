package org.zfin.gwt.root.dto;

/**
 * Created by IntelliJ IDEA.
 * User: Prita
 * Date: 10/28/11
 * Time: 10:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class OrganizationDTO extends RelatedEntityDTO{
    private String prefix;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}