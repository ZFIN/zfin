package org.zfin.nomenclature.repair;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.zfin.AbstractDatabaseTest;
import org.zfin.AppConfig;
import org.zfin.TestConfiguration;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureAlias;
import org.zfin.feature.FeatureMarkerRelationship;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.curation.dto.FeatureMarkerRelationshipTypeEnum;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.gwt.root.ui.ValidationException;
import org.zfin.infrastructure.DataAliasGroup;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.Fish;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeFeature;
import org.zfin.mutant.GenotypeService;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.search.Category;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;
import static org.zfin.repository.RepositoryFactory.*;
import static org.zfin.repository.RepositoryFactory.getMutantRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class})
@WebAppConfiguration
public class GenotypeNameChangeTest extends AbstractDatabaseTest {

    @Test
    public void testCauseEmptyGenotypeName() throws ValidationException {
        MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
        PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();

        //create gene1
        /*
            type: GENE
            publicationId: ZDB-PUB-210616-17
            name: testgene001
            abbreviation: testg001
        */
        Marker newGene = new Marker();
        newGene.setMarkerType(markerRepository.getMarkerTypeByName("GENE"));
        newGene.setName("testgene001");
        newGene.setAbbreviation("testg001");
        Publication reference = publicationRepository.getPublication("ZDB-PUB-210616-17");
        markerRepository.createMarker(newGene, reference);

        //create gene2
        /*
            type: GENE
            publicationId: ZDB-PUB-210616-17
            name: testgene002
            abbreviation: testg002
        */
        Marker newGene2 = new Marker();
        newGene2.setMarkerType(markerRepository.getMarkerTypeByName("GENE"));
        newGene2.setName("testgene002");
        newGene2.setAbbreviation("testg002");
        Publication reference2 = publicationRepository.getPublication("ZDB-PUB-210616-17");
        markerRepository.createMarker(newGene2, reference2);

        //create allele
        //see: FeatureRPCService|createFeature
        FeatureDTO featureDTO = new FeatureDTO();
        featureDTO.setAbbreviation("zf5555");
        featureDTO.setLineNumber("5555");
        featureDTO.setLabPrefix("zf");
        featureDTO.setLabOfOrigin("ZDB-LAB-000914-1");
        featureDTO.setPublicationZdbID("ZDB-PUB-210616-17");
        featureDTO.setName("zf5555");
        featureDTO.setFeatureType(FeatureTypeEnum.SEQUENCE_VARIANT);
        DTOConversionService.escapeFeatureDTO(featureDTO);
        Publication publication = getPublicationRepository().getPublication(featureDTO.getPublicationZdbID());
        Feature feature = DTOConversionService.convertToFeature(featureDTO);
        getFeatureRepository().saveFeature(feature, publication);
        featureDTO.setZdbID(feature.getZdbID());

        //create the relationship as 'allele of' gene1
        //see: org.zfin.gwt.curation.ui.FeatureRPCService|addFeatureMarkerRelationShip
        FeatureMarkerRelationship featureMarkerRelationship = new FeatureMarkerRelationship();
        featureMarkerRelationship.setFeature(feature);
        featureMarkerRelationship.setMarker(newGene);
        featureMarkerRelationship.setType(FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF);
        HibernateUtil.currentSession().save(featureMarkerRelationship);
//        infrastructureRepository.insertPublicAttribution(featureMarkerRelationship.getZdbID(), featureMarkerRelationshipDTO.getPublicationZdbID());
//        infrastructureRepository.insertUpdatesTable(featureMarkerRelationship.getZdbID(), "FeatureMarkerRelationship", featureMarkerRelationship.toString(), "Created feature marker relationship");

        //create genotype & fish of allele
        Genotype genotype;
        GenotypeCreationReportDTO report = new GenotypeCreationReportDTO();

        List<Genotype> genotypeBackgroundList = new ArrayList<>(3);
        GenotypeFeatureDTO genotypeFeatureDTO = new GenotypeFeatureDTO();
        ArrayList<GenotypeFeatureDTO> genotypeFeatureDTOList = new ArrayList<>();

        ZygosityDTO heterozygous = new ZygosityDTO();
        heterozygous.setZdbID("ZDB-ZYG-070117-2");
        genotypeFeatureDTO.setZygosity(heterozygous);
        genotypeFeatureDTO.setMaternalZygosity(heterozygous);
        genotypeFeatureDTO.setPaternalZygosity(heterozygous);
        genotypeFeatureDTO.setFeatureDTO(featureDTO);
        genotypeFeatureDTOList.add(genotypeFeatureDTO);
        genotype = GenotypeService.createGenotype(genotypeFeatureDTOList, genotypeBackgroundList);
        getMutantRepository().saveGenotype(genotype, publication.getZdbID());

        FishDTO fishDTO = new FishDTO();
        fishDTO.setStrList((new ArrayList<>()));
        GenotypeDTO genoDTO = new GenotypeDTO();
        genoDTO.setZdbID(genotype.getZdbID());
        genoDTO.setName(genotype.getName());
        fishDTO.setGenotypeDTO(genoDTO);

        Fish fish = DTOConversionService.convertToFishFromFishDTO(fishDTO);
        getMutantRepository().createFish(fish, publication);

        //confirm fish name is correct
        assertEquals("zf5555/+", fish.getName());
        HibernateUtil.flushAndCommitCurrentSession();
        HibernateUtil.currentSession().beginTransaction();

        //change allele to 'allele of' gene2 (delete, and re-add)
        //see: org.zfin.gwt.curation.ui.FeatureRPCService|deleteFeatureMarkerRelationship
        String fmrZdbId = featureMarkerRelationship.getZdbID();
        System.out.println("fmrZdbId: " + fmrZdbId);
//        infrastructureRepository.insertUpdatesTable(zdbID, "Feature", zdbID, "",...
        RepositoryFactory.getInfrastructureRepository().deleteActiveDataByZdbID(fmrZdbId);
        HibernateUtil.flushAndCommitCurrentSession();
        HibernateUtil.currentSession().beginTransaction();

        //re-add: (see: org.zfin.gwt.curation.ui.FeatureRPCService|addFeatureMarkerRelationShip)
        FeatureMarkerRelationship featureMarkerRelationship2 = new FeatureMarkerRelationship();
        Feature feature2 = getFeatureRepository().getFeatureByID(feature.getZdbID());
        featureMarkerRelationship2.setFeature(feature2);
        featureMarkerRelationship2.setMarker(newGene2);
        featureMarkerRelationship2.setType(FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF);
        HibernateUtil.currentSession().save(featureMarkerRelationship2);

        //confirm fish name not changed blank
        assertEquals("zf5555/+", fish.getName());
        HibernateUtil.flushAndCommitCurrentSession();
        HibernateUtil.currentSession().beginTransaction();

        fish = RepositoryFactory.getMutantRepository().getFish(fish.getZdbID());
        assertEquals("zf5555/+", fish.getName());

        genotype = RepositoryFactory.getMutantRepository().getGenotypeByID(fish.getGenotype().getZdbID());
        assertEquals("zf5555/+", genotype.getName());

//        Set<GenotypeFeature> features = genotype.getGenotypeFeatures();
//        for(GenotypeFeature f : features) {
//            Marker marker = f.getFeature().getSingleRelatedMarker();
//            assertEquals("testgene02", marker.getName());
//        }

    }

