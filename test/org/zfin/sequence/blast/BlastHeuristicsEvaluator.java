package org.zfin.sequence.blast;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;
import org.hibernate.SessionFactory;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.TestConfiguration;
import org.zfin.sequence.blast.presentation.XMLBlastBean;
import org.zfin.repository.RepositoryFactory;
import org.apache.log4j.Logger;
import org.apache.commons.math.stat.regression.SimpleRegression;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;

/**
 * See fogbugz 3970.
 * For M accessions and P databases, need to get
 *
 */
public class BlastHeuristicsEvaluator {

    private static final Logger logger = Logger.getLogger(BlastHeuristicsEvaluator.class) ;

    private static List<String> accessionList = new ArrayList<String>() ;
    // maps the accessions to their sequence
    private static Map<String,String> mappedAccessions = new HashMap<String,String>() ;
    private static List<Database> databaseList = new ArrayList<Database>() ;

    static{
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

        if (sessionFactory == null) {
            new HibernateSessionCreator(false, TestConfiguration.getHibernateConfiguration());
        }


    }

    @Before
    public void setUp() {
        TestConfiguration.configure();
        TestConfiguration.initApplicationProperties();

        accessionList.clear();
        accessionList.add("NM_131304") ;
        accessionList.add("X63183") ;
        accessionList.add("OTTDART00000023086") ;

        databaseList.clear();
        databaseList.add(RepositoryFactory.getBlastRepository().getDatabase(Database.AvailableAbbrev.LOADEDMICRORNAMATURE)) ; // < 1K
        databaseList.add(RepositoryFactory.getBlastRepository().getDatabase(Database.AvailableAbbrev.VEGA_ZFIN)) ;  // ~20K
        databaseList.add(RepositoryFactory.getBlastRepository().getDatabase(Database.AvailableAbbrev.ZFINGENESWITHEXPRESSION)) ; // ~40K
        databaseList.add(RepositoryFactory.getBlastRepository().getDatabase(Database.AvailableAbbrev.GBK_ZF_DNA)) ; // ~200K
        databaseList.add(RepositoryFactory.getBlastRepository().getDatabase(Database.AvailableAbbrev.GBK_EST_ZF)) ; // ~2000K
        databaseList.add(RepositoryFactory.getBlastRepository().getDatabase(Database.AvailableAbbrev.WGS_ZF)) ; // ~6000K
    }

    @After
    public void closeSession() {
        HibernateUtil.closeSession();
    }

    @Test
    public void testGetAverageTime(){
        SplitTiming splitTiming = new SplitTiming(2) ;
        splitTiming.addTime((long) 100);
        assertEquals(100f,splitTiming.getAverageTime()) ;
        splitTiming.addTime((long) 30f);
        assertEquals(65f,splitTiming.getAverageTime()) ;
        splitTiming.addTime((long) 10f);
        assertEquals(46.666f,splitTiming.getAverageTime(),0.001f) ;
    }

    @Test
    public void testFindMin(){
        DatabaseSample databaseSample = new DatabaseSample(100) ;
        SplitTiming splitTiming1 = new SplitTiming(1) ;
        splitTiming1.addTime((long) 100);
        splitTiming1.addTime((long) 30f);
        splitTiming1.addTime((long) 10f);
        databaseSample.add(splitTiming1) ;
        SplitTiming splitTiming2 = new SplitTiming(2) ;
        splitTiming2.addTime((long) 100);
        splitTiming2.addTime((long) 50f);
        splitTiming2.addTime((long) 60f);
        databaseSample.add(splitTiming2) ;
        SplitTiming splitTiming3 = new SplitTiming(3) ;
        splitTiming3.addTime((long) 10);
        splitTiming3.addTime((long) 5f);
        splitTiming3.addTime((long) 6f);
        databaseSample.add(splitTiming3) ;

        assertEquals(3,databaseSample.findMinSplit()) ;
    }



    /**
     * This class holds the timings for a set of accessions.
     */
    private class SplitTiming implements Comparable<SplitTiming>{
        private int split ;
        private List<Long> timings = new ArrayList<Long>() ;

        public SplitTiming(int split){
            this.split = split ;
        }

        public int getSplit() {
            return split;
        }

        public void addTime(Long time){
            timings.add(time) ;
        }

