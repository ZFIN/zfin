package org.zfin.framework;


import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.comparators.ComparatorChain;
import org.apache.commons.collections.comparators.NullComparator;

import java.util.Comparator;


public class ComparatorCreator {

    /**
     * A service method for sorting by multiple fields, nested properties are ok too.
     */
    public static Comparator orderBy(String ... fields) {
        ComparatorChain comparatorChain = new ComparatorChain( );
        for (String field : fields) {
            comparatorChain.addComparator( new BeanComparator( field, new NullComparator(false) ) );
        }
        return comparatorChain;
    }

}
