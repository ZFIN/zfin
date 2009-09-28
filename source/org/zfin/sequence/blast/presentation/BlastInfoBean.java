package org.zfin.sequence.blast.presentation;

import org.zfin.sequence.blast.Database;

import java.util.Collection;

/**
 * This bean contains data necessary for executing a blast query.
 */
public class BlastInfoBean {

    private Collection<DatabasePresentationBean> nucleotideDatabases;
    private Collection<DatabasePresentationBean> proteinDatabases ;
    private boolean showTitle = true ;
    private boolean doRefresh = false ;

    public Collection<DatabasePresentationBean> getNucleotideDatabases() {
        return nucleotideDatabases;
    }

    public void setNucleotideDatabases(Collection<DatabasePresentationBean> nucleotideDatabases) {
        this.nucleotideDatabases = nucleotideDatabases;
    }

    public Collection<DatabasePresentationBean> getProteinDatabases() {
        return proteinDatabases;
    }

    public void setProteinDatabases(Collection<DatabasePresentationBean> proteinDatabases) {
        this.proteinDatabases = proteinDatabases;
    }

    public void setNucleotideDatabasesFromRoot(Collection<Database> nucleotideDatabases) {
        this.nucleotideDatabases = BlastPresentationService.orderDatabasesFromRoot(nucleotideDatabases);
    }

    public void setProteinDatabasesFromRoot(Collection<Database> proteinDatabases) {
        this.proteinDatabases = BlastPresentationService.orderDatabasesFromRoot(proteinDatabases);
    }

    public boolean isShowTitle() {
        return showTitle;
    }

    public void setShowTitle(boolean showTitle) {
        this.showTitle = showTitle;
    }

    public boolean isDoRefresh() {
        return doRefresh;
    }

    public void setDoRefresh(boolean doRefresh) {
        this.doRefresh = doRefresh;
    }
}