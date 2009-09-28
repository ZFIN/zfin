package org.zfin.sequence.blast.presentation;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import org.zfin.sequence.blast.*;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.sequence.MarkerDefline;
import org.zfin.sequence.Defline;
import org.zfin.marker.Marker;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.*;

import static junit.framework.Assert.fail;

/**
 * Tests for the BlastPresentationService.
 */
public class BlastPresentationServiceTest {

    private final static Logger logger = Logger.getLogger(BlastPresentationServiceTest.class) ;

    @Test
    public void parentOnlyOrdering(){

        String D1 = "d1" ;
        String D2 = "d2" ;

        Database d1 = new Database();
        d1.setName(D1);
        Database d2 = new Database();
        d2.setName(D2);

        DatabaseRelationship dr1 = new DatabaseRelationship() ;
        dr1.setChild(d1);
        dr1.setOrder(1);
        Set<DatabaseRelationship> dr1Set = new HashSet<DatabaseRelationship>() ;
        dr1Set.add(dr1) ;
        d1.setChildrenRelationships(dr1Set);

        DatabaseRelationship dr2 = new DatabaseRelationship() ;
        dr2.setChild(d2);
        dr2.setOrder(2);
        Set<DatabaseRelationship> dr2Set = new HashSet<DatabaseRelationship>() ;
        dr2Set.add(dr2) ;
        d2.setChildrenRelationships(dr2Set);

        Set<Database> databases = new HashSet<Database>() ;
        databases.add(d1) ;
        databases.add(d2) ;

        List<DatabasePresentationBean> returnDatabases = BlastPresentationService.orderDatabasesFromRoot(databases) ;
        assertEquals("Should be two databases",returnDatabases.size(),2) ;
        assertEquals("First database should be dr1",returnDatabases.iterator().next().getDatabase().getName(),D1) ;
        assertEquals("First database should have indent 0",returnDatabases.get(0).getIndent(),0) ;
        assertEquals("Second database should be dr2",returnDatabases.get(1).getDatabase().getName(),D2) ;
        assertEquals("Second database should have indent 0",returnDatabases.get(0).getIndent(),0) ;
        assertEquals("d1 should have no children",0,BlastPresentationService.getDirectChildren(d1).size());
        assertEquals("d2 should have no children",0,BlastPresentationService.getDirectChildren(d2).size());
        try {
            assertEquals("d1 should have no leaved",1,BlastPresentationService.getLeaves(d1).size());
            assertEquals("d2 should have no leaves",1,BlastPresentationService.getLeaves(d2).size());
        } catch (BlastDatabaseException e) {
            fail(e.toString()) ;
        }

        dr1.setOrder(2);
        dr2.setOrder(1);
        returnDatabases = BlastPresentationService.orderDatabasesFromRoot(databases) ;
        assertEquals("Should be two databases",returnDatabases.size(),2) ;
        assertEquals("First database should be dr2",returnDatabases.get(0).getDatabase().getName(),D2) ;
        assertEquals("First database should have indent 0",returnDatabases.get(0).getIndent(),0) ;
        assertEquals("Second database should be dr1",returnDatabases.get(1).getDatabase().getName(),D1) ;
        assertEquals("Second database should have indent 0",returnDatabases.get(1).getIndent(),0) ;
        assertEquals("d1 should have no children",0,BlastPresentationService.getDirectChildren(d1).size());
        assertEquals("d2 should have no children",0,BlastPresentationService.getDirectChildren(d2).size());
        try {
            assertEquals("d1 should have no leaved",1,BlastPresentationService.getLeaves(d1).size());
            assertEquals("d2 should have no leaves",1,BlastPresentationService.getLeaves(d2).size());
        } catch (BlastDatabaseException e) {
            fail(e.toString()) ;
        }


    }




