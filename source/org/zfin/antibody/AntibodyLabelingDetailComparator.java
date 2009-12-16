package org.zfin.antibody;

import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.presentation.AnatomyLabel;
import org.zfin.ontology.GoTerm;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 */
public class AntibodyLabelingDetailComparator implements Comparator<AnatomyLabel> {

    public int compare(AnatomyLabel labeling1, AnatomyLabel labeling2) {
        if (labeling1 == null && labeling2 == null)
            return 0;
        else if (labeling1 == null)
            return -1;
        else if (labeling2 == null)
            return 1;

        AnatomyItem ao1 = labeling1.getAnatomyItem();
        AnatomyItem ao2 = labeling2.getAnatomyItem();
        GoTerm cc1 = labeling1.getCellularComponent();
        GoTerm cc2 = labeling2.getCellularComponent();
        DevelopmentStage stage1 = labeling1.getStartStage();
        DevelopmentStage stage2 = labeling2.getStartStage();

        int result = ao1.compareTo(ao2);
        if (result == 0) {
            if (cc1 == null && cc2 == null) {
                return stage1.compareTo(stage2);
            } else if (cc1 == null) {
                return -1;
            } else if (cc2 == null) {
                return 1;
            } else {
                int resultCC = cc1.compareTo(cc2);
                if (resultCC == 0)
                    return stage1.compareTo(stage2);
                else
                    return resultCC;
            }
        } else {
            return result;
        }
    }
}