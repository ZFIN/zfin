#!/usr/bin/env groovy

chromosomes = [:]
new File(args[0]).eachLine { line ->
    tokens = line.split(/\|/)
    if (!chromosomes.containsKey(tokens[0])) {
        chromosomes[tokens[0]] = []
    }
    chromosomes[tokens[0]] << tokens[1]
}

new File(args[1]).eachLine { line ->
    zdbMatch = line =~ /ZDB-[A-Z]+-\d+-\d+/
    if (!zdbMatch) {
        return
    }
    def (name, flag, chromosome, start, quality, cigar, rnext, pnext, tlen, sequence, qual) = line.split();
    if (chromosome == '*') {
        return
    }
    zdbMatch = name =~ /ZDB-[A-Z]+-\d+-\d+/
    if (!zdbMatch) {
        return
    }
    zdbId = zdbMatch[0]
    if (chromosomes.containsKey(zdbId) && chromosome in chromosomes[zdbId]) {
        System.out.println(line)
    }
}
