package org.zfin.publication;

import org.hibernate.Transaction;
import org.zfin.framework.HibernateUtil;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.publication.presentation.PublicationService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;
import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

/**
 * Finds problems with publication files and exits non-zero if any remain.
 *
 * Issues reported:
 *   EMPTY_FILE                  the file on disk is missing or has zero size
 *   MULTIPLE_ORIGINAL_ARTICLES  a publication has more than one Original Article row
 *   DUPLICATE_FILE              the same stored file is recorded more than once for a publication
 *
 * Nothing is changed unless a fix flag is given. Each fix handles one specific case, is logged to the
 * updates table (no submitter), and is listed in fixed_publication_files.csv. Fixes run before the
 * report, so the report shows what is left.
 *
 *   --fix-empty-superseded-original  delete an empty Original Article row when the same publication
 *                                    has a non-empty Original Article.
 *   --fix-empty-promote-zdbid-pdf    delete an empty Original Article row when the publication has no
 *                                    non-empty Original Article but does have a non-empty
 *                                    {@code <zdbId>.pdf} recorded as another type, and promote that
 *                                    file to Original Article.
 */
public class FindPublicationFileIssuesTask extends AbstractScriptWrapper {

    static final String FIX_EMPTY_SUPERSEDED_ORIGINAL = "--fix-empty-superseded-original";
    static final String FIX_EMPTY_PROMOTE_ZDBID_PDF = "--fix-empty-promote-zdbid-pdf";

    private record Issue(String type, String pubZdbId, String fileName, String details) {
    }

    public static void main(String[] args) throws IOException {
        List<String> argList = Arrays.asList(args);
        for (String arg : argList) {
            if (!arg.equals(FIX_EMPTY_SUPERSEDED_ORIGINAL) && !arg.equals(FIX_EMPTY_PROMOTE_ZDBID_PDF)) {
                System.err.println("Unknown argument: " + arg);
                System.err.println("Valid flags: " + FIX_EMPTY_SUPERSEDED_ORIGINAL + " " + FIX_EMPTY_PROMOTE_ZDBID_PDF);
                System.exit(2);
            }
        }
        new FindPublicationFileIssuesTask().runTask(
                argList.contains(FIX_EMPTY_SUPERSEDED_ORIGINAL),
                argList.contains(FIX_EMPTY_PROMOTE_ZDBID_PDF));
    }

