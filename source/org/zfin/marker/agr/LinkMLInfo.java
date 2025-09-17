package org.zfin.marker.agr;

import org.alliancegenome.curation_api.model.entities.AffectedGenomicModel;
import org.alliancegenome.curation_api.model.entities.VocabularyTerm;
import org.alliancegenome.curation_api.model.entities.slotAnnotations.AgmFullNameSlotAnnotation;
import org.alliancegenome.curation_api.model.ingest.dto.ExperimentalConditionDTO;
import org.alliancegenome.curation_api.model.ingest.dto.IngestDTO;
import org.zfin.expression.ExperimentCondition;
import org.zfin.infrastructure.ActiveData;
import org.zfin.mutant.DiseaseAnnotation;
import org.zfin.mutant.Fish;
import org.zfin.mutant.FishExperiment;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.publication.Publication;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.zfin.repository.RepositoryFactory.getMutantRepository;
import static org.zfin.repository.RepositoryFactory.getOntologyRepository;

public class LinkMLInfo extends AbstractScriptWrapper {

    static String LINK_ML_VERSION = "v1.11.0";
    protected int numfOfRecords = 0;

    public LinkMLInfo(int number) {
        numfOfRecords = number;
    }

    static void mainParent(String[] args){
        if (args.length == 0)
            throw new RuntimeException("No argument / version provided. Should start with a 'v'");
        LINK_ML_VERSION = args[0];
        if (!LINK_ML_VERSION.toLowerCase().startsWith("v"))
            throw new RuntimeException("First argument is not a valid version, does not start with a 'v'");
        System.out.println("Link ML Version: " + LINK_ML_VERSION);
    }

    public String getSingleReference(Publication publication) {
        if (publication.getAccessionNumber() != null)
            return "PMID:" + publication.getAccessionNumber();
        return "ZFIN:" + publication.getZdbID();
    }

    protected IngestDTO getIngestDTO() {
        IngestDTO ingestDTO = new IngestDTO();
        ingestDTO.setLinkMLVersion(LINK_ML_VERSION);
        return ingestDTO;
    }


    public static String format(String zdbID) {
        GregorianCalendar date = ActiveData.getDateFromId(zdbID);
        return format(date);
    }

