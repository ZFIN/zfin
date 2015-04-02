#!/usr/bin/env groovy

class IdGenerator {

    private map = [:]

    public def getId(base) {
        def suffix = map.get(base, 0) + 1;
        map[base] = suffix;
        "$base:$suffix"
    }

}

gen = new IdGenerator();

new File(args[0]).eachLine { line ->
    if (!line.isAllWhitespace() && !line.startsWith('#')) {
        columns = line.split("\\t")
        match = columns[8] =~ /zdb_id=([A-Z0-9-]+);/
        zdb_id = match[0][1]
        gff_id = gen.getId(zdb_id)

        println "${columns[0]}|" +
                "${columns[1]}|" +
                "${columns[2]}|" +
                "${columns[3]}|" +
                "${columns[4]}|" +
                "${columns[5]}|" +
                "${columns[6]}|" +
                "${columns[7]}|" +
                "$gff_id|" +
                "$zdb_id|||"
    }
}