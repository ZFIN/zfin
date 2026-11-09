package org.zfin.publication;

import org.hibernate.Transaction;
import org.zfin.framework.HibernateUtil;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.publication.presentation.PublicationService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HexFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;
import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

/**
 * Finds problems with publication files and reports what it finds. The process exits 0 even
 * when issues remain: the Jenkins job marks the build UNSTABLE if the report has any, and FAILED only when
 * the task itself errors.
 *
 * Issues reported:
 *   EMPTY_FILE                  the file on disk is missing or has zero size
 *   MULTIPLE_ORIGINAL_ARTICLES  a publication has more than one Original Article row. Details give the
 *                               number of distinct md5 sums and, per row, the original file name, row id,
 *                               date entered, file modified time, size and md5, to show whether they differ.
 *                               Only files whose size matches another original's are hashed.
 *   MULTIPLE_ORIGINAL_ARTICLES_DUPLICATED
 *                               as above, but at least two of the rows have the same md5 (num rows > num
 *                               distinct md5s), so some of the rows are copies of the same content
 *   DUPLICATE_FILE              the same stored file is recorded more than once for a publication, all
 *                               with the same type (fixable with --fix-duplicate-files)
 *   DUPLICATE_FILE_MIXED_TYPES  the same stored file is recorded more than once with different types;
 *                               Details list each row's type, id and date entered. Needs a person.
 *   FILE_NAMED_FOR_OTHER_PUBLICATION
 *                               a file's uploaded name or stored path contains another publication's ZDB ID
 *                               (e.g. ZDB-PUB-190118-10.pdf on ZDB-PUB-190118-1, or a path under another
 *                               publication's directory), which is usually that other publication's PDF
 *                               attached by mistake or moved by a merge. Details say which name matched and
 *                               whether the other publication has a file of identical content.
 *   IDENTICAL_CONTENT           (only with --check-identical-content) a publication has rows with
 *                               different file names whose files have the same md5
 *   IDENTICAL_CONTENT_ACROSS_PUBLICATIONS
 *                               (only with --check-identical-content) the same content (md5) is recorded
 *                               on more than one publication. Sometimes legitimate (a supplement shared by
 *                               related publications), often a PDF attached to the wrong publication.
 *                               Only files whose size is shared with a file of another publication are hashed.
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
 *   --fix-identical-originals        when a publication has several Original Article rows whose files on
 *                                    disk are byte-identical (same md5, non-empty), delete all but the
 *                                    lowest-id row of each identical group. Only rows are deleted; files
 *                                    on disk are left alone. Rows whose files differ are never touched.
 *   --fix-duplicate-files            when a publication records the same stored file more than once with
 *                                    the same type, delete all but the lowest-id row. The rows share one
 *                                    file on disk, so nothing is removed from disk. Rows for the same
 *                                    file with different types are left for a person to review.
 */
public class FindPublicationFileIssuesTask extends AbstractScriptWrapper {

    static final String FIX_EMPTY_SUPERSEDED_ORIGINAL = "--fix-empty-superseded-original";
    static final String FIX_EMPTY_PROMOTE_ZDBID_PDF = "--fix-empty-promote-zdbid-pdf";

    static final String FIX_IDENTICAL_ORIGINALS = "--fix-identical-originals";
    static final String FIX_DUPLICATE_FILES = "--fix-duplicate-files";
    /**
     * Not a fix: md5s files on disk (only those that share a size within a publication, or with a file of
     * another publication), so it is opt-in.
     */
    static final String CHECK_IDENTICAL_CONTENT = "--check-identical-content";

    private static final String UNIQUE_SIZE = "unique-size";

    /** The files of one publication that are named for other publications. */
    private static final class NamedForOther {
        final java.util.Set<String> names = new java.util.LinkedHashSet<>();
        final java.util.Set<String> storedPaths = new java.util.LinkedHashSet<>();
        final java.util.Set<String> otherPubs = new java.util.LinkedHashSet<>();
        final List<String> more = new ArrayList<>();
    }
    private static final java.util.regex.Pattern PUB_ZDB_ID = java.util.regex.Pattern.compile("ZDB-PUB-[0-9]{6}-[0-9]+");

    private final Map<Long, String> md5Cache = new java.util.HashMap<>();

