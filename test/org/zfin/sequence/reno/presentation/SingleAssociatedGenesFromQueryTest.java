package org.zfin.sequence.reno.presentation;

import org.apache.log4j.Logger;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerType;
import org.zfin.marker.MarkerRelationship;
import org.zfin.sequence.reno.RunCandidate;
import org.zfin.sequence.reno.Candidate;
import org.zfin.sequence.blast.Query;
import org.zfin.sequence.blast.Hit;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.sequence.Accession;
import org.zfin.sequence.DBLink;
import org.zfin.TestConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import static org.junit.Assert.* ;

import java.util.HashSet;
import java.util.Set;
import java.util.Collection;

/**
 * Requirements based on errors in FogBugz 2373.
 * tests RunCandidate.getAllSingleAssociatedGenesFromQueries
 * Only markers that have a single small segment encode relationship show up at least once should be visible.
 * <ul>
 * <li>est encoded by 1 genes in 1 hit (visible)
 * <li>est encoded by 2 genes in 1 hit (hide)
 * <li>est encoded by 2 genes in 1 hit, but 1 gene in another (visible)
 * <li>est encoded by 1 gene in 1 hit, and 1 gene in another hit  (visible once)
 * <li>est encoded by 2 genes in 1 hit, but 2 genes in another (hide)
 * <li>est hybridized by in 1 gene (hide)
 * <li>est hybridized by in 1 gene, encoded in 1 gene in another (visible)
 * </ul>
 *
 *  @link http://zfinwinserver1/FogBUGZ/default.asp?W86
 *  @link http://zfinwinserver1/FogBUGZ/default.asp?2373
 */
public class SingleAssociatedGenesFromQueryTest {
    private final Logger logger = Logger.getLogger(SingleAssociatedGenesFromQueryTest.class) ;

    private RunCandidate runCandidate;
    private Marker gene1;
    private Query query = new Query();
    private Hit hit1 = new Hit() ;
    private Hit hit2 = new Hit();
    private Accession hit1Accession = new Accession() ;
    private Accession hit2Accession = new Accession() ;
	private Set<Hit> blastHits = new HashSet<Hit>() ;
    private Set<MarkerRelationship> est1SecondMarkerRelationships = new HashSet<MarkerRelationship>() ;
    private Set<MarkerRelationship> est2SecondMarkerRelationships = new HashSet<MarkerRelationship>() ;
	private MarkerDBLink gene1DBLink = new MarkerDBLink();
	private Set<MarkerDBLink> gene1DBLinks = new HashSet<MarkerDBLink>();
	private Set<DBLink> est1DBLinks = new HashSet<DBLink>() ;
	private MarkerDBLink est1HitDBLink = new MarkerDBLink() ;
	private Set<MarkerDBLink> est1MarkerDBLinks = new HashSet<MarkerDBLink>() ;
	private MarkerDBLink est2HitDBLink = new MarkerDBLink() ;
	private Set<DBLink> est2DBLinks = new HashSet<DBLink>() ;
	private Set<MarkerDBLink> est2MarkerDBLinks = new HashSet<MarkerDBLink>() ;
	private MarkerRelationship est1Gene1MarkerRelationship= new MarkerRelationship() ;
	private MarkerRelationship est1Gene2MarkerRelationship= new MarkerRelationship() ;
	private MarkerRelationship est2Gene2MarkerRelationship= new MarkerRelationship() ;
	private MarkerRelationship est2Gene1MarkerRelationship= new MarkerRelationship() ;


