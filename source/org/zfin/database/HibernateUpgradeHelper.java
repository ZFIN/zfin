package org.zfin.database;


// Using this class as a way to bridge from hibernate 5 to hibernate 6.
// Specifically, the ResultTransformer interface has been deprecated in Hibernate 5.  There is no replacement until
// Hibernate 6.  This class is a way to bridge the gap.  Uncomment the code for Hibernate 6 when we upgrade.

// USE THIS CLASS AND IMPORTS FOR HIBERNATE 5
import org.hibernate.query.Query;
import org.hibernate.transform.BasicTransformerAdapter;

public class HibernateUpgradeHelper {
    public interface TupleTransformer {
        Object transformTuple(Object[] var1, String[] var2);
    }

    public static Query setTupleResultTransformer(Query query, TupleTransformer transformer) {
        return query.setResultTransformer(new BasicTransformerAdapter() {
            @Override
            public Object transformTuple(Object[] tuple, String[] aliases) {
                return transformer.transformTuple(tuple, aliases);
            }
        });
    }
}

// USE THIS CLASS FOR HIBERNATE 6
//import org.hibernate.query.Query;
//import org.hibernate.query.TupleTransformer;
//
//public class HibernateUpgradeHelper {
//
//    public static Query setTupleResultTransformer(Query query, TupleTransformer transformer) {
//        return query.setTupleTransformer(transformer);
//    }
//
//}