    /**
     * details: a short summary (e.g. "2 x Supplemental Material"); moreDetails: the full text with row ids,
     * dates and md5s; comparePubs: for issues spanning publications, the existing publications to compare
     * (else null).
     */
    private record Issue(String type, String pubZdbId, String fileName, String details, String moreDetails,
                         List<String> comparePubs) {
        Issue(String type, String pubZdbId, String fileName, String details, String moreDetails) {
            this(type, pubZdbId, fileName, details, moreDetails, null);
        }
    }

    public static void main(String[] args) throws IOException {
        List<String> argList = Arrays.asList(args);
        for (String arg : argList) {
            if (!arg.equals(FIX_EMPTY_SUPERSEDED_ORIGINAL) && !arg.equals(FIX_EMPTY_PROMOTE_ZDBID_PDF)
                    && !arg.equals(FIX_IDENTICAL_ORIGINALS) && !arg.equals(FIX_DUPLICATE_FILES)
                    && !arg.equals(CHECK_IDENTICAL_CONTENT)) {
                System.err.println("Unknown argument: " + arg);
                System.err.println("Valid flags: " + FIX_EMPTY_SUPERSEDED_ORIGINAL + " " + FIX_EMPTY_PROMOTE_ZDBID_PDF + " " + FIX_IDENTICAL_ORIGINALS + " " + FIX_DUPLICATE_FILES + " " + CHECK_IDENTICAL_CONTENT);
                System.exit(2);
            }
        }
        new FindPublicationFileIssuesTask().runTask(
                argList.contains(FIX_EMPTY_SUPERSEDED_ORIGINAL),
                argList.contains(FIX_EMPTY_PROMOTE_ZDBID_PDF),
                argList.contains(FIX_IDENTICAL_ORIGINALS),
                argList.contains(FIX_DUPLICATE_FILES),
                argList.contains(CHECK_IDENTICAL_CONTENT));
    }

