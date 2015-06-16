package org.zfin.curation.presentation;

import java.util.Date;

public class CorrespondenceDTO {

    private long id;
    private String pub;
    private CuratorDTO curator;
    private Date openedDate;
    private Date closedDate;
    private boolean replyReceived;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPub() {
        return pub;
    }

    public void setPub(String pub) {
        this.pub = pub;
    }

    public CuratorDTO getCurator() {
        return curator;
    }

    public void setCurator(CuratorDTO curator) {
        this.curator = curator;
    }

    public Date getOpenedDate() {
        return openedDate;
    }

    public void setOpenedDate(Date openedDate) {
        this.openedDate = openedDate;
    }

    public Date getClosedDate() {
        return closedDate;
    }

    public void setClosedDate(Date closedDate) {
        this.closedDate = closedDate;
    }

    public boolean isReplyReceived() {
        return replyReceived;
    }

    public void setReplyReceived(boolean replyReceived) {
        this.replyReceived = replyReceived;
    }

}
