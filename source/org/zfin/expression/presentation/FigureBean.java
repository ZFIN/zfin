package org.zfin.expression.presentation;

import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.antibody.Antibody;
import org.zfin.audit.AuditLogItem;
import org.zfin.audit.repository.AuditLogRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

public class FigureBean {
    private Antibody antibody;

    private AnatomyItem anatomyItem;

    private DevelopmentStage startStage;

    private DevelopmentStage endStage;

    private List<FigureSummaryDisplay> figureSummaryRows;

    public Antibody getAntibody() {
        if (antibody == null) {
            antibody = new Antibody();
        }
        return antibody;
    }

    public void setAntibody(Antibody antibody) {
        this.antibody = antibody;
    }

    public AnatomyItem getAnatomyItem() {
        if (anatomyItem == null) {
            anatomyItem = new AnatomyItem();
        }
        return anatomyItem;
    }

    public void setAnatomyItem(AnatomyItem anatomyItem) {
        this.anatomyItem = anatomyItem;
    }

    public DevelopmentStage getStartStage() {
        if (startStage == null) {
            startStage = new DevelopmentStage();
        }
        return startStage;
    }

    public void setStartStage(DevelopmentStage startStage) {
        this.startStage = startStage;
    }

    public DevelopmentStage getEndStage() {
        if (endStage == null) {
            endStage = new DevelopmentStage();
        }
        return endStage;
    }

    public void setEndStage(DevelopmentStage endStage) {
        this.endStage = endStage;
    }

    public List<FigureSummaryDisplay> getFigureSummaryRows() {
        return figureSummaryRows;
    }

    public void setFigureSummaryRows(List<FigureSummaryDisplay> figureSummaryRows) {
        this.figureSummaryRows = figureSummaryRows;
    }

    public AuditLogItem getLatestUpdate() {
        AuditLogRepository alr = RepositoryFactory.getAuditLogRepository();
        return alr.getLatestAuditLogItem(antibody.getZdbID());
    }
}
