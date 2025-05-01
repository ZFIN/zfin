package org.zfin.construct.presentation;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.hibernate.query.Query;
import org.springframework.stereotype.Service;
import org.zfin.Species;
import org.zfin.antibody.AntibodyService;
import org.zfin.construct.ConstructComponent;
import org.zfin.construct.ConstructCuration;
import org.zfin.construct.InvalidConstructNameException;
import org.zfin.construct.name.*;
import org.zfin.construct.repository.ConstructRepository;
import org.zfin.database.InformixUtil;
import org.zfin.gwt.root.dto.TermNotFoundException;
import org.zfin.gwt.root.ui.DuplicateEntryException;
import org.zfin.infrastructure.ControlledVocab;
import org.zfin.infrastructure.DataAlias;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerType;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerAttributionService;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.ForeignDBDataType;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.repository.SequenceRepository;

import java.util.*;
import java.util.stream.Collectors;

import static org.zfin.construct.name.Cassette.COMPONENT_SEPARATOR;
import static org.zfin.framework.HibernateUtil.currentSession;

@Service
@Log4j2
public class ConstructComponentService {
    private static final ConstructRepository cr = RepositoryFactory.getConstructRepository();
    private static final MarkerRepository mr = RepositoryFactory.getMarkerRepository();
    private static final PublicationRepository pr = RepositoryFactory.getPublicationRepository();
    private static final InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
    private static final SequenceRepository sr = RepositoryFactory.getSequenceRepository();
    private static final String openP = "(";

    //TODO convert category to Enum

    /**
     * Sets the beginning components of the construct, eg. "TgBAC(atoh1c:GAL4FF)" would get the components: "Tg", "BAC", "("
     *
     * @param constructType
     * @param constructPrefix
     * @param constructId
     */
    public static void setConstructWrapperComponents(String constructType, String constructPrefix, String constructId) {
        //2 or 3 components: type, prefix (optional) and Open Parenthesis: "("
        boolean emptyPrefix = constructPrefix.equals("");
        int constructComponentNumber = 1;
        int cassetteNumber = 1;

        //first component is always the construct type
        mr.addConstructComponent(cassetteNumber, constructComponentNumber++, constructId, constructType, ConstructComponent.Type.TEXT_COMPONENT, "construct wrapper component", null);

        //second component is the construct prefix, if it exists
        if (!emptyPrefix) {
            mr.addConstructComponent(cassetteNumber, constructComponentNumber++, constructId, constructPrefix, ConstructComponent.Type.TEXT_COMPONENT, "construct wrapper component", null);
        }

        //last component is always the open parenthesis
        mr.addConstructComponent(cassetteNumber, constructComponentNumber++, constructId, openP, ConstructComponent.Type.CONTROLLED_VOCAB_COMPONENT, "construct wrapper component", ir.getCVZdbIDByTerm(openP).getZdbID());
    }

    public static void setPromotersAndCoding(Cassette cassette, String zdbID, String newPub) {

        Set<Marker> promoterMarkers = new HashSet<>();
        Set<Marker> codingMarkers = new HashSet<>();

        Promoter promoter = cassette.getPromoter();
        Coding coding = cassette.getCoding();

        //add the promoter parts
        for (String promoterPart : promoter.getPromoter()) {
            createComponentRecords(StringUtils.trim(promoterPart),"promoter component", promoterMarkers,ConstructComponent.Type.PROMOTER_OF, cassette.getCassetteNumber(), newPub,zdbID);
        }

        //add the ":" separator (if one needed)
        if (coding.size() > 0) {
            createComponentRecords(COMPONENT_SEPARATOR, "promoter component", promoterMarkers, ConstructComponent.Type.PROMOTER_OF, cassette.getCassetteNumber(), newPub, zdbID);
        }

        //add the coding parts
        for (String codingPart : coding.getCoding()) {
            createComponentRecords(StringUtils.trim(codingPart), "coding component", codingMarkers, ConstructComponent.Type.CODING_SEQUENCE_OF, cassette.getCassetteNumber(), newPub, zdbID);
        }

        //add relationships
        if (!promoterMarkers.isEmpty() || !codingMarkers.isEmpty()) {
            cr.addConstructRelationships(promoterMarkers, codingMarkers, cr.getConstructByID(zdbID), newPub);
        }

    }