    @Test
    public void parentChildOnlyOrdering(){

        // (dr1) d1
        // (dr2) -> d2 

        String D1 = "d1" ;
        String D2 = "d2" ;

        Database d1 = new Database();
        d1.setName(D1);
        Database d2 = new Database();
        d2.setName(D2);

        DatabaseRelationship dr1 = new DatabaseRelationship() ;
        dr1.setChild(d1);
        dr1.setOrder(1);
        Set<DatabaseRelationship> dr1Set = new HashSet<DatabaseRelationship>() ;
        dr1Set.add(dr1) ;
        d1.setChildrenRelationships(dr1Set);

        DatabaseRelationship dr2 = new DatabaseRelationship() ;
        dr2.setParent(d1);
        dr2.setChild(d2);
        dr2.setOrder(1);
        Set<DatabaseRelationship> dr2Set = new HashSet<DatabaseRelationship>() ;
        dr2Set.add(dr2) ;
        d2.setChildrenRelationships(dr2Set);
        d1.setParentRelationships(dr2Set);

        Set<Database> databases = new HashSet<Database>() ;
        databases.add(d1) ;
        databases.add(d2) ;

        List<DatabasePresentationBean> returnDatabases = BlastPresentationService.orderDatabasesFromRoot(databases) ;
        assertEquals("Should be two databases",2,returnDatabases.size()) ;
        assertEquals("First database should be dr1",D1,returnDatabases.get(0).getDatabase().getName()) ;
        assertEquals("First database should have indent 0",0,returnDatabases.get(0).getIndent()) ;
        assertEquals("Second database should be dr2",D2,returnDatabases.get(1).getDatabase().getName()) ;
        assertEquals("Second database should have indent 1",1,returnDatabases.get(1).getIndent()) ;
        assertEquals("d1 children",1,BlastPresentationService.getDirectChildren(d1).size());
        assertEquals("d2 children",0,BlastPresentationService.getDirectChildren(d2).size());
        try {
            assertEquals("d1 leaves",1,BlastPresentationService.getLeaves(d1).size());
            assertEquals("d2 leaves",1,BlastPresentationService.getLeaves(d2).size());
        } catch (BlastDatabaseException e) {
            fail(e.toString()) ;
        }

        returnDatabases = BlastPresentationService.processFromChild(d1,true) ;
        assertEquals("Should be two databases",2,returnDatabases.size()) ;
        assertEquals("First database should be dr1",D1,returnDatabases.get(0).getDatabase().getName()) ;
        assertEquals("First database should have indent 0",0,returnDatabases.get(0).getIndent()) ;
        assertEquals("Second database should be dr2",D2,returnDatabases.get(1).getDatabase().getName()) ;
        assertEquals("Second database should have indent 1",1,returnDatabases.get(1).getIndent()) ;
        
        assertEquals("d1 children",1,BlastPresentationService.getDirectChildren(d1).size());
        assertEquals("d2 children",0,BlastPresentationService.getDirectChildren(d2).size());
        try {
            assertEquals("d1 leaves",1,BlastPresentationService.getLeaves(d1).size());
            assertEquals("d2 leaves",1,BlastPresentationService.getLeaves(d2).size());
        } catch (BlastDatabaseException e) {
            fail(e.toString()) ;
        }
    }


