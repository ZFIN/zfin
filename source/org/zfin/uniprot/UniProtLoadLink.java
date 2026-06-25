package org.zfin.uniprot;

import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.sequence.ForeignDB;

import java.util.EnumMap;
import java.util.Map;

import static org.zfin.repository.RepositoryFactory.getSequenceRepository;

public record UniProtLoadLink(String title, String href)  implements Comparable<UniProtLoadLink> {

    // foreign_db is effectively static reference data; cache each database's URL prefix so we
    // don't run a getForeignDBByName() query for every link we build. During a full UniProt load
    // this was ~462k identical FOREIGN_DB queries (one per link), each in its own transaction.
    private static final Map<ForeignDB.AvailableName, String> URL_PREFIX_CACHE =
            new EnumMap<>(ForeignDB.AvailableName.class);
    @Override
    public int compareTo(UniProtLoadLink other) {
        if (title.compareTo(other.title) != 0) {
            return title.compareTo(other.title);
        } else {
            return href.compareTo(other.href);
        }
    }

    public static UniProtLoadLink create(ForeignDB.AvailableName foreignDBName, String accession, String suffix) {
        String linkTitle = foreignDBName + ":" + accession;
        String linkUrl;
        if (suffix == null) {
            suffix = "";
        }

        //handle ZFIN special case
        if (foreignDBName == ForeignDB.AvailableName.ZFIN) {
            linkUrl = ZfinPropertiesEnum.SECURE_HTTP.value() + ZfinPropertiesEnum.DOMAIN_NAME.value() + "/" + accession + suffix;
        } else {
            String urlPrefix = URL_PREFIX_CACHE.computeIfAbsent(foreignDBName,
                    name -> getSequenceRepository().getForeignDBByName(name).getDbUrlPrefix());
            linkUrl = urlPrefix + accession + suffix;
        }
        return new UniProtLoadLink(linkTitle, linkUrl);
    }

    public static UniProtLoadLink create(ForeignDB.AvailableName foreignDBName, String accession) {
        return create(foreignDBName, accession, null);
    }
}