     public static int getComponentCount(String zdbID){
        String sqlCount = " select MAX(cc_order) from construct_component where cc_construct_zdb_id=:zdbID ";
        Query query = currentSession().createNativeQuery(sqlCount);
        query.setParameter("zdbID", zdbID);
        int lastComp = (Integer) query.uniqueResult() + 1;
        return lastComp;
    }

    //TODO: refactor this. Some ideas:
    // 1. it's weird that componentArray is getting populated in the method
    // 2. type is being passed in but overridden for some cases
    // 3. can componentCategory handling be improved?
    /**
     * Creates the construct component record in the database for a given component string.
     * If the component is a marker, it will add a marker component (either promoter or coding based on `type`) and it will be added to the componentArray.
     * If not a marker, it will create either a text or controlled vocab component.
     *
     * @param componentString the string representation of the component to be processed
     * @param componentCategory either "promoter component" or "coding component"
     * @param componentArray a collection to be populated with markers
     * @param type the type of component to be created. If the component is not a marker, this will be overridden.
     * @param cassetteNumber the cassette number (starting at 1)
     * @param constructPub the publication of the construct
     * @param constructID the construct ID
     *
     */
    private static void createComponentRecords(String componentString,String componentCategory,Set<Marker> componentArray,ConstructComponent.Type type,int cassetteNumber,String constructPub,String constructID) {
        int componentCount = getComponentCount(constructID);
        String componentZdbID = null;
        if (!(componentString.isEmpty())) {
            Marker mrkr = mr.getMarkerByAbbreviationAndAttribution(componentString, constructPub);
            if (mrkr != null) {
                componentZdbID = mrkr.getZdbID();
                componentArray.add(mrkr);
            } else {
                ControlledVocab cVocab = ir.getCVZdbIDByTerm(componentString);
                if (cVocab != null) {
                    componentZdbID = cVocab.getZdbID();
                    type = ConstructComponent.Type.CONTROLLED_VOCAB_COMPONENT;
                } else {
                    type = ConstructComponent.Type.TEXT_COMPONENT;
                }
            }
            mr.addConstructComponent(cassetteNumber, componentCount, constructID, componentString, type, componentCategory, componentZdbID);
        }
    }

    /**
     * Validate construct name
     * If it fails validation, return an error message
     * If it passes validation, return an empty optional
     * @param constructName
     * @return
     */
    public static Optional<String> getValidationErrorMessageForConstructName(String constructName) {
        if (constructName.contains("..")) {
            return Optional.of("\"" + constructName + "\" contains a dot followed by a dot, Please check");
        } else if (constructName.contains(".,")) {
            return Optional.of("\"" + constructName + "\" contains a dot followed by a comma, Please check");
        } else if (constructName.contains("()")) {
            return Optional.of("\"" + constructName + "\": Construct Name cannot be blank");
        }
        return Optional.empty();
    }

    public static boolean isConstructNameInvalid(String constructName) {
        return getValidationErrorMessageForConstructName(constructName).isPresent();
    }

    public static Optional<Marker.Type> getConstructTypeEnumByConstructName(String constructName) {
        return switch (constructName.substring(0, 2)) {
            case "Tg" -> Optional.of(Marker.Type.TGCONSTRCT);
            case "Et" -> Optional.of(Marker.Type.ETCONSTRCT);
            case "Gt" -> Optional.of(Marker.Type.GTCONSTRCT);
            case "Pt" -> Optional.of(Marker.Type.PTCONSTRCT);
            default -> Optional.empty();
        };
    }

    public static Optional<String> getTypeAbbreviationFromType(Marker.Type type) {
        return switch (type) {
            case TGCONSTRCT -> Optional.of("Tg");
            case ETCONSTRCT -> Optional.of("Et");
            case GTCONSTRCT -> Optional.of("Gt");
            case PTCONSTRCT -> Optional.of("Pt");
            default -> Optional.empty();
        };
    }