    /**
     * SQL TEST that works on command line:
     *
     * delete from zdb_active_data where zactvd_zdb_id = 'ZDB-FMREL-220102-5';
     * delete from record_attribution where recattrib_data_zdb_id = 'ZDB-FMREL-220102-5';
     *
     * delete from zdb_active_source where zactvs_zdb_id in ('ZDB-PUB-220101-1');
     * delete from publication where zdb_id in ('ZDB-PUB-220101-1');
     * delete from genotype_feature where genofeat_zdb_id= 'ZDB-GENOFEAT-220101-1';
     * delete from zdb_active_data where zactvd_zdb_id in ('ZDB-GENE-220101-1', 'ZDB-DALIAS-220101-1', 'ZDB-GENE-220101-2',  'ZDB-DALIAS-220101-2', 'ZDB-ALT-220101-1','ZDB-FMREL-220101-3','ZDB-FMREL-220101-2','ZDB-FMREL-220101-1',
     *     'ZDB-GENO-220101-1',
     *     'ZDB-GENOFEAT-220101-1',
     *     'ZDB-FISH-220101-1');
     * delete from zdb_active_data where zactvd_zdb_id = 'ZDB-GENE-220101-1';
     * delete from marker where mrkr_zdb_id = 'ZDB-GENE-220101-1';
     * delete from zdb_active_data where zactvd_zdb_id = 'ZDB-GENE-220101-2';
     * delete from marker where mrkr_zdb_id = 'ZDB-GENE-220101-2';
     * delete from feature where feature_zdb_id = 'ZDB-ALT-220101-1';
     * delete from feature_tracking where ft_feature_zdb_id = 'ZDB-ALT-220101-1';
     * delete from feature_marker_relationship where fmrel_zdb_id = 'ZDB-FMREL-220101-1';
     * delete from zdb_active_data where zactvd_zdb_id = 'ZDB-FMREL-220101-1';
     * delete from genotype where geno_zdb_id = 'ZDB-GENO-220101-1';
     * delete from zdb_active_data where zactvd_zdb_id = 'ZDB-GENO-220101-1';
     * delete from fish where fish_zdb_id = 'ZDB-FISH-220101-1';
     * delete from zdb_active_data where zactvd_zdb_id = 'ZDB-FISH-220101-1';
     * delete from zdb_active_data where zactvd_zdb_id = 'ZDB-PUB-220101-1';
     * delete from zdb_active_source where zactvs_zdb_id = 'ZDB-PUB-220101-1';
     *
     * -- Publication
     * insert into zdb_active_source (zactvs_zdb_id) values ('ZDB-PUB-220101-1');
     * insert into publication (title, authors, pub_mini_ref, pub_abstract, pub_date, pub_completion_date, pub_volume, pub_pages, jtype, accession_no, pub_doi, pub_can_show_images, pub_is_curatable, pub_is_indexed, pub_indexed_date, pub_acknowledgment, status, pub_errata_and_notes, keywords, pub_jrnl_zdb_id, pub_indexed_by, pub_last_correspondence_date, zdb_id) values ('Test Title', 'Test Author', NULL, NULL, '2021-12-31', NULL, NULL, NULL, 'Journal', NULL, NULL, 'f', 't', 'f', NULL, NULL, 'active', NULL, NULL, 'ZDB-JRNL-050621-1391', NULL, NULL, 'ZDB-PUB-220101-1');
     * insert into pub_tracking_history (pth_status_insert_date, pth_location_id, pth_claimed_by, pth_pub_zdb_id, pth_status_id, pth_status_set_by) values ('2022-01-01 22:24:01.35', NULL, NULL, 'ZDB-PUB-220101-1', '1', 'ZDB-PERS-210917-1');
     *
     * --gene
     * insert into zdb_active_data (zactvd_zdb_id) values ('ZDB-GENE-220101-1');
     * insert into marker (mrkr_name, mrkr_abbrev, mrkr_owner, mrkr_type, mrkr_abbrev_order, mrkr_comments, mrkr_zdb_id) values ( 'testgene001', 'testg001', 'ZDB-PERS-210917-1', 'GENE', NULL, '', 'ZDB-GENE-220101-1');
     * insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type) values ('ZDB-GENE-220101-1', 'ZDB-PUB-220101-1', 'standard');
     * insert into updates (comments, field_name, new_value, old_value, rec_id, submitter_id, submitter_name, upd_when) values ('', 'New GENE', 'testg001', NULL, 'ZDB-GENE-220101-1', 'ZDB-PERS-210917-1', 'Taylor, Ryan', '2022-01-01 22:31:37.42');
     * insert into zdb_active_data (zactvd_zdb_id) values ( 'ZDB-DALIAS-220101-1');
     * insert into data_alias (dalias_alias, dalias_group_id, dalias_alias_lower, dalias_data_zdb_id, dalias_zdb_id) values ('testg001', '1', NULL, 'ZDB-GENE-220101-1', 'ZDB-DALIAS-220101-1');
     * insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type) values ( 'ZDB-DALIAS-220101-1', 'ZDB-PUB-220101-1', 'standard');
     * insert into updates (comments, field_name, new_value, old_value, rec_id, submitter_id, submitter_name, upd_when) values ( 'Added alias: ''testg001'' attributed to publication: ''ZDB-PUB-220101-1''', '', 'testg001', NULL, 'ZDB-GENE-220101-1', 'ZDB-PERS-210917-1', 'Taylor, Ryan', '2022-01-01 22:31:37.474');
     *
     * --gene 2
     * insert into zdb_active_data (zactvd_zdb_id) values ('ZDB-GENE-220101-2');
     * insert into marker (mrkr_name, mrkr_abbrev, mrkr_owner, mrkr_type, mrkr_abbrev_order, mrkr_comments, mrkr_zdb_id) values ( 'testgene002', 'testg002', 'ZDB-PERS-210917-1', 'GENE', NULL, '', 'ZDB-GENE-220101-2');
     * insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type) values ('ZDB-GENE-220101-2', 'ZDB-PUB-220101-1', 'standard');
     * insert into updates (comments, field_name, new_value, old_value, rec_id, submitter_id, submitter_name, upd_when) values ('', 'New GENE', 'testg002', NULL, 'ZDB-GENE-220101-2', 'ZDB-PERS-210917-1', 'Taylor, Ryan', '2022-01-01 22:31:37.42');
     * insert into zdb_active_data (zactvd_zdb_id) values ( 'ZDB-DALIAS-220101-2');
     * insert into data_alias (dalias_alias, dalias_group_id, dalias_alias_lower, dalias_data_zdb_id, dalias_zdb_id) values ('testg002', '1', NULL, 'ZDB-GENE-220101-2', 'ZDB-DALIAS-220101-2');
     * insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type) values ( 'ZDB-DALIAS-220101-2', 'ZDB-PUB-220101-1', 'standard');
     * insert into updates (comments, field_name, new_value, old_value, rec_id, submitter_id, submitter_name, upd_when) values ( 'Added alias: ''testg002'' attributed to publication: ''ZDB-PUB-220101-1''', '', 'testg002', NULL, 'ZDB-GENE-220101-2', 'ZDB-PERS-210917-1', 'Taylor, Ryan', '2022-01-01 22:31:37.474');
     *
     *
     * --feature
     * insert into zdb_active_data (zactvd_zdb_id) values ('ZDB-ALT-220101-1');
     * insert into feature (feature_abbrev, feature_abbrev_order, feature_lab_prefix_id, ftr_chr_info_date, feature_date_entered, feature_dominant, feature_known_insertion_site, feature_unspecified, feature_line_number, feature_name, feature_name_order, feature_tg_suffix, feature_type, feature_zdb_id) values ( 'zf5555', 'zf5555', '194', NULL, NULL, 'f', 'f', 'f', '5555', 'zf5555', 'zf5555', 'Tg', 'SEQUENCE_VARIANT', 'ZDB-ALT-220101-1');
     * insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type) values ( 'ZDB-ALT-220101-1', 'ZDB-PUB-220101-1', 'feature type');
     * insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type) values ( 'ZDB-ALT-220101-1', 'ZDB-PUB-220101-1', 'standard');
     * insert into int_data_source (ids_data_zdb_id, ids_source_zdb_id) values ( 'ZDB-ALT-220101-1', 'ZDB-LAB-000914-1');
     * insert into feature_assay (featassay_feature_zdb_id, featassay_mutagee, featassay_mutagen) values ( 'ZDB-ALT-220101-1', 'not specified', 'not specified');
     *
     *
     * --fmr
     * insert into zdb_active_data (zactvd_zdb_id) values ('ZDB-FMREL-220101-3');
     * insert into feature_marker_relationship (fmrel_ftr_zdb_id, fmrel_mrkr_zdb_id, fmrel_type, fmrel_zdb_id) values ('ZDB-ALT-220101-1', 'ZDB-GENE-220101-1', 'is allele of', 'ZDB-FMREL-220101-3');
     * insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type) values ('ZDB-FMREL-220101-3', 'ZDB-PUB-220101-1', 'standard');
     *
     *
     * -- fish and genotype
     * insert into zdb_active_data (zactvd_zdb_id) values ( 'ZDB-GENO-220101-1');
     * insert into genotype (geno_complexity_order, geno_is_extinct, geno_handle, geno_display_name, geno_name_order, geno_nickname, geno_is_wildtype, geno_zdb_id) values ( NULL, 'f', 'zf5555[2,1,1]', 'testg001<sup>zf5555/zf5555</sup>', NULL, 'zf5555[2,1,1]', 'f', 'ZDB-GENO-220101-1');
     * insert into zdb_active_data (zactvd_zdb_id) values ( 'ZDB-GENOFEAT-220101-1');
     * insert into genotype_feature (genofeat_zygocity, genofeat_dad_zygocity, genofeat_mom_zygocity, genofeat_geno_zdb_id, genofeat_feature_zdb_id, genofeat_zdb_id) values ( 'ZDB-ZYG-070117-1', 'ZDB-ZYG-070117-2', 'ZDB-ZYG-070117-2', 'ZDB-GENO-220101-1', 'ZDB-ALT-220101-1', 'ZDB-GENOFEAT-220101-1');
     * insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type) values ( 'ZDB-GENO-220101-1', 'ZDB-PUB-220101-1', 'standard');
     * insert into updates (comments, field_name, new_value, old_value, rec_id, submitter_id, submitter_name, upd_when) values ( 'create new record', 'geno_zdb_id', 'ZDB-GENO-220101-1', NULL, 'ZDB-PUB-220101-1', 'ZDB-PERS-210917-1', 'Taylor, Ryan', '2022-01-01 23:05:24.324');
     * insert into zdb_active_data (zactvd_zdb_id) values ( 'ZDB-FISH-220101-1');
     * insert into fish (fish_name, fish_name_order, fish_full_name, fish_handle, fish_order, fish_is_wildtype, fish_functional_affected_gene_count, fish_phenotypic_construct_count, fish_genotype_zdb_id, fish_zdb_id) values ( 'testg001<sup>zf5555/zf5555</sup>', 'testg001<sup>zf5555/zf5555</sup>', NULL, 'zf5555[2,1,1]', '0', 'f', '0', '0', 'ZDB-GENO-220101-1', 'ZDB-FISH-220101-1');
     * insert into updates (comments, field_name, new_value, old_value, rec_id, submitter_id, submitter_name, upd_when) values ( 'create new record', 'fish_zdb_id', 'ZDB-PUB-220101-1', NULL, 'ZDB-FISH-220101-1', 'ZDB-PERS-210917-1', 'Taylor, Ryan', '2022-01-01 23:05:24.341');
     * insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type) values ( 'ZDB-FISH-220101-1', 'ZDB-PUB-220101-1', 'standard');
     *
     *
     *
     * -- delete relationship
     * insert into updates (comments, field_name, new_value, old_value, rec_id, submitter_id, submitter_name, upd_when) values ('deleted feature/marker relationship between: zf5555 and testg001 of type is allele of', 'Feature', '', 'ZDB-FMREL-220101-3', 'ZDB-FMREL-220101-3', 'ZDB-PERS-210917-1', 'Taylor, Ryan', '2022-01-02 18:16:47.181');
     * delete from zdb_active_data where zactvd_zdb_id= 'ZDB-FMREL-220101-3';
     * --new fmr
     *
     *
     * insert into zdb_active_data (zactvd_zdb_id) values ( 'ZDB-FMREL-220102-5');
     * insert into feature_marker_relationship (fmrel_ftr_zdb_id, fmrel_mrkr_zdb_id, fmrel_type, fmrel_zdb_id) values ( 'ZDB-ALT-220101-1', 'ZDB-GENE-220101-2', 'is allele of', 'ZDB-FMREL-220102-5');
     * insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type) values ( 'ZDB-FMREL-220102-5', 'ZDB-PUB-220101-1', 'standard');
     * insert into updates (comments, field_name, new_value, old_value, rec_id, submitter_id, submitter_name, upd_when) values ( 'Created feature marker relationship', 'FeatureMarkerRelationship', 'FeatureMarkerRelationship{zdbID=''ZDB-FMREL-220102-5'', type=''is allele of'', feature=''Feature{zdbID=''ZDB-ALT-220101-1'', name=''zf5555'', lineNumber=''5555'', labPrefix=FeaturePrefix{prefixString=''zf'', institute=''Zebrafish Model Organism Database''}, abbreviation=''zf5555'', transgenicSuffix=''Tg'', isKnownInsertionSite=false, isDominantFeature=false, type=SEQUENCE_VARIANT}'', marker=MARKER
     * 	zdbID: ZDB-GENE-220101-2
     * 	symbol: testg002
     * 	name: testgene002
     * 	type: name[GENE]type[GENE]typeGroupStrings[GENEDOM_EFG_EREGION_K SEARCH_MK GENEDOM_AND_NTR GENEDOM SEARCHABLE_PROTEIN_CODING_GENE GENEDOM_PROD_PROTEIN CONSTRUCT_COMPONENTS GENE CAN_BE_PROMOTER DEFICIENCY_TLOC_MARK FEATURE SEARCH_MKSEG GENEDOM_AND_EFG CAN_HAVE_MRPHLN ]typeGroups[GENEDOM_PROD_PROTEIN CONSTRUCT_COMPONENTS CAN_HAVE_MRPHLN SEARCHABLE_PROTEIN_CODING_GENE GENEDOM_AND_EFG SEARCH_MK GENEDOM_AND_NTR DEFICIENCY_TLOC_MARK SEARCH_MKSEG FEATURE GENEDOM_EFG_EREGION_K CAN_BE_PROMOTER GENEDOM GENE ]
     *        }', NULL, 'ZDB-FMREL-220102-5', 'ZDB-PERS-210917-1', 'Taylor, Ryan', '2022-01-02 18:19:59.772');
     *
     *
     */

    @BeforeClass
    public static void setUpBeforeClass() {
        TestConfiguration.setAuthenticatedRootUser();
    }
}
