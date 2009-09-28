package org.zfin.sequence.blast.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.zfin.sequence.blast.*;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

/**
 * Object that represents a database in the interface.
 */
public class DatabasePresentationBean implements Comparable<DatabasePresentationBean>{

    private final static Logger logger = Logger.getLogger(DatabasePresentationBean.class) ;

    private Database database;
    private int indent = 0 ;
    private List<String> childNames = new ArrayList<String>() ;
    private Integer order  = -1 ;
    private List<Database> directChildren ;
    private List<Database> leaves ;
    private DatabaseStatistics databaseStatistics ;

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public int getIndent() {
        return indent;
    }

    public void setIndent(int indent) {
        this.indent = indent;
    }

    public void addChild(String newChild){
        childNames.add(newChild) ;
    }

    public void clearChildren(){
        childNames.clear();
    }

    public String getChildren(){
        String returnString = "" ;
        for(int i = 0 ; i < childNames.size() ; i++){
            returnString += childNames.get(i)  ;
            if(i < childNames.size()-1){
                returnString += "::" ;
            }
        }
        return returnString ;
    }

    public int compareTo(DatabasePresentationBean databasePresentationBean) {
        int compareValue = 0 ;

        // we only compare the order, which comes from the DatabaseRelationship
        // object
        return (getOrder() - databasePresentationBean.getOrder()) ;
    }

    public boolean isEmpty(){
        if(database.getOrigination().getType()==Origination.Type.GENERATED){
            List<Database> leaves = getLeaves() ;
            if(CollectionUtils.isEmpty(leaves)){
                logger.error("somehow database has no leaves: "+ database);
                return true ;
            }
            for(Database databaseLeaf : leaves){
                try {
                    DatabaseStatistics databaseStatistic = WebHostDatabaseStatisticsCache.getInstance().getDatabaseStatistics(databaseLeaf) ;
                    if(databaseStatistic.getNumSequences()==0){
                        return true ;
                    }
                } catch (BlastDatabaseException e) {
                    logger.error(e);
                    return true ;
                }
            }
            return false ;
        }
        else{
            return databaseStatistics.getNumSequences()==0 ;
        }
    }

    public boolean isUnavailable(){
        try {
            if(database.getOrigination().getType()==Origination.Type.GENERATED){
            List<Database> leaves = getLeaves() ;
                if(CollectionUtils.isEmpty(leaves)){
                    logger.error("somehow database has no leaves: "+ database);
                    return true ;
                }
                for(Database databaseLeaf : leaves){
                    try {
                        DatabaseStatistics databaseStatistic = WebHostDatabaseStatisticsCache.getInstance().getDatabaseStatistics(databaseLeaf) ;
                        if(databaseStatistic.getNumSequences()<0){
                            return true ;
                        }
                    } catch (BlastDatabaseException e) {
                        logger.error(e);
                        return true ;
                    }
                }
                return false ;
            }
            else{
                return databaseStatistics.getNumSequences()<0 ;
            }
        } catch (Exception e) {
            logger.error(e);
            return true ; 
        }
    }

    public Integer getTotalNumSequences(){
       if(CollectionUtils.isEmpty(getLeaves())) {
           return databaseStatistics.getNumSequences();
       }
       else{
           int totalNumSequences = 0 ;
           for(Database database : getLeaves()){
               try {
                   totalNumSequences += WebHostDatabaseStatisticsCache.getInstance().getDatabaseStatistics(database).getNumSequences();
               } catch (BlastDatabaseException e) {
                   logger.error("Error retrieving sequences for: "+ database);
               }
           }
           return totalNumSequences ;
       }
    }

    public DatabaseStatistics getDatabaseStatistics() {
        return databaseStatistics;
    }

    public void setDatabaseStatistics(DatabaseStatistics databaseStatistics) {
        this.databaseStatistics = databaseStatistics;
    }

    public List<Database> getDirectChildren() {
        return directChildren;
    }

    public void setDirectChildren(List<Database> directChildren) {
        this.directChildren = directChildren;
    }

    public List<Database> getLeaves() {
        if(leaves==null){
            try {
                setLeaves(BlastPresentationService.getLeaves(database));
            } catch (BlastDatabaseException e) {
                logger.error("failed to find leaves for database ["+database.getAbbrev()+"]",e);
            }
        }
        return leaves;
    }

    public void setLeaves(List<Database> leaves) {
        this.leaves = leaves;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("DatabasePresentationBean");
        sb.append("{database=").append(database.getName());
        sb.append(", indent=").append(indent);
        sb.append(", childNames =").append(childNames);
        sb.append(", order=").append(order);
        sb.append(", databaseStatistics=").append(databaseStatistics);
        if(CollectionUtils.isNotEmpty(directChildren)){
            String directChildrenString = "" ;
            for(Database database : directChildren){
                directChildrenString.concat(database.getAbbrev().toString()) ;
            }
            sb.append(", directChildren=").append(directChildrenString);
        }
        if(CollectionUtils.isNotEmpty(leaves)){
            String leavesString = "" ;
            for(Database database : leaves){
                leavesString.concat(database.getAbbrev().toString()) ;
            }
            sb.append(", leaves=").append(leavesString);
        }
        sb.append('}');
        return sb.toString();
    }
}
