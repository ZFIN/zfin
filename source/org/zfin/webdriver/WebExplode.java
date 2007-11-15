package org.zfin.webdriver;

/**
 * Form Bean for web datablade output.
 */
public class WebExplode {

    private String MIval;
    private String contents;

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }


    public String getName() {
        return MIval;
    }

    public String getMIval() {
        return MIval;
    }

    public void setMIval(String MIval) {
        this.MIval = MIval;
    }
}
