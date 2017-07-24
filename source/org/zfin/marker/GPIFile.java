package org.zfin.marker;

import org.apache.commons.collections.CollectionUtils;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.sequence.DBLink;
import java.util.Calendar;
import java.io.*;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

public class GPIFile extends AbstractScriptWrapper{
        private int numfOfRecords = 0;

        public GPIFile(int number) {
            numfOfRecords = number;
        }


        public static void main(String[] args) throws IOException {
            int number = 0;
            if (args.length > 0) {
                number = Integer.valueOf(args[0]);
            }
            org.zfin.marker.GPIFile GPIFile = new org.zfin.marker.GPIFile(number);
            GPIFile.init();
            System.exit(0);
        }

        private void init() throws IOException {
            initAll();

            File gpiFile = new File(ZfinPropertiesEnum.TARGETROOT + "/server_apps/data_transfer/GO/zfin.gpi");

            OutputStream os = new FileOutputStream(gpiFile);
            String encoding = "UTF8";
            OutputStreamWriter osw = new OutputStreamWriter(os, encoding);
            BufferedWriter bw = new BufferedWriter(osw);

            List<Marker> genes = getMarkerRepository().getMarkerByGroup(Marker.TypeGroup.GENEDOM, numfOfRecords);

            bw.write("!gpi-version:1.2");
            bw.write('\n');
            bw.write("!" + Calendar.getInstance().getTime());
            bw.write('\n');
            bw.write('\n');
            System.out.println("Total genes to return: " + genes.size());
            for (Marker gene : genes) {
                StringBuilder geneRow = new StringBuilder();
                geneRow.append(gene.getZdbID());
                geneRow.append('\t');
                geneRow.append(gene.getAbbreviation());
                geneRow.append('\t');
                geneRow.append(gene.getName());
                geneRow.append('\t');
                if (CollectionUtils.isNotEmpty(gene.getAliases())) {
                    for (MarkerAlias geneAlias : gene.getAliases()) {
                        geneRow.append(geneAlias.getAlias());
                        geneRow.append("|");
                    }
                    Integer lastPipe = geneRow.length();
                    geneRow.deleteCharAt(lastPipe-1);
                }

                geneRow.append('\t');
                geneRow.append(gene.getSoTerm().getTermName().toLowerCase());
                geneRow.append('\t');
                geneRow.append("7955");
                geneRow.append('\t');
                if (CollectionUtils.isNotEmpty(gene.getDbLinks())) {
                    for (DBLink dblink : gene.getDbLinks()) {
                        geneRow.append(dblink.getAccessionNumberDisplay());
                        geneRow.append("|");
                    }
                    Integer lastPipe = geneRow.length();
                    geneRow.deleteCharAt(lastPipe-1);
                }


                geneRow.append('\n');
                bw.write(geneRow.toString());
            }

        }
}
