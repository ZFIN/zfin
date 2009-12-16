package org.zfin.orthology;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.criteria.ZfinCriteria;
import org.zfin.orthology.repository.HibernateOrthologyRepository;
import org.zfin.util.FilterType;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test for utiltity methods in the repository class.
 */
public class OrthologyRepositoryTest {

    @Before
    public void setUp() {
        TestConfiguration.configure();
    }

    @Test
    public void getGeneWhereClauseZfOnly() {
        HibernateOrthologyRepository ori = new HibernateOrthologyRepository();
        StringBuilder sb = new StringBuilder();
        ori.getSpeciesJoinWhereClause(getSpeciesCriteriaZebrafishOnly(), sb, false);
        assertEquals("Gene join Where clause", "", sb.toString());
    }

    @Test
    public void getGeneWhereClauseZfAndHuman() {
        HibernateOrthologyRepository ori = new HibernateOrthologyRepository();
        StringBuilder sb = new StringBuilder();
        ori.getSpeciesJoinWhereClause(getSpeciesCriteriaZebrafishAndHuman(), sb, false);
        assertEquals("Gene Join Where clause", "Zebrafish.geneID=Human.geneID", sb.toString());
    }

    @Test
    public void getGeneWhereClauseZfAndHumanOuterJoin() {
        HibernateOrthologyRepository ori = new HibernateOrthologyRepository();
        StringBuilder sb = new StringBuilder();
        ori.getSpeciesOuterJoinWhereClause(getSpeciesCriteriaZebrafishAndHuman(), sb);
        assertEquals("Gene Join Where clause", "Zebrafish.species= 'Zebrafish' AND Human.species= 'Human'", sb.toString());
    }

    @Test
    public void getGeneWhereClauseZfAndHumanAndMouse() {
        HibernateOrthologyRepository ori = new HibernateOrthologyRepository();
        StringBuilder sb = new StringBuilder();
        ori.getSpeciesJoinWhereClause(getSpeciesCriteriaZebrafishAndHumanAndMouse(), sb, false);
        assertEquals("Gene Join Where clause", "Zebrafish.geneID=Human.geneID AND Zebrafish.geneID=Mouse.geneID", sb.toString());
    }

    @Test
    public void createGeneSymbolWhereClauseZebrafish() {
        HibernateOrthologyRepository ori = new HibernateOrthologyRepository();
        StringBuilder sb = new StringBuilder();
        ori.createGeneSymbolWhereClause(getZebrafishSpeciesCriteria(), sb, false);
        assertEquals("Gene Join Where clause", " AND upper(Zebrafish.symbol) like 'FG%'", sb.toString());
    }

    @Test
    public void createGeneSymbolWhereClauseHuman() {
        HibernateOrthologyRepository ori = new HibernateOrthologyRepository();
        StringBuilder sb = new StringBuilder();
        ori.createGeneSymbolWhereClause(getHumanSpeciesCriteria(), sb, false);
        assertEquals("Gene Join Where clause", " AND upper(Human.symbol) like '%UY%'", sb.toString());
    }

    @Test
    public void createGeneSymbolWhereClauseMouse() {
        HibernateOrthologyRepository ori = new HibernateOrthologyRepository();
        StringBuilder sb = new StringBuilder();
        ori.createGeneSymbolWhereClause(getMouseSpeciesCriteria(), sb, false);
        assertEquals("Gene Join Where clause", " AND upper(Mouse.symbol) = 'WQ'", sb.toString());
    }

    @Test
    public void createGeneSymbolWhereClauseFly() {
        HibernateOrthologyRepository ori = new HibernateOrthologyRepository();
        StringBuilder sb = new StringBuilder();
        ori.createGeneSymbolWhereClause(getFlySpeciesCriteria(), sb, false);
        assertEquals("Gene Join Where clause", " AND upper(Fly.symbol) like '%RTD5'", sb.toString());
    }

    // Single chromosome: test chromosome = 19
    @Test
    public void createChromsomeWhereClauseZebrafish() {
        HibernateOrthologyRepository ori = new HibernateOrthologyRepository();
        StringBuilder sb = new StringBuilder();
        ZfinCriteria criteria = new ZfinCriteria();
        criteria.setOrRelationship(false);
        ori.createCromosomeWhereClause(getZebrafishSpeciesCriteria(), sb, criteria);
        assertEquals("Chromosome Where clause", " AND Zebrafish.chromosome = '19'", sb.toString());
    }

