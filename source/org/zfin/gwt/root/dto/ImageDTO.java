package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.List;


public class ImageDTO extends RelatedEntityDTO implements IsSerializable   {
    private List<TermDTO> anatomyTerms;
    private StageDTO start;
    private StageDTO end;

    public List<TermDTO> getAnatomyTerms() {
        return anatomyTerms;
    }

    public void setAnatomyTerms(List<TermDTO> anatomyTerms) {
        this.anatomyTerms = anatomyTerms;
    }

    public StageDTO getStart() {
        return start;
    }

    public void setStart(StageDTO start) {
        this.start = start;
    }

    public StageDTO getEnd() {
        return end;
    }

    public void setEnd(StageDTO end) {
        this.end = end;
    }
}