        public float getAverageTime(){
            Float avgTime = 0f ;
            for(Long timing : timings){
                avgTime += timing ;
            }
            return avgTime / (float) timings.size() ;
        }

        public int compareTo(SplitTiming splitTiming) {
            return split - splitTiming.getSplit() ;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("SplitTiming");
            sb.append("{split=").append(split);
            sb.append(", timings=").append(timings);
            sb.append(", avg=").append(getAverageTime());
            sb.append('}');
            return sb.toString();
        }
    }

    /**
     *
     */
    private class DatabaseSample extends TreeSet<SplitTiming> implements Comparable<DatabaseSample>{
        private int databaseSize ;

        public DatabaseSample(int databaseSize){
            this.databaseSize = databaseSize ;
        }

        public int getDatabaseSize() {
            return databaseSize;
        }

        public int findMinSplit(){
            int minSplit = 1 ;
            float minTime = Float.MAX_VALUE ;
            for(SplitTiming splitTiming : this){
                if(splitTiming.getAverageTime() < minTime){
                    minTime = splitTiming.getAverageTime() ;
                    minSplit = splitTiming.getSplit() ;
                }
            }
            return minSplit ;
        }


        public int compareTo(DatabaseSample o) {
            return databaseSize - o.getDatabaseSize();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder() ;
            sb.append("DatabaseSample{").append("databaseSize=").append(databaseSize) ;


            sb.append("splitTiming={") ;
            for(SplitTiming splitTiming : this){
                sb.append(splitTiming.toString()) ;
            }
            sb.append("}") ;
            sb.append("}") ;

            return sb.toString() ;
        }
    }

    @Test
    public void testCacheAccessionSequences(){
        cacheAccessionSequences();
        assertEquals(3,accessionList.size());
        assertEquals(mappedAccessions.keySet().size(),accessionList.size());
        assertTrue(CollectionUtils.isEqualCollection(mappedAccessions.keySet(),accessionList));
        for(String string : mappedAccessions.values() ){
            assertNotNull(string);
        }
    }

    private void cacheAccessionSequences() {
        for(String accession: accessionList){
            String sequenceData = MultipleBlastServerService.getSequencesForAccessionAndReferenceDBs(accession).get(0).getFormattedData();
            mappedAccessions.put(accession, sequenceData) ;
        }
    }

    /**
     * For each database we cache the query and accessions so there should be no penalty for going first.
     */
    public void cacheDBQueries(){

        logger.info( "START CACHING"  );

        cacheAccessionSequences() ;

        if(false==CollectionUtils.isEqualCollection(mappedAccessions.keySet(),accessionList)){
            logger.fatal("Failed to return an equivalent collection");
            return ;
        }

        //for each database
        //for each accession
        // take sampling of times of splits from 1 to max_split (10)
        List<Database> actualDatabaseList = new ArrayList<Database>() ;
        for(Database database : databaseList){

            actualDatabaseList.clear();
            actualDatabaseList.add(database) ;

            try {
                int split = 1 ;
                for(String accession: accessionList){
                    XMLBlastBean xmlBlastBean = new XMLBlastBean() ;
                    xmlBlastBean.setProgram("blastn");
                    xmlBlastBean.setQueryType(XMLBlastBean.QueryTypes.FASTA.toString());
                    xmlBlastBean.setQuerySequence(mappedAccessions.get(accession));
                    xmlBlastBean.setExpectValue(1.0E-25);
                    xmlBlastBean.setActualDatabaseTargets(actualDatabaseList);
                    MountedWublastBlastService.getInstance().setBlastResultFile(xmlBlastBean);

                    BlastHeuristicCollection blastHeuristicCollection = (new SettableBlastHeuristicFactory(split,true)).createBlastHeuristics(xmlBlastBean) ;
                    BlastDistributedQueryThread blastDistributedQueryThread = new BlastDistributedQueryThread(xmlBlastBean,blastHeuristicCollection) ;

                    blastDistributedQueryThread.run();
                }
            } catch (Exception e) {
                fail(e.toString());
            }
        }

        logger.info( "END CACHING"  );

    }



