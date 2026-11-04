import groovy.xml.StreamingMarkupBuilder
import groovy.xml.slurpersupport.GPathResult

/**
 * ZFIN-10520: matches ZFIN figures to the figures of a PubMed Central article, for
 * reportMissingFigureImages.groovy (phase 1, report) and fillMissingFigureImages.groovy
 * (phase 2, fill). Kept in one place so the report and the fill can never disagree
 * about which PMC figure belongs to a ZFIN figure.
 *
 * A PMC article is read from two sources: the article XML via OAI (figure and
 * supplementary-figure labels, captions, image references) and the open-access S3
 * package (which image files actually exist). Figures are matched by normalized label.
 */
class PmcFigureMatcher {

    static final String FILLABLE = 'FILLABLE'
    static final String NO_PMC_FIGURE = 'NO_PMC_FIGURE_WITH_LABEL'
    static final String AMBIGUOUS = 'AMBIGUOUS_LABEL'
    static final String IMAGE_NOT_IN_PACKAGE = 'IMAGE_NOT_IN_S3_PACKAGE'
    static final String NO_PACKAGE = 'NO_OPEN_ACCESS_PACKAGE'
    static final String NO_FIGURE_NUMBER = 'NO_FIGURE_NUMBER'
    static final String EXCLUDED_LABEL = 'EXCLUDED_LABEL'
    static final String PMC_ERROR = 'PMC_LOOKUP_FAILED'

    static final List<String> STATUSES = [FILLABLE, NO_PMC_FIGURE, AMBIGUOUS, IMAGE_NOT_IN_PACKAGE,
                                          NO_PACKAGE, EXCLUDED_LABEL, NO_FIGURE_NUMBER, PMC_ERROR]

    static final Set<String> IMAGE_EXTENSIONS = ['jpg', 'jpeg', 'png', 'gif', 'tif', 'tiff', 'webp'] as Set

    /**
     * One PMC article: its figures and its S3 package. {@code files} maps each package
     * file name to its full S3 key ("PMC123.2/fig1.jpg"), or is empty when the article
     * is not in the open-access subset.
     */
    static Map article(String pmcId) {
        def keys = PubmedUtils.listS3Files(pmcId)
        def files = keys.collectEntries { [(it.substring(it.indexOf('/') + 1)): it] }
        def figures = files ? figuresOf(PubmedUtils.getFullText(pmcId.replace("PMC", ""))) : []
        [pmcId: pmcId, files: files, figures: figures]
    }

    /**
     * The match for one ZFIN figure label in an article: [status, figure (the PMC
     * figure map, if found), key (S3 key of its image, when FILLABLE)].
     */
    static Map match(String zfinLabel, Map article) {
        // Label checks first: they do not depend on PMC, so a table is reported as
        // excluded whether or not its article is in the open-access subset.
        if (isExcludedLabel(zfinLabel)) {
            return [status: EXCLUDED_LABEL]
        }
        def key = normalizeLabel(zfinLabel)
        if (!key) {
            return [status: NO_FIGURE_NUMBER]
        }
        if (!article.files) {
            return [status: NO_PACKAGE]
        }
        def matches = article.figures.findAll { it.key == key }
        if (matches.isEmpty()) {
            return [status: NO_PMC_FIGURE]
        }
        if (matches.size() > 1) {
            return [status: AMBIGUOUS, figure: [label: matches*.label.join(' / ')]]
        }
        def figure = matches[0]
        def file = fileInPackage(figure.href, article.files.keySet())
        if (!file) {
            return [status: IMAGE_NOT_IN_PACKAGE, figure: figure]
        }
        return [status: FILLABLE, figure: figure, file: file, s3Key: article.files[file]]
    }

    /**
     * "Fig. 3" / "Figure 3" / "FIGURE 3." -> "3"; "Fig. S1" / "S1 File" / "Supplementary
     * Figure S1" -> "S1"; sub-figures "Figure 3—figure supplement 1" / "Fig. 3 S1" -> "3-S1";
     * "Extended Data Fig. 4" -> "E4". Tables, videos, movies and author response images
     * are not figures to fill, and neither is "text only": all -> null. Without that,
     * "Video 1" or "Fig. Table 1" would compete with (or be mistaken for) Figure 1.
     */
    static String normalizeLabel(String label) {
        if (isExcludedLabel(label)) {
            return null
        }
        def s = (label ?: '').trim().toLowerCase().replaceAll(/[.:]+$/, '')
        def words = s.replaceAll(/[^a-z0-9 ]/, ' ')
        def m = words =~ /\b(s?)(\d+)\b/
        if (!m.find()) {
            return null
        }
        def prefix = s.substring(0, m.start())
        boolean supplementary = m.group(1) || (prefix =~ /\bsupp\w*/)
        // Nature-style "Extended Data Fig. 4" is a different figure from "Figure 4".
        boolean extendedData = prefix =~ /\bextended data\b/
        def key = (supplementary ? 'S' : extendedData ? 'E' : '') + Integer.parseInt(m.group(2))
        // A sub-figure of a main figure: "...figure supplement 1" or a trailing "S1".
        def sub = words.substring(m.end()) =~ /\b(?:supplement\w*\s+|s)(\d+)\b/
        if (!supplementary && sub.find()) {
            key += '-S' + Integer.parseInt(sub.group(1))
        }
        return key
    }

