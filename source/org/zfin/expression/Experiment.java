package org.zfin.expression;

/**
 * Created by IntelliJ IDEA.
 * User: Xiang Shao
 * Date: Jun 15, 2007
 * Time: 5:06:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class Experiment {
    private String zdbID;
    private String name;


    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
