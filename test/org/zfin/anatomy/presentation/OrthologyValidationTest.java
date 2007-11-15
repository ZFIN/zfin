package org.zfin.anatomy.presentation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.zfin.orthology.CriteriaType;
import org.zfin.orthology.Species;
import org.zfin.orthology.presentation.OrthologyFormValidator;
import org.zfin.util.ErrorCollection;

/**
 * Test class for the orthology search validation.
 */
public class OrthologyValidationTest {

    public static void main(String args[]) {
/*
        String[] configFiles = {"/home/WEB-INF/zfin-servlet.xml",
                "/home/WEB-INF/conf/anatomy.xml",
                "/home/WEB-INF/conf/orthology.xml",
                "/home/WEB-INF/conf/profile.xml",
                "/home/WEB-INF/conf/publication.xml",
                "/home/WEB-INF/conf/developer-tools.xml"};
        ApplicationContext context = new FileSystemXmlApplicationContext(configFiles);
*/
    }

    @Test
    public void testZebrafishFalseSpecies() {
        String species = "zebrafsi";
        OrthologyFormValidator validator = new OrthologyFormValidator(species, null, null, null);
        assertTrue(!validator.isValid());
        ErrorCollection coll = validator.getErrors();
        assertEquals("Number of errors", 1, coll.getErrors().size());
        String error = coll.getErrors().get(0);
        assertEquals("Error message", "The species 'zebrafsi' is not supported by this search.", error);

    }

    public void testZebrafishFalseCriteriaType() {
        String species = Species.ZEBRAFISH.toString();
        String criteriaType = "symboll";
        OrthologyFormValidator validator = new OrthologyFormValidator(species, criteriaType, null, null);
        assertTrue(!validator.isValid());
        ErrorCollection coll = validator.getErrors();
        assertEquals("Number of errors", 1, coll.getErrors().size());
        String error = coll.getErrors().get(0);
        assertEquals("Error message", "The criteria type 'symboll' is not supported by this search.", error);
    }

    public void testZebrafishFalseFilterType() {
        String species = Species.ZEBRAFISH.toString();
        String criteriaType = CriteriaType.GENE_SYMBOL.getName();
        String filterType = "kkk";
        OrthologyFormValidator validator = new OrthologyFormValidator(species, criteriaType, filterType, null);
        assertTrue(!validator.isValid());
        ErrorCollection coll = validator.getErrors();
        assertEquals("Number of errors", 1, coll.getErrors().size());
        String error = coll.getErrors().get(0);
        assertEquals("Error message", "The filter type 'kkk' is not supported by the symbol field for Zebrafish in this search.", error);
    }

    public void testZebrafishEmptySymbol() {
        String species = Species.ZEBRAFISH.toString();
        String criteriaType = CriteriaType.GENE_SYMBOL.getName();
        String filterType = "contains";
        OrthologyFormValidator validator = new OrthologyFormValidator(species, criteriaType, filterType, "");
        assertTrue(validator.isValid());
    }

    public void testZebrafishFalseChromosome() {
        String species = Species.ZEBRAFISH.toString();
        String criteriaType = CriteriaType.CHROMOSOME.getName();
        String filterType = "equals";
        String chromoNumber = "25";
        OrthologyFormValidator validator = new OrthologyFormValidator(species, criteriaType, filterType, chromoNumber);
        assertTrue(validator.isValid());
        chromoNumber = "26";
        validator = new OrthologyFormValidator(species, criteriaType, filterType, chromoNumber);
        assertTrue(!validator.isValid());
        chromoNumber = "-1";
        validator = new OrthologyFormValidator(species, criteriaType, filterType, chromoNumber);
        assertTrue(!validator.isValid());
    }

