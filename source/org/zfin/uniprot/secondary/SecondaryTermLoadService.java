package org.zfin.uniprot.secondary;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.zfin.sequence.ReferenceDatabase;

import java.util.HashMap;
import java.util.Map;

import static org.zfin.repository.RepositoryFactory.getSequenceRepository;
import static org.zfin.uniprot.UniProtTools.AUTOMATED_CURATION_OF_UNIPROT_DATABASE_LINKS;

@Getter
@Setter
@Log4j2
public class SecondaryTermLoadService {

    public static final String DBLINK_PUBLICATION_ATTRIBUTION_ID = AUTOMATED_CURATION_OF_UNIPROT_DATABASE_LINKS;
    public static final String EXTNOTE_PUBLICATION_ATTRIBUTION_ID = AUTOMATED_CURATION_OF_UNIPROT_DATABASE_LINKS;

    public static final String EXTNOTE_REFERENCE_DATABASE_ID = "ZDB-FDBCONT-040412-47";
    public static final String INTERPRO_REFERENCE_DATABASE_ID = "ZDB-FDBCONT-040412-48";
    // ZFIN-10418 retired Pfam (-50) and PROSITE (-51). EC stays: it feeds the ec2go derivation
    // and is retired with it (see LOAD_INTERPRO2GO_EC2GO).
    public static final String EC_REFERENCE_DATABASE_ID = "ZDB-FDBCONT-040412-49";
    public static final Map<String, ReferenceDatabase> referenceDatabasesCache = new HashMap<>();

    public static ReferenceDatabase getReferenceDatabaseForAction(SecondaryTermLoadAction action) {
        return getReferenceDatabase(getReferenceDatabaseIDForAction(action));
    }

    public static String getReferenceDatabaseIDForAction(SecondaryTermLoadAction action) {
        String referenceDatabaseID = null;
        switch (action.getDbName()) {
            case INTERPRO -> referenceDatabaseID = INTERPRO_REFERENCE_DATABASE_ID;
            case EC -> referenceDatabaseID = EC_REFERENCE_DATABASE_ID;
            default -> log.error("Unknown dblink dbname to load " + action.getDbName());
        }
        return referenceDatabaseID;
    }

    public static ReferenceDatabase getReferenceDatabase(String referenceDatabaseID) {
        ReferenceDatabase refDB = referenceDatabasesCache.get(referenceDatabaseID);
        if (refDB == null) {
            refDB = getSequenceRepository().getReferenceDatabaseByID(referenceDatabaseID);
        }
        return refDB;
    }
}
