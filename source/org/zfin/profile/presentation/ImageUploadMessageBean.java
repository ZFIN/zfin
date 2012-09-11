package org.zfin.profile.presentation;

import java.io.Serializable;

/**
 */
public class ImageUploadMessageBean implements Serializable{

    private String zdbID ;
    private String securityPersonZdbID;
    private byte[] snapshot ;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getSecurityPersonZdbID() {
        return securityPersonZdbID;
    }

    public void setSecurityPersonZdbID(String securityPersonZdbID) {
        this.securityPersonZdbID = securityPersonZdbID;
    }

    public byte[] getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(byte[] snapshot) {
        this.snapshot = snapshot;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ImageUploadMessageBean");
        sb.append("{zdbID='").append(zdbID).append('\'');
        sb.append(", securityPersonZdbID='").append(securityPersonZdbID).append('\'');
        sb.append(", snapshot=").append(snapshot == null ? "null" : "");
        for (int i = 0; snapshot != null && i < snapshot.length; ++i)
            sb.append(i == 0 ? "" : ", ").append(snapshot[i]);
        sb.append('}');
        return sb.toString();
    }
}