    // No chromosome: no clause
    @Test
    public void createChromsomeWhereClauseZebrafishNoChromsome() {
        HibernateOrthologyRepository ori = new HibernateOrthologyRepository();
        StringBuilder sb = new StringBuilder();
        ZfinCriteria criteria = new ZfinCriteria();
        criteria.setOrRelationship(false);
        ori.createCromosomeWhereClause(getZebrafishSpeciesCriteriaWithoutChromosome(), sb, criteria);
        assertEquals("Chromosome Where clause", "", sb.toString());
    }

    // No chromosome: no clause
    @Test
    public void createChromsomeWhereClauseHumanNoChromosome() {
        HibernateOrthologyRepository ori = new HibernateOrthologyRepository();
        StringBuilder sb = new StringBuilder();
        ZfinCriteria criteria = new ZfinCriteria();
        criteria.setOrRelationship(false);
        ori.createCromosomeWhereClause(getHumanSpeciesCriteriaNoChromosome(), sb, criteria);
        assertEquals("Chromosome Where clause", "", sb.toString());
    }

    // multiple chromosomes of a list: test chromosome in (4,7,22)
    @Test
    public void createChromsomeWhereClauseHuman() {
        HibernateOrthologyRepository ori = new HibernateOrthologyRepository();
        StringBuilder sb = new StringBuilder();
        ZfinCriteria criteria = new ZfinCriteria();
        criteria.setOrRelationship(false);
        ori.createCromosomeWhereClause(getHumanSpeciesCriteria(), sb, criteria);
        assertEquals("Chromosome Where clause", " AND Human.chromosome in ('4','7','22')", sb.toString());
    }

    // multiple chromosomes range: test chromosome in (,,,,,,,,,,,)
    @Test
    public void createChromsomeWhereClauseMouse() {
        HibernateOrthologyRepository ori = new HibernateOrthologyRepository();
        StringBuilder sb = new StringBuilder();
        ZfinCriteria criteria = new ZfinCriteria();
        criteria.setOrRelationship(false);
        ori.createCromosomeWhereClause(getMouseSpeciesCriteria(), sb, criteria);
        assertEquals("Chromosome Where clause", " AND Mouse.chromosome in ('3','4','5','6','7','8','9','10','11','12','13','14','15','16','17','18','19','20')", sb.toString());
    }

    // Equals one position
    @Ignore
    public void createChromsomeWhereClauseHumanPosition() {
        HibernateOrthologyRepository ori = new HibernateOrthologyRepository();
        StringBuilder sb = new StringBuilder();
        ori.createPositionWhereClause(getHumanSpeciesCriteria(), sb);
        assertEquals("Chromosome Where clause", " AND Human.chromosome in ('4','7','22')", sb.toString());
    }


    private List<SpeciesCriteria> getSpeciesCriteriaZebrafishOnly() {

        List<SpeciesCriteria> speciesCriteria = new ArrayList<SpeciesCriteria>();
        SpeciesCriteria zebrafish = getZebrafishSpeciesCriteria();
        speciesCriteria.add(zebrafish);
        return speciesCriteria;
    }

    private SpeciesCriteria getZebrafishSpeciesCriteria() {
        SpeciesCriteria zebrafish = new SpeciesCriteria();
        zebrafish.setName(Species.ZEBRAFISH.toString());
        ChromosomeCriteria zebChrom = new ChromosomeCriteria();
        List<String> chromos = new ArrayList<String>();
        chromos.add("19");
        zebChrom.setChromosomesNames(chromos);
        zebChrom.setType(FilterType.EQUALS);
        zebrafish.setChromosome(zebChrom);
        GeneSymbolCriteria zebGene = new GeneSymbolCriteria();
        zebGene.setSymbol("fg");
        zebGene.setType(FilterType.BEGINS);
        zebrafish.setSymbol(zebGene);
        return zebrafish;
    }

    private SpeciesCriteria getZebrafishSpeciesCriteriaWithoutChromosome() {
        SpeciesCriteria zebrafish = new SpeciesCriteria();
        zebrafish.setName(Species.ZEBRAFISH.toString());
        ChromosomeCriteria zebChrom = new ChromosomeCriteria();
        List<Integer> chromos = new ArrayList<Integer>();
        zebChrom.setChromosomes(chromos);
        zebChrom.setType(FilterType.EQUALS);
        zebrafish.setChromosome(zebChrom);
        GeneSymbolCriteria zebGene = new GeneSymbolCriteria();
        zebGene.setSymbol("fg");
        zebGene.setType(FilterType.BEGINS);
        zebrafish.setSymbol(zebGene);
        return zebrafish;
    }

