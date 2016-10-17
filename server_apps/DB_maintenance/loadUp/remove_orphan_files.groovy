#!/bin/bash
//usr/bin/env groovy -cp "$GROOVY_CLASSPATH:." "$0" $@; exit $?

import groovy.io.FileType
import groovy.io.FileVisitResult
import groovy.sql.Sql
import org.zfin.properties.ZfinProperties
import org.zfin.properties.ZfinPropertiesEnum

import java.nio.file.Files
import java.nio.file.StandardCopyOption

ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")

def db = [
        url: "jdbc:informix-sqli://${ZfinPropertiesEnum.SQLHOSTS_HOST}:${ZfinPropertiesEnum.INFORMIX_PORT}/${ZfinPropertiesEnum.DBNAME}:INFORMIXSERVER=${ZfinPropertiesEnum.INFORMIXSERVER}",
        driver: 'com.informix.jdbc.IfxDriver'
]

def processDirectory(File src, String logFileName, Closure moveCriteria) {
    println("==> Removing orphan files from $src.absolutePath")
    def opts = [
            type : FileType.FILES,
            preRoot : true,
            preDir : {
                if (it.name == 'bkup') {
                    return FileVisitResult.SKIP_SUBTREE
                }
                File backupDir = new File(it, 'bkup');
                if (!backupDir.exists()) {
                    backupDir.mkdir()
                }
                return FileVisitResult.CONTINUE;
            }
    ]
    new File('.', logFileName).withWriter { log ->
        src.traverse(opts) { file ->
            if (moveCriteria(file.absolutePath)) {
                log.writeLine(file.absolutePath)
                dest = new File(file.getParentFile(), 'bkup')
                Files.move(file.toPath(), new File(dest, file.name).toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }

}

new File('.').eachFileMatch(FileType.FILES, ~/.*\.txt/, { File txt -> txt.delete() })

File imageLoadUpDir = new File(ZfinPropertiesEnum.LOADUP_FULL_PATH.value, ZfinPropertiesEnum.IMAGE_LOAD.value)
String imageFullPath = imageLoadUpDir.absolutePath + '/'
String mediumFullPath = imageFullPath + 'medium/'

File pdfLoadUpDir = new File(ZfinPropertiesEnum.LOADUP_FULL_PATH.value, ZfinPropertiesEnum.PDF_LOAD.value)
String pdfFullPath = pdfLoadUpDir.absolutePath + '/'

File videoLoadUpDir = new File(ZfinPropertiesEnum.LOADUP_FULL_PATH.value, ZfinPropertiesEnum.VIDEO_LOAD.value)
String videoFullPath = videoLoadUpDir.absolutePath + '/'

imageFiles = new HashSet()
pubFiles = new HashSet()
videoFiles = new HashSet()
Sql.withInstance(db) { Sql sql ->
    sql.eachRow("SELECT img_image, img_image_with_annotation, img_thumbnail FROM image") { row ->
        imageFiles.add(imageFullPath + row.img_image)
        imageFiles.add(mediumFullPath + row.img_image)
        if (row.img_image_with_annotation) {
            imageFiles.add(imageFullPath + row.img_image_with_annotation)
            imageFiles.add(mediumFullPath + row.img_image_with_annotation)
        }
        if (row.img_thumbnail) {
            imageFiles.add(imageFullPath + row.img_thumbnail)
        }
    }

    sql.eachRow("SELECT pf_file_name FROM publication_file") { row ->
        pubFiles.add(pdfFullPath + row.pf_file_name)
    }

    sql.eachRow("SELECT video_path_to_file FROM video") { row ->
        videoFiles.add(videoFullPath + row.video_path_to_file)
    }
}

processDirectory(imageLoadUpDir, 'movedImageFiles.txt') { !imageFiles.contains(it) }
processDirectory(pdfLoadUpDir, 'movedPdfFiles.txt') { !pubFiles.contains(it) }
processDirectory(videoLoadUpDir, 'movedVideoFiles.txt') { !videoFiles.contains(it) }

cmd = "${ZfinPropertiesEnum.ROOT_PATH}/server_apps/DB_maintenance/loadUp/rsync.pl"
println(cmd)
cmd.execute().in.eachLine { println(it) }
