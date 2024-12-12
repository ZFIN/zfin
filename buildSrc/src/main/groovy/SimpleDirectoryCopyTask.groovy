import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
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

    // Access global properties from the project.ext
    @Internal
    def targetroot = project.ext.targetroot

    @Internal
    def ttNameMap = project.ext.ttNameMap

    @Internal
    def whitelistTemplateFiles = project.ext.whitelistTemplateFiles

    @TaskAction
    void copyFiles() {
        def debugMode = false
        println "Starting copy task for sourcePath: $sourcePath"

        def targetDir = new File("$targetroot/$sourcePath")
        def sourceDir = new File(sourcePath)

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
            if (file.isFile()) {
                def relativePath = sourceDir.toURI().relativize(file.toURI()).path
                def pathRelativeToThisBuildFile = sourcePath + '/' + relativePath
                def containsSubDir = relativePath.contains('/')
                def relativePathWithoutFileName = containsSubDir ? relativePath.substring(0, relativePath.lastIndexOf('/')) : relativePath

                // Skip files in excluded directories
                if (excludeDirs.contains(relativePathWithoutFileName)) {
                    if (debugMode) {
                        println "    Skipped: $relativePath (Excluded directory)"
                    }
                    return
                }

                // Filter files based on includes and excludes
                def extension = file.name.tokenize('.').last()
                def filename = file.name
                if ((includes.isEmpty() || includes.contains(extension)) && !excludes.contains(filename)) {
                    def destinationFile = new File(targetDir, relativePath)
                    destinationFile.parentFile.mkdirs()

                    if (whitelistTemplateFiles.contains(pathRelativeToThisBuildFile)) {
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
}
