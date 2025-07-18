package org.zfin.marker;

import org.apache.commons.collections4.CollectionUtils;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.service.TranscriptService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

public class GPIFileVer2 extends AbstractScriptWrapper {
    private int numfOfRecords = 0;

    public GPIFileVer2(int number) {
        numfOfRecords = number;
    }

    public static void main(String[] args) throws IOException {
        int number = 0;
        if (args.length > 0) {
            number = Integer.valueOf(args[0]);
        }
        GPIFileVer2 gpiFile = new GPIFileVer2(number);
        gpiFile.init();
        System.exit(0);
    }

    private void init() throws IOException {
        initAll();
        File gpiFile = new File(ZfinPropertiesEnum.TARGETROOT + "/server_apps/data_transfer/GO/zfin.gpi.gz");

        try (OutputStream os = new GZIPOutputStream(new FileOutputStream(gpiFile));
             OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
             BufferedWriter bw = new BufferedWriter(osw)) {

            List<Marker> genes = getMarkerRepository().getMarkerByGroup(Marker.TypeGroup.GENEDOM, numfOfRecords);
            System.out.println("Total genes to return: " + genes.size());

            bw.write("!gpi-version:2.0\n");
            bw.write("!namespace:ZFIN\n");
            bw.write("!generated-by:ZFIN\n");
            bw.write("!date-generated:" + new SimpleDateFormat("MM/dd/yyyy").format(new Date()) + "\n\n");

            for (Marker gene : genes) {
                List<String> csvRow = new ArrayList<>();
                csvRow.add("ZFIN:" + gene.getZdbID());
                csvRow.add(gene.getAbbreviation());
                csvRow.add(gene.getName());

                if (CollectionUtils.isNotEmpty(gene.getAliases())) {
                    csvRow.add(gene.getAliases().stream().map(MarkerAlias::getAlias).collect(Collectors.joining("|")));
                } else {
                    //Is there a reason we use a space instead of empty string?
                    csvRow.add(" ");
                }

                csvRow.add(gene.getSoTerm().getOboID());
                csvRow.add("NCBITaxon:7955");
                //purposeful tab here, to represent parent id that we don't have
                csvRow.add("");
                csvRow.add("ZFIN:" + gene.getZdbID());
                csvRow.add("");
                List<String> geneDbLinks = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(gene.getDbLinks())) {
                    for (DBLink dblink : gene.getDbLinks()) {
                        String dbName = dblink.getReferenceDatabase().getForeignDB().getDbName().toString();
                        if (dbName == null) {
                            continue;
                        }
                        if (Objects.equals(dbName, "Ensembl(GRCz11)")) {
                            dbName = "ENSEMBL";
                        }
                        if (Objects.equals(dbName, "UniProtKB-KW")) {
                            dbName = "UniProtKB";
                        }
                        if (Objects.equals(dbName, "Gene")) {
                            dbName = "NCBIGene";
                        }
                        if (Objects.equals(dbName, "Alliance")) {
                            dbName = "ZFIN";
                        }
                        if (dbName.contains("miR")) {
                            dbName = "ZFIN";
                        }
                        geneDbLinks.add(dbName + ":" + dblink.getAccessionNumber());
                    }
                }
                List<DBLink> relatedRNACentralIdDbLinks = TranscriptService.getRelatedRNACentralIDs(gene);
                geneDbLinks.addAll(relatedRNACentralIdDbLinks.stream().map(id -> "RNAcentral:" + id.getAccessionNumber()).toList());

                csvRow.add(String.join("|", geneDbLinks));

                //purposeful tab here, to represent the field 'Properties' that we don't have
                csvRow.add("");
                bw.write(String.join("\t", csvRow) + "\n");
            }
            System.out.println("Wrote GPI2 file to " + gpiFile.getAbsolutePath() + " with " + genes.size() + " genes");
        }
    }
}
