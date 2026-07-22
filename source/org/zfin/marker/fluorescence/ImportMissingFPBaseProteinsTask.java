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
        // Link EFG markers to their (possibly newly imported) protein rows by exact
        // base name, so the reporter/emission colors that read through fpProtein_efg
        // stay current without a hand-written migration per new fluorophore (ZFIN-10352).
        int newLinks = service.linkEfgsToProteinsByName();
        NativeQuery query = currentSession().createNativeQuery("select cast(create_color_info() as varchar)");
        query.uniqueResult();
        currentSession().getTransaction().commit();

        System.out.println("Imported " + missingProteins.size() + " missing proteins");
        System.out.println("Created " + newLinks + " new EFG->protein links");
        for(FluorescentProtein protein : missingProteins) {
            System.out.println(protein.toStringSingleLine());
        }
    }
}
