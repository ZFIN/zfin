package org.zfin.anatomy.presentation;

import org.junit.Test;
import org.zfin.orthology.CriteriaType;
import org.zfin.orthology.Species;
import org.zfin.orthology.presentation.OrthologyFormValidator;
import org.zfin.util.ErrorCollection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test class for the orthology search validation.
 */
public class OrthologyValidationTest {

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

    @Test
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

    @Test
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

    @Test
    public void testZebrafishEmptySymbol() {
        String species = Species.ZEBRAFISH.toString();
        String criteriaType = CriteriaType.GENE_SYMBOL.getName();
        String filterType = "contains";
        OrthologyFormValidator validator = new OrthologyFormValidator(species, criteriaType, filterType, "");
        assertTrue(validator.isValid());
    }

    @Test
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

    @Test
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

    @Test
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

    @Test
    public void testFlyFalseChromosome() {
        String species = Species.FRUIT_FLY.toString();
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

    @Test
    public void testZebrafishFalseCriteria() {
        String species = "Zebrafish";
        String criteriaType = "chromosome";
        String filterType = "equals";
        String criteria = "harry";
        OrthologyFormValidator validator = new OrthologyFormValidator(species, criteriaType, filterType, criteria);
        assertTrue(!validator.isValid());
        ErrorCollection coll = validator.getErrors();
        assertEquals("Number of errors", 1, coll.getErrors().size());
        String error = coll.getErrors().get(0);
        assertEquals("Error message", "The chromosome field for Zebrafish must be an integer.", error);
    }


}