    public static Optional<MarkerType> getConstructTypeByConstructName(String constructName) {
        Optional<Marker.Type> typeEnum = getConstructTypeEnumByConstructName(constructName);
        return typeEnum.map(type -> mr.getMarkerTypeByName(type.toString()));
    }

    public static Marker createNewConstructFromSubmittedForm(AddConstructFormFields form) throws InvalidConstructNameException {
        //will use the logged in user as the submitting curator
        return createNewConstructFromSubmittedForm(form, null);
    }

    public static Marker createNewConstructFromSubmittedForm(AddConstructFormFields form, Person submittingCurator) throws InvalidConstructNameException {
        validateConstructNameOrThrowException(form.getConstructName());
        ConstructCuration newConstruct = ConstructCuration.create(form.getConstructName());
        newConstruct.setPublicCommentsIfNotEmpty(form.getConstructComments());

        Publication constructPub = pr.getPublication(form.getPubZdbID());

        //persist the new construct to the DB as the submitting curator (if null, use currently logged in user)
        cr.createConstruct(newConstruct, constructPub, submittingCurator);

        //the new construct is now persisted to the DB, so we can get its ZDB ID
        String constructZdbID = newConstruct.getZdbID();

        //create the construct components in the DB -- "Tg4(ubb:mir155smn1-DsRed)" becomes ["Tg", "4", "(", "ubb", ":", "mir155smn1", "-", "DsRed", ")"] with metadata
        if (form.getConstructStoredName() != null) {
            createConstructComponentsInDatabaseUsingStoredName(form, constructZdbID);
        } else if (form.getConstructNameObject() != null) {
            createConstructComponentsInDatabase(form.getConstructNameObject(), constructZdbID, form.getPubZdbID());
        } else {
            throw new RuntimeException("No construct name or stored name found");
        }

        //adding construct record to marker table
        InformixUtil.runProcedure("regen_construct_marker", constructZdbID + "");

        //retrieve the latest version of the construct from the marker table
        Marker latestConstruct = mr.getMarkerByID(newConstruct.getZdbID());

        //add the alias, curator note, and sequence to the construct
        setConstructAliasNoteAndSequence(form, constructPub, latestConstruct);

        return latestConstruct;
    }




        private static void setConstructAliasNoteAndSequence(AddConstructFormFields form, Publication constructPub, Marker latestConstruct) {
        if (!StringUtils.isEmpty(form.getConstructAlias())) {
            mr.addMarkerAlias(latestConstruct, form.getConstructAlias(), constructPub);
        }

        if (!StringUtils.isEmpty(form.getConstructCuratorNote())) {
            mr.addMarkerDataNote(latestConstruct, form.getConstructCuratorNote());
        }

        if (!StringUtils.isEmpty(form.getConstructSequence())) {
            ReferenceDatabase genBankRefDB = sr.getReferenceDatabase(ForeignDB.AvailableName.GENBANK,
                    ForeignDBDataType.DataType.GENOMIC, ForeignDBDataType.SuperType.SEQUENCE, Species.Type.ZEBRAFISH);
            mr.addDBLink(latestConstruct, form.getConstructSequence(), genBankRefDB, form.getPubZdbID());
        }
    }

    public static Marker updateConstructName(String constructZdbID, ConstructName newName, String pubZdbID) throws InvalidConstructNameException {
        Marker oldMarker = mr.getMarkerByID(constructZdbID);
        String oldName = oldMarker.getName();

        //TODO: add better handling for this
        // Do we want to support changing marker type for constructs?
        String oldNameType = oldName.substring(0, 2);
        String newNameType = newName.getTypeAbbreviation();
        if (!oldNameType.equals(newNameType)) {
            throw new InvalidConstructNameException("Cannot change construct type");
        }

        //updating construct to the same name is allowed--presumably the components will have changed in some way
        if (!oldMarker.getName().equals(newName.toString())) {
            validateConstructNameOrThrowException(newName.toString());
        }

        //update the construct relationships based on the name change
        updateConstructRelationships(constructZdbID, newName, pubZdbID);

        //delete the components
        mr.deleteConstructComponents(constructZdbID);

        //create the new components
        createConstructComponentsInDatabase(newName, constructZdbID, pubZdbID);

        //update the construct name
        cr.updateConstructName(constructZdbID, newName.toString());

        //create alias for old name
        AntibodyService.addDataAliasRelatedEntity(constructZdbID, oldName, pubZdbID);

        //adding construct record to marker table
        InformixUtil.runProcedure("regen_construct_marker", constructZdbID + "");

        return mr.getMarkerByID(constructZdbID);
    }

