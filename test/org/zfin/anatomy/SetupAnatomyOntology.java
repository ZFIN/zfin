package org.zfin.anatomy;

import org.geneontology.dataadapter.DataAdapterException;
import org.geneontology.dataadapter.IOOperation;
import org.geneontology.oboedit.dataadapter.OBOFileAdapter;
import org.geneontology.oboedit.datamodel.Namespace;
import org.geneontology.oboedit.datamodel.OBOClass;
import org.geneontology.oboedit.datamodel.OBORestriction;
import org.geneontology.oboedit.datamodel.OBOSession;
import org.geneontology.oboedit.datamodel.impl.DbxrefImpl;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.repository.RepositoryFactory;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

/**
 * This class reads the OBO file for AO and inserts the terms
 * into the mock objects to make them available for unit tests.
 */
public class SetupAnatomyOntology {

    public static final String ZEBRAFISH_STAGES = "zebrafish_stages";
    public static final String ZEBRAFISH_ANATOMY = "zebrafish_anatomy";
    static boolean firstTime = true;

    public static void setupAO(String fileName) {
        // only load if this is not the first time.
        if(!firstTime)
            return;
        firstTime = false;
        File temp = new File("test");
        File oboFile = new File(temp, fileName);

        OBOFileAdapter adapter = new OBOFileAdapter();
        OBOFileAdapter.OBOAdapterConfiguration config = new OBOFileAdapter.OBOAdapterConfiguration();
        config.getReadPaths().add(oboFile.toString());
        OBOSession session = null;
        try {
            session = (OBOSession) adapter.doOperation(IOOperation.READ, config, null);
        } catch (DataAdapterException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        Set terms = session.getTerms();
        // First import the stage
        if (terms != null) {
            Iterator iter = terms.iterator();
            while (iter.hasNext()) {
                OBOClass term = (OBOClass) iter.next();
                Namespace namespace = term.getNamespace();
                if (namespace != null && namespace.getID().equals(ZEBRAFISH_STAGES))
                    importStageTerm(term);
            }
        }
        // Then import anatomy items because they depend on the stages
        if (terms != null) {
            Iterator iter = terms.iterator();
            while (iter.hasNext()) {
                OBOClass term = (OBOClass) iter.next();
                Namespace namespace = term.getNamespace();
                if (namespace != null && namespace.getID().equals(ZEBRAFISH_ANATOMY))
                    importAnatomyTerm(term);
            }
        }

    }

    private static void importAnatomyTerm(OBOClass term) {
        AnatomyItem item = new AnatomyItem();
        item.setOboID(term.getID());
        Set refs = term.getDbxrefs();
        // if no reference given it is not a stage term.
        if (refs == null || refs.size() == 0)
            return;
        DbxrefImpl xref = (DbxrefImpl) refs.iterator().next();
        item.setZdbID(xref.getID());
        String definition = term.getDefinition();
        if (definition != null)
            item.setDefinition(definition);
        Set<OBORestriction> parents = term.getParents();
        AnatomyRepository ar = RepositoryFactory.getAnatomyRepository();
        for(OBORestriction oboRest : parents){
            String id = oboRest.getType().getID();
            if(id != null && id.equals("start")){
               String oboStageID = oboRest.getParent().getID();
                DevelopmentStage stag = new DevelopmentStage();
                stag.setOboID(oboStageID);
                DevelopmentStage stage = ar.getStage(stag);
                item.setStart(stage);
            }
            if(id != null && id.equals("end")){
               String oboStageID = oboRest.getParent().getID();
                DevelopmentStage stag = new DevelopmentStage();
                stag.setOboID(oboStageID);
                DevelopmentStage stage = ar.getStage(stag);
                item.setEnd(stage);
            }
        }
        ar.insertAnatomyItem(item);
    }

    private static void importStageTerm(OBOClass term) {
        DevelopmentStage stage = new DevelopmentStage();
        stage.setOboID(term.getID());
        stage.setName(term.getName());
        Set refs = term.getDbxrefs();
        // if no reference given it is not a stage term.
        if (refs == null || refs.size() == 0)
            return;
        DbxrefImpl xref = (DbxrefImpl) refs.iterator().next();
        stage.setZdbID(xref.getID());
        String definition = term.getDefinition();
        if (definition != null) {
            ParseHoursAndFeatures(stage, definition);
        }
        AnatomyRepository ar = RepositoryFactory.getAnatomyRepository();
        ar.insertDevelopmentStage(stage);
    }

    private static void ParseHoursAndFeatures(DevelopmentStage stage, String definition) {
        int hourStart = definition.indexOf("(");
        definition = definition.substring(hourStart + 1);
        int hourDelimit = definition.indexOf("-");
        String startHour = definition.substring(0, hourDelimit);
        float startHrs = Float.parseFloat(startHour);
        stage.setHoursStart(startHrs);
        int hourEnd = definition.indexOf(")");
        String endHour = definition.substring(hourDelimit + 1, hourEnd);
        float endHrs = Float.parseFloat(endHour);
        stage.setHoursEnd(endHrs);

        definition = definition.substring(hourEnd);
        String str = "having a length of ";
        int teeth = definition.indexOf(str);
        StringBuilder sb = new StringBuilder();
        if (teeth > -1) {
            definition = definition.substring(teeth + str.length());
            int teethEnd = definition.indexOf(" ");
            if (teethEnd == -1)
                teethEnd = definition.lastIndexOf(".");
            String numOfTeeth = definition.substring(0, teethEnd);
            sb.append(numOfTeeth);
        }
        String and = "and";
        int comment = definition.indexOf(and);
        if (comment > -1) {
            definition = definition.substring(comment + and.length() + 1);
            if (teeth > -1)
                sb.append(", ");
            sb.append(definition.substring(0, definition.length() - 1));
        }
        stage.setOtherFeature(sb.toString());
    }
}
