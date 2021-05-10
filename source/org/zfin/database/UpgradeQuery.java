package org.zfin.database;

/**
 * Created by IntelliJ IDEA.
 */
public class UpgradeQuery {

    private String query;
    private UpgradeFile upgradeFile;


    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public UpgradeFile getUpgradeFile() {
        return upgradeFile;
    }

    public void setuFile(UpgradeFile uFile) {
        this.upgradeFile = uFile;
    }
}
