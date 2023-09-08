package org.zfin.uniprot;

import org.biojavax.CrossRef;
import org.biojavax.Note;
import org.biojavax.ontology.ComparableTerm;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.sequence.DBLink;
import org.zfin.uniprot.dto.DBLinkSlimDTO;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;
import static org.zfin.repository.RepositoryFactory.getSequenceRepository;

public class UniProtTools {
    public static final String MANUAL_CURATION_OF_PROTEIN_IDS = "ZDB-PUB-170131-9";
    public static final String UNIPROT_ID_LOAD_FROM_ENSEMBL = "ZDB-PUB-170502-16";
    public static final String MANUAL_CURATION_OF_UNIPROT_IDS = "ZDB-PUB-220705-2";
    public static final String CURATION_OF_PROTEIN_DATABASE_LINKS = "ZDB-PUB-020723-2";
    public static final String AUTOMATED_CURATION_OF_UNIPROT_DATABASE_LINKS = "ZDB-PUB-230615-71";

    public static final String[] NON_LOAD_PUBS = new String[]{MANUAL_CURATION_OF_PROTEIN_IDS, UNIPROT_ID_LOAD_FROM_ENSEMBL, MANUAL_CURATION_OF_UNIPROT_IDS};
    public static final String[] LOAD_PUBS = new String[]{CURATION_OF_PROTEIN_DATABASE_LINKS, AUTOMATED_CURATION_OF_UNIPROT_DATABASE_LINKS};


    //use passed in lambda expression to transform the notes
    public static void transformCrossRefNoteSetByTerm(CrossRef crossRef, ComparableTerm term, Function<String, String> transformer) {
        Set<Note> notes = crossRef.getNoteSet();
        for (Note note : notes) {
            if (note.getTerm().equals(term)) {
                note.setValue(transformer.apply(note.getValue()));
            }
        }
    }

    public static String getArgOrEnvironmentVar(String[] args, int index, String envVar, String defaultValue) {
        try {
            return getArgOrEnvironmentVar(args, index, envVar);
        } catch (IllegalArgumentException e) {
            if (defaultValue == null) {
                throw e;
            } else {
                return defaultValue;
            }
        }
    }

    public static String getArgOrEnvironmentVar(String[] args, int index, String envVar) throws IllegalArgumentException {
        if (args.length > index && args[index] != null) {
            if ("null".equals(args[index])) {
                throw new IllegalArgumentException("Empty required argument: " + envVar + ". Please provide it as an environment variable or as argument: " + (index + 1) + ". ");
            }
            return args[index];
        }

        String result = System.getenv(envVar);

        if (result == null) {
            throw new IllegalArgumentException("Missing required argument: " + envVar + ". Please provide it as an environment variable or as argument: " + (index + 1) + ". ");
        }

        return result;
    }

    //TODO: remove this method once we have a proper way to set accession
    //CrossRef doesn't allow setting accession, so we have to use reflection
    //We already wrap RichSequence with a custom class, so we should do the same for CrossRef
    public static void setAccession(CrossRef xref, String accession) {
        try {
            Method method;
            method = xref.getClass().getDeclaredMethod("setAccession", String.class);
            method.setAccessible(true);
            method.invoke(xref, accession);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> getAttributionsSupportingGeneAccessionRelationship(String geneID, String accession, List<DBLinkSlimDTO> dbLinkSlimDTOs) {
        if (dbLinkSlimDTOs == null) {
            return Collections.emptyList();
        }

        return dbLinkSlimDTOs.stream()
                .filter(dbLinkSlimDTO -> dbLinkSlimDTO.getAccession().equals(accession) && dbLinkSlimDTO.getDataZdbID().equals(geneID))
                .flatMap(dbLinkSlimDTO -> dbLinkSlimDTO.getPublicationIDs().stream())
                .toList();
    }

    public static boolean isGeneAccessionRelationshipSupportedByNonLoadPublication(String geneID, String accession, List<DBLinkSlimDTO> dbLinkSlimDTOs) {
        List<String> attributionPubIDs = getAttributionsSupportingGeneAccessionRelationship(geneID, accession, dbLinkSlimDTOs);
        if (attributionPubIDs == null) {
            return false;
        }
        return attributionPubIDs.stream().anyMatch(pubID -> isNonLoadPublication(pubID));
    }

    public static boolean isAnyGeneAccessionRelationshipSupportedByNonLoadPublication(String accession,
                                                                                      List<String> geneIDs,
                                                                                      List<DBLinkSlimDTO> dbLinkSlimDTOs) {
        return geneIDs.stream().anyMatch(geneID -> isGeneAccessionRelationshipSupportedByNonLoadPublication(geneID, accession, dbLinkSlimDTOs));
    }

    public static boolean isLoadPublication(String pubID) {return List.of(LOAD_PUBS).contains(pubID);}

    public static boolean isNonLoadPublication(String pubID) {return !isLoadPublication(pubID);}

}