    @Before
    public void setUp() {
        TestConfiguration.initApplicationProperties();


        MarkerType geneType = new MarkerType();
        geneType.setType(Marker.Type.GENE);
        Set<Marker.TypeGroup> groups = new HashSet<Marker.TypeGroup>();
        groups.add(Marker.TypeGroup.GENEDOM);
        geneType.setTypeGroups(groups);

        gene1 = new Marker();
        gene1.setZdbID("ZDB-GENE-081507-1");
        gene1.setAbbreviation("fgf8");
        gene1.setName("fibroblast growth factor 8 a");
        gene1.setMarkerType(geneType);


        Marker gene2=new Marker();
        gene2.setAbbreviation("gene2");
        gene2.setZdbID("ZDB-GENE-5678-1");
        geneType.setTypeGroups(groups);
        gene2.setMarkerType(geneType);


        // set est types
		MarkerType estType = new MarkerType() ;
		estType.setType(Marker.Type.EST) ;
		Set<Marker.TypeGroup> typeGroups = new HashSet<Marker.TypeGroup>() ;
		estType.setTypeGroups(typeGroups);

        Marker est1=new Marker();
        est1.setAbbreviation("est1");
        est1.setZdbID("ZDB-EST-1111-1");
        est1.setMarkerType(estType);

        Marker est2=new Marker();
        est2.setAbbreviation("est2");
        est2.setZdbID("ZDB-EST-2222-1");
        est2.setMarkerType(estType);


        runCandidate = new RunCandidate();
        Candidate candidate = new Candidate() ;
        runCandidate.setCandidate(candidate);


        gene1DBLink.setMarker(gene1);
        gene1DBLinks.add(gene1DBLink);
        est1HitDBLink.setMarker(est1);
        est1DBLinks.add(est1HitDBLink) ;
        est1MarkerDBLinks.add(est1HitDBLink) ;
        est2HitDBLink.setMarker(est2);
        est2DBLinks.add(est2HitDBLink) ;
        est2MarkerDBLinks.add(est2HitDBLink) ;



        // setup marker relations
        est1Gene1MarkerRelationship.setFirstMarker(gene1);
        est1Gene1MarkerRelationship.setSecondMarker(est1);
        est1Gene1MarkerRelationship.setType(MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT);

        est1Gene2MarkerRelationship.setFirstMarker(gene2);
        est1Gene2MarkerRelationship.setSecondMarker(est1);
        est1Gene2MarkerRelationship.setType(MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT);

        // est2
        est2Gene2MarkerRelationship.setFirstMarker(gene2);
        est2Gene2MarkerRelationship.setSecondMarker(est2);
        est2Gene2MarkerRelationship.setType(MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT);


        est2Gene1MarkerRelationship.setFirstMarker(gene1);
        est2Gene1MarkerRelationship.setSecondMarker(est2);
        est2Gene1MarkerRelationship.setType(MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT);


		// implied, but not necessary
        est1.setSecondMarkerRelationships(est1SecondMarkerRelationships);
        est2.setSecondMarkerRelationships(est2SecondMarkerRelationships);


        Accession geneAccession = new Accession();
        geneAccession.setNumber("ACC:GENE1");
        geneAccession.setBlastableMarkerDBLinks(gene1DBLinks);

        hit2Accession.setNumber("ACC:EST2");
        hit2Accession.setDbLinks( est2DBLinks);

        hit2Accession.setBlastableMarkerDBLinks(est2MarkerDBLinks  );
        hit2.setTargetAccession(hit2Accession);
        hit2.setQuery(query);

        query.setAccession(geneAccession);

        hit1Accession.setNumber("ACC:EST1");
        hit1Accession.setDbLinks( est1DBLinks);

        hit1Accession.setBlastableMarkerDBLinks(  est1MarkerDBLinks);
        hit1.setQuery(query);
        hit1.setTargetAccession(hit1Accession);
        blastHits.add(hit1) ;
        query.setBlastHits(blastHits);


        Set<Query> queries = new HashSet<Query>();
        queries.add(query);
        
        runCandidate.setCandidateQueries(queries);

    }


	@After
    public void cleanUp(){
		est1SecondMarkerRelationships.clear() ; 
		est2SecondMarkerRelationships.clear() ; 
		blastHits.clear() ; 
	}

	@Test
	public void estEncodedByOneGeneIsVisible(){
        // defined, but not necessary

		est1SecondMarkerRelationships.add(est1Gene1MarkerRelationship) ;

//        * <li> START est encoded by 1 genes in 1 hit (visible)
        logger.debug("est encoded by 1 genes in 1 hit (visible) getAllSingleAssociatedGenesFromQueries");
        assertEquals(1,runCandidate.getAllSingleAssociatedGenesFromQueries().size());
//        * <li> END est encoded by 1 genes in 1 hit (visible)

	}

	@Test
    public void estEncodedByTwoGenesHidden(){
//      * <li> START est encoded by 2 genes in 1 hit (hide)
        // est1 is encoded by 2 genes
		est1SecondMarkerRelationships.add(est1Gene1MarkerRelationship) ;
		est1SecondMarkerRelationships.add(est1Gene2MarkerRelationship) ;
        // add this marker relationship to secondMarkerRelationship set (already set)

        // need to add gene2 encoding, though this is unnecessary 

        logger.debug("est encoded by 2 genes in 1 hit (hide) getAllSingleAssociatedGenesFromQueries");
        assertEquals(0,runCandidate.getAllSingleAssociatedGenesFromQueries().size());
//      * <li> END est encoded by 2 genes in 1 hit (hide)
	}