    /**
     * Create the construct components in the database
     * This includes the wrapper components (e.g. "Tg", "(", ")"), the prefix, and the cassette components (e.g. "promoter", "coding")
     * @param form
     * @param constructZdbID
     */
    //TODO: deprecate this method and all uses of StoredName (just use ConstructName). jquery for the stored name is used by legacy interface
    private static void createConstructComponentsInDatabaseUsingStoredName(AddConstructFormFields form, String constructZdbID) {
        ConstructName constructName = new ConstructName(form.getConstructName(), form.getConstructPrefix());
        Cassettes cassettes = Cassettes.fromStoredName(form.getConstructStoredName());
        constructName.setCassettes(cassettes);

        createConstructComponentsInDatabase(constructName, constructZdbID, form.getPubZdbID());
    }

    private static void createConstructComponentsInDatabase(ConstructName constructName, String constructZdbID, String pubZdbID) {
        Cassettes cassettes = constructName.getCassettes();

        //create the constructs for the opening wrapper components -- e.g. "Tg", "optionalPrefix", "("
        ConstructComponentService.setConstructWrapperComponents(constructName.getTypeAbbreviation(), constructName.getPrefix(), constructZdbID);

        //create the construct components for the cassettes
        for (Cassette cassette : cassettes) {
            ConstructComponentService.setPromotersAndCoding(cassette, constructZdbID, pubZdbID);
        }

        //create the construct components for the closing wrapper components -- e.g. ")"
        int lastComp = ConstructComponentService.getComponentCount(constructZdbID);
        mr.addConstructComponent(cassettes.size(), lastComp, constructZdbID, ")", ConstructComponent.Type.CONTROLLED_VOCAB_COMPONENT, "construct wrapper component", ir.getCVZdbIDByTerm(")").getZdbID());
    }

    private static void validateConstructNameOrThrowException(String constructName) throws InvalidConstructNameException {
        //Some validation
        List<Marker> markerList = mr.getMarkersByAbbreviation(constructName);

        if (markerList.size() > 0) {
            throw new InvalidConstructNameException("\"" + constructName + "\" is not a unique name");
        }
        if (isConstructNameInvalid(constructName)) {
            throw new InvalidConstructNameException(getValidationErrorMessageForConstructName(constructName).orElse("Invalid construct name"));
        }
    }