    @Test
    public void childrenAreOrderedProperly(){

        //parent
        String D1 = "d1" ;

        //children
        String D2 = "d2" ;
        String D3 = "d3" ;

        Database d1 = new Database();
        d1.setName(D1);
        Database d2 = new Database();
        d2.setName(D2);
        Database d3 = new Database();
        d3.setName(D3);

        DatabaseRelationship dr1 = new DatabaseRelationship() ;
        dr1.setChild(d1);
        dr1.setOrder(1);
        Set<DatabaseRelationship> dr1Set = new HashSet<DatabaseRelationship>() ;
        dr1Set.add(dr1) ;
        d1.setChildrenRelationships(dr1Set);


        DatabaseRelationship dr2 = new DatabaseRelationship() ;
        dr2.setParent(d1);
        dr2.setChild(d2);
        dr2.setOrder(2);
        Set<DatabaseRelationship> dr2Set = new HashSet<DatabaseRelationship>() ;
        dr2Set.add(dr2) ;
        d2.setChildrenRelationships(dr2Set);

        DatabaseRelationship dr3 = new DatabaseRelationship() ;
        dr3.setParent(d1);
        dr3.setChild(d3);
        dr3.setOrder(1);
        Set<DatabaseRelationship> dr3Set = new HashSet<DatabaseRelationship>() ;
        dr3Set.add(dr3) ;
        d3.setChildrenRelationships(dr3Set);


        Set<DatabaseRelationship> dr1ParentRelationships = new HashSet<DatabaseRelationship>() ;
        dr1ParentRelationships.add(dr2) ;
        dr1ParentRelationships.add(dr3) ;
        d1.setParentRelationships(dr1ParentRelationships);

        Set<Database> databases = new HashSet<Database>() ;
        databases.add(d1) ;
        databases.add(d2) ;
        databases.add(d3) ;

        List<DatabasePresentationBean> returnDatabases = BlastPresentationService.orderDatabasesFromRoot(databases) ;
        assertEquals("Should be three databases",returnDatabases.size(),3) ;
        assertEquals("First database should be dr1",returnDatabases.get(0).getDatabase().getName(),D1) ;
        assertEquals("First database should have indent 0",returnDatabases.get(0).getIndent(),0) ;
        assertEquals("Second database should be dr3",returnDatabases.get(1).getDatabase().getName(),D3) ;
        assertEquals("Second database should have indent 1",returnDatabases.get(1).getIndent(),1) ;
        assertEquals("Third database should be dr2",returnDatabases.get(2).getDatabase().getName(),D2) ;
        assertEquals("Third database should have indent 1",returnDatabases.get(2).getIndent(),1) ;

        dr3.setOrder(2);
        dr2.setOrder(1);

        assertEquals("d1 children",2,BlastPresentationService.getDirectChildren(d1).size());
        assertEquals("d2 children",0,BlastPresentationService.getDirectChildren(d2).size());
        assertEquals("d3 children",0,BlastPresentationService.getDirectChildren(d3).size());
        try {
            assertEquals("d1 leaves",2,BlastPresentationService.getLeaves(d1).size());
            assertEquals("d2 leaves",1,BlastPresentationService.getLeaves(d2).size());
            assertEquals("d3 leaves",1,BlastPresentationService.getLeaves(d3).size());
        } catch (BlastDatabaseException e) {
            fail(e.toString()) ;
        }

        returnDatabases = BlastPresentationService.orderDatabasesFromRoot(databases) ;
        assertEquals("Should be three databases",returnDatabases.size(),3) ;
        assertEquals("First database should be dr1",returnDatabases.get(0).getDatabase().getName(),D1) ;
        assertEquals("First database should have indent 0",returnDatabases.get(0).getIndent(),0) ;
        assertEquals("Second database should be dr2",returnDatabases.get(1).getDatabase().getName(),D2) ;
        assertEquals("Second database should have indent 1",returnDatabases.get(1).getIndent(),1) ;
        assertEquals("Third database should be dr3",returnDatabases.get(2).getDatabase().getName(),D3) ;
        assertEquals("Third database should have indent 1",returnDatabases.get(2).getIndent(),1) ;

        returnDatabases = BlastPresentationService.processFromChild(d1,true) ;
        assertEquals("Should be three databases",returnDatabases.size(),3) ;
        assertEquals("First database should be dr1",returnDatabases.get(0).getDatabase().getName(),D1) ;
        assertEquals("First database should have indent 0",returnDatabases.get(0).getIndent(),0) ;
        assertEquals("Second database should be dr2",returnDatabases.get(1).getDatabase().getName(),D2) ;
        assertEquals("Second database should have indent 1",returnDatabases.get(1).getIndent(),1) ;
        assertEquals("Third database should be dr3",returnDatabases.get(2).getDatabase().getName(),D3) ;
        assertEquals("Third database should have indent 1",returnDatabases.get(2).getIndent(),1) ;

        assertEquals("d1 children",2,BlastPresentationService.getDirectChildren(d1).size());
        assertEquals("d2 children",0,BlastPresentationService.getDirectChildren(d2).size());
        assertEquals("d3 children",0,BlastPresentationService.getDirectChildren(d3).size());
        try {
            assertEquals("d1 leaves",2,BlastPresentationService.getLeaves(d1).size());
            assertEquals("d2 leaves",1,BlastPresentationService.getLeaves(d2).size());
            assertEquals("d3 leaves",1,BlastPresentationService.getLeaves(d3).size());
        } catch (BlastDatabaseException e) {
            fail(e.toString()) ;
        }

    }




