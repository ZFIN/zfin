import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.GradleException

class SimpleDirectoryCopyTask extends DefaultTask {

    @Input
    String sourcePath

    @Input
    List<String> includes = []  // List of file extensions to include (e.g., 'sh', 'sql')

    @Input
    List<String> excludes = []  // List of filenames to exclude (e.g., 'Makefile', 'build.gradle')

    @Input
    List<String> excludeDirs = []  // List of directories to exclude (relative to sourcePath)

    @Input
    @Optional
    String targetPath = null //Override the default target ($targetroot/$sourcePath)

    // Access global properties from the project.ext
    @Internal
    def targetroot = project.ext.targetroot

    @Internal
    def ttNameMap = project.ext.ttNameMap

//These are the only files that need to be searched and replaced (<!--|KEY|-->)
//Let's whittle this list down over time
    @Internal
    def whitelistTemplateFiles = [
            'cgi-bin/send_request.perl',
            'cgi-bin/merge_markers.pl',
            'server_apps/DB_maintenance/backupBlastDbsAndRsyncAlmostBlastDbs.sh',
            'server_apps/DB_maintenance/checkVarcharOctetLength.pl',
            'server_apps/DB_maintenance/check_undefined_environment.pl',
            'server_apps/DB_maintenance/disable_updates.pl',
            'server_apps/DB_maintenance/extentMonitoring/dailyExtentCheck.sh',
            'server_apps/DB_maintenance/fx_permission_check.pl',
            'server_apps/DB_maintenance/loadExternalNotes.pl',
            'server_apps/DB_maintenance/loadExternalNotes.sql',
            'server_apps/DB_maintenance/loadUp/README_loadUp',
            'server_apps/DB_maintenance/loadUp/rsync.pl',
            'server_apps/DB_maintenance/orphanChecks.pl',
            'server_apps/DB_maintenance/postgres/compareTables.groovy',
            'server_apps/DB_maintenance/pushDataToPublic.sh',
            'server_apps/DB_maintenance/queryMonitoring/dailyQueryCostCheck.sh',
            'server_apps/DB_maintenance/rotateInformixLog.sh',
            'server_apps/DB_maintenance/set_unload_timestamp.sql',
            'server_apps/DB_maintenance/warehouse/phenotypeMart/runPhenotypeMart.sh',
            'server_apps/DB_maintenance/warehouse/switch.sh',
            'server_apps/DB_maintenance/warehouse/who_is.sh',
            'server_apps/DB_maintenance/warehouse/who_is_not.sh',
            'server_apps/Reports/AnnualStats/runStats.sh',
            'server_apps/Reports/BetterFish/betterFish.sql',
            'server_apps/Reports/PubTracking/runAverageTimeInBinsCumulative.sh',
            'server_apps/Reports/PubTracking/runLongestBinResidents.sh',
            'server_apps/Reports/PubTracking/runMonthlyBinLengthReport.sh',
            'server_apps/Reports/PubTracking/runPaperlessPubTrackingDailyIndexedMetrics.sh',
            'server_apps/Reports/elsevier_report.pl',
            'server_apps/Reports/reportPubsForGeneAndFeature.pl',
            'server_apps/WebSiteTools/signsoflife.sh',
            'server_apps/apache/inc-redirect',
            'server_apps/cron/crontab.production',
            'server_apps/data_transfer/Downloads/GFF3/E_unload_ensembl_contig.sql',
            'server_apps/data_transfer/Downloads/generateStagedAnatomy.pl',
            'server_apps/data_transfer/Ensembl/fetch_ensdarT_dbacc.sh',
            'server_apps/data_transfer/Ensembl/fetch_ensdarg.sh',
            'server_apps/data_transfer/Ensembl/fetch_ensdargOttdargTable.sh',
            'server_apps/data_transfer/Ensembl/fetch_ensembl_agp.sh',
            'server_apps/data_transfer/Ensembl/fetch_sangerMutantInfo.sh',
            'server_apps/data_transfer/Ensembl/notifyStaleIds.sh',
            'server_apps/data_transfer/Ensembl/pullFromBioMart.pl',
            'server_apps/data_transfer/Ensembl/reportEnsembleStatistics.sh',
            'server_apps/data_transfer/ExternalSearch/CreateMarkerSearchPage.pl',
            'server_apps/data_transfer/ExternalSearch/CreateMarkerSearchPage.sql',
            'server_apps/data_transfer/GO/go.pl',
            'server_apps/data_transfer/GO/gofile.sql',
            'server_apps/data_transfer/GO/gofile2.sql',
            'server_apps/data_transfer/GO/gofile2_all.sql',
            'server_apps/data_transfer/GO/gp2protein.pl',
            'server_apps/data_transfer/GO/gpad.pl',
            'server_apps/data_transfer/GO/gpad2.0.sql',
            'server_apps/data_transfer/GO/validateUniprotIDsZFIN.pl',
            'server_apps/data_transfer/Genbank/gbaccession.pl',
            'server_apps/data_transfer/Load/ReNo/Nomenclature/generate_special_Sanger_uniprot_nomenclature_run.sh',
            'server_apps/data_transfer/Load/ReNo/load_run_report_hit.sh',
            'server_apps/data_transfer/Load/blast_withdrawn.pl',
            'server_apps/data_transfer/LoadOntology/parseHeader.pl',
            'server_apps/data_transfer/LoadOntology/parseObo.pl',
            'server_apps/data_transfer/MEOW/meow.pl',
            'server_apps/data_transfer/NCBIStartEnd/NCBIStartEnd.pl',
            'server_apps/data_transfer/OMIM/OMIM.pl',
            'server_apps/data_transfer/OMIM/loadOMIM.sql',
            'server_apps/data_transfer/ORTHO/downloadFiles.pl',
            'server_apps/data_transfer/ORTHO/emailOrthologyReports.pl',
            'server_apps/data_transfer/ORTHO/loadAndUpdateNCBIOrthologs.sql',
            'server_apps/data_transfer/ORTHO/loadHumanSynonyms.sql',
            'server_apps/data_transfer/ORTHO/parseHumanData.pl',
            'server_apps/data_transfer/ORTHO/parseOrthoFile.pl',
            'server_apps/data_transfer/ORTHO/reportOrthoNameChanges.pl',
            'server_apps/data_transfer/ORTHO/runOrthology.pl',
            'server_apps/data_transfer/ORTHO/updateZebrafishGeneNames.pl',
            'server_apps/data_transfer/ORTHO/updateZebrafishGeneNames.sql',
            'server_apps/data_transfer/PUBMED/Journal/checkAndUpdateJournals.sql',
            'server_apps/data_transfer/PUBMED/Journal/insertJournalAlias.sql',
            'server_apps/data_transfer/PUBMED/addMeshTermsToAllPubs.groovy',
            'server_apps/data_transfer/PUBMED/addPMCidsToAllPubs.groovy',
            'server_apps/data_transfer/PUBMED/loadNewPubs.sql',
            'server_apps/data_transfer/PUBMED/load_complete_author_names.sql',
            'server_apps/data_transfer/PUBMED/pubActivation.groovy',
            'server_apps/data_transfer/PUBMED/updatePublicationDate.groovy',
            'server_apps/data_transfer/RNACentral/loadTranscriptSequences.sql',
            'server_apps/data_transfer/RNACentral/loadTscriptSeq.pl',
            'server_apps/data_transfer/RNACentral/preLoadTranscriptSequence.sql',
            'server_apps/data_transfer/RNACentral/runTscriptSequenceLoad.pl',
            'server_apps/data_transfer/ResourceCenters/pullFromEZRC.pl',
            'server_apps/data_transfer/ResourceCenters/pullFromZIRC.pl',
            'server_apps/data_transfer/ResourceCenters/pushToZirc.pl',
            'server_apps/data_transfer/ResourceCenters/pushToZirc.sql',
            'server_apps/data_transfer/SNP/addNewClonesJSmith.pl',
            'server_apps/data_transfer/SNP/addTalbotSNPAttr.sql',
            'server_apps/data_transfer/SNP/dbSNP.pl',
            'server_apps/data_transfer/SNP/loadNewSNPAttrs.sql',
            'server_apps/data_transfer/SNP/loadNewSNPs.sql',
            'server_apps/data_transfer/eco_go_mapping/insert_eco_go_map.sql',
            'server_apps/data_transfer/maintainTermDisplay/checkRNAConsequenceTerm.groovy',
            'server_apps/data_transfer/zfishbook/preprocess_zfishbook.pl',
            'server_apps/data_transfer/zfishbook/zfishbook.pl',
            'server_apps/data_transfer/zfishbook/zfishbook.sh',
    ]

