package org.zfin.construct;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.construct.name.*;
import org.zfin.construct.presentation.AddConstructFormFields;
import org.zfin.construct.presentation.ConstructComponentService;
import org.zfin.construct.repository.ConstructRepository;
import org.zfin.database.InformixUtil;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerType;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.zfin.repository.RepositoryFactory.*;

@Log4j2
public class ConstructTest  extends AbstractDatabaseTest {


    @Test
    public void validateConstructNameTest() {
        Optional<String> result = ConstructComponentService.getValidationErrorMessageForConstructName("AB..CD");
        assertTrue(result.isPresent());
        result = ConstructComponentService.getValidationErrorMessageForConstructName("AB.,CD");
        assertTrue(result.isPresent());
        result = ConstructComponentService.getValidationErrorMessageForConstructName("AB()CD");
        assertTrue(result.isPresent());
    }

    @Test
    public void getConstructTypeEnumByName() {
        Optional<Marker.Type> type = ConstructComponentService.getConstructTypeEnumByConstructName("Tg(....)");
        assertEquals(type.get(), Marker.Type.TGCONSTRCT);

        type = ConstructComponentService.getConstructTypeEnumByConstructName("Gt(....)");
        assertEquals(type.get(), Marker.Type.GTCONSTRCT);

        type = ConstructComponentService.getConstructTypeEnumByConstructName("Et(....)");
        assertEquals(type.get(), Marker.Type.ETCONSTRCT);

        type = ConstructComponentService.getConstructTypeEnumByConstructName("Pt(....)");
        assertEquals(type.get(), Marker.Type.PTCONSTRCT);

        type = ConstructComponentService.getConstructTypeEnumByConstructName("Xy(....)");
        assertTrue(type.isEmpty());
    }

    @Test
    public void getConstructTypeByName() {
        Optional<MarkerType> type = ConstructComponentService.getConstructTypeByConstructName("Tg(....)");
        assertEquals("TGCONSTRCT", type.get().getName());

        type = ConstructComponentService.getConstructTypeByConstructName("Gt(....)");
        assertEquals("GTCONSTRCT", type.get().getName());

        type = ConstructComponentService.getConstructTypeByConstructName("Et(....)");
        assertEquals("ETCONSTRCT", type.get().getName());

        type = ConstructComponentService.getConstructTypeByConstructName("Pt(....)");
        assertEquals("PTCONSTRCT", type.get().getName());

        type = ConstructComponentService.getConstructTypeByConstructName("Xy(....)");
        assertTrue(type.isEmpty());
    }

    @Test
    public void getCassettesFromStoredName() {
        //the so-called "storedName" that is sent by the browser
        String storedName = "tdg.1#-#Hsa.TEST1#:EGFP#Cassette#,#tdg.2#-#Hsa.TEST2#:EGFP#";
        Cassettes cassettes = Cassettes.fromStoredName(storedName);
        assertEquals(2, cassettes.size());
        assertEquals("tdg.1-Hsa.TEST1:EGFP", cassettes.get(0).toString());
        assertEquals(",tdg.2-Hsa.TEST2:EGFP", cassettes.get(1).toString());

        //test that it handles trimming:
        storedName = "   tdg.1 # - #  Hsa.TEST1  #  :  EGFP  #  Cassette  # , # tdg.2 # - # Hsa.TEST2 # : EGFP #  ";
        cassettes = Cassettes.fromStoredName(storedName);
        assertEquals(2, cassettes.size());
        assertEquals("tdg.1-Hsa.TEST1:EGFP", cassettes.get(0).toString());
        assertEquals(",tdg.2-Hsa.TEST2:EGFP", cassettes.get(1).toString());
    }

    @Test
    public void parseConstructNameToCassettes() {
        //TODO:
        // figure out how to parse construct names into parts by starting with the name
        // the tricky part is that decimal can be a separator or part of a marker name
        // examples:
        //  Tg(FRT-Xla.Actc1:DsRed-GAB-FRT-LOXP,otpa-LOXP-E1B:mmGFP-5HS4,myl7:RFP)
        //  Et(-0.6hsp70l:GAL4-VP16)

        String name = "Tg(rnu6-32:CRISPR1-tyr,rnu6-32:CRISPR1-insra,rnu6-14:CRISPR2-insra,rnu6-7:CRISPR1-insrb,rnu6-279:CRISPR2-insrb,cryaa:Cerulean)";
//        ConstructName constructName = ConstructComponentService.parseConstructName(name);

    }

    @Test
    public void parseStoredNameToCassettes() {
        String storedName = "tdg.1#-#Hsa.TEST1#:EGFP#Cassette#,#tdg.2#-#Hsa.TEST2#:EGFP#";
        Cassettes cassettes = Cassettes.fromStoredName(storedName);
        assertEquals(2, cassettes.size());

        Cassette cassette1 = cassettes.get(0);
        Promoter promoter1 = cassette1.getPromoter();
        Coding coding1 = cassette1.getCoding();
        assertEquals("tdg.1-Hsa.TEST1", String.join("", promoter1.getPromoterParts()));
        assertEquals("EGFP", coding1.getCodingParts().get(0));

        Cassette cassette2 = cassettes.get(1);
        Promoter promoter2 = cassette2.getPromoter();
        Coding coding2 = cassette2.getCoding();
        assertEquals(",tdg.2-Hsa.TEST2", String.join("", promoter2.getPromoterParts()));
        assertEquals("EGFP", coding2.getCodingParts().get(0));
    }

