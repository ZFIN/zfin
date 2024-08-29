package org.zfin.sequence.load;

import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.sequence.ForeignDB;

import static org.zfin.repository.RepositoryFactory.getSequenceRepository;

public record LoadLink(String title, String href)  implements Comparable<LoadLink> {
    @Override
    public int compareTo(LoadLink other) {
        if (title.compareTo(other.title) != 0) {
            return title.compareTo(other.title);
        } else {
            return href.compareTo(other.href);
        }
    }

    public static LoadLink create(ForeignDB.AvailableName foreignDBName, String accession, String suffix) {
        String linkTitle = foreignDBName + ":" + accession;
        String linkUrl;
        if (suffix == null) {
            suffix = "";
        }

        //handle ZFIN special case
        if (foreignDBName == ForeignDB.AvailableName.ZFIN) {
            linkUrl = ZfinPropertiesEnum.SECURE_HTTP.value() + ZfinPropertiesEnum.DOMAIN_NAME.value() + "/" + accession + suffix;
        } else {
            linkUrl = getSequenceRepository().getForeignDBByName(foreignDBName).getDbUrlPrefix() + accession + suffix;
        }
        return new LoadLink(linkTitle, linkUrl);
    }

    public static LoadLink create(ForeignDB.AvailableName foreignDBName, String accession) {
        return create(foreignDBName, accession, null);
    }
}