    private void runTask(boolean fixSupersededOriginal, boolean fixPromote, boolean fixIdentical,
                         boolean fixDuplicateFiles, boolean checkIdenticalContent) throws IOException {
        initAll();
        PublicationService pubService = new PublicationService();

        List<String> fixes = new ArrayList<>();
        boolean anyFix = fixSupersededOriginal || fixPromote || fixIdentical || fixDuplicateFiles;
        if (fixSupersededOriginal || fixPromote) {
            fixes = fixEmptyOriginalArticles(pubService, fixSupersededOriginal, fixPromote);
        }
        if (fixIdentical) {
            fixes.addAll(fixIdenticalOriginalArticles(pubService));
        }
        if (fixDuplicateFiles) {
            fixes.addAll(fixDuplicateFileRows());
        }
        List<Issue> issues = findIssues(pubService, checkIdenticalContent);

        String outputDir = System.getenv("ARTIFACTS_DIR") != null ? System.getenv("ARTIFACTS_DIR") : ".";
        File issuesFile = new File(outputDir, "publication_file_issues.csv");
        System.out.println("Writing report to " + issuesFile.getAbsolutePath());
        System.out.println("Issue,Publication ZDB ID,Filename,Details,More Details");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(issuesFile))) {
            writer.write("Issue,Publication ZDB ID,Filename,Details,More Details\n");
            for (Issue issue : issues) {
                String line = csv(issue.type()) + "," + csv(issue.pubZdbId()) + "," + csv(issue.fileName()) + ","
                        + csv(issue.details()) + "," + csv(issue.moreDetails());
                System.out.println(line);
                writer.write(line + "\n");
            }
        }
        List<String[]> issueRows = new ArrayList<>();
        issueRows.add(new String[]{"Issue", "Publication ZDB ID", "Filename", "Details", "Compare publications", "More Details"});
        Map<Integer, String> comparePaths = new java.util.HashMap<>();
        for (Issue issue : issues) {
            String compare = "";
            if (issue.comparePubs() != null && issue.comparePubs().size() > 1) {
                compare = "View publications";
                // the first path segment is the record the page is "about"; use the issue's publication
                comparePaths.put(issueRows.size(), "/action/infrastructure/data-citation-list/" + issue.pubZdbId()
                        + "/" + String.join(",", issue.comparePubs()));
            }
            issueRows.add(new String[]{issue.type(), issue.pubZdbId(), issue.fileName(), issue.details(), compare,
                    issue.moreDetails()});
        }
        File issuesXlsx = new File(outputDir, "publication_file_issues.xlsx");
        writeXlsx(issuesXlsx, "Issues", issueRows, 1, new int[]{38, 20, 60, 60, 22, 120}, comparePaths, 4);
        System.out.println("Wrote " + issuesXlsx.getAbsolutePath());

        if (anyFix) {
            File fixedFile = new File(outputDir, "fixed_publication_files.csv");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fixedFile))) {
                writer.write("Publication ZDB ID,Action,Filename\n");
                // lines are "pubZdbId,action,filename": order by action, then publication, then filename
                fixes.sort(java.util.Comparator
                        .comparing((String l) -> l.split(",", 3)[1])
                        .thenComparing(l -> l.split(",", 3)[0])
                        .thenComparing(l -> l.split(",", 3)[2]));
                for (String line : fixes) {
                    writer.write(line + "\n");
                }
            }
            List<String[]> fixRows = new ArrayList<>();
            fixRows.add(new String[]{"Publication ZDB ID", "Action", "Filename"});
            for (String line : fixes) {
                fixRows.add(line.split(",", 3));
            }
            writeXlsx(new File(outputDir, "fixed_publication_files.xlsx"), "Fixed", fixRows, 0, new int[]{20, 70, 70}, Map.of(), -1);
            System.out.println("Fixed " + fixes.size() + " publication file rows (see " + fixedFile.getAbsolutePath() + ")");
        }

        Map<String, Long> counts = issues.stream().collect(Collectors.groupingBy(Issue::type, LinkedHashMap::new, Collectors.counting()));
        System.out.println("Found " + issues.size() + " publication file issues " + counts);
        // Finding issues is not a failure: the process exits 0 and the Jenkins job marks the build UNSTABLE when
        // the report has issues. An unexpected error propagates, which fails the task (and the build).
    }

    /** Base URL the publication ZDB IDs in the xlsx link to: INSTANCE_URL if set, else this instance's domain. */
    private static String instanceUrl() {
        String url = System.getenv("INSTANCE_URL");
        if (url == null || url.isBlank()) {
            url = ZfinPropertiesEnum.SECURE_HTTP.value() + ZfinPropertiesEnum.DOMAIN_NAME.value();
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    /**
     * Writes the rows (first row is the header) to an xlsx file. The cells in pubColumn that hold a
     * ZDB-PUB ID link to {instance URL}/{ZDB ID}. Cells in linkColumn link to {instance URL}{linkPaths.get(row)}. Excel limits a cell to 32,767 characters, so longer
     * values are cut.
     */
    private static void writeXlsx(File file, String sheetName, List<String[]> rows, int pubColumn, int[] columnWidths,
                                  Map<Integer, String> linkPaths, int linkColumn) throws IOException {
        String baseUrl = instanceUrl();
        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.xssf.usermodel.XSSFSheet sheet = workbook.createSheet(sheetName);
            org.apache.poi.ss.usermodel.CreationHelper helper = workbook.getCreationHelper();

            org.apache.poi.ss.usermodel.Font linkFont = workbook.createFont();
            linkFont.setUnderline(org.apache.poi.ss.usermodel.Font.U_SINGLE);
            linkFont.setColor(org.apache.poi.ss.usermodel.IndexedColors.BLUE.getIndex());
            org.apache.poi.ss.usermodel.CellStyle linkStyle = workbook.createCellStyle();
            linkStyle.setFont(linkFont);
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);

            for (int r = 0; r < rows.size(); r++) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(r);
                String[] values = rows.get(r);
                for (int c = 0; c < values.length; c++) {
                    String value = values[c] == null ? "" : values[c];
                    if (value.length() > 32000) {
                        value = value.substring(0, 32000) + " ...(truncated)";
                    }
                    org.apache.poi.ss.usermodel.Cell cell = row.createCell(c);
                    cell.setCellValue(value);
                    if (r == 0) {
                        cell.setCellStyle(headerStyle);
                    } else if (c == linkColumn && linkPaths.containsKey(r)) {
                        org.apache.poi.ss.usermodel.Hyperlink link = helper.createHyperlink(org.apache.poi.common.usermodel.HyperlinkType.URL);
                        link.setAddress(baseUrl + linkPaths.get(r));
                        cell.setHyperlink(link);
                        cell.setCellStyle(linkStyle);
                    } else if (c == pubColumn && value.startsWith("ZDB-PUB-")) {
                        org.apache.poi.ss.usermodel.Hyperlink link = helper.createHyperlink(org.apache.poi.common.usermodel.HyperlinkType.URL);
                        link.setAddress(baseUrl + "/" + value);
                        cell.setHyperlink(link);
                        cell.setCellStyle(linkStyle);
                    }
                }
            }
            for (int c = 0; c < columnWidths.length; c++) {
                sheet.setColumnWidth(c, columnWidths[c] * 256);
            }
            sheet.createFreezePane(0, 1);
            if (rows.size() > 1) {
                sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, rows.size() - 1, 0, rows.get(0).length - 1));
            }
            try (java.io.FileOutputStream out = new java.io.FileOutputStream(file)) {
                workbook.write(out);
            }
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

    private List<Issue> findIssues(PublicationService pubService, boolean checkIdenticalContent) {
        List<PublicationFile> files = getPublicationRepository().getAllPublicationFiles();
        List<Issue> issues = new ArrayList<>();

        for (PublicationFile file : files) {
            if (pubService.getPublicationFileSizeOnDisk(file) == 0) {
                issues.add(new Issue("EMPTY_FILE", file.getPublication().getZdbID(), baseName(file.getFileName()),
                        file.getType().getName().toString(), ""));
            }
        }

        Map<String, List<PublicationFile>> byPub = files.stream()
                .collect(Collectors.groupingBy(f -> f.getPublication().getZdbID()));
        // one FILE_NAMED_FOR_OTHER_PUBLICATION issue per publication, listing the offending files
        Map<String, NamedForOther> namedForOther = new LinkedHashMap<>();
        for (PublicationFile file : files) {
            String ownId = file.getPublication().getZdbID();
            // other publications' ZDB IDs in the uploaded name and in the stored path, and where each was found
            Map<String, List<String>> foundIn = new LinkedHashMap<>();
            String uploadedName = file.getOriginalFileName() == null ? "" : file.getOriginalFileName();
            for (String[] source : new String[][]{{"uploaded name", uploadedName}, {"stored path", file.getFileName()}}) {
                java.util.regex.Matcher m = PUB_ZDB_ID.matcher(source[1]);
                while (m.find()) {
                    if (!m.group().equals(ownId)) {
                        List<String> where = foundIn.computeIfAbsent(m.group(), k -> new ArrayList<>());
                        if (!where.contains(source[0])) {
                            where.add(source[0]);
                        }
                    }
                }
            }
            if (foundIn.isEmpty()) {
                continue;
            }
            NamedForOther agg = namedForOther.computeIfAbsent(ownId, k -> new NamedForOther());
            boolean inUploadedName = foundIn.values().stream().anyMatch(w -> w.contains("uploaded name"));
            agg.names.add(inUploadedName ? uploadedName : baseName(file.getFileName()));
            agg.storedPaths.add(baseName(file.getFileName()));
            for (Map.Entry<String, List<String>> other : foundIn.entrySet()) {
                // the other publication may no longer exist (merged away); only link to ones that do
                if (getPublicationRepository().getPublication(other.getKey()) != null) {
                    agg.otherPubs.add(other.getKey());
                }
                // the directory is only shown when it is where the other ID was found (e.g. a moved file)
                String storedShown = baseName(file.getFileName()).contains(other.getKey())
                        ? baseName(file.getFileName()) : file.getFileName();
                agg.more.add(file.getType().getName() + " [id=" + file.getId() + ", entered=" + formatDate(file.getDateEntered())
                        + "] was uploaded as '" + uploadedName + "' and stored as '" + storedShown
                        + "'; its " + String.join(" and ", other.getValue())
                        + (other.getValue().size() > 1 ? " both mention " : " mentions ")
                        + other.getKey() + ", not " + ownId + ". " + compareWithOtherPublication(file, byPub.get(other.getKey())));
            }
        }
        for (Map.Entry<String, NamedForOther> e : namedForOther.entrySet()) {
            NamedForOther agg = e.getValue();
            List<String> comparePubs = new ArrayList<>(List.of(e.getKey()));
            comparePubs.addAll(agg.otherPubs);
            issues.add(new Issue("FILE_NAMED_FOR_OTHER_PUBLICATION", e.getKey(),
                    agg.storedPaths.size() == 1 ? agg.storedPaths.iterator().next() : null,
                    "Contains files named: " + agg.names.stream().map(n -> "'" + n + "'").collect(Collectors.joining(", ")),
                    String.join(" | ", agg.more),
                    comparePubs.size() > 1 ? comparePubs : null));
        }

        for (Map.Entry<String, List<PublicationFile>> entry : byPub.entrySet()) {
            List<PublicationFile> pubFiles = entry.getValue();

            List<PublicationFile> originals = pubFiles.stream()
                    .filter(f -> f.getType().getName() == PublicationFileType.Name.ORIGINAL_ARTICLE)
                    .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                    .toList();
            if (originals.size() > 1) {
                // Only files sharing a size with another original are hashed; a unique size is already distinct.
                SizeIndex originalSizes = new SizeIndex(originals);
                List<String> md5s = originals.stream()
                        .map(f -> !originalSizes.exists(f) ? null : originalSizes.shared(f) ? md5(f) : UNIQUE_SIZE).toList();
                long hashedDistinct = md5s.stream().filter(m -> m != null && !m.equals(UNIQUE_SIZE)).distinct().count();
                long hashed = md5s.stream().filter(m -> m != null && !m.equals(UNIQUE_SIZE)).count();
                long uniqueSize = md5s.stream().filter(UNIQUE_SIZE::equals).count();
                long distinct = hashedDistinct + uniqueSize;
                boolean duplicated = hashedDistinct < hashed;
                boolean anyMissing = md5s.stream().anyMatch(m -> m == null);
                StringBuilder details = new StringBuilder(originals.size() + " rows, "
                        + distinct + " distinct md5" + (anyMissing ? " (some files missing)" : "") + ": ");
                for (int i = 0; i < originals.size(); i++) {
                    PublicationFile f = originals.get(i);
                    if (i > 0) {
                        details.append(" | ");
                    }
                    details.append(f.getOriginalFileName())
                            .append(" [id=").append(f.getId())
                            .append(", entered=").append(formatDate(f.getDateEntered()))
                            .append(", modified=").append(formatDate(modifiedOnDisk(f)))
                            .append(", size=").append(fileOnDisk(f).length())
                            .append(", md5=").append(md5s.get(i) == null ? "missing"
                                    : md5s.get(i).equals(UNIQUE_SIZE) ? "not hashed (unique size)" : md5s.get(i))
                            .append("]");
                }
                issues.add(new Issue(duplicated ? "MULTIPLE_ORIGINAL_ARTICLES_DUPLICATED" : "MULTIPLE_ORIGINAL_ARTICLES",
                        entry.getKey(), null, originals.size() + " x Original Article", details.toString()));
            }

            Map<String, List<PublicationFile>> byFileName = pubFiles.stream()
                    .collect(Collectors.groupingBy(PublicationFile::getFileName));
            for (Map.Entry<String, List<PublicationFile>> sameFile : byFileName.entrySet()) {
                if (sameFile.getValue().size() > 1) {
                    List<PublicationFile> rows = sameFile.getValue().stream()
                            .sorted((a, b) -> Long.compare(a.getId(), b.getId())).toList();
                    boolean mixed = rows.stream().map(f -> f.getType().getName()).distinct().count() > 1;
                    issues.add(new Issue(mixed ? "DUPLICATE_FILE_MIXED_TYPES" : "DUPLICATE_FILE",
                            entry.getKey(), baseName(sameFile.getKey()), duplicateFileSummary(rows, mixed),
                            duplicateFileDetails(rows, mixed)));
                }
            }

            if (checkIdenticalContent && pubFiles.size() > 1) {
                Map<String, List<PublicationFile>> byMd5 = new LinkedHashMap<>();
                SizeIndex pubSizes = new SizeIndex(pubFiles);
                pubFiles.stream().sorted((a, b) -> Long.compare(a.getId(), b.getId())).forEach(f -> {
                    String sum = pubSizes.shared(f) ? md5(f) : null;
                    if (sum != null && fileOnDisk(f).length() > 0) {
                        byMd5.computeIfAbsent(sum, k -> new ArrayList<>()).add(f);
                    }
                });
                for (Map.Entry<String, List<PublicationFile>> same : byMd5.entrySet()) {
                    List<PublicationFile> rows = same.getValue();
                    if (rows.stream().map(PublicationFile::getFileName).distinct().count() > 1) {
                        issues.add(new Issue("IDENTICAL_CONTENT", entry.getKey(), null,
                                "Identical content: " + rows.stream().map(f -> "'" + f.getOriginalFileName() + "'")
                                        .distinct().collect(Collectors.joining(", ")),
                                "md5=" + same.getKey() + ": " + rows.stream().map(f -> baseName(f.getFileName())
                                        + " [" + f.getType().getName() + ", id=" + f.getId()
                                        + ", entered=" + formatDate(f.getDateEntered()) + "]")
                                        .collect(Collectors.joining(" | "))));
                    }
                }
            }
        }

        if (checkIdenticalContent) {
            issues.addAll(findIdenticalContentAcrossPublications(files));
        }

        issues.sort((a, b) -> {
            int c = a.type().compareTo(b.type());
            if (c != 0) {
                return c;
            }
            c = a.pubZdbId().compareTo(b.pubZdbId());
            return c != 0 ? c : String.valueOf(a.fileName()).compareTo(String.valueOf(b.fileName()));
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
                HibernateUtil.currentSession().delete(empty);
                getInfrastructureRepository().insertUpdatesTableWithoutPerson(pub.getZdbID(), "publication file",
                        desc, null, "Delete file: empty on disk (FindPublicationFileIssuesTask " + flag + ")");
                changes.add(pub.getZdbID() + ",deleted empty Original Article," + baseName(empty.getFileName()));

                if (promote != null) {
                    String before = promote.getFileName() + " (" + promote.getType().getName() + ")";
                    promote.setType(originalType);
                    getInfrastructureRepository().insertUpdatesTableWithoutPerson(pub.getZdbID(), "publication file",
                            before, promote.getFileName() + " (" + originalType.getName() + ")",
                            "Edit file type: promoted to Original Article (FindPublicationFileIssuesTask " + flag + ")");
                    changes.add(pub.getZdbID() + ",promoted to Original Article," + baseName(promote.getFileName()));
                }
            }
            tx.commit();
        } catch (RuntimeException e) {
            tx.rollback();
            throw e;
        }
        return changes;
    }

    /**
     * Deletes duplicate Original Article rows whose files on disk are byte-identical, keeping the
     * lowest-id row of each identical group. Returns one CSV line per change.
     */
    private List<String> fixIdenticalOriginalArticles(PublicationService pubService) {
        List<String> changes = new ArrayList<>();
        Map<String, List<PublicationFile>> originalsByPub = getPublicationRepository().getAllPublicationFiles().stream()
                .filter(f -> f.getType().getName() == PublicationFileType.Name.ORIGINAL_ARTICLE)
                .collect(Collectors.groupingBy(f -> f.getPublication().getZdbID()));

        Transaction tx = HibernateUtil.createTransaction();
        try {
            for (List<PublicationFile> originals : originalsByPub.values()) {
                if (originals.size() < 2) {
                    continue;
                }
                Map<String, List<PublicationFile>> byMd5 = new LinkedHashMap<>();
                SizeIndex originalSizes = new SizeIndex(originals);
                originals.stream().sorted((a, b) -> Long.compare(a.getId(), b.getId())).forEach(f -> {
                    if (pubService.getPublicationFileSizeOnDisk(f) == 0) {
                        return; // missing or empty: not comparable, left to the empty-file fixes
                    }
                    String sum = originalSizes.shared(f) ? md5(f) : null;
                    if (sum != null) {
                        byMd5.computeIfAbsent(sum, k -> new ArrayList<>()).add(f);
                    }
                });
                for (List<PublicationFile> group : byMd5.values()) {
                    PublicationFile keep = group.get(0);
                    // one updates row and one report line per stored file, however many rows it had
                    Map<String, List<PublicationFile>> dupsByFile = new LinkedHashMap<>();
                    for (PublicationFile dup : group.subList(1, group.size())) {
                        HibernateUtil.currentSession().delete(dup);
                        dupsByFile.computeIfAbsent(dup.getFileName(), k -> new ArrayList<>()).add(dup);
                    }
                    for (List<PublicationFile> dups : dupsByFile.values()) {
                        PublicationFile first = dups.get(0);
                        String pubId = first.getPublication().getZdbID();
                        getInfrastructureRepository().insertUpdatesTableWithoutPerson(pubId, "publication file",
                                first.getFileName() + " (" + first.getType().getName() + ")", null,
                                "Delete file: " + describeRows(dups) + " identical to Original Article "
                                        + keep.getFileName() + " (FindPublicationFileIssuesTask "
                                        + FIX_IDENTICAL_ORIGINALS + ")");
                        changes.add(pubId + ",deleted " + dups.size() + " Original Article row(s) identical to "
                                + baseName(keep.getFileName()) + "," + baseName(first.getFileName()));
                    }
                }
            }
            tx.commit();
        } catch (RuntimeException e) {
            tx.rollback();
            throw e;
        }
        return changes;
    }

    /**
     * Deletes rows that record the same stored file more than once with the same type, keeping the
     * lowest-id row. The rows share one file on disk, which is left alone. Returns one CSV line per change.
     */
    private List<String> fixDuplicateFileRows() {
        List<String> changes = new ArrayList<>();
        Map<String, List<PublicationFile>> groups = getPublicationRepository().getAllPublicationFiles().stream()
                .collect(Collectors.groupingBy(f -> f.getPublication().getZdbID() + "\t" + f.getFileName()
                        + "\t" + f.getType().getName()));

        Transaction tx = HibernateUtil.createTransaction();
        try {
            for (List<PublicationFile> group : groups.values()) {
                if (group.size() < 2) {
                    continue;
                }
                List<PublicationFile> sorted = group.stream().sorted((a, b) -> Long.compare(a.getId(), b.getId())).toList();
                PublicationFile keep = sorted.get(0);
                List<PublicationFile> dups = sorted.subList(1, sorted.size());
                dups.forEach(dup -> HibernateUtil.currentSession().delete(dup));
                // one updates row and one report line per group, however many rows it had
                String pubId = keep.getPublication().getZdbID();
                getInfrastructureRepository().insertUpdatesTableWithoutPerson(pubId, "publication file",
                        keep.getFileName() + " (" + keep.getType().getName() + ")", null,
                        "Delete file: " + describeRows(dups) + " duplicating the same file and type, kept row id "
                                + keep.getId() + " (FindPublicationFileIssuesTask " + FIX_DUPLICATE_FILES + ")");
                changes.add(pubId + ",deleted " + dups.size() + " duplicate " + keep.getType().getName()
                        + " row(s) (kept id " + keep.getId() + ")," + baseName(keep.getFileName()));
            }
            tx.commit();
        } catch (RuntimeException e) {
            tx.rollback();
            throw e;
        }
        return changes;
    }

    /** "2 x Supplemental Material" for same-type duplicates, "Other + Original Article" for mixed types. */
    private static String duplicateFileSummary(List<PublicationFile> rows, boolean mixed) {
        if (mixed) {
            return rows.stream().map(f -> f.getType().getName().toString()).collect(Collectors.joining(" + "));
        }
        return rows.size() + " x " + rows.get(0).getType().getName();
    }

    /**
     * Mixed types list every row; same-type duplicates can number in the hundreds, so they are summarized
     * as a count with the id and date-entered range.
     */
    private static String duplicateFileDetails(List<PublicationFile> rows, boolean mixed) {
        if (mixed) {
            return rows.stream().map(f -> f.getType().getName() + " [id=" + f.getId()
                    + ", entered=" + formatDate(f.getDateEntered()) + "]").collect(Collectors.joining(" + "));
        }
        PublicationFile first = rows.get(0);
        PublicationFile last = rows.get(rows.size() - 1);
        return rows.size() + " x " + first.getType().getName() + " [ids=" + first.getId() + ".." + last.getId()
                + ", entered=" + formatDate(first.getDateEntered()) + " .. " + formatDate(last.getDateEntered()) + "]";
    }

    /**
     * Says whether the other publication has a file with the same content as this one. Only files of the
     * same size are hashed.
     */
    private String compareWithOtherPublication(PublicationFile file, List<PublicationFile> otherFiles) {
        if (otherFiles == null || otherFiles.isEmpty()) {
            return "that publication has no files";
        }
        if (!fileOnDisk(file).isFile()) {
            return "this file is missing on disk";
        }
        long size = fileOnDisk(file).length();
        String mine = null;
        for (PublicationFile o : otherFiles) {
            if (fileOnDisk(o).isFile() && fileOnDisk(o).length() == size) {
                mine = mine == null ? md5(file) : mine;
                if (mine != null && mine.equals(md5(o))) {
                    return "identical to " + o.getType().getName() + " " + baseName(o.getFileName()) + " of that publication";
                }
            }
        }
        return "that publication has " + otherFiles.size() + " file(s), none identical";
    }

    /**
     * Groups every file on disk by size, hashes only sizes that occur on more than one publication, and
     * reports each md5 that appears on more than one publication.
     */
    private List<Issue> findIdenticalContentAcrossPublications(List<PublicationFile> files) {
        Map<Long, List<PublicationFile>> bySize = new LinkedHashMap<>();
        for (PublicationFile f : files) {
            File onDisk = fileOnDisk(f);
            if (onDisk.isFile() && onDisk.length() > 0) {
                bySize.computeIfAbsent(onDisk.length(), k -> new ArrayList<>()).add(f);
            }
        }

        List<Issue> issues = new ArrayList<>();
        for (List<PublicationFile> sameSize : bySize.values()) {
            if (sameSize.stream().map(f -> f.getPublication().getZdbID()).distinct().count() < 2) {
                continue;
            }
            Map<String, List<PublicationFile>> byMd5 = new LinkedHashMap<>();
            sameSize.stream().sorted((a, b) -> Long.compare(a.getId(), b.getId())).forEach(f -> {
                String sum = md5(f);
                if (sum != null) {
                    byMd5.computeIfAbsent(sum, k -> new ArrayList<>()).add(f);
                }
            });
            for (Map.Entry<String, List<PublicationFile>> same : byMd5.entrySet()) {
                List<PublicationFile> rows = same.getValue();
                if (rows.stream().map(f -> f.getPublication().getZdbID()).distinct().count() < 2) {
                    continue;
                }
                String firstPub = rows.stream().map(f -> f.getPublication().getZdbID()).sorted().findFirst().orElseThrow();
                List<String> pubIds = rows.stream().map(f -> f.getPublication().getZdbID()).distinct().sorted().toList();
                issues.add(new Issue("IDENTICAL_CONTENT_ACROSS_PUBLICATIONS", firstPub, null,
                        "Also on: " + pubIds.stream().filter(id -> !id.equals(firstPub)).collect(Collectors.joining(", "))
                                + " (" + rows.stream().map(f -> baseName(f.getFileName())).distinct()
                                .collect(Collectors.joining(", ")) + ")",
                        "md5=" + same.getKey() + ": " + rows.stream().map(f -> f.getPublication().getZdbID() + " "
                                + baseName(f.getFileName()) + " [" + f.getType().getName() + ", id=" + f.getId()
                                + ", entered=" + formatDate(f.getDateEntered()) + "]")
                                .collect(Collectors.joining(" | ")),
                        pubIds));
            }
        }
        return issues;
    }

    /** "1 row (id 5)" or "564 rows (ids 36762..38211)" for rows sorted by id. */
    private static String describeRows(List<PublicationFile> rows) {
        List<PublicationFile> sorted = rows.stream().sorted((a, b) -> Long.compare(a.getId(), b.getId())).toList();
        if (sorted.size() == 1) {
            return "1 row (id " + sorted.get(0).getId() + ")";
        }
        return sorted.size() + " rows (ids " + sorted.get(0).getId() + ".." + sorted.get(sorted.size() - 1).getId() + ")";
    }

    /** The file name without its directories, for the reports ("2022/ZDB-PUB-1-1/a.pdf" becomes "a.pdf"). */
    private static String baseName(String path) {
        return path == null ? null : path.replaceAll("^.*/", "");
    }

    private static File fileOnDisk(PublicationFile file) {
        return new File(new File(ZfinPropertiesEnum.LOADUP_FULL_PATH.toString()), file.getFileName());
    }

    private static Date modifiedOnDisk(PublicationFile file) {
        File f = fileOnDisk(file);
        return f.exists() ? new Date(f.lastModified()) : null;
    }

    private static String formatDate(Date date) {
        return date == null ? "unknown" : new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
    }

    /**
     * File sizes for a group of rows, read once each. Files of different sizes cannot be identical, so
     * only files whose size is shared with another row need hashing.
     */
    private static final class SizeIndex {
        private final Map<Long, Long> sizeByRowId = new java.util.HashMap<>();
        private final Map<Long, Integer> rowsPerSize = new java.util.HashMap<>();

        SizeIndex(List<PublicationFile> rows) {
            for (PublicationFile row : rows) {
                File f = fileOnDisk(row);
                if (f.isFile()) {
                    long size = f.length();
                    sizeByRowId.put(row.getId(), size);
                    rowsPerSize.merge(size, 1, Integer::sum);
                }
            }
        }

        boolean exists(PublicationFile row) {
            return sizeByRowId.containsKey(row.getId());
        }

        /** True if the file exists and another row's file has the same size. */
        boolean shared(PublicationFile row) {
            Long size = sizeByRowId.get(row.getId());
            return size != null && rowsPerSize.get(size) > 1;
        }
    }

    /** Hex md5 of the file on disk, or null if it is missing or unreadable. */
    private String md5(PublicationFile file) {
        return md5Cache.computeIfAbsent(file.getId(), id -> computeMd5(file));
    }

    private static String computeMd5(PublicationFile file) {
        File f = fileOnDisk(file);
        if (!f.isFile()) {
            return null;
        }
        try (InputStream in = Files.newInputStream(f.toPath())) {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) > 0) {
                digest.update(buffer, 0, n);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (IOException | java.security.NoSuchAlgorithmException e) {
            return null;
        }
    }
}
