package org.zfin.sequence.blast.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.zfin.sequence.blast.*;

import java.io.IOException;
import java.util.*;

/**
 * This class is used exclusively for blast presentation.
 * It is not used for accessing the blast databases directly.
 * For that follow the BlastService interface.
 */
public class BlastPresentationService {

    private final static Logger logger = Logger.getLogger(BlastPresentationService.class) ;

    /**
     * This method processes the databases so that they appear in the correct order with the correct indent.
     * Each database can be included multiple times.
     *
     * @param databases databases to process
     * @return Ordered list of DatabasePresentationBean objects.
     */
    public static List<DatabasePresentationBean> orderDatabasesFromRoot(Collection<Database> databases){
        if(CollectionUtils.isEmpty(databases)){
            return new ArrayList<DatabasePresentationBean>();
        }

        // 1. get parent nodes
        List<DatabasePresentationBean> databasePresentationBeans = new ArrayList<DatabasePresentationBean>() ;

        for(Database database : databases){
            // get relationships where I am the child, but there is no parent, thus I am the root
            TreeSet<DatabaseRelationship> childRelationships = new TreeSet<DatabaseRelationship>(database.getChildrenRelationships()) ;

            // if there is no parent, then we must be the parent
            // as such, set the order based on the child
            if(CollectionUtils.isNotEmpty(childRelationships) ){
                for(DatabaseRelationship childRelationship : childRelationships){
                    if(childRelationship.getParent()==null){
                        // first we add ourselves
                        DatabasePresentationBean databasePresentationBean = new DatabasePresentationBean() ;
                        databasePresentationBean.setDatabase(database);
                        databasePresentationBean.setIndent(0);
                        databasePresentationBean.setOrder(childRelationship.getOrder());
                        databasePresentationBeans.add(databasePresentationBean)  ;
                    }
                }
            }
        }
        // 2. sort parent nodes, we have to sort here, because there is no guarantee that they are sorted
        Collections.sort(databasePresentationBeans);

        // 3. recurse through parent nodes and index
        for(int i = 0 ; i < databasePresentationBeans.size() ; i++){
            // only process the parents here
            if(databasePresentationBeans.get(i).getIndent()==0){
                databasePresentationBeans.addAll(i+1, processChildrenFromList(databasePresentationBeans.get(i),databases));
            }
        }

        return databasePresentationBeans ;
    }

    private static List<DatabasePresentationBean> processChildrenFromList( DatabasePresentationBean parentDatabasePresentationBean , Collection<Database> databases) {

        Database parentDatabase  = parentDatabasePresentationBean.getDatabase() ;
        List<DatabasePresentationBean> databasePresentationBeans= new ArrayList<DatabasePresentationBean>() ;

        if(CollectionUtils.isNotEmpty(parentDatabase.getParentRelationships())){
            TreeSet<DatabaseRelationship> parentRelationships = new TreeSet<DatabaseRelationship>(parentDatabase.getParentRelationships()) ;
            for(DatabaseRelationship parentRelationship : parentRelationships){
                Database childDatabase = parentRelationship.getChild() ;
                if(databases.contains(childDatabase)){
                    DatabasePresentationBean childDatabasePresentationBean =  new DatabasePresentationBean() ;
                    childDatabasePresentationBean.setDatabase(childDatabase);
                    parentDatabasePresentationBean.addChild(childDatabase.getName());
                    childDatabasePresentationBean.setIndent(parentDatabasePresentationBean.getIndent()+1);
                    childDatabasePresentationBean.setOrder(parentRelationship.getOrder());
                    databasePresentationBeans.add (childDatabasePresentationBean) ;
//                    databasePresentationBeans.add(databasePresentationBeans.indexOf(childDatabasePresentationBean)+1,childDatabasePresentationBean) ;

                    // recurse here
                    databasePresentationBeans.addAll(databasePresentationBeans.size(), processChildrenFromList(childDatabasePresentationBean,databases)) ;
                }
            }
        }

        return databasePresentationBeans ;
    }


    public static List<DatabasePresentationBean> createPresentationBeansWithRemoteSize(Collection<Database> databases){
        List<DatabasePresentationBean> databasePresentationBeans = new ArrayList<DatabasePresentationBean>() ;
        for(Database database: databases){
            DatabasePresentationBean databasePresentationBean =  createPresentationBean(database) ;
            try {
                databasePresentationBean.setDatabaseStatistics(WebHostDatabaseStatisticsCache.getInstance().getDatabaseStatistics(database)); ;
            } catch (BlastDatabaseException e) {
                logger.error("Failed to get the size for database: "+ database);
            }
            databasePresentationBeans.add(databasePresentationBean) ;
        }
        return databasePresentationBeans ;
    }

    public static DatabasePresentationBean createPresentationBean(Database database){
        DatabasePresentationBean databasePresentationBean = new DatabasePresentationBean() ;
        databasePresentationBean.setDatabase(database);
        databasePresentationBean.setIndent(0);
        databasePresentationBean.setOrder(0);
        return databasePresentationBean ;
    }