    @Test
    public void recursiveIndents(){

        //parent
        String D1 = "d1" ;

        //child1
        String D2 = "d2" ;
        //child of child
        String D3 = "d3" ;

        Database d1 = new Database();
        d1.setName(D1);
        Database d2 = new Database();
        d2.setName(D2);
        Database d3 = new Database();
        d3.setName(D3);

        DatabaseRelationship dr1 = new DatabaseRelationship() ;
        dr1.setChild(d1);
        dr1.setOrder(1);
        Set<DatabaseRelationship> dr1Set = new HashSet<DatabaseRelationship>() ;
        dr1Set.add(dr1) ;
        d1.setChildrenRelationships(dr1Set);


        DatabaseRelationship dr2 = new DatabaseRelationship() ;
        dr2.setParent(d1);
        dr2.setChild(d2);
        dr2.setOrder(3);
        Set<DatabaseRelationship> dr2Set = new HashSet<DatabaseRelationship>() ;
        dr2Set.add(dr2) ;
        d2.setChildrenRelationships(dr2Set);
        d1.setParentRelationships(dr2Set);

        DatabaseRelationship dr3 = new DatabaseRelationship() ;
        dr3.setParent(d2);
        dr3.setChild(d3);
        dr3.setOrder(1);
        Set<DatabaseRelationship> dr3Set = new HashSet<DatabaseRelationship>() ;
        dr3Set.add(dr3) ;
        d3.setChildrenRelationships(dr3Set);
        d2.setParentRelationships(dr3Set);


        Set<Database> databases = new HashSet<Database>() ;
        databases.add(d1) ;
        databases.add(d2) ;
        databases.add(d3) ;

        List<DatabasePresentationBean> returnDatabases = BlastPresentationService.orderDatabasesFromRoot(databases) ;
        assertEquals("Should be three databases",returnDatabases.size(),3) ;
        assertEquals("First database should be dr1",returnDatabases.get(0).getDatabase().getName(),D1) ;
        assertEquals("First database should have indent 0",returnDatabases.get(0).getIndent(),0) ;
        assertEquals("Second database should be dr2",returnDatabases.get(1).getDatabase().getName(),D2) ;
        assertEquals("Second database should have indent 1",returnDatabases.get(1).getIndent(),1) ;
        assertEquals("Third database should be dr3",returnDatabases.get(2).getDatabase().getName(),D3) ;
        assertEquals("Third database should have indent 2",returnDatabases.get(2).getIndent(),2) ;

        assertEquals("d1 children",1,BlastPresentationService.getDirectChildren(d1).size());
        assertEquals("d2 children",1,BlastPresentationService.getDirectChildren(d2).size());
        assertEquals("d3 children",0,BlastPresentationService.getDirectChildren(d3).size());
        try {
            assertEquals("d1 leaves",1,BlastPresentationService.getLeaves(d1).size());
            assertEquals("d2 leaves",1,BlastPresentationService.getLeaves(d2).size());
            assertEquals("d3 leaves",1,BlastPresentationService.getLeaves(d3).size());
        } catch (BlastDatabaseException e) {
            fail(e.toString()) ;
        }

        returnDatabases = BlastPresentationService.processFromChild(d1,true) ;
        assertEquals("Should be three databases",returnDatabases.size(),3) ;
        assertEquals("First database should be dr1",returnDatabases.get(0).getDatabase().getName(),D1) ;
        assertEquals("First database should have indent 0",returnDatabases.get(0).getIndent(),0) ;
        assertEquals("Second database should be dr2",returnDatabases.get(1).getDatabase().getName(),D2) ;
        assertEquals("Second database should have indent 1",returnDatabases.get(1).getIndent(),1) ;
        assertEquals("Third database should be dr3",returnDatabases.get(2).getDatabase().getName(),D3) ;
        assertEquals("Third database should have indent 2",returnDatabases.get(2).getIndent(),2) ;
    }


