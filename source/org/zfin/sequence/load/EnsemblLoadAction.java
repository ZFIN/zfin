package org.zfin.sequence.load;

import lombok.Getter;
import lombok.Setter;
import org.zfin.sequence.TranscriptDBLink;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class EnsemblLoadAction extends LoadAction {

    public static final String HTTPS_WWW_ENSEMBL_ORG_DANIO_RERIO_GENE_SUMMARY_G = "https://www.ensembl.org/Danio_rerio/Gene/Summary?g=";
    public static final String ZFIN_WWW = "https://zfin.org/";

    public EnsemblLoadAction(Type type, SubType subType, String accessionNumber, String transcriptTitle, String s, Integer length, Map<String, String> actions) {
        super(type, subType, accessionNumber, transcriptTitle, "This DB_LINK had no length info and got updated", length, actions);
    }
}
