package org.zfin.gwt.curation.ui;

import org.junit.Before;
import org.junit.Test;
import org.zfin.gwt.curation.dto.FeatureMarkerRelationshipTypeEnum;
import org.zfin.gwt.root.dto.FeatureDTO;
import org.zfin.gwt.root.dto.FeatureMarkerRelationshipDTO;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.ui.ValidationException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 */
public class FeatureMarkerRelationshipTest {

    FeatureDTO featureDTO = new FeatureDTO();
    MarkerDTO m1 = new MarkerDTO();
    MarkerDTO m2 = new MarkerDTO();
    MarkerDTO m3 = new MarkerDTO();
    List<FeatureMarkerRelationshipDTO> featureMarkerRelationshipDTOList = new ArrayList<FeatureMarkerRelationshipDTO>();

    @Before
    public void createBaseData() {
        featureDTO.setName("bob");
        m1.setName("m1");
        m2.setName("m2");
        m3.setName("m3");
        featureMarkerRelationshipDTOList.clear();
    }


    @Test
    public void validateAlleles() throws ValidationException {
        // validat point mutation, small deletion, insertion, unspecified
        // translocation, deficiency, inversion
        FeatureMarkerRelationshipDTO fmr1 = new FeatureMarkerRelationshipDTO();
        fmr1.setMarkerDTO(m1);
        fmr1.setFeatureDTO(featureDTO);
        fmr1.setRelationshipType(FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF.toString());

        try {
            featureDTO.setFeatureType(FeatureTypeEnum.POINT_MUTATION);
            FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(fmr1, featureMarkerRelationshipDTOList);
            featureDTO.setFeatureType(FeatureTypeEnum.DELETION);
            FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(fmr1, featureMarkerRelationshipDTOList);
            featureDTO.setFeatureType(FeatureTypeEnum.INSERTION);
            FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(fmr1, featureMarkerRelationshipDTOList);
            featureDTO.setFeatureType(FeatureTypeEnum.TRANSLOC);
            FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(fmr1, featureMarkerRelationshipDTOList);
            featureDTO.setFeatureType(FeatureTypeEnum.DEFICIENCY);
            FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(fmr1, featureMarkerRelationshipDTOList);
            featureDTO.setFeatureType(FeatureTypeEnum.INVERSION);
            FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(fmr1, featureMarkerRelationshipDTOList);
        } catch (ValidationException e) {
            fail(e.toString());
        }

        featureMarkerRelationshipDTOList.add(fmr1);

        FeatureMarkerRelationshipDTO fmr2 = new FeatureMarkerRelationshipDTO();
        fmr2.setMarkerDTO(m2);
        fmr2.setFeatureDTO(featureDTO);
        fmr2.setRelationshipType(FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF.toString());

        try {
            featureDTO.setFeatureType(FeatureTypeEnum.POINT_MUTATION);
            assertTrue(FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(fmr2, featureMarkerRelationshipDTOList));
            FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(fmr1, featureMarkerRelationshipDTOList);
            fail("should not validate");
        } catch (ValidationException ve) {
        }
        try {
            featureDTO.setFeatureType(FeatureTypeEnum.DELETION);
            assertTrue(FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(fmr2, featureMarkerRelationshipDTOList));
            FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(fmr1, featureMarkerRelationshipDTOList);
            fail("should not validate");
        } catch (ValidationException ve) {
        }
        try {
            featureDTO.setFeatureType(FeatureTypeEnum.INSERTION);
            assertTrue(FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(fmr2, featureMarkerRelationshipDTOList));
            FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(fmr1, featureMarkerRelationshipDTOList);
            fail("should not validate");
        } catch (ValidationException ve) {
        }

        featureDTO.setFeatureType(FeatureTypeEnum.TRANSLOC);
        assertTrue(FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(fmr2, featureMarkerRelationshipDTOList));

        try {
            FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(fmr1, featureMarkerRelationshipDTOList);
            fail("relationship should already exist");
        } catch (ValidationException ve) {
        }

        featureDTO.setFeatureType(FeatureTypeEnum.DEFICIENCY);
        assertTrue(FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(fmr2, featureMarkerRelationshipDTOList));

        assertTrue(FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(fmr2, featureMarkerRelationshipDTOList));

    }

