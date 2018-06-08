package org.zfin.marker;

import org.apache.commons.collections.CollectionUtils;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.sequence.DBLink;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.zip.GZIPOutputStream;

import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

public class GPIFile extends AbstractScriptWrapper {
    private int numfOfRecords = 0;

    public GPIFile(int number) {
        numfOfRecords = number;
    }

    public static void main(String[] args) throws IOException {
        int number = 0;
        if (args.length > 0) {
            number = Integer.valueOf(args[0]);
        }
        GPIFile gpiFile = new GPIFile(number);
        gpiFile.init();
        System.exit(0);
    }

    private void init() throws IOException {
        initAll();

        File gpiFile = new File(ZfinPropertiesEnum.TARGETROOT + "/server_apps/data_transfer/GO/zfin.gpi.gz");

        OutputStream os = new GZIPOutputStream(new FileOutputStream(gpiFile));
        String encoding = "UTF-8";
        try (OutputStreamWriter osw = new OutputStreamWriter(os, encoding)) {
            BufferedWriter bw = new BufferedWriter(osw);

            List<Marker> genes = getMarkerRepository().getMarkerByGroup(Marker.TypeGroup.GENEDOM, numfOfRecords);

            bw.write("!gpi-version:1.2");
            bw.write('\n');

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            Date date = new Date();
            String dateOutput = sdf.format(date);

            bw.write("!" + dateOutput + " $");
            bw.write('\n');
            bw.write('\n');
            System.out.println("Total genes to return: " + genes.size());
            for (Marker gene : genes) {
                StringBuilder geneRow = new StringBuilder();
                geneRow.append("ZFIN");
                geneRow.append('\t');
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
                    geneRow.deleteCharAt(lastPipe - 1);
                } else {
                    geneRow.append(" ");
                }
                geneRow.append('\t');
                geneRow.append(gene.getSoTerm().getTermName().toLowerCase());
                geneRow.append('\t');
                geneRow.append("taxon:7955");
                geneRow.append('\t');
                //purposeful tab here, to represent parent id that we don't have
                geneRow.append("");
                geneRow.append('\t');
                if (CollectionUtils.isNotEmpty(gene.getDbLinks())) {
                    for (DBLink dblink : gene.getDbLinks()) {
                        String dbName = dblink.getReferenceDatabase().getForeignDB().getDbName().toString();
                        if (dbName == null) {
                            continue;
                        }
                        if (Objects.equals(dbName, "Ensembl(GRCz10)")) {
                            dbName = "ENSEMBL";
                        }
                        if (Objects.equals(dbName, "UniProtKB-KW")) {
                            dbName = "UniProtKB";
                        }
                        if (Objects.equals(dbName, "Gene")) {
                            dbName = "NCBIGene";
                        }
                        geneRow.append(dbName).append(":").append(dblink.getAccessionNumber());
                        geneRow.append("|");
                    }
                    Integer lastPipe = geneRow.length();
                    geneRow.deleteCharAt(lastPipe - 1);
                } else {
                    geneRow.append("");
                }
                //purposeful tab here, to represent the field 'Properties' that we don't have
                geneRow.append('\t');
                geneRow.append("");
                geneRow.append('\n');
                bw.write(geneRow.toString());
            }
        }
    }
}