    private void runTask(boolean fixSupersededOriginal, boolean fixPromote) throws IOException {
        initAll();
        PublicationService pubService = new PublicationService();

        List<String> fixes = new ArrayList<>();
        if (fixSupersededOriginal || fixPromote) {
            fixes = fixEmptyOriginalArticles(pubService, fixSupersededOriginal, fixPromote);
        }
        List<Issue> issues = findIssues(pubService);

        String outputDir = System.getenv("ARTIFACTS_DIR") != null ? System.getenv("ARTIFACTS_DIR") : ".";
        File issuesFile = new File(outputDir, "publication_file_issues.csv");
        System.out.println("Writing report to " + issuesFile.getAbsolutePath());
        System.out.println("Issue,Publication ZDB ID,Filename,Details");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(issuesFile))) {
            writer.write("Issue,Publication ZDB ID,Filename,Details\n");
            for (Issue issue : issues) {
                String line = csv(issue.type()) + "," + csv(issue.pubZdbId()) + "," + csv(issue.fileName()) + "," + csv(issue.details());
                System.out.println(line);
                writer.write(line + "\n");
            }
        }

        if (fixSupersededOriginal || fixPromote) {
            File fixedFile = new File(outputDir, "fixed_publication_files.csv");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fixedFile))) {
                writer.write("Publication ZDB ID,Action,Filename\n");
                for (String line : fixes) {
                    writer.write(line + "\n");
                }
            }
            System.out.println("Fixed " + fixes.size() + " publication file rows (see " + fixedFile.getAbsolutePath() + ")");
        }

        Map<String, Long> counts = issues.stream().collect(Collectors.groupingBy(Issue::type, LinkedHashMap::new, Collectors.counting()));
        System.out.println("Found " + issues.size() + " publication file issues " + counts);
        if (!issues.isEmpty()) {
            System.exit(1);
        }
    }

    private static String csv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private List<Issue> findIssues(PublicationService pubService) {
        List<PublicationFile> files = getPublicationRepository().getAllPublicationFiles();
        List<Issue> issues = new ArrayList<>();

        for (PublicationFile file : files) {
            if (pubService.getPublicationFileSizeOnDisk(file) == 0) {
                issues.add(new Issue("EMPTY_FILE", file.getPublication().getZdbID(), file.getFileName(),
                        file.getType().getName().toString()));
            }
        }

        Map<String, List<PublicationFile>> byPub = files.stream()
                .collect(Collectors.groupingBy(f -> f.getPublication().getZdbID()));
        for (Map.Entry<String, List<PublicationFile>> entry : byPub.entrySet()) {
            List<PublicationFile> pubFiles = entry.getValue();

            List<PublicationFile> originals = pubFiles.stream()
                    .filter(f -> f.getType().getName() == PublicationFileType.Name.ORIGINAL_ARTICLE)
                    .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                    .toList();
            if (originals.size() > 1) {
                issues.add(new Issue("MULTIPLE_ORIGINAL_ARTICLES", entry.getKey(), null,
                        originals.size() + " rows: " + originals.stream()
                                .map(PublicationFile::getOriginalFileName).collect(Collectors.joining(" | "))));
            }

            Map<String, List<PublicationFile>> byFileName = pubFiles.stream()
                    .collect(Collectors.groupingBy(PublicationFile::getFileName));
            for (Map.Entry<String, List<PublicationFile>> sameFile : byFileName.entrySet()) {
                if (sameFile.getValue().size() > 1) {
                    issues.add(new Issue("DUPLICATE_FILE", entry.getKey(), sameFile.getKey(),
                            sameFile.getValue().stream().sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                                    .map(f -> f.getType().getName().toString()).collect(Collectors.joining(" + "))));
                }
            }
        }

        issues.sort((a, b) -> {
            int c = a.pubZdbId().compareTo(b.pubZdbId());
            return c != 0 ? c : a.type().compareTo(b.type());
        });
        return issues;
    }

    /**
     * Handles empty Original Article rows according to the enabled fix flags.
     * Returns one CSV line per change.
     */
    private List<String> fixEmptyOriginalArticles(PublicationService pubService, boolean fixSuperseded, boolean fixPromote) {
        List<String> changes = new ArrayList<>();
        List<PublicationFile> emptyOriginals = getPublicationRepository().getAllPublicationFiles().stream()
                .filter(f -> f.getType().getName() == PublicationFileType.Name.ORIGINAL_ARTICLE)
                .filter(f -> pubService.getPublicationFileSizeOnDisk(f) == 0)
                .toList();
        PublicationFileType originalType = getPublicationRepository().getPublicationFileTypeByName(PublicationFileType.Name.ORIGINAL_ARTICLE);

        Transaction tx = HibernateUtil.createTransaction();
        try {
            for (PublicationFile empty : emptyOriginals) {
                Publication pub = empty.getPublication();
                List<PublicationFile> others = pub.getFiles().stream()
                        .filter(f -> f.getId() != empty.getId())
                        .filter(f -> pubService.getPublicationFileSizeOnDisk(f) > 0)
                        .toList();

                boolean hasWorkingOriginal = others.stream()
                        .anyMatch(f -> f.getType().getName() == PublicationFileType.Name.ORIGINAL_ARTICLE);

                PublicationFile promote = null;
                if (hasWorkingOriginal) {
                    if (!fixSuperseded) {
                        continue;
                    }
                } else {
                    if (!fixPromote) {
                        continue;
                    }
                    // only the loader or an Original Article upload ever names a file exactly <zdbId>.pdf
                    promote = others.stream()
                            .filter(f -> f.getFileName().endsWith("/" + pub.getZdbID() + ".pdf"))
                            .findFirst()
                            .orElse(null);
                    if (promote == null) {
                        continue;
                    }
                }

                String flag = promote == null ? FIX_EMPTY_SUPERSEDED_ORIGINAL : FIX_EMPTY_PROMOTE_ZDBID_PDF;
                String desc = empty.getFileName() + " (" + empty.getType().getName() + ")";
                pub.getFiles().remove(empty);
                HibernateUtil.currentSession().delete(empty);
                getInfrastructureRepository().insertUpdatesTableWithoutPerson(pub.getZdbID(), "publication file",
                        desc, null, "Delete file: empty on disk (FindPublicationFileIssuesTask " + flag + ")");
                changes.add(pub.getZdbID() + ",deleted empty Original Article," + empty.getFileName());

                if (promote != null) {
                    String before = promote.getFileName() + " (" + promote.getType().getName() + ")";
                    promote.setType(originalType);
                    getInfrastructureRepository().insertUpdatesTableWithoutPerson(pub.getZdbID(), "publication file",
                            before, promote.getFileName() + " (" + originalType.getName() + ")",
                            "Edit file type: promoted to Original Article (FindPublicationFileIssuesTask " + flag + ")");
                    changes.add(pub.getZdbID() + ",promoted to Original Article," + promote.getFileName());
                }
            }
            tx.commit();
        } catch (RuntimeException e) {
            tx.rollback();
            throw e;
        }
        return changes;
    }
}
