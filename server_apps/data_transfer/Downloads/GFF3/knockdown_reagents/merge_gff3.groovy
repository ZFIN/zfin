#!/usr/bin/env groovy

new File(args.head()).withWriter { out ->

    out.writeLine('##gff-version 3')
    out.writeLine('')

    args.tail().each { gff3 ->
        new File(gff3).eachLine { line ->
            if (!line.isAllWhitespace() && !line.startsWith('#')) {
                out.writeLine(line)
            }
        }
    }

}