    @Test
    public void indentsOrderBeforeChildren(){

        // (dr1) -> d1  #1
        //   (dr1_1) -> d1.1 #4
        // (dr2) -> d2  #3 
        //   (dr2_1) -> d2.1
        //   (dr2_2) -> d2.2



        //parent
        String D1 = "d1" ;
        String D1_1 = "d1.1" ;

        //child1
        String D2 = "d2" ;
        String D2_1 = "d2.1" ;
        String D2_2 = "d2.2" ;

        Database d1 = new Database();
        d1.setName(D1);
        Database d1_1 = new Database();
        d1_1.setName(D1_1);
        Database d2 = new Database();
        d2.setName(D2);
        Database d2_1 = new Database();
        d2_1.setName(D2_1);
        Database d2_2 = new Database();
        d2_2.setName(D2_2);

        DatabaseRelationship dr1 = new DatabaseRelationship() ;
        dr1.setChild(d1);
        dr1.setOrder(1);
        Set<DatabaseRelationship> dr1Set = new HashSet<DatabaseRelationship>() ;
        dr1Set.add(dr1) ;
        d1.setChildrenRelationships(dr1Set);


        DatabaseRelationship dr2 = new DatabaseRelationship() ;
        dr2.setChild(d2);
        dr2.setOrder(3);
        Set<DatabaseRelationship> dr2Set = new HashSet<DatabaseRelationship>() ;
        dr2Set.add(dr2) ;
        d2.setChildrenRelationships(dr2Set);

        DatabaseRelationship dr1_1 = new DatabaseRelationship() ;
        dr1_1.setParent(d1);
        dr1_1.setChild(d1_1);
        dr1_1.setOrder(4);
        Set<DatabaseRelationship> dr1_1Set = new HashSet<DatabaseRelationship>() ;
        dr1_1Set.add(dr1_1) ;
        d1_1.setChildrenRelationships(dr1_1Set);
        d1.setParentRelationships(dr1_1Set);

        DatabaseRelationship dr2_1 = new DatabaseRelationship() ;
        dr2_1.setParent(d2);
        dr2_1.setChild(d2_1);
        dr2_1.setOrder(2);
        Set<DatabaseRelationship> dr2_1ChildSet = new HashSet<DatabaseRelationship>() ;
        dr2_1ChildSet.add(dr2_1) ;
        d2_1.setChildrenRelationships(dr2_1ChildSet);

        DatabaseRelationship dr2_2 = new DatabaseRelationship() ;
        dr2_2.setParent(d2);
        dr2_2.setChild(d2_2);
        dr2_2.setOrder(7);
        Set<DatabaseRelationship> dr2_2Set = new HashSet<DatabaseRelationship>() ;
        dr2_2Set.add(dr2_2) ;
        d2_2.setChildrenRelationships(dr2_2Set);

        Set<DatabaseRelationship> dr2ParentSet = new HashSet<DatabaseRelationship>() ;
        dr2ParentSet.add(dr2_1) ;
        dr2ParentSet.add(dr2_2) ;
        d2.setParentRelationships(dr2ParentSet);

        Set<Database> databases = new HashSet<Database>() ;
        databases.add(d1) ;
        databases.add(d1_1) ;
        databases.add(d2) ;
        databases.add(d2_1) ;
        databases.add(d2_2) ;

        List<DatabasePresentationBean> returnDatabases = BlastPresentationService.orderDatabasesFromRoot(databases) ;
        assertEquals("Should be five databases",returnDatabases.size(),5) ;
        assertEquals("First database should be d1",D1,returnDatabases.get(0).getDatabase().getName()) ;
        assertEquals("First database should have indent 0",0,returnDatabases.get(0).getIndent()) ;
        assertEquals("Second database should be d1_1",D1_1,returnDatabases.get(1).getDatabase().getName()) ;
        assertEquals("Second database should have indent 1",1,returnDatabases.get(1).getIndent()) ;


        assertEquals("d1 children",1,BlastPresentationService.getDirectChildren(d1).size());
        assertEquals("d1_1 children",0,BlastPresentationService.getDirectChildren(d1_1).size());
        assertEquals("d2 children",2,BlastPresentationService.getDirectChildren(d2).size());
        assertEquals("d2_1 children",0,BlastPresentationService.getDirectChildren(d2_1).size());
        assertEquals("d2_2 children",0,BlastPresentationService.getDirectChildren(d2_2).size());
        try {
            assertEquals("d1 leaves",1,BlastPresentationService.getLeaves(d1).size());
            assertEquals("d1_1 leaves",1,BlastPresentationService.getLeaves(d1_1).size());
            assertEquals("d2 leaves",2,BlastPresentationService.getLeaves(d2).size());
            assertEquals("d2_1 leaves",1,BlastPresentationService.getLeaves(d2_1).size());
            assertEquals("d2_2 leaves",1,BlastPresentationService.getLeaves(d2_2).size());
        } catch (BlastDatabaseException e) {
            fail(e.toString()) ;
        }

        returnDatabases = BlastPresentationService.processFromChild(d1,true) ;
        assertEquals("Should be two databases",returnDatabases.size(),2) ;
        assertEquals("First database should be d1",D1,returnDatabases.get(0).getDatabase().getName()) ;
        assertEquals("First database should have indent 0",0,returnDatabases.get(0).getIndent()) ;
        assertEquals("Second database should be d1_1",D1_1,returnDatabases.get(1).getDatabase().getName()) ;
        assertEquals("Second database should have indent 1",1,returnDatabases.get(1).getIndent()) ;


        returnDatabases = BlastPresentationService.processFromChild(d2,true) ;
        assertEquals("Should be three databases",returnDatabases.size(),3) ;
        assertEquals("First database should be d2",D2,returnDatabases.get(0).getDatabase().getName()) ;
        assertEquals("First database should have indent 0",0,returnDatabases.get(0).getIndent()) ;
        assertEquals("Second database should be d2_1",D2_1,returnDatabases.get(1).getDatabase().getName()) ;
        assertEquals("Second database should have indent 1",1,returnDatabases.get(1).getIndent()) ;
        assertEquals("Third database should be d2_2",D2_2,returnDatabases.get(2).getDatabase().getName()) ;
        assertEquals("Third database should have indent 1",1,returnDatabases.get(2).getIndent()) ;
    }



