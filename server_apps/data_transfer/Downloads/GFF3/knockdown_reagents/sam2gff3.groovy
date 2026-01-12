#!/bin/bash
//usr/bin/env groovy -cp "$GROOVY_CLASSPATH" "$0" $@; exit $?
import groovy.cli.commons.CliBuilder

def cli = new CliBuilder(usage: 'sam2gff3.groovy')
cli.with {
    h longOpt: 'help', 'Show usage information'
    i longOpt: 'input', args: 1, argName: 'samfile', 'Use specified sam file as input. If not specified, stdin will be used.'
    _ longOpt: 'useZdbId', 'Use ZDB ID in the alias ID field. If not specified, a unique ID will be generated.'
}
def options = cli.parse(args)

soTerms = [
        'MRPHLNO': 'morpholino_oligo',
        'CRISPR' : 'DNA_binding_site',
        'TALEN'  : 'nuclease_binding_site'
]

System.out.println("##gff-version 3")
System.out.println()

// track number of times a constructed ID has been used
ids = [:]

Map<String, String> errorMap = new HashMap<>()
List<String> successList = new ArrayList<>()
(options.input ? new File(options.input) : System.in).withReader { input ->
    input.eachLine { line ->
        if (!line.startsWith('ID=;')) {
            return
        }
        def (name, flag, chromosome, start, quality, cigar, rnext, pnext, tlen, sequence, qual) = line.split();
        int startIndex = line.indexOf('zdb_id=')
        if(startIndex == -1)
            return
        truncatedLine = line.substring(startIndex)
        int endIndex = truncatedLine.indexOf(";")
        truncatedLine = truncatedLine.substring(0, endIndex)
        if (chromosome != '*' && !chromosome.startsWith('CHR_ALT') && start.isNumber() && cigar.substring(0, 2).isNumber()) {
            strand = (flag == '16') ? '-' : '+'
            zdbMatch = name =~ /ZDB-([A-Z]+)-\d+-\d+/
            if (!zdbMatch) {
                return
            }
            zdbId = zdbMatch[0][0]
            zdbType = zdbMatch[0][1]
            type = soTerms.get(zdbType, 'sequence_feature')
            if (options.useZdbId) {
                id = zdbId
            } else {
                base = "STR" + chromosome + ":" + start
                ids[base] = ids.get(base, 0) + 1
                id = "$base-${ids[base]}"
            }
            end = start.toInteger() + cigar.substring(0, 2).toInteger()
            newName = "ID=" + id + name.substring(3)
            newName = newName.replace(",", "%2C")
            System.out.println("$chromosome\tZFIN_knockdown_reagent\t$type\t$start\t$end\t.\t$strand\t.\t$newName")
            errorMap.remove(truncatedLine)
            successList.add(truncatedLine)
        } else {
            if (!successList.contains(truncatedLine))
                errorMap.put(truncatedLine, ">$name" + System.lineSeparator() + sequence)
/*
            System.err.println(">$name")
            System.err.println(sequence)
*/
        }
    }
}

// print out all records that do not have a success record
errorMap.values().forEach(element -> System.err.println(element))