    @Test
    public void validateTranslocationExclusiveRelationships() throws ValidationException {

        featureDTO.setFeatureType(FeatureTypeEnum.TRANSLOC);

        FeatureMarkerRelationshipDTO fmr1 = new FeatureMarkerRelationshipDTO();
        fmr1.setMarkerDTO(m1);
        fmr1.setFeatureDTO(featureDTO);
        fmr1.setRelationshipType(FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF.toString());

        FeatureMarkerRelationshipDTO fmr2 = new FeatureMarkerRelationshipDTO();
        fmr2.setMarkerDTO(m2);
        fmr2.setFeatureDTO(featureDTO);
        fmr2.setRelationshipType(FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF.toString());

        FeatureMarkerRelationshipDTO fmr3 = new FeatureMarkerRelationshipDTO();
        fmr3.setMarkerDTO(m1);
        fmr3.setFeatureDTO(featureDTO);
        fmr3.setRelationshipType(FeatureMarkerRelationshipTypeEnum.MARKERS_MOVED.toString());

        assertTrue(FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(fmr1, featureMarkerRelationshipDTOList));
        assertTrue(FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(fmr2, featureMarkerRelationshipDTOList));
        assertTrue(FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(fmr3, featureMarkerRelationshipDTOList));

        featureMarkerRelationshipDTOList.add(fmr1);

        try {
            FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(fmr3, featureMarkerRelationshipDTOList);
            fail("should not validate");
        } catch (ValidationException ve) {
        }

        try {
            assertTrue(FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(fmr1, featureMarkerRelationshipDTOList));
            fail("relationships already exists");
        } catch (ValidationException e) {
        }
        assertTrue(FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(fmr2, featureMarkerRelationshipDTOList));

        featureMarkerRelationshipDTOList.add(fmr2);

        try {
            FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(fmr3, featureMarkerRelationshipDTOList);
            fail("should not validate");
        } catch (ValidationException ve) {
        }
    }

    @Test
    public void validateCounts() {
        featureDTO.setFeatureType(FeatureTypeEnum.DEFICIENCY);


        FeatureMarkerRelationshipDTO fmr1 = new FeatureMarkerRelationshipDTO();
        fmr1.setMarkerDTO(m1);
        fmr1.setFeatureDTO(featureDTO);
        fmr1.setRelationshipType(FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF.toString());

        assertEquals(0, FeatureMarkerRelationshipValidationService.countFeatureMarkerRelationshipsForType(fmr1, featureMarkerRelationshipDTOList));

        featureMarkerRelationshipDTOList.add(fmr1);
        assertEquals(1, FeatureMarkerRelationshipValidationService.countFeatureMarkerRelationshipsForType(fmr1, featureMarkerRelationshipDTOList));

        FeatureMarkerRelationshipDTO fmr2 = new FeatureMarkerRelationshipDTO();
        fmr2.setMarkerDTO(m2);
        fmr2.setFeatureDTO(featureDTO);
        fmr2.setRelationshipType(FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF.toString());

        featureMarkerRelationshipDTOList.add(fmr2);

        assertEquals(2, FeatureMarkerRelationshipValidationService.countFeatureMarkerRelationshipsForType(fmr1, featureMarkerRelationshipDTOList));
        assertEquals(2, FeatureMarkerRelationshipValidationService.countFeatureMarkerRelationshipsForType(fmr2, featureMarkerRelationshipDTOList));

        FeatureMarkerRelationshipDTO fmr3 = new FeatureMarkerRelationshipDTO();
        fmr3.setMarkerDTO(m3);
        fmr3.setFeatureDTO(featureDTO);
        fmr3.setRelationshipType(FeatureMarkerRelationshipTypeEnum.MARKERS_MISSING.toString());

        featureMarkerRelationshipDTOList.add(fmr3);
        assertEquals(2, FeatureMarkerRelationshipValidationService.countFeatureMarkerRelationshipsForType(fmr1, featureMarkerRelationshipDTOList));
        assertEquals(2, FeatureMarkerRelationshipValidationService.countFeatureMarkerRelationshipsForType(fmr2, featureMarkerRelationshipDTOList));
        assertEquals(1, FeatureMarkerRelationshipValidationService.countFeatureMarkerRelationshipsForType(fmr3, featureMarkerRelationshipDTOList));

    }

