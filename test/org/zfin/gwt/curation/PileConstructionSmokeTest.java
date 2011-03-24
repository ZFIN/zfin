package org.zfin.gwt.curation;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import org.zfin.AbstractSecureSmokeTest;

import java.io.IOException;
import java.util.List;

/**
 * This class uses the more raw HtmlUnit protocols.
 */
public class PileConstructionSmokeTest extends AbstractSecureSmokeTest {

    public void testOntologyCombinations() {
        for (WebClient webClient : curationWebClients) {
            webClient.setJavaScriptEnabled(true);
            try {
                login(webClient);
                // retrieve curation page for given publication
                //  Johnson et al., 1995, Genetic control of adult pigment stripe development in zebrafish
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/dev-tools/gwt/phenotype-curation#ZDB-TERM-100331-611");
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                assertEquals("GWT Phenotype Curation", page.getTitleText());
                HtmlInput inputField = (HtmlInput) page.getByXPath("//input[@id='ENTITY_SUPERTERM']").get(0);
                assertNotNull(inputField);

                // check that unvalidated entry has red characters (error css class)
                // enter unvalidated MF term
                inputField.setValueAttribute("triglyceride lipase activity");
                String cssClass = inputField.getAttribute("class");
                assertEquals("error", cssClass);

                // validate entered term
                HtmlSelect entitySuperTerm = validateSelectionBox(EntityPart.ENTITY_SUPERTERM_NAME, "AO", page);
                entitySuperTerm.setSelectedAttribute("molecular_function", true);
                List<?> options = entitySuperTerm.getSelectedOptions();
                HtmlOption option = (HtmlOption) options.get(0);
                assertEquals("GO-MF", option.asText());
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                // check that the term is validate and the string is black again
                inputField = (HtmlInput) page.getByXPath("//input[@id='ENTITY_SUPERTERM']").get(0);
                cssClass = inputField.getAttribute("class");
                assertEquals("", cssClass);
                // check that the Quality is Process
                validateSelectionBox(EntityPart.QUALITY_NAME, "Quality - Processes", page);

                // ADD: MF[triglyceride lipase activity] : AO[] - PATO[] - AO[] : AO[] gives error
                validateErrorOnSubmission(page);

                // ADD: MF[triglyceride lipase activity] : AO[] - PATO[decreased distance] - AO[] : AO[]
                // gives error: Cannot add MF with related quality
                setSingleTermEntry("quality.process", "decreased distance", EntityPart.QUALITY_NAME, page);
                // no related entity box available
                checkNoRelatedEntityIsDisplayed(page);
                //validateErrorOnSubmission(page);

                // ADD: MF[triglyceride lipase activity] : AO[eye] - PATO[decreased rate] - AO[] : AO[]
                // gives error: Cannot post-compose MF with AO
                setSingleTermEntry("quality.process", "decreased rate", EntityPart.QUALITY_NAME, page);
                setSingleTermEntry("zebrafish_anatomy", "eye", EntityPart.ENTITY_SUBTERM_NAME, page);
                validateErrorOnSubmission(page);

                // add

            } catch (Exception e) {
                e.printStackTrace();
                fail(e.toString());
            }
        }
    }

    private void checkNoRelatedEntityIsDisplayed(HtmlPage page) {
        HtmlDivision relatedEntityPanel = (HtmlDivision) (page.getByXPath("//div[@id='related-terms-panel']").get(0));
        assertEquals("display: none;", relatedEntityPanel.getAttribute("style"));
    }

    private HtmlSelect validateSelectionBox(EntityPart entityPart, String selectedOntology, HtmlPage page) throws IOException {
        List<?> entityOntologyList = page.getByXPath("//div[@id='" + entityPart.getSelectionBoxName() + "']//select");
        assertNotNull(entityOntologyList);
        HtmlSelect entitySuperTerm = (HtmlSelect) entityOntologyList.get(0);
        List<?> options = entitySuperTerm.getSelectedOptions();
        assertEquals(1, options.size());
        HtmlOption option = (HtmlOption) options.get(0);
        assertEquals(selectedOntology, option.asText());
        return entitySuperTerm;
    }

    private void validateErrorOnSubmission(HtmlPage page) throws IOException {
        HtmlButton addButton = (HtmlButton) page.getByXPath("//div[@id='structure-pile-construction-zone-submit-reset']//button[. = 'Add']").get(0);
        assertNotNull(addButton);
        addButton.click();
        List<?> errorMessageNode = page.getByXPath("//div[@id='structure-pile-construction-zone-errors']/div");
        assertNotNull(errorMessageNode);
        assertEquals(errorMessageNode.size(), 1);
        HtmlDivision errorMessage = (HtmlDivision) errorMessageNode.get(0);
        assertTrue(errorMessage.getFirstChild().asText().length() > 5);
    }

    private void setStructureCombination(String entitySuperOnto, String entitySuperName,
                                         String entitySubOnto, String entitySubName,
                                         String qualityOnt, String qualityName,
                                         String relatedEntitySuperOnto, String relatedEntitySuperName,
                                         String relatedEntitySubOnto, String relatedEntitySubName, HtmlPage page) {
        // entity super term
        setSingleTermEntry(entitySuperOnto, entitySuperName, PileConstructionSmokeTest.EntityPart.ENTITY_SUPERTERM_NAME, page);
    }

    private void setSingleTermEntry(String entityOnto, String entityName, EntityPart entityPart, HtmlPage page) {
        HtmlInput inputField = (HtmlInput) page.getByXPath("//input[@id='" + entityPart.getInputName() + "']").get(0);
        assertNotNull(inputField);
        inputField.setValueAttribute(entityName);
        // validate entered term
        List<?> entitySuperTermList = page.getByXPath("//div[@id='" + entityPart.getSelectionBoxName() + "']//select");
        assertNotNull(entitySuperTermList);
        HtmlSelect entitySuperTerm = (HtmlSelect) entitySuperTermList.get(0);
        entitySuperTerm.setSelectedAttribute(entityOnto, true);
    }

    static enum EntityPart {
        ENTITY_SUPERTERM_NAME("ENTITY_SUPERTERM", "structure-pile-construction-zone-entity_superterm-info"),
        ENTITY_SUBTERM_NAME("ENTITY_SUBTERM", "structure-pile-construction-zone-entity_subterm-info"),
        QUALITY_NAME("QUALITY", "structure-pile-construction-zone-quality-info"),
        RELATED_ENTITY_SUPERTERM_NAME("RELATED_ENTITY_SUPERTERM", "structure-pile-construction-zone-related_entity_superterm-info"),
        RELATED_ENTITY_SUBTERM_NAME("RELATED_ENTITY_SUBTERM", "structure-pile-construction-zone-related_entity_subterm-info");

        private String inputName;
        private String selectionBoxName;

        EntityPart(String inputName, String selectionBoxName) {
            this.inputName = inputName;
            this.selectionBoxName = selectionBoxName;
        }

        public String getInputName() {
            return inputName;
        }

        public String getSelectionBoxName() {
            return selectionBoxName;
        }
    }

}