    public static List<DatabasePresentationBean> processFromChild( Database database, boolean showPrivate) {

        List<DatabasePresentationBean> databasePresentationBeans= new ArrayList<DatabasePresentationBean>() ;

        // create a bean for self
        if(database.isPublicDatabase() || showPrivate==true){
            DatabasePresentationBean databasePresentationBean =  new DatabasePresentationBean() ;
            databasePresentationBean.setDatabase(database);
            databasePresentationBean.setIndent(0);
            databasePresentationBean.setOrder(0);
            databasePresentationBeans.add (databasePresentationBean) ;

            databasePresentationBeans.addAll(databasePresentationBeans.size(),processAllChildren(databasePresentationBean, showPrivate)) ;
        }

        return databasePresentationBeans ;
    }

    private static List<DatabasePresentationBean> processAllChildren( DatabasePresentationBean parentDatabasePresentationBean , boolean showPrivate) {

        Database parentDatabase  = parentDatabasePresentationBean.getDatabase() ;
        List<DatabasePresentationBean> databasePresentationBeans= new ArrayList<DatabasePresentationBean>() ;

        if(CollectionUtils.isNotEmpty(parentDatabase.getParentRelationships())){
            TreeSet<DatabaseRelationship> parentRelationships = new TreeSet<DatabaseRelationship>(parentDatabase.getParentRelationships()) ;
            for(DatabaseRelationship parentRelationship : parentRelationships){
                Database childDatabase = parentRelationship.getChild() ;
                if(childDatabase.isPublicDatabase() || showPrivate==true){
                    DatabasePresentationBean childDatabasePresentationBean =  new DatabasePresentationBean() ;
                    childDatabasePresentationBean.setDatabase(childDatabase);
                    parentDatabasePresentationBean.addChild(childDatabase.getName());
                    childDatabasePresentationBean.setIndent(parentDatabasePresentationBean.getIndent()+1);
                    childDatabasePresentationBean.setOrder(parentRelationship.getOrder());
                    databasePresentationBeans.add (childDatabasePresentationBean) ;

                    // recurse here
                    databasePresentationBeans.addAll(databasePresentationBeans.size(), processAllChildren(childDatabasePresentationBean, showPrivate)) ;
                }
            }
        }
        return databasePresentationBeans ;
    }


    public static List<Database> getLeaves(Database rootDatabase) throws BlastDatabaseException{
        return getLeaves(rootDatabase, new ArrayList<Database>()) ;
    }

    /**
     *
     * Returns a list of database leaves,  returning itself if it is itself a leaf.
     * Throws an error if it is a leaf and also generated.
     * @param rootDatabase
     * @return A list of database leaves.
     */
    protected static List<Database> getLeaves(Database rootDatabase, List<Database> databases) throws BlastDatabaseException{

        Set<DatabaseRelationship> databaseRelationships = rootDatabase.getParentRelationships() ;

        // if there are no children, then add self and return
        if(CollectionUtils.isEmpty(databaseRelationships) ){
            databases.add(rootDatabase) ;
        }
        else{
            // getting as root, because we always want all of the leaves
            List<Database> childDatabases = getDirectChildren(rootDatabase,true) ;
            for(Database childDatabase : childDatabases){
                getLeaves(childDatabase,databases) ;
            }
        }

        // make sure that all leaves are valid
        for(Database database: databases){
            if(database.getOrigination()!=null && database.getOrigination().getType()== Origination.Type.GENERATED){
                throw new BlastDatabaseException("A leaf node may not be of generated type: "+ database) ;
            }
        }

        return databases ;
    }

    /**
     *
     * A convenience method for returning children when root is always true.
     * @param database
     * @return A list of direct database children.
     */
    public static List<Database> getDirectChildren(Database database){
        return getDirectChildren(database,true) ;
    }

    /**
     *
     * Returns a list of direct database children.  It returns an empty list if it is a leaf.
     * @param database
     * @param isRoot 
     * @return A list of direct database children.
     */
    public static List<Database> getDirectChildren(Database database, boolean isRoot){
        List<Database> databases = new ArrayList<Database>() ;

        Set<DatabaseRelationship> parentDatabases = database.getParentRelationships() ;
        if(parentDatabases != null){
            for(DatabaseRelationship parentDatabaseRelationship : parentDatabases){
                Database childDatabase = parentDatabaseRelationship.getChild() ;
                if(childDatabase.isPublicDatabase()==true || isRoot==true) {
                    databases.add(childDatabase) ;
                }
            }
        }

        return databases ;
    }

    public static Database getFirstPublicParentDatabase(Database database){
        if(database.isPublicDatabase()){
            return database ;
        }

        Set<DatabaseRelationship> childDatabaseRelationships = database.getChildrenRelationships() ;
        if(childDatabaseRelationships != null){
            for(DatabaseRelationship childDatabaseRelationship : childDatabaseRelationships){
                Database parentDatabase = childDatabaseRelationship.getParent() ;
                if(parentDatabase==null){
                    return null ; 
                }
                return getFirstPublicParentDatabase(parentDatabase) ;
            }
        }

        return null ; 
    }

}