	@Test
	public void estEncodedByTwoGenesOtherEstByOneGeneVisible(){
		// setup est1 being encoded by two genes
		est1SecondMarkerRelationships.add(est1Gene1MarkerRelationship) ;
		est1SecondMarkerRelationships.add(est1Gene2MarkerRelationship) ;

		// setup est2 being encoded by one gene, "gene2"
        est2SecondMarkerRelationships.add(est2Gene2MarkerRelationship) ; 
        blastHits.add(hit2) ;


        logger.debug("est encoded by 2 genes in 1 hit, but 1 gene in another (visible)");
        Collection<Marker> associatedGenes = runCandidate.getAllSingleAssociatedGenesFromQueries() ;
        assertEquals(1,associatedGenes.size());
        assertEquals("gene2",associatedGenes.iterator().next().getAbbreviation());
	}


	@Test
	public void twoHitsEncodedTwoGenesEachHidden(){
		// setup est1 being encoded by two genes
		est1SecondMarkerRelationships.add(est1Gene1MarkerRelationship) ;
		est1SecondMarkerRelationships.add(est1Gene2MarkerRelationship) ;

		// setup est2 being encoded by one gene, "gene2"
        est2SecondMarkerRelationships.add(est2Gene2MarkerRelationship) ; 
        est2SecondMarkerRelationships.add(est2Gene1MarkerRelationship) ;

        blastHits.add(hit2) ;

        logger.debug("est encoded by 2 genes in 1 hit, but 2 gene in another (hidden)");
        assertEquals(0,runCandidate.getAllSingleAssociatedGenesFromQueries().size());
	}

	@Test
	public void differentEstsEncodeSameGeneVisible(){
        est1SecondMarkerRelationships.add(est1Gene2MarkerRelationship) ;
        est2SecondMarkerRelationships.add(est2Gene2MarkerRelationship) ;


        blastHits.add(hit2) ;

        logger.debug("est encoded by 1 gene in 1 hit, and same gene in another hit: hits have different EST(visible once)");
        Collection<Marker> associatedGenes = runCandidate.getAllSingleAssociatedGenesFromQueries() ;
        assertEquals(1,associatedGenes.size());
        assertEquals("gene2",associatedGenes.iterator().next().getAbbreviation());
	}

	@Test
	public void differentEstsEncodeDifferentGeneCount2(){
        est1SecondMarkerRelationships.add(est1Gene1MarkerRelationship) ;
        est2SecondMarkerRelationships.add(est2Gene2MarkerRelationship) ;
        blastHits.add(hit2) ;
        logger.debug("est encoded by 1 gene in 1 hit, and another gene in another hit: hits have different EST(visible twice)");
        assertEquals(2,runCandidate.getAllSingleAssociatedGenesFromQueries().size());

	}

	@Test
	public void differentHitsWithSameEstVisible(){
        est1SecondMarkerRelationships.add(est1Gene1MarkerRelationship) ;
        est2SecondMarkerRelationships.add(est2Gene2MarkerRelationship) ;
        blastHits.add(hit2) ;
        hit2.setTargetAccession(hit1Accession);
        logger.debug("2 hits with same EST(visible once)");
        assertEquals(1,runCandidate.getAllSingleAssociatedGenesFromQueries().size());
	}

	@Test
	public void estHybridizedInOneGeneHide(){
        est2Gene1MarkerRelationship.setType(MarkerRelationship.Type.GENE_HYBRIDIZED_BY_SMALL_SEGMENT);
        est2SecondMarkerRelationships.add(est2Gene1MarkerRelationship) ;
        logger.debug("est hybridized by in 1 gene (hide)");
        assertEquals(0,runCandidate.getAllSingleAssociatedGenesFromQueries().size());
	}

	@Test
	public void estHybridizedInOneGeneEncodedInAnotherVisible(){

        est1SecondMarkerRelationships.add(est1Gene1MarkerRelationship) ; // encoded

        est2Gene1MarkerRelationship.setType(MarkerRelationship.Type.GENE_HYBRIDIZED_BY_SMALL_SEGMENT);
        est2SecondMarkerRelationships.add(est2Gene1MarkerRelationship) ;
		blastHits.add(hit2) ; 
        logger.debug("est hybridized by in 1 gene (hide)");
        Collection<Marker> associatedGenes = runCandidate.getAllSingleAssociatedGenesFromQueries() ;
        assertEquals(1,associatedGenes.size());
        assertEquals(gene1.getAbbreviation(),associatedGenes.iterator().next().getAbbreviation());

	}

}