    @Test
    public void indentsOrder2Deep(){

        //parent
        String D0 = "d0" ;
        String D0_0 = "d0.0" ;
        String D0_0_0 = "d0.0.0" ;
        String D0_1 = "d0.1" ;
        String D0_1_0 = "d0.1.0" ;


        Database d0 = new Database();
        d0.setName(D0);
        Database d0_0 = new Database();
        d0_0.setName(D0_0);
        Database d0_0_0 = new Database();
        d0_0_0.setName(D0_0_0);
        Database d0_1 = new Database();
        d0_1.setName(D0_1);
        Database d0_1_0 = new Database();
        d0_1_0.setName(D0_1_0);

        DatabaseRelationship dr0 = new DatabaseRelationship() ;
        dr0.setChild(d0);
        dr0.setOrder(0);
        Set<DatabaseRelationship> dr0ChildSet = new HashSet<DatabaseRelationship>() ;
        dr0ChildSet.add(dr0) ;
        d0.setChildrenRelationships(dr0ChildSet);

        DatabaseRelationship dr1 = new DatabaseRelationship() ;
        dr1.setChild(d0_0);
        dr1.setParent(d0);
        dr1.setOrder(0);
        Set<DatabaseRelationship> dr1ChildSet = new HashSet<DatabaseRelationship>() ;
        dr1ChildSet.add(dr1) ;
        d0_0.setChildrenRelationships(dr1ChildSet);

        DatabaseRelationship dr2 = new DatabaseRelationship() ;
        dr2.setChild(d0_1);
        dr2.setParent(d0);
        dr2.setOrder(1);
        Set<DatabaseRelationship> dr2ChildSet = new HashSet<DatabaseRelationship>() ;
        dr2ChildSet.add(dr2) ;
        d0_1.setChildrenRelationships(dr2ChildSet);

        // add parent relationships for d0
        Set<DatabaseRelationship> dr0ParentSet = new HashSet<DatabaseRelationship>() ;
        dr0ParentSet.add(dr1);
        dr0ParentSet.add(dr2);
        d0.setParentRelationships(dr0ParentSet);


        DatabaseRelationship dr3 = new DatabaseRelationship() ;
        dr3.setParent(d0_0);
        dr3.setChild(d0_0_0);
        dr3.setOrder(0);
        Set<DatabaseRelationship> dr3ChildSet = new HashSet<DatabaseRelationship>() ;
        dr3ChildSet.add(dr3) ;
        d0_0_0.setChildrenRelationships(dr3ChildSet);
        d0_0.setParentRelationships(dr3ChildSet);

        DatabaseRelationship dr4 = new DatabaseRelationship() ;
        dr4.setParent(d0_1);
        dr4.setChild(d0_1_0);
        dr4.setOrder(0);
        Set<DatabaseRelationship> dr4ChildSet = new HashSet<DatabaseRelationship>() ;
        dr4ChildSet.add(dr4) ;
        d0_1_0.setChildrenRelationships(dr4ChildSet);
        d0_1.setParentRelationships(dr4ChildSet);


        Set<Database> databases = new HashSet<Database>() ;
        databases.add(d0) ;
        databases.add(d0_0) ;
        databases.add(d0_0_0) ;
        databases.add(d0_1) ;
        databases.add(d0_1_0) ;

        List<DatabasePresentationBean> returnDatabases = BlastPresentationService.orderDatabasesFromRoot(databases) ;
        assertEquals("Should be five databases",returnDatabases.size(),5) ;
        assertEquals("First database should be d1",D0,returnDatabases.get(0).getDatabase().getName()) ;
        assertEquals("First database should have indent 0",0,returnDatabases.get(0).getIndent()) ;
        assertEquals("Second database should be d0_0",D0_0,returnDatabases.get(1).getDatabase().getName()) ;
        assertEquals("Second database should have indent 1",1,returnDatabases.get(1).getIndent()) ;
        assertEquals("Third database should be d0_0_0",D0_0_0,returnDatabases.get(2).getDatabase().getName()) ;
        assertEquals("Third database should have indent 2",2,returnDatabases.get(2).getIndent()) ;
        assertEquals("Fourth database should be d0_1",D0_1,returnDatabases.get(3).getDatabase().getName()) ;
        assertEquals("Fourth database should have indent 1",1,returnDatabases.get(3).getIndent()) ;
        assertEquals("Fifth database should be d0_1_0",D0_1_0,returnDatabases.get(4).getDatabase().getName()) ;
        assertEquals("Fifth database should have indent 2",2,returnDatabases.get(4).getIndent()) ;

        assertEquals("d0 children",2,BlastPresentationService.getDirectChildren(d0).size());
        assertEquals("d0_0 children",1,BlastPresentationService.getDirectChildren(d0_0).size());
        assertEquals("d0_0_0 children",0,BlastPresentationService.getDirectChildren(d0_0_0).size());
        assertEquals("d0_1 children",1,BlastPresentationService.getDirectChildren(d0_1).size());
        assertEquals("d0_1_0 children",0,BlastPresentationService.getDirectChildren(d0_1_0).size());
        try {
            assertEquals("d0 leaves",2,BlastPresentationService.getLeaves(d0).size());
            assertEquals("d0_0 leaves",1,BlastPresentationService.getLeaves(d0_0).size());
            assertEquals("d0_0_0 leaves",1,BlastPresentationService.getLeaves(d0_0_0).size());
            assertEquals("d0_1 leaves",1,BlastPresentationService.getLeaves(d0_1).size());
            assertEquals("d0_1_0 leaves",1,BlastPresentationService.getLeaves(d0_1_0).size());
        } catch (BlastDatabaseException e) {
            fail(e.toString()) ;
        }

        returnDatabases = BlastPresentationService.processFromChild(d0,true) ;
        assertEquals("Should be five databases",returnDatabases.size(),5) ;
        assertEquals("First database should be d1",D0,returnDatabases.get(0).getDatabase().getName()) ;
        assertEquals("First database should have indent 0",0,returnDatabases.get(0).getIndent()) ;
        assertEquals("Second database should be d0_0",D0_0,returnDatabases.get(1).getDatabase().getName()) ;
        assertEquals("Second database should have indent 1",1,returnDatabases.get(1).getIndent()) ;
        assertEquals("Third database should be d0_0_0",D0_0_0,returnDatabases.get(2).getDatabase().getName()) ;
        assertEquals("Third database should have indent 2",2,returnDatabases.get(2).getIndent()) ;
        assertEquals("Fourth database should be d0_1",D0_1,returnDatabases.get(3).getDatabase().getName()) ;
        assertEquals("Fourth database should have indent 1",1,returnDatabases.get(3).getIndent()) ;
        assertEquals("Fifth database should be d0_1_0",D0_1_0,returnDatabases.get(4).getDatabase().getName()) ;
        assertEquals("Fifth database should have indent 2",2,returnDatabases.get(4).getIndent()) ;
    }



}