    @Test
    public void parseStoredNameToConstructName() {
        ConstructName name = ConstructName.fromStoredName(
                "Tg",
                "someprefix",
                "tdg.1#-#Hsa.TEST1#:EGFP#Cassette#,#tdg.2#-#Hsa.TEST2#:EGFP#");
        assertEquals("Tgsomeprefix(tdg.1-Hsa.TEST1:EGFP,tdg.2-Hsa.TEST2:EGFP)", name.toString());
    }


    /**
     * Test that we can create a construct from a construct "stored" name.
     * The stored name is the name that is sent by the browser with encoding for separating parts using "#" and "Cassette"
     */
    @Test
    public void computeCreateConstructAndComponentParts() {
        ConstructRepository cr = getConstructRepository();

        String constructStoredName = "tdg.1#-#Hsa.TEST1#:EGFP#Cassette#,#tdg.2#-#Hsa.TEST2#:EGFP#";
        ConstructName constructNameObject = ConstructName.fromStoredName(
                "Tg",
                "",
                constructStoredName);
        String constructName = "Tg(tdg.1-Hsa.TEST1:EGFP,tdg.2-Hsa.TEST2:EGFP)";
        assertEquals(constructName, constructNameObject.toString());

        String constructPubID = "ZDB-PUB-220315-16";
        Publication constructPub = getPublicationRepository().getPublication(constructPubID);

        ConstructCuration newConstruct = ConstructCuration.create(constructName);

//TEST PART 1 (create the construct in the DB based on the construct "stored" name)
        //create the construct (as a random curator user)
        cr.createConstruct(newConstruct, constructPub, getCurator());

        String constructZdbID = newConstruct.getZdbID();
        assertTrue(StringUtils.isNotEmpty(constructZdbID));
        assertTrue(constructZdbID.startsWith("ZDB-TGCONSTRCT-"));

        //moving construct record to marker table
        InformixUtil.runProcedure("regen_construct_marker", constructZdbID + "");

        Marker newConstructMarker = getMarkerRepository().getMarkerByID(constructZdbID);
        assertNotNull(newConstructMarker);

//TEST PART 2 (add the beginning construct components to the construct and persist to DB)
        //this should add construct components to the database -- "Tg" and "("
        ConstructComponentService.setConstructWrapperComponents(constructNameObject.getTypeAbbreviation(), constructNameObject.getPrefix(), constructZdbID);

        List<ConstructComponent> parts = getMarkerRepository().getConstructComponent(constructZdbID);
        assertEquals(2, parts.size());
        assertEquals("Tg", parts.get(0).getComponentValue());
        assertEquals("(", parts.get(1).getComponentValue());

//TEST PART 3 (add the cassettes and their components to the construct and persist to DB)
        Cassettes parsedCassettes = constructNameObject.getCassettes();
        assertEquals(2, parsedCassettes.size());

        //parses out the cassette parts and adds them to the construct (in the DB)
        for (Cassette cassette : parsedCassettes) {
            ConstructComponentService.setPromotersAndCoding(cassette, constructZdbID, constructPubID);
        }

        //Adds the final ")" to the construct (in the DB)
        int lastComp = ConstructComponentService.getComponentCount(constructZdbID);
        getMarkerRepository().addConstructComponent(parsedCassettes.size(),
                lastComp,
                constructZdbID,
                ")",
                ConstructComponent.Type.CONTROLLED_VOCAB_COMPONENT,
                "construct wrapper component",
                getInfrastructureRepository().getCVZdbIDByTerm(")").getZdbID());

        List<ConstructComponent> parts2 = getMarkerRepository().getConstructComponent(constructZdbID);

        //make sure the components that were added to the DB are correct
        assertEquals(14, parts2.size());
        assertEquals("Tg", parts2.get(0).getComponentValue());
        assertEquals("(", parts2.get(1).getComponentValue());
        assertEquals("tdg.1", parts2.get(2).getComponentValue());
        assertEquals("-", parts2.get(3).getComponentValue());
        assertEquals("Hsa.TEST1", parts2.get(4).getComponentValue());
        assertEquals(":", parts2.get(5).getComponentValue());
        assertEquals("EGFP", parts2.get(6).getComponentValue());
        assertEquals(",", parts2.get(7).getComponentValue());
        assertEquals("tdg.2", parts2.get(8).getComponentValue());
        assertEquals("-", parts2.get(9).getComponentValue());
        assertEquals("Hsa.TEST2", parts2.get(10).getComponentValue());
        assertEquals(":", parts2.get(11).getComponentValue());
        assertEquals("EGFP", parts2.get(12).getComponentValue());
        assertEquals(")", parts2.get(13).getComponentValue());
    }