    /**
     * Tables, videos, movies and author response images: not image figures to fill, and
     * matching their number would pair them with the figure of the same number
     * ("Table S4" with supplementary figure S4, "Video 1" with Figure 1).
     */
    static boolean isExcludedLabel(String label) {
        (label ?: '').toLowerCase() =~ /table|video|movie|author response/
    }

    /**
     * Figures from the article XML: <fig> elements, plus supplementary figures, which
     * PMC carries as <supplementary-material>. Only image files count there, and they
     * are always supplementary whatever their label says. Each figure: label, key,
     * href, caption (text) and captionMarkup (the caption's inner XML, as the PMC
     * figure load stores it: "<p>... <italic>in vitro</italic> ...</p>").
     */
    static List<Map> figuresOf(GPathResult article) {
        def figures = []
        article.'**'.findAll { it.name() == 'fig' }.each { fig ->
            def label = fig.'*'.find { it.name() == 'label' }?.text()?.trim() ?: ''
            def caption = fig.'*'.find { it.name() == 'caption' }
            def graphic = fig.'**'.find { it.name() == 'graphic' }
            figures << [label: label, key: normalizeLabel(label), href: graphic ? href(graphic) : null,
                        caption: whitespace(caption?.text() ?: ''), captionMarkup: innerMarkup(caption)]
        }
        article.'**'.findAll { it.name() == 'supplementary-material' }.each { sm ->
            def label = sm.'*'.find { it.name() == 'label' }?.text()?.trim() ?: ''
            def file = href(sm) ?: sm.'**'.find { it.name() == 'media' }?.with { href(it) }
            if (!file || !IMAGE_EXTENSIONS.contains(extension(file))) {
                return
            }
            if (label =~ /(?i)table|video|movie|data|audio/) {
                return
            }
            def key = normalizeLabel(label)
            def caption = sm.'*'.find { it.name() == 'caption' }
            figures << [label: label, key: key ? 'S' + key.replaceFirst(/^S/, '') : null, href: file,
                        caption: whitespace(caption?.text() ?: ''), captionMarkup: innerMarkup(caption)]
        }
        return figures
    }

    /** The xlink:href of a node, whatever prefix the parser kept on the attribute name. */
    static String href(node) {
        node.attributes().find { k, v -> k.toString().endsWith('href') }?.value
    }

    static String extension(String file) {
        file.contains('.') ? file.substring(file.lastIndexOf('.') + 1).toLowerCase() : ''
    }

    /** The package file for an XML image reference; PMC often omits the extension. */
    static String fileInPackage(String href, Collection<String> files) {
        if (!href) {
            return null
        }
        def base = href.substring(href.lastIndexOf('/') + 1)
        if (files.contains(base)) {
            return base
        }
        def stem = base.contains('.') ? base.substring(0, base.lastIndexOf('.')) : base
        return files.find {
            it.contains('.') && it.substring(0, it.lastIndexOf('.')) == stem && IMAGE_EXTENSIONS.contains(extension(it))
        }
    }

    /**
     * The inner markup of a caption element without namespace prefixes or declarations,
     * e.g. "<p>Lentivirus enabled <italic>in vitro</italic> ...</p>" -- the same shape
     * the PMC figure load stores in fig_caption. Empty for a missing caption.
     */
    static String innerMarkup(caption) {
        if (caption == null || caption.isEmpty()) {
            return ''
        }
        def xml = new StreamingMarkupBuilder().bindNode(caption).toString()
        xml = xml.replaceAll(/\s+xmlns(:\w+)?='[^']*'/, '').replaceAll(/\s+xmlns(:\w+)?="[^"]*"/, '')
        xml = xml.replaceAll(/(<\/?)[A-Za-z_][\w.-]*:/, '$1')
        xml = xml.replaceFirst(/^<caption[^>]*>/, '').replaceFirst(/<\/caption>\s*$/, '')
        return toHtml(whitespace(xml))
    }

    /**
     * JATS caption markup to the HTML the figure page can display. ZFIN has no styles
     * for JATS tags, so <italic>/<bold> left as they are lose their formatting:
     * <italic> -> <i>, <bold> -> <b>, <title> -> bold lead-in; attributes such as
     * toggle='yes' are dropped; PLOS's file-type notes ("<p>(TIF)</p>") are removed.
     * <p>, <sup> and <sub> are already HTML and stay.
     */
    static String toHtml(String jats) {
        String html = jats
                .replaceAll('<italic\\b[^>]*>', '<i>').replace('</italic>', '</i>')
                .replaceAll('<bold\\b[^>]*>', '<b>').replace('</bold>', '</b>')
                .replaceAll('<title\\b[^>]*>', '<p><b>').replace('</title>', '</b></p>')
                .replaceAll('<(p|sup|sub)\\b[^>]*>', '<$1>')
                .replaceAll('<p>\\s*\\((?:TIF|TIFF|JPG|JPEG|PNG|GIF|PDF|EPS|DOCX?|XLSX?|PPTX?|ZIP|AVI|MOV|MP4)\\)\\s*</p>', '')
        return whitespace(html)
    }

    static String whitespace(String s) {
        s.replaceAll(/\s+/, ' ').trim()
    }
}
