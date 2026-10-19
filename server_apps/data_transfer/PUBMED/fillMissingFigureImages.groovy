#!/bin/bash
//usr/bin/env groovy -cp "$GROOVY_CLASSPATH:$(dirname "$0")" "$0" $@; exit $?

/**
 * ZFIN-10520, phase 2: attach the PubMed Central image to existing figures that have
 * none, keeping each figure's ZDB ID so the curated data on it stays attached.
 *
 * Unlike getPDFandImages.groovy, which only ever inserts new figures, this fills a
 * figure in place:
 *   - the PMC figure is found by label with PmcFigureMatcher, the same matching the
 *     phase 1 report (reportMissingFigureImages.groovy) uses, re-checked at fill time;
 *   - the image file is downloaded from the PMC open-access package and attached with
 *     ImageService.processImage() -- the same path as a curator upload, so the file is
 *     named after its image ID and TIFF/WebP are converted for browsers;
 *   - the PMC caption is copied only when the figure has none.
 *
 * Only figures named on the command line are touched, and nothing is written unless
 * --apply is given; without it the script prints what it would do.
 *
 * Usage, from the source root (Hibernate looks for its mapping files under
 * source/org/zfin relative to the working directory):
 *   server_apps/data_transfer/PUBMED/fillMissingFigureImages.groovy [--apply] ZDB-FIG-... [ZDB-FIG-...]
 */

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.zfin.expression.Image
import org.zfin.figure.service.ImageService
import org.zfin.framework.HibernateSessionCreator
import org.zfin.framework.HibernateUtil
import org.zfin.profile.Person
import org.zfin.properties.ZfinProperties
import org.zfin.properties.ZfinPropertiesEnum

import static org.zfin.repository.RepositoryFactory.getFigureRepository

ZfinProperties.init("${System.getenv()['ZFIN_PROPERTIES_PATH']}")
new HibernateSessionCreator()

// Owner of the images, as for the images getPDFandImages.groovy loads from PMC
// (load_figs_and_images.sql). processImage() hard-codes a different person for its
// file-path variant, so the owner is set explicitly below.
final String OWNER_ZDB_ID = 'ZDB-PERS-030612-1'

def apply = args.contains('--apply')
def figureIds = args.findAll { it.startsWith('ZDB-FIG-') }
if (!figureIds) {
    println "Usage: fillMissingFigureImages.groovy [--apply] ZDB-FIG-... [ZDB-FIG-...]"
    System.exit(1)
}
println(apply ? "Filling ${figureIds.size()} figure(s)." : "DRY RUN for ${figureIds.size()} figure(s); add --apply to write.")

def downloadDir = File.createTempDir('pmc-figure-images', '')
def articles = [:]  // pmcId -> article, so figures of one publication share a lookup
def results = []

figureIds.each { figId ->
    def figure = getFigureRepository().getFigure(figId)
    if (!figure) {
        results << [figId, 'SKIPPED', 'no such figure']
        return
    }
    def pub = figure.publication
    // pub_pmc_id is not mapped on Publication, so read it directly.
    String pmcId = HibernateUtil.currentSession()
            .createNativeQuery("select pub_pmc_id from publication where zdb_id = :id", String)
            .setParameter('id', pub.zdbID).uniqueResult()
    def reason = !pmcId ? 'publication has no PMC ID'
            : !pub.canShowImages ? 'publication may not show images'
            : figure.images ? 'figure already has an image'
            : null
    if (reason) {
        results << [figId, 'SKIPPED', reason]
        return
    }

    def article
    try {
        article = articles.computeIfAbsent(pmcId) { PmcFigureMatcher.article(it) }
    } catch (Exception e) {
        results << [figId, 'SKIPPED', "PMC lookup failed: ${e.message}"]
        return
    }
    def match = PmcFigureMatcher.match(figure.label, article)
    if (match.status != PmcFigureMatcher.FILLABLE) {
        results << [figId, 'SKIPPED', "${match.status}${match.figure ? ' (' + match.figure.label + ')' : ''}"]
        return
    }

    def setCaption = !figure.caption?.trim() && match.figure.captionMarkup
    def plan = "'${figure.label}' <- PMC '${match.figure.label}' ${match.file}" +
            (setCaption ? ", caption ${match.figure.captionMarkup.length()} chars" : ", caption kept")
    if (!apply) {
        results << [figId, 'WOULD FILL', plan]
        return
    }

    def tx = HibernateUtil.createTransaction()
    Image image = null
    try {
        def owner = HibernateUtil.currentSession().get(Person, OWNER_ZDB_ID)
        // Run as that persisted Person: processImage() records the change through
        // insertUpdatesTable, which uses the current security user -- in a headless run
        // an unsaved "Guest" that makes the flush fail (as in MoensHcrImageLoad).
        def securityContext = new SecurityContextImpl()
        securityContext.authentication = new UsernamePasswordAuthenticationToken(owner, null)
        SecurityContextHolder.context = securityContext
        // Keep the PMC file name: processImage() takes the format from its extension.
        def downloaded = new File(downloadDir, match.file)
        PubmedUtils.downloadS3File(match.s3Key, downloaded.absolutePath)
        image = ImageService.processImage(figure, downloaded.absolutePath, false, null, pub.zdbID)
        image.owner = owner
        image.insertedBy = owner
        image.updatedBy = owner
        if (setCaption) {
            figure.caption = match.figure.captionMarkup
        }
        HibernateUtil.currentSession().flush()
        tx.commit()
        results << [figId, 'FILLED', "$plan -> ${image.zdbID} (${image.imageFilename})"]
    } catch (Exception e) {
        tx.rollback()
        // processImage() writes the files before the flush; without this a failure
        // leaves them behind under an image ID the rollback has just released.
        deleteImageFiles(image)
        results << [figId, 'FAILED', "$plan: ${e.message}"]
        e.printStackTrace()
    }
}

downloadDir.deleteDir()
println ''
results.each { println String.format('%-22s %-11s %s', it[0], it[1], it[2]) }
println ''
results.groupBy { it[1] }.each { status, rows -> println "${status}: ${rows.size()}" }
System.exit(results.any { it[1] == 'FAILED' } ? 1 : 0)

// Removes the stored file, its JPEG/thumbnail/medium derivatives and any kept original.
def deleteImageFiles(Image image) {
    if (!image?.imageFilename || image.imageFilename == 'tmpvalue') {
        return
    }
    def stored = new File(ZfinPropertiesEnum.LOADUP_FULL_PATH.toString(), image.imageFilename)
    stored.parentFile?.listFiles()?.findAll {
        it.name.startsWith(image.zdbID + '.') || it.name.startsWith(image.zdbID + '_')
    }?.each { println "  removing ${it}"; it.delete() }
}
