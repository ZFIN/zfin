package org.zfin.anatomy;

import java.util.ArrayList;
import java.util.List;

/**
 * Please provide JavaDoc info!!!
 */
public class DevelopmentStageData {

    private static List<DevelopmentStage> stages;

    public static List<DevelopmentStage> getStages() {
        if (stages == null)
            init();
        return stages;
    }

    public DevelopmentStage getStage(String name){
        return null;
    }

    private static void init() {
        stages = new ArrayList<DevelopmentStage>();
        DevelopmentStage adult = new DevelopmentStage();
        adult.setName("Adult");
        adult.setHoursStart(2160);
        adult.setHoursEnd(17520);
        adult.setOtherFeature("breeding adult");
        stages.add(adult);

        DevelopmentStage unknown = new DevelopmentStage();
        unknown.setName(DevelopmentStage.UNKNOWN);
        unknown.setHoursStart(0);
        unknown.setHoursEnd(17520);
        stages.add(unknown);

        DevelopmentStage blastula = new DevelopmentStage();
        blastula.setName("Blastula:256-cell");
        blastula.setHoursStart(2.50F);
        blastula.setHoursEnd(2.75F);
        blastula.setOboID("ZFS:0000009");
        stages.add(blastula);

        DevelopmentStage pharyngular = new DevelopmentStage();
        pharyngular.setName("Pharyngula:Prim-5");
        pharyngular.setHoursStart(24);
        pharyngular.setHoursEnd(30);
        pharyngular.setOboID("ZFS:0000029");
        stages.add(pharyngular);

        DevelopmentStage cleavage = new DevelopmentStage();
        cleavage.setName("Cleavage:4-cell");
        cleavage.setHoursStart(1.00F);
        cleavage.setHoursEnd(1.25F);
        cleavage.setOboID("ZFS:0000003");
        stages.add(cleavage);

        DevelopmentStage gastrula = new DevelopmentStage();
        gastrula.setName("Gastrula:Bud");
        gastrula.setHoursStart(10);
        gastrula.setHoursEnd(10.33F);
        gastrula.setOboID("ZFS:0000022");
        stages.add(gastrula);

        DevelopmentStage segmentation = new DevelopmentStage();
        segmentation.setName("Segmentation:20-25 somites");
        segmentation.setHoursStart(19);
        segmentation.setHoursEnd(22);
        segmentation.setOboID("ZFS:0000027");
        stages.add(segmentation);


    }
}
