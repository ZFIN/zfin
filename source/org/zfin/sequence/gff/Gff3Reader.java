package org.zfin.sequence.gff;

import htsjdk.tribble.AbstractFeatureReader;
import htsjdk.tribble.FeatureReader;
import htsjdk.tribble.gff.Gff3Codec;
import htsjdk.tribble.gff.Gff3Feature;
import org.hibernate.SessionFactory;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.mapping.GenomeLocation;
import org.zfin.properties.ZfinProperties;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.zfin.repository.RepositoryFactory.getSequenceRepository;

/**
 * Utility class for reading GFF3 files using HTSJDK
 */
public class Gff3Reader {

    private final String filePath;
    private FeatureReader<Gff3Feature> reader;

    public Gff3Reader(String filePath) {
        this.filePath = filePath;
    }

    public static void init() {
        ZfinProperties.init();
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator(false);
        }
    }

    /**
     * Initialize the GFF3 reader
     */
    public void initialize() throws IOException {
        init();
        File gff3File = new File(filePath);
        if (!gff3File.exists()) {
            throw new IOException("GFF3 file not found: " + filePath);
        }

        // Create reader with GFF3 codec
        reader = AbstractFeatureReader.getFeatureReader(filePath, new Gff3Codec(), false);
    }

    /**
     * Read all features from the GFF3 file
     */
    public List<Gff3Feature> readAllFeatures() throws IOException {
        if (reader == null) {
            initialize();
        }

        List<Gff3Feature> features = new ArrayList<>();
        Iterator<Gff3Feature> iterator = reader.iterator();

        while (iterator.hasNext()) {
            features.add(iterator.next());
        }

        return features;
    }

    /**
     * Read features by type (e.g., "gene", "mRNA", "CDS")
     */
    public List<Gff3Feature> readFeaturesByType(String featureType) throws IOException {
        List<Gff3Feature> allFeatures = readAllFeatures();
        return allFeatures.stream()
            .filter(feature -> feature.getType().equals(featureType))
            .toList();
    }

    /**
     * Print feature information for debugging
     */
    public void printFeature(Gff3Feature feature) {
        System.out.println("=== GFF3 Feature ===");
        System.out.println("Sequence ID: " + feature.getContig());
        System.out.println("Source: " + feature.getSource());
        System.out.println("Type: " + feature.getType());
        System.out.println("Start: " + feature.getStart());
        System.out.println("End: " + feature.getEnd());
        System.out.println("Score: " + feature.getScore());
        System.out.println("Strand: " + feature.getStrand());
        System.out.println("Phase: " + feature.getPhase());

        // Print attributes
        Map<String, List<String>> attributes = feature.getAttributes();
        System.out.println("Attributes:");
        attributes.forEach((key, values) -> {
            System.out.println("  " + key + " = " + String.join(",", values));
        });
        System.out.println();
    }

    /**
     * Get specific attribute values from a feature
     */
    public List<String> getAttributeValues(Gff3Feature feature, String attributeName) {
        return feature.getAttributes().getOrDefault(attributeName, new ArrayList<>());
    }

    /**
     * Close the reader
     */
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        }
    }

    /**
     * Example usage and testing
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: java Gff3Reader <path-to-gff3-file>");
            return;
        }

        String gff3FilePath = args[0];
        Gff3Reader reader = new Gff3Reader(gff3FilePath);

        try {
            // Read all features
            List<Gff3Feature> allFeatures = reader.readAllFeatures();
            System.out.println("Total features: " + allFeatures.size());

            // Example: Get all gene features
            List<Gff3Feature> genes = reader.readFeaturesByType("gene");
            System.out.println("Number of genes: " + genes.size());

            // Print first few features for inspection
            allFeatures.stream().limit(5).forEach(reader::printFeature);

            // Example: Find features with specific attributes
            allFeatures.stream()
                .filter(feature -> reader.getAttributeValues(feature, "ID").contains("gene1"))
                .forEach(reader::printFeature);

        } catch (IOException e) {
            System.err.println("Error reading GFF3 file: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                System.err.println("Error closing reader: " + e.getMessage());
            }
        }
    }

}