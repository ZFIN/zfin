package org.zfin.database;


// Using this class as a way to bridge from hibernate 5 to hibernate 6.
// Specifically, the ResultTransformer interface has been deprecated in Hibernate 5.  There is no replacement until
// Hibernate 6.  This class is a way to bridge the gap.  Uncomment the code for Hibernate 6 when we upgrade.

// USE THIS CLASS AND IMPORTS FOR HIBERNATE 5
//import org.hibernate.query.Query;
//import org.hibernate.transform.BasicTransformerAdapter;
//
//import java.util.List;
//
//public class HibernateUpgradeHelper {
//    public interface TupleTransformer {
//        Object transformTuple(Object[] var1, String[] var2);
//    }
//
//    public interface ListTransformer {
//        List transformList(List list) ;
//    }
//
//    public static Query setTupleResultTransformer(Query query, TupleTransformer transformer) {
//        return query.setResultTransformer(new BasicTransformerAdapter() {
//            @Override
//            public Object transformTuple(Object[] tuple, String[] aliases) {
//                return transformer.transformTuple(tuple, aliases);
//            }
//        });
//    }
//
//    public static Query setTupleResultAndListTransformer(Query query, TupleTransformer transformer, ListTransformer listTransformer) {
//        return query.setResultTransformer(new BasicTransformerAdapter() {
//            @Override
//            public Object transformTuple(Object[] tuple, String[] aliases) {
//                return transformer.transformTuple(tuple, aliases);
//            }
//
//            @Override
//            public List transformList(List list) {
//                return listTransformer.transformList(list);
//            }
//        });
//    }
//}

// USE THIS CLASS FOR HIBERNATE 6
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.query.TupleTransformer;
import org.zfin.marker.presentation.PreviousNameLight;

import java.util.List;
import java.util.function.Function;

public class HibernateUpgradeHelper {

    public static Query setTupleResultTransformer(Query query, TupleTransformer transformer) {
        return query.setTupleTransformer(transformer);
    }

    //slightly different from hibernate 5 version. This version calls the query.list() method
    //so query.scroll() will need to be handled differently
    public static <T, R> List<R> setTupleResultAndListTransformer(Query<T> query,
                                                                  TupleTransformer transformer,
                                                                  Function<List<T>, List<R>> listTransformer) {
        List tempList = setTupleResultTransformer(query, transformer).list();
        return listTransformer.apply(tempList);
    }

}