    private List<SpeciesCriteria> getSpeciesCriteriaZebrafishAndHuman() {

        List<SpeciesCriteria> speciesCriteria = new ArrayList<SpeciesCriteria>();
        SpeciesCriteria zebrafish = getZebrafishSpeciesCriteria();
        speciesCriteria.add(zebrafish);
        SpeciesCriteria human = getHumanSpeciesCriteria();
        speciesCriteria.add(human);

        return speciesCriteria;
    }

    private SpeciesCriteria getHumanSpeciesCriteria() {
        SpeciesCriteria human = new SpeciesCriteria();
        human.setName(Species.HUMAN.toString());
        ChromosomeCriteria humanChrom = new ChromosomeCriteria();
        List<String> chromosHuman = new ArrayList<String>();
        chromosHuman.add("4");
        chromosHuman.add("7");
        chromosHuman.add("22");
        humanChrom.setChromosomesNames(chromosHuman);
        humanChrom.setType(FilterType.LIST);
        human.setChromosome(humanChrom);
        GeneSymbolCriteria humanGene = new GeneSymbolCriteria();
        humanGene.setSymbol("uy");
        humanGene.setType(FilterType.CONTAINS);
        human.setSymbol(humanGene);

        PositionCriteria position = new PositionCriteria();
        position.setType(FilterType.EQUALS);
        position.setPosition(4);
        position.setHumanPosCharacter("q");
        human.setPosition(position);

        return human;
    }

    private SpeciesCriteria getHumanSpeciesCriteriaNoChromosome() {
        SpeciesCriteria human = new SpeciesCriteria();
        human.setName(Species.HUMAN.toString());
        ChromosomeCriteria humanChrom = new ChromosomeCriteria();
        humanChrom.setType(FilterType.LIST);
        human.setChromosome(humanChrom);
        GeneSymbolCriteria humanGene = new GeneSymbolCriteria();
        humanGene.setSymbol("uy");
        humanGene.setType(FilterType.CONTAINS);
        human.setSymbol(humanGene);
        return human;
    }

    private List<SpeciesCriteria> getSpeciesCriteriaZebrafishAndHumanAndMouse() {

        List<SpeciesCriteria> speciesCriteria = new ArrayList<SpeciesCriteria>();
        SpeciesCriteria mouse = getMouseSpeciesCriteria();
        SpeciesCriteria human = getHumanSpeciesCriteria();
        SpeciesCriteria zebrafish = getZebrafishSpeciesCriteria();
        speciesCriteria.add(zebrafish);
        speciesCriteria.add(human);
        speciesCriteria.add(mouse);
        return speciesCriteria;
    }

    private SpeciesCriteria getMouseSpeciesCriteria() {
        SpeciesCriteria mouse = new SpeciesCriteria();
        mouse.setName(Species.MOUSE.toString());
        ChromosomeCriteria mouseChrom = new ChromosomeCriteria();
        mouseChrom.setMin(3);
        mouseChrom.setMax(20);
        mouseChrom.setType(FilterType.RANGE);
        mouse.setChromosome(mouseChrom);
        GeneSymbolCriteria mouseGene = new GeneSymbolCriteria();
        mouseGene.setSymbol("WQ");
        mouseGene.setType(FilterType.EQUALS);
        mouse.setSymbol(mouseGene);
        return mouse;
    }

    private SpeciesCriteria getFlySpeciesCriteria() {
        SpeciesCriteria fly = new SpeciesCriteria();
        fly.setName(Species.FLY.toString());
        ChromosomeCriteria flyChrom = new ChromosomeCriteria();
        List<Integer> chromosFly = new ArrayList<Integer>();
        chromosFly.add(1);
        flyChrom.setChromosomes(chromosFly);
        fly.setChromosome(flyChrom);
        GeneSymbolCriteria mouseGene = new GeneSymbolCriteria();
        mouseGene.setSymbol("rtd5");
        mouseGene.setType(FilterType.ENDS);
        fly.setSymbol(mouseGene);
        return fly;
    }
}