    @TaskAction
    void copyFiles() {
        def debugMode = false
        println "Starting copy task for sourcePath: $sourcePath"

        def targetDir = new File("$targetroot/$sourcePath")
        def sourceDir = new File(sourcePath)

        //override the default target dir if provided
        if (targetPath != null) {
            targetDir = new File("$targetroot/$targetPath")
        }

        if (!sourceDir.exists()) {
            println "Source directory does not exist: $sourceDir"
            return
        }

        println "Exclude dirs: " + (excludeDirs.isEmpty() ? "None" : excludeDirs)
        println "Include extensions: " + (includes.isEmpty() ? "All" : includes)
        println "Exclude extensions: " + (excludes.isEmpty() ? "None" : excludes)
        println "--------------------------------------------------"

        def copySingleFile = { source, destination ->
            def relativeDestination = sourceDir.toURI().relativize(source.toURI()).path

            //are the files the same?
            if (destination.exists()) {
                def diffProc = ["diff", source, destination].execute()
                diffProc.waitForOrKill(3600)
                if (diffProc.exitValue() == 0) {
                    if (debugMode) {
                        println "    Skipped: $relativeDestination (Identical file)"
                    }
                    return
                }
            }

            //if not, copy the file
            def sout = new StringBuilder(), serr = new StringBuilder()
            def proc = ["cp", source, destination].execute()
            proc.consumeProcessOutput(sout, serr)
            proc.waitForOrKill(3600)
            if (proc.exitValue() != 0) {
                println "Error"
                println "$serr"
                throw new GradleException("Failed to copy file: $source")
            }
            println "     Copied: $relativeDestination"
//            println "out> $sout\nerr> $serr"
        }

        sourceDir.eachFileRecurse { file ->
                def relativePath = sourceDir.toURI().relativize(file.toURI()).path
                def pathRelativeToThisBuildFile = sourcePath + '/' + relativePath

                // Skip files in excluded directories (or if any of the excluded directories is a parent/grandparent/etc. directory)
                if (excludeDirs.any { excludedDir -> relativePath.startsWith("${excludedDir}/") || relativePath == excludedDir }) {
                    if (debugMode) {
                        println "    Skipped: $relativePath (Excluded directory or subdirectory)"
                    }
                    return
                }

                // Filter files based on includes and excludes
                def filename = file.name

                if ((includes.isEmpty() || includes.any(include -> include.endsWith(file.name))) && !excludes.contains(filename)) {
                    def destinationFile = new File(targetDir, relativePath)
                    destinationFile.parentFile.mkdirs()

                    if (file.isDirectory()) {
                        if (!destinationFile.exists()) {
                            destinationFile.mkdirs()
                            println "  Directory: $relativePath"
                        }
                    } else if (whitelistTemplateFiles.contains(pathRelativeToThisBuildFile)) {
                        // Perform search-and-replace for whitelisted files
                        def processedContent = file.text
                        ttNameMap.each { name, value ->
                            processedContent = processedContent.replaceAll("<!--\\|${name}\\|-->", value)
                        }
                        if (!destinationFile.exists() || destinationFile.text != processedContent) {
                            destinationFile.text = processedContent
                            println "Transformed: $relativePath"
                        } else {
                            if (debugMode) {
                                println "    Skipped: $relativePath (Identical file after transform)"
                            }
                        }
                    } else {
                        copySingleFile(file, destinationFile)
                    }
                } else {
                    if (debugMode) {
                        println "    Skipped: $relativePath (Skipped by includes/excludes)"
                    }
                }
        }
    }
}