    @Test
    public void testConstructCreationService() throws InvalidConstructNameException {
        AddConstructFormFields form = new AddConstructFormFields();
        form.setConstructName("Tg(tdg.1-Hsa.TEST1:EGFP,tdg.2-Hsa.TEST2:EGFP)");
        form.setConstructPrefix("someprefix");
        form.setConstructType("Tg");
        form.setPubZdbID("ZDB-PUB-220315-16");
        form.setConstructStoredName("tdg.1#-#Hsa.TEST1#:EGFP#Cassette#,#tdg.2#-#Hsa.TEST2#:EGFP#");

        Marker newConstruct = ConstructComponentService.createNewConstructFromSubmittedForm(form, getCurator());
        List<ConstructComponent> parts = getMarkerRepository().getConstructComponent(newConstruct.getZdbID());

        assertEquals(15, parts.size());
        int partsIndex = 0;
        assertEquals("Tg", parts.get(partsIndex++).getComponentValue());
        assertEquals("someprefix", parts.get(partsIndex++).getComponentValue());
        assertEquals("(", parts.get(partsIndex++).getComponentValue());
        assertEquals("tdg.1", parts.get(partsIndex++).getComponentValue());
        assertEquals("-", parts.get(partsIndex++).getComponentValue());
        assertEquals("Hsa.TEST1", parts.get(partsIndex++).getComponentValue());
        assertEquals(":", parts.get(partsIndex++).getComponentValue());
        assertEquals("EGFP", parts.get(partsIndex++).getComponentValue());
        assertEquals(",", parts.get(partsIndex++).getComponentValue());
        assertEquals("tdg.2", parts.get(partsIndex++).getComponentValue());
        assertEquals("-", parts.get(partsIndex++).getComponentValue());
        assertEquals("Hsa.TEST2", parts.get(partsIndex++).getComponentValue());
        assertEquals(":", parts.get(partsIndex++).getComponentValue());
        assertEquals("EGFP", parts.get(partsIndex++).getComponentValue());
        assertEquals(")", parts.get(partsIndex++).getComponentValue());
    }

    @Test
    public void testConstructNameFromComponentParts() {
        String constructName = "Tg(tdg.1-Hsa.TEST1:EGFP,tdg.2-Hsa.TEST2:EGFP)";
        ConstructName constructNameObject = new ConstructName("Tg", "");
        constructNameObject.addCassette(Promoter.create("tdg.1", "-", "Hsa.TEST1"), Coding.create("EGFP"));
        constructNameObject.addCassette(Promoter.create(",", "tdg.2", "-", "Hsa.TEST2"), Coding.create("EGFP"));
        assertEquals(constructName, constructNameObject.toString());
        assertEquals("Tg", constructNameObject.getTypeAbbreviation());
        assertEquals("", constructNameObject.getPrefix());
        assertEquals(2, constructNameObject.getCassettes().size());
    }

    @Test
    public void testConstructRename() throws InvalidConstructNameException {
//        Construct updates for ZDB-PUB-190507-21
//        Tg(and1-Hsa.HBB:EGFP) ZDB-TGCONSTRCT-161115-2
//        Should be
//        Tg(en.epi-Hsa.HBB:EGFP)
//        Relationships
//        promoter of en.epi
//        Coding EGFP
//         constructStoredName("en.epi#-#Hsa.HBB#:EGFP");
        String pubZdbID = "ZDB-PUB-190507-21";
        String constructZdbID = "ZDB-TGCONSTRCT-161115-2";
        ConstructName newName = new ConstructName("Tg", "");
        newName.addCassette(Promoter.create("en.epi", "-", "Hsa.HBB"), Coding.create("EGFP"));
        Marker newMarker = ConstructComponentService.updateConstructName(constructZdbID, newName, pubZdbID);
        assertEquals("Tg(en.epi-Hsa.HBB:EGFP)", newMarker.getAbbreviation());
        assertEquals("Tg(en.epi-Hsa.HBB:EGFP)", newMarker.getName());

        List<ConstructComponent> components = getMarkerRepository().getConstructComponent(constructZdbID);

        int partsIndex = 0;
        assertEquals(8, components.size());
        assertEquals("Tg", components.get(partsIndex++).getComponentValue());
        assertEquals("(", components.get(partsIndex++).getComponentValue());
        assertEquals("en.epi", components.get(partsIndex++).getComponentValue());
        assertEquals("-", components.get(partsIndex++).getComponentValue());
        assertEquals("Hsa.HBB", components.get(partsIndex++).getComponentValue());
        assertEquals(":", components.get(partsIndex++).getComponentValue());
        assertEquals("EGFP", components.get(partsIndex++).getComponentValue());
        assertEquals(")", components.get(partsIndex++).getComponentValue());
    }

    public Person getCurator() {
        return getProfileRepository().getRootUsers().stream().findFirst().orElse(null);
    }

}