    @Test
    public void validateDeficiencyExclusiveRelationships() throws ValidationException {
        featureDTO.setFeatureType(FeatureTypeEnum.DEFICIENCY);

        FeatureMarkerRelationshipDTO fmr1 = new FeatureMarkerRelationshipDTO();
        fmr1.setMarkerDTO(m1);
        fmr1.setFeatureDTO(featureDTO);
        fmr1.setRelationshipType(FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF.toString());

        FeatureMarkerRelationshipDTO fmr2 = new FeatureMarkerRelationshipDTO();
        fmr2.setMarkerDTO(m2);
        fmr2.setFeatureDTO(featureDTO);
        fmr2.setRelationshipType(FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF.toString());

        FeatureMarkerRelationshipDTO fmr3 = new FeatureMarkerRelationshipDTO();
        fmr3.setMarkerDTO(m1);
        fmr3.setFeatureDTO(featureDTO);
        fmr3.setRelationshipType(FeatureMarkerRelationshipTypeEnum.MARKERS_MOVED.toString());

        assertTrue(FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(fmr1, featureMarkerRelationshipDTOList));
        assertTrue(FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(fmr2, featureMarkerRelationshipDTOList));
        assertTrue(FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(fmr3, featureMarkerRelationshipDTOList));

        featureMarkerRelationshipDTOList.add(fmr1);

        try {
            assertTrue(FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(fmr1, featureMarkerRelationshipDTOList));
            fail("Relationship already exists");
        } catch (ValidationException e) {
        }
        assertTrue(FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(fmr2, featureMarkerRelationshipDTOList));
        assertTrue(FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(fmr3, featureMarkerRelationshipDTOList));


        fmr3.setRelationshipType(FeatureMarkerRelationshipTypeEnum.MARKERS_PRESENT.toString());
        assertTrue(FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(fmr2, featureMarkerRelationshipDTOList));
        try {
            FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(fmr3, featureMarkerRelationshipDTOList);
            fail("should not validate");
        } catch (ValidationException ve) {
        }


        featureMarkerRelationshipDTOList.add(fmr2);

        try {
            FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(fmr1, featureMarkerRelationshipDTOList);
            fail("should not validate");
        } catch (ValidationException ve) {
        }

        try {
            FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(fmr2, featureMarkerRelationshipDTOList);
            fail("should not validate");
        } catch (ValidationException ve) {
        }

        try {
            FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(fmr3, featureMarkerRelationshipDTOList);
            fail("should not validate");
        } catch (ValidationException ve) {
        }

        fmr3.setRelationshipType(FeatureMarkerRelationshipTypeEnum.MARKERS_MISSING.toString());


        try {
            FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(fmr1, featureMarkerRelationshipDTOList);
            fail("should not validate");
        } catch (ValidationException ve) {
        }

        try {
            FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(fmr2, featureMarkerRelationshipDTOList);
            fail("should not validate");
        } catch (ValidationException ve) {
        }

        try {
            FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(fmr3, featureMarkerRelationshipDTOList);
            fail("should not validate");
        } catch (ValidationException ve) {
        }

    }

}
