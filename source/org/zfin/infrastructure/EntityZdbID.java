package org.zfin.infrastructure;

import com.fasterxml.jackson.annotation.JsonView;
import org.zfin.framework.api.View;

/**
 * General ZDB ID domain object.
 */
public interface EntityZdbID extends ZdbID{

    @JsonView(View.API.class)
    public String getAbbreviation();

    public String getAbbreviationOrder();


    @JsonView(View.API.class)
    public String getEntityType();

    public String getEntityName();

}