    /**
     * Get the ConstructName object from the construct ID
     * It retrieves the ConstructComponent objects from the database and parses them into a ConstructName object
     * @param constructID
     * @return
     */
    public static ConstructName getExistingConstructName(String constructID) {
        List<ConstructComponent> components = cr.getConstructComponentsByConstructZdbId(constructID);
        Iterator<ConstructComponent> componentIterator = components.iterator();
        ConstructName constructName = new ConstructName();

        //Set the TYPE
        ConstructComponent currentComponent = componentIterator.next();
        constructName.setTypeByAbbreviation(currentComponent.getComponentValue());

        //Set the PREFIX if it exists
        currentComponent = componentIterator.next();
        if (currentComponent.getComponentValue().equals("(")) {
            constructName.setPrefix("");
        } else {
            constructName.setPrefix(currentComponent.getComponentValue());
        }

        //Set the Cassettes
        Cassette currentCassette = new Cassette();
        Promoter currentPromoter = new Promoter();
        Coding currentCoding = new Coding();
        int cassetteNumber = 1;
        int componentNumber = 2;

        //are we parsing promoter parts of the cassette (true), or the coding parts (false)
        boolean parsingCassettePromoter = true;

        while (componentIterator.hasNext()) {
            componentNumber++;
            currentComponent = componentIterator.next();
            if(cassetteNumber != currentComponent.getComponentCassetteNum()) {
                currentCassette.setPromoter(currentPromoter);
                currentCassette.setCoding(currentCoding);
                constructName.addCassette(currentCassette);
                currentCassette = new Cassette();
                cassetteNumber++;
                currentPromoter = new Promoter();
                currentCoding = new Coding();
                parsingCassettePromoter = true;
            }
            if (currentComponent.getComponentValue().equals(COMPONENT_SEPARATOR)) {
                continue;
            } else if (currentComponent.getComponentValue().equals(")") && !componentIterator.hasNext()) {
                continue;
            }
            if (currentComponent.getType().equals(ConstructComponent.Type.CODING_SEQUENCE_OF)) {
                parsingCassettePromoter = false; //parsing coding part now
                currentCoding.addCodingPart(currentComponent.getComponentValue());
                continue;
            }

            switch (currentComponent.getComponentCategoryEnum()) {
                case PROMOTER_COMPONENT -> currentPromoter.addPromoterPart(currentComponent.getComponentValue());
                case CODING_COMPONENT, CODING_SEQUENCE_COMPONENT -> {
                    parsingCassettePromoter = false;
                    currentCoding.addCodingPart(currentComponent.getComponentValue());
                }
                case CASSETTE_DELIMITER -> {
                    if (parsingCassettePromoter) {
                        currentPromoter.addPromoterPart(currentComponent.getComponentValue());
                    } else {
                        currentCoding.addCodingPart(currentComponent.getComponentValue());
                    }
                }
                case CONSTRUCT_WRAPPER_COMPONENT -> {} // Do nothing
                default -> log.error("Unknown component category: " + currentComponent.getComponentCategory());
            }
        }

        currentCassette.setPromoter(currentPromoter);
        currentCassette.setCoding(currentCoding);
        constructName.addCassette(currentCassette);

        return constructName;
    }

    /**
     * When renaming a construct, this gets the old name and does a comparison to figure out which markers were added/deleted
     * from the promoters and codings. For any marker removed, this removes the construct relationship. For any markers added,
     * this creates new relationships.
     *
     * @param constructZdbID
     * @param newConstructName
     * @param pubZdbID
     */
    private static void updateConstructRelationships(String constructZdbID, ConstructName newConstructName, String pubZdbID) {
        ConstructName oldConstructName = getExistingConstructName(constructZdbID);
        CassettesDiff diff = CassettesDiff.calculate(oldConstructName.getCassettes(), newConstructName.getCassettes());

        if (diff.getCodingMarkersAdded().isEmpty() && diff.getPromoterMarkersAdded().isEmpty()) {
            //no new markers, so no new relationships to add
        } else {
            Set<Marker> promotersAdded = diff.getPromoterMarkersAdded()
                    .stream()
                    .map(mr::getMarkerByAbbreviation)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            Set<Marker> codingAdded = diff.getCodingMarkersAdded()
                    .stream()
                    .map(mr::getMarkerByAbbreviation)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            cr.addConstructRelationships(promotersAdded, codingAdded, cr.getConstructByID(constructZdbID), pubZdbID);
            addMarkerAttributionIfNotPresent(promotersAdded, pubZdbID);
            addMarkerAttributionIfNotPresent(codingAdded, pubZdbID);
        }

        if (diff.getCodingMarkersRemoved().isEmpty() && diff.getPromoterMarkersRemoved().isEmpty()) {
            //no markers removed, so no relationships to remove
        } else {
            Set<Marker> promotersRemoved = diff.getPromoterMarkersRemoved()
                    .stream()
                    .map(mr::getMarkerByAbbreviation)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            Set<Marker> codingRemoved = diff.getCodingMarkersRemoved()
                    .stream()
                    .map(mr::getMarkerByAbbreviation)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            cr.removeConstructRelationships(promotersRemoved, codingRemoved, cr.getConstructByID(constructZdbID), pubZdbID);
        }
    }

    private static void addMarkerAttributionIfNotPresent(Set<Marker> markers, String pubZdbID) {
        markers.forEach(marker -> {
            try {
                MarkerAttributionService.addAttributionForMarker(marker, pubZdbID);
            } catch (TermNotFoundException e) {
                throw new RuntimeException(e);
            } catch (DuplicateEntryException e) {
                //do nothing
            }
        });
    }
}