    public static String format(GregorianCalendar calendar) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        fmt.setCalendar(calendar);
        return fmt.format(calendar.getTime());
    }

    public static String format(Date calendar) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        Calendar instance = Calendar.getInstance();
        instance.setTime(calendar);
        fmt.setCalendar(instance);
        return fmt.format(calendar.getTime());
    }

    private void populateConditionClass(ExperimentalConditionDTO expcond, ExperimentCondition condition) {
        String oboID = condition.getZecoTerm().getOboID();
        if (highLevelConditionTerms.stream().map(GenericTerm::getOboID).toList().contains(oboID)) {
            expcond.setConditionClassCurie(oboID);
        } else {
            Optional<GenericTerm> highLevelterm = highLevelConditionTerms.stream().filter(parentTerm -> getOntologyRepository().isParentChildRelationshipExist(parentTerm, condition.getZecoTerm()))
                .findFirst();
            if (highLevelterm.isPresent()) {
                expcond.setConditionClassCurie(highLevelterm.get().getOboID());
                expcond.setConditionIdCurie(oboID);
            }
        }
    }

    private AffectedGenomicModel getAffectedGenomicModel(Fish fish) {
        AffectedGenomicModel model = new AffectedGenomicModel();
        model.setCurie("ZFIN:" + fish.getZdbID());
        AgmFullNameSlotAnnotation agmFullName = new AgmFullNameSlotAnnotation();
        agmFullName.setDisplayText(fish.getDisplayName());
        agmFullName.setFormatText(fish.getDisplayName());
        VocabularyTerm nameType = new VocabularyTerm();
        nameType.setName("full_name");
        agmFullName.setNameType(nameType);
        model.setAgmFullName(agmFullName);
        return model;
    }

    public org.alliancegenome.curation_api.model.ingest.dto.ConditionRelationDTO populateExperimentConditions(FishExperiment fishExperiment) {
        org.alliancegenome.curation_api.model.ingest.dto.ConditionRelationDTO relation = new org.alliancegenome.curation_api.model.ingest.dto.ConditionRelationDTO();
        if (fishExperiment.getExperiment() != null) {
            List<ExperimentCondition> allConditions = getMutantRepository().getExperimentConditions(fishExperiment.getExperiment());
            relation.setConditionRelationTypeName("has_condition");
            List<ExperimentalConditionDTO> expconds = new ArrayList<>();
            for (ExperimentCondition condition : allConditions) {
                ExperimentalConditionDTO expcond = new ExperimentalConditionDTO();
                String conditionStatement = condition.getZecoTerm().getTermName();
                if (condition.getAoTerm() != null) {
                    conditionStatement = conditionStatement + " " + condition.getAoTerm().getTermName();
                    expcond.setConditionAnatomyCurie(condition.getAoTerm().getOboID());
                }
                if (condition.getChebiTerm() != null) {
                    expcond.setConditionChemicalCurie(condition.getChebiTerm().getOboID());
                    conditionStatement = conditionStatement + " " + condition.getChebiTerm().getTermName();
                }
                if (condition.getGoCCTerm() != null) {
                    expcond.setConditionGeneOntologyCurie(condition.getGoCCTerm().getOboID());
                    conditionStatement = conditionStatement + " " + condition.getGoCCTerm().getTermName();
                }
                if (condition.getTaxaonymTerm() != null) {
                    expcond.setConditionTaxonCurie(condition.getTaxaonymTerm().getOboID());
                    conditionStatement = conditionStatement + " " + condition.getTaxaonymTerm().getTermName();
                }
                populateConditionClass(expcond, condition);
/*
                String highLevelTermName =
                expcond.setConditionStatement(expcond.getConditionClass()+": "+conditionStatement);
*/
                expconds.add(expcond);
            }
            relation.setConditionDtos(expconds);

        }
        return relation;
    }

    public EvidenceDTO getEvidenceDTO(Publication publication, List<String> evidences) {
        PublicationAgrDTO fixedPub = new PublicationAgrDTO();
        List<String> pubPages = new ArrayList<>();
        pubPages.add("reference");
        CrossReferenceDTO pubXref = new CrossReferenceDTO("ZFIN", publication.getZdbID(), pubPages);
        if (publication.getAccessionNumber() != null) {
            fixedPub.setPublicationId("PMID:" + publication.getAccessionNumber());
            fixedPub.setCrossReference(pubXref);
        } else {
            fixedPub.setPublicationId("ZFIN:" + publication.getZdbID());
        }

        EvidenceDTO evDto = new EvidenceDTO(fixedPub);
        evDto.setEvidenceCodes(evidences);
        return evDto;
    }

    // hard-coded for now as the ECO ontology does not provide the codes in
    // abbreviated form easily. The term names are very long and only in the synonym list
    // you can find TAS an IC.
    // Needs to be changed in the future.
    private String getEvidenceCodeString(DiseaseAnnotation diseaseAnnotations) {
        return getEvidenceCodeFromString(diseaseAnnotations.getEvidenceCode().getZdbID());
    }

    private String getEvidenceCodeFromString(String ecoValue) {
        if (ecoValue.equals("ZDB-TERM-170419-250"))
            return "ECO:0000304";
        if (ecoValue.equals("ZDB-TERM-170419-251"))
            return "ECO:0000305";
        return "";
    }

    class Item {

        private String name;
        private int qty;
        private BigDecimal price;

        public Item(String name, int qty, BigDecimal price) {
            this.name = name;
            this.qty = qty;
            this.price = price;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getQty() {
            return qty;
        }

        public void setQty(int qty) {
            this.qty = qty;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        //constructors, getter/setters
    }

    // ToDo: This list should be a slim in ZECO to identify those high-level terms.
    public static final List<GenericTerm> highLevelConditionTerms = new ArrayList<>(18);

    static {
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-7", "ZECO:0000105"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-13", "ZECO:0000111"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-14", "ZECO:0000112"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-15", "ZECO:0000113"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-33", "ZECO:0000131"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-42", "ZECO:0000140"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-45", "ZECO:0000143"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-48", "ZECO:0000146"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-56", "ZECO:0000154"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-62", "ZECO:0000160"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-82", "ZECO:0000182"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-108", "ZECO:0000208"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-122", "ZECO:0000222"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-129", "ZECO:0000229"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-171108-6", "ZECO:0000252"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-3", "ZECO:0000101"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-5", "ZECO:0000103"));
        // make sure it's the last entry as it is a root term.
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-6", "ZECO:0000104"));
    }

    private static void populateHighLevelConditionTerms() {
        highLevelConditionTerms.forEach(genericTerm -> {
            genericTerm.setTermName(getOntologyRepository().getTermByOboID(genericTerm.getOboID()).getTermName());
        });
    }

}
//test