package org.zfin.uniprot;

import org.biojavax.CrossRef;
import org.biojavax.Note;
import org.biojavax.SimpleRichAnnotation;
import org.biojavax.ontology.ComparableTerm;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.sequence.DBLink;
import org.zfin.uniprot.datfiles.UniProtFormatZFIN;
import org.zfin.uniprot.dto.UniProtContextSequenceDTO;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;
import static org.zfin.repository.RepositoryFactory.getSequenceRepository;

public class UniProtTools {
    public static final String MANUAL_CURATION_OF_PROTEIN_IDS = "ZDB-PUB-170131-9";
    public static final String UNIPROT_ID_LOAD_FROM_ENSEMBL = "ZDB-PUB-170502-16";
    public static final String MANUAL_CURATION_OF_UNIPROT_IDS = "ZDB-PUB-220705-2";

    public static final String[] NON_LOAD_PUBS = new String[]{MANUAL_CURATION_OF_PROTEIN_IDS, UNIPROT_ID_LOAD_FROM_ENSEMBL, MANUAL_CURATION_OF_UNIPROT_IDS};


    //use passed in lambda expression to transform the notes
    public static void transformCrossRefNoteSetByTerm(CrossRef crossRef, ComparableTerm term, Function<String, String> transformer) {
        Set<Note> notes = crossRef.getNoteSet();
        for (Note note : notes) {
            if (note.getTerm().equals(term)) {
                note.setValue(transformer.apply(note.getValue()));
            }
        }
    }

    public static String getArgOrEnvironmentVar(String[] args, int index, String envVar) {
        if (args.length > index && args[index] != null) {
            return args[index];
        }

        String result = System.getenv(envVar);

        if (result == null) {
            System.err.println("Missing required argument: " + envVar + ". Please provide it as an environment variable or as argument: " + (index + 1) + ". ");
            System.exit(1);
        }

        return result;
    }

    public static void setAccession(CrossRef xref, String accession) {
        //CrossRef doesn't allow setting accession, so we have to use reflection
        try {
            Method method;
            method = xref.getClass().getDeclaredMethod("setAccession", String.class);
            method.setAccessible(true);
            method.invoke(xref, accession);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> getAttributionsSupportingGeneAccessionRelationship(String geneID, String accession) {
        DBLink dblink = getSequenceRepository().getDBLink(geneID, accession);
        if (dblink != null) {
            List<RecordAttribution> attributions = getInfrastructureRepository().getRecordAttributions(dblink.getZdbID());
            return attributions.stream().map(RecordAttribution::getSourceZdbID).toList();
        } else {
            return null;
        }
    }

    public static boolean isGeneAccessionRelationshipSupportedByNonLoadPublication(String geneID, String accession) {
        List<String> attributionPubIDs = getAttributionsSupportingGeneAccessionRelationship(geneID, accession);
        if (attributionPubIDs == null) {
            return false;
        }
        return attributionPubIDs.stream().anyMatch(pubID -> isNonLoadPublication(pubID));
    }

    public static boolean isGeneAccessionRelationshipSupportedByNonLoadPublication(UniProtContextSequenceDTO sequence) {
        return isGeneAccessionRelationshipSupportedByNonLoadPublication(sequence.getDataZdbID(), sequence.getAccession());
    }

    public static boolean isAnyGeneAccessionRelationshipSupportedByNonLoadPublication(String accession, List<String> geneIDs) {
        return geneIDs.stream().anyMatch(geneID -> isGeneAccessionRelationshipSupportedByNonLoadPublication(geneID, accession));
    }


    private static boolean isNonLoadPublication(String pubID) {
        return List.of(NON_LOAD_PUBS).contains(pubID);
    }
}
