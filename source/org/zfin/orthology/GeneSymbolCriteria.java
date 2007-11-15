package org.zfin.orthology;

import org.zfin.util.FilterType;

/**
 * User: giles
 * Date: Aug 3, 2006
 * Time: 4:17:31 PM
 */

/**
 * Business criteria object for holding gene symbol input.  Created by the OrthologyCriteriaService
 * class and is passed on to the repository by the controller.
 */
public class GeneSymbolCriteria {
    private FilterType type;
    private String symbol;

    public FilterType getType() {
        return type;
    }

    public void setType(FilterType type) {
        this.type = type;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