    public void testHumanFalseChromosome() {
        String species = Species.HUMAN.toString();
        String criteriaType = CriteriaType.CHROMOSOME.getName();
        String filterType = "equals";
        String chromoNumber = "23";
        OrthologyFormValidator validator = new OrthologyFormValidator(species, criteriaType, filterType, chromoNumber);
        assertTrue(validator.isValid());
        chromoNumber = "24";
        validator = new OrthologyFormValidator(species, criteriaType, filterType, chromoNumber);
        assertTrue(!validator.isValid());
        chromoNumber = "-1";
        validator = new OrthologyFormValidator(species, criteriaType, filterType, chromoNumber);
        assertTrue(!validator.isValid());
        chromoNumber = "X";
        validator = new OrthologyFormValidator(species, criteriaType, filterType, chromoNumber);
        assertTrue(validator.isValid());
        chromoNumber = "Y";
        validator = new OrthologyFormValidator(species, criteriaType, filterType, chromoNumber);
        assertTrue(validator.isValid());
        chromoNumber = "YX";
        validator = new OrthologyFormValidator(species, criteriaType, filterType, chromoNumber);
        assertTrue(!validator.isValid());
        chromoNumber = "";
        validator = new OrthologyFormValidator(species, criteriaType, filterType, chromoNumber);
        assertTrue(validator.isValid());
    }

    public void testMouseFalseChromosome() {
        String species = Species.MOUSE.toString();
        String criteriaType = CriteriaType.CHROMOSOME.getName();
        String filterType = "equals";
        String chromoNumber = "20";
        OrthologyFormValidator validator = new OrthologyFormValidator(species, criteriaType, filterType, chromoNumber);
        assertTrue(validator.isValid());
        chromoNumber = "21";
        validator = new OrthologyFormValidator(species, criteriaType, filterType, chromoNumber);
        assertTrue(!validator.isValid());
        chromoNumber = "-1";
        validator = new OrthologyFormValidator(species, criteriaType, filterType, chromoNumber);
        assertTrue(!validator.isValid());
        chromoNumber = "X";
        validator = new OrthologyFormValidator(species, criteriaType, filterType, chromoNumber);
        assertTrue(validator.isValid());
        chromoNumber = "Y";
        validator = new OrthologyFormValidator(species, criteriaType, filterType, chromoNumber);
        assertTrue(validator.isValid());
        chromoNumber = "x";
        validator = new OrthologyFormValidator(species, criteriaType, filterType, chromoNumber);
        assertTrue(validator.isValid());
        chromoNumber = "YX";
        validator = new OrthologyFormValidator(species, criteriaType, filterType, chromoNumber);
        assertTrue(!validator.isValid());
    }

    public void testFlyFalseChromosome() {
        String species = Species.FLY.toString();
        String criteriaType = CriteriaType.CHROMOSOME.getName();
        String filterType = "equals";
        String chromoNumber = "4";
        OrthologyFormValidator validator = new OrthologyFormValidator(species, criteriaType, filterType, chromoNumber);
        assertTrue(validator.isValid());
        chromoNumber = "5";
        validator = new OrthologyFormValidator(species, criteriaType, filterType, chromoNumber);
        assertTrue(!validator.isValid());
        chromoNumber = "-1";
        validator = new OrthologyFormValidator(species, criteriaType, filterType, chromoNumber);
        assertTrue(!validator.isValid());
        chromoNumber = "X";
        validator = new OrthologyFormValidator(species, criteriaType, filterType, chromoNumber);
        assertTrue(validator.isValid());
        chromoNumber = "Y";
        validator = new OrthologyFormValidator(species, criteriaType, filterType, chromoNumber);
        assertTrue(validator.isValid());
        chromoNumber = "YX";
        validator = new OrthologyFormValidator(species, criteriaType, filterType, chromoNumber);
        assertTrue(!validator.isValid());
    }

    public void stestZebrafishFalseCriteria() {
        String species = "Zebrafish";
        String criteriaType = "chromosome";
        String filterType = "equals";
        String criteria = "harry";
        OrthologyFormValidator validator = new OrthologyFormValidator(species, criteriaType, filterType, criteria);
        assertTrue(!validator.isValid());
        ErrorCollection coll = validator.getErrors();
        assertEquals("Number of errors", 1, coll.getErrors().size());
        String error = coll.getErrors().get(0);
        assertEquals("Error message", "The filter type 'kkk' is not supported by the symbol field for Zebrafish in this search.", error);
    }


}
