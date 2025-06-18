package org.zfin.sequence.gff;

import htsjdk.tribble.gff.Gff3Feature;
import lombok.extern.log4j.Log4j2;
import org.hibernate.SessionFactory;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.properties.ZfinProperties;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
public class NCBIGff3Processor {

    public static void main(String[] args) {
        init();
        NCBIGff3Processor processor = new NCBIGff3Processor();
        try {
            processor.processEnsemblGff3("GCF_049306965.1_GRCz12tu_genomic.gff");
        } catch (IOException e) {
            log.error("Error processing GFF3 file", e);
        }
    }

    public static void init() {
        ZfinProperties.init();
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator(false);
        }
    }

    public void processEnsemblGff3(String gff3FilePath) throws IOException {
        Gff3Reader reader = new Gff3Reader(gff3FilePath);

        // Get all gene features
        List<Gff3Feature> allRecords = reader.readAllFeatures();
        System.out.println("Total records: " + allRecords.size());
        List<Gff3Ncbi> records = allRecords.stream()
            .map(feature -> {
                Gff3Ncbi ncbi = new Gff3Ncbi();
                ncbi.setChromosome(chromoMap.get(feature.getContig()));
                ncbi.setStart(feature.getStart());
                ncbi.setEnd(feature.getEnd());
                //ncbi.setAttributes(feature.);
                ncbi.setSource(feature.getSource() != null ? feature.getSource() : "unknown");
                ncbi.setFeature(feature.getType() != null ? feature.getType() : "unknown");
                ncbi.setScore(String.valueOf(feature.getScore()));
                ncbi.setFrame(String.valueOf(feature.getPhase()));
                ncbi.setStrand(feature.getStrand() != null ? feature.getStrand().name() : "unknown");
                ncbi.setAttributePairs(generateAttributePairs(feature.getAttributes()));
                return ncbi;
            })
            .toList();
        
        
/*
        genes.forEach(gene -> {
            String geneId = reader.getFirstAttributeValue(gene, "gene_id");
            String geneName = reader.getFirstAttributeValue(gene, "gene_name");

            // Process gene data...
        });
*/

        Gff3NcbiService service = new Gff3NcbiService();
        service.saveAll(records);

        reader.close();
    }

    private Set<Gff3NcbiAttributePair> generateAttributePairs(Map<String, List<String>> attributes) {
        return attributes.entrySet().stream()
            .map((entry) -> {
                Gff3NcbiAttributePair pair = new Gff3NcbiAttributePair();
                pair.setKey(entry.getKey());
                pair.setValue(entry.getValue().stream().map(String::trim).collect(Collectors.joining(",")));
                return pair;
            }).collect(Collectors.toSet());
    }

    static Map<String, String> chromoMap = new HashMap<>();

    static {
        chromoMap.put("NC_133176.1", "1");
        chromoMap.put("NC_133177.1", "2");
        chromoMap.put("NC_133178.1", "3");
        chromoMap.put("NC_133179.1", "4");
        chromoMap.put("NC_133180.1", "5");
        chromoMap.put("NC_133181.1", "6");
        chromoMap.put("NC_133182.1", "7");
        chromoMap.put("NC_133183.1", "8");
        chromoMap.put("NC_133184.1", "9");
        chromoMap.put("NC_133185.1", "10");
        chromoMap.put("NC_133186.1", "11");
        chromoMap.put("NC_133187.1", "12");
        chromoMap.put("NC_133188.1", "13");
        chromoMap.put("NC_133189.1", "14");
        chromoMap.put("NC_133190.1", "15");
        chromoMap.put("NC_133191.1", "16");
        chromoMap.put("NC_133192.1", "17");
        chromoMap.put("NC_133193.1", "18");
        chromoMap.put("NC_133194.1", "19");
        chromoMap.put("NC_133195.1", "20");
        chromoMap.put("NC_133196.1", "21");
        chromoMap.put("NC_133197.1", "22");
        chromoMap.put("NC_133198.1", "23");
        chromoMap.put("NC_133199.1", "24");
        chromoMap.put("NC_133200.1", "25");
        chromoMap.put("NC_002333.2", "MT");

    }
}


