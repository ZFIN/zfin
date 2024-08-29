package org.zfin.sequence.load;

import lombok.Getter;
import lombok.Setter;
import org.zfin.sequence.TranscriptDBLink;

import java.util.Set;

@Getter
@Setter
public class UpdateLengthLoadAction extends LoadAction {

    public static final String HTTPS_WWW_ENSEMBL_ORG_DANIO_RERIO_GENE_SUMMARY_G = "https://www.ensembl.org/Danio_rerio/Gene/Summary?g=";

    public UpdateLengthLoadAction(Type type, SubType subType, Set<LoadLink> actions, TranscriptDBLink transcriptDBLink) {
        super(type, subType, transcriptDBLink.getAccessionNumber(), getTranscriptTitle(transcriptDBLink), "This DB_LINK had no length info and got updated", transcriptDBLink.getLength(), actions);
        setLoadLinks(transcriptDBLink);
    }

    private static String getTranscriptTitle(TranscriptDBLink transcriptDBLink) {
        return transcriptDBLink.getTranscript().getAbbreviation() + " [" + transcriptDBLink.getTranscript().getZdbID() + "]";
    }

    public UpdateLengthLoadAction(Type type, SubType subType, Set<LoadLink> actions, TranscriptDBLink transcriptDBLink, int oldLength) {
        super(type, subType, transcriptDBLink.getAccessionNumber(), getTranscriptTitle(transcriptDBLink), "This DB_LINK changed length from " + oldLength + " to " + transcriptDBLink.getLength(), transcriptDBLink.getLength(), actions);
        setLoadLinks(transcriptDBLink);
    }

    private void setLoadLinks(TranscriptDBLink transcriptDBLink) {
        LoadLink loadLink = new LoadLink(transcriptDBLink.getAccessionNumber(), HTTPS_WWW_ENSEMBL_ORG_DANIO_RERIO_GENE_SUMMARY_G + transcriptDBLink.getAccessionNumber());
        this.addLink(loadLink);
        LoadLink loadLinkNew = new LoadLink(transcriptDBLink.getTranscript().getAbbreviation(), "https://zfin.org/" + transcriptDBLink.getDataZdbID());
        this.addLink(loadLinkNew);

    }


}