    @Test
    public void testGettingWorse(){
        DatabaseSample databaseSample = new DatabaseSample(100) ;
        SplitTiming splitTiming1 = new SplitTiming(1) ;
        splitTiming1.addTime((long) 100);
        databaseSample.add(splitTiming1) ;
        assertFalse(gettingWorse(databaseSample)) ;

        SplitTiming splitTiming2 = new SplitTiming(2) ;
        splitTiming2.addTime((long) 200);
        databaseSample.add(splitTiming2) ;
        assertFalse(gettingWorse(databaseSample)) ;

        SplitTiming splitTiming3 = new SplitTiming(3) ;
        splitTiming3.addTime((long) 50);
        databaseSample.add(splitTiming3) ;
        assertFalse(gettingWorse(databaseSample)) ;

        SplitTiming splitTiming4 = new SplitTiming(4) ;
        splitTiming4.addTime((long) 500);
        databaseSample.add(splitTiming4) ;
        assertTrue(gettingWorse(databaseSample)) ;
    }


    public boolean gettingWorse(DatabaseSample databaseSample){
        if(databaseSample.size()<3){
            return false;
        }
        else{
            SplitTiming[] splitTimings = databaseSample.toArray(new SplitTiming[databaseSample.size()]) ;
            return splitTimings[splitTimings.length-1].getAverageTime() > splitTimings[splitTimings.length-2].getAverageTime();
        }
    }


    @Test
    public void evaluateBlastPerformance(){

        final int MAX_SPLIT = 10 ;

        List<Database> actualDatabaseList = new ArrayList<Database>() ;

        Set<DatabaseSample> databaseSamples = new TreeSet<DatabaseSample>() ;


        cacheDBQueries();


        //for each database
        //for each accession
        // take sampling of times of splits from 1 to max_split (10)
        for(Database database : databaseList){

            actualDatabaseList.clear();
            actualDatabaseList.add(database) ;

            DatabaseSample databaseSample = null;
            try {
                databaseSample = new DatabaseSample(WebHostDatabaseStatisticsCache.getInstance().getDatabaseStatistics(database).getNumSequences());

//                for(int split = 1 ; split < MAX_SPLIT && false==gettingWorse(databaseSample); ++split) {
                for(int split = 1 ; split < MAX_SPLIT ; ++split) {
                    SplitTiming splitTiming = new SplitTiming(split) ;
                    for(String accession: accessionList){
                        XMLBlastBean xmlBlastBean = new XMLBlastBean() ;
                        xmlBlastBean.setProgram("blastn");
                        xmlBlastBean.setQueryType(XMLBlastBean.QueryTypes.FASTA.toString());
                        xmlBlastBean.setQuerySequence(mappedAccessions.get(accession));
                        xmlBlastBean.setExpectValue(1.0E-25);
                        xmlBlastBean.setActualDatabaseTargets(actualDatabaseList);
                        MountedWublastBlastService.getInstance().setBlastResultFile(xmlBlastBean);

                        BlastHeuristicCollection blastHeuristicCollection = (new SettableBlastHeuristicFactory(split,true)).createBlastHeuristics(xmlBlastBean) ;
                        BlastDistributedQueryThread blastDistributedQueryThread = new BlastDistributedQueryThread(xmlBlastBean,blastHeuristicCollection) ;

                        long startTime = System.currentTimeMillis() ;
                        blastDistributedQueryThread.run();
                        long endTime = System.currentTimeMillis() ;
                        splitTiming.addTime(endTime-startTime);
                    }
                    databaseSample.add(splitTiming) ;
                }
            } catch (Exception e) {
                fail(e.toString());
            }
            databaseSamples.add(databaseSample) ;
        }



        for(DatabaseSample databaseSample : databaseSamples){
            logger.info(databaseSample.toString());
        }

        logger.info("Split" + ", "+ "Size");

        SimpleRegression simpleRegression = new SimpleRegression() ;

        for(DatabaseSample databaseSample : databaseSamples){
            logger.info(databaseSample.findMinSplit() + ", "+ databaseSample.getDatabaseSize());
            simpleRegression.addData(databaseSample.findMinSplit(),databaseSample.getDatabaseSize());
        }

        logger.info("intercept: " + simpleRegression.getIntercept()) ;
        logger.info("slope (size / split) : " + simpleRegression.getSlope()) ;
        logger.info("confidence: " + simpleRegression.getSlopeStdErr()) ;
    }
}
