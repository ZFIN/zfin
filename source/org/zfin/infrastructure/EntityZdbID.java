package org.zfin.infrastructure;

/**
 * General ZDB ID domain object.
 */
public interface EntityZdbID extends ZdbID{

    public String getAbbreviation();

    public String getAbbreviationOrder();

    public String getEntityType();

    public String getEntityName();

}
