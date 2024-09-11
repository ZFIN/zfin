package org.zfin.marker.fluorescence;

import org.hibernate.query.NativeQuery;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;

import java.util.List;

import static org.zfin.framework.HibernateUtil.currentSession;

public class ImportMissingFPBaseProteinsTask extends AbstractScriptWrapper {
    public static void main(String[] args) {

        ImportMissingFPBaseProteinsTask task = new ImportMissingFPBaseProteinsTask();
        task.runTask();
    }

    private void runTask() {
        initAll();

        FPBaseService service = new FPBaseService();

        currentSession().beginTransaction();
        List<FluorescentProtein> missingProteins = service.importMissingProteins();
        NativeQuery query = currentSession().createNativeQuery("select cast(create_color_info() as varchar)");
        query.uniqueResult();
        currentSession().getTransaction().commit();

        System.out.println("Imported " + missingProteins.size() + " missing proteins");
        for(FluorescentProtein protein : missingProteins) {
            System.out.println(protein.toStringSingleLine());
        }
    }
}
