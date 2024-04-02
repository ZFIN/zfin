package org.zfin.construct.name;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Data
@NoArgsConstructor
public class Cassettes implements Iterable<Cassette>{
    private static final String CASSETTE_SEPARATOR = "Cassette";
    private List<Cassette> cassettes = new ArrayList<>();

    public static Cassettes fromStoredName(String storedName) {
        List<String> cassettesAsStrings = splitStoredNameByCassetteSeparator(storedName);
        Cassettes cassettes = new Cassettes();
        for (String cassetteAsString : cassettesAsStrings) {
            cassettes.add(Cassette.fromStoredName(cassetteAsString));
        }
        return cassettes;
    }

    public static List<String> splitStoredNameByCassetteSeparator(String storedName) {
        return Arrays.stream(storedName.split(CASSETTE_SEPARATOR))
                .map(String::trim)
                .toList();
    }

    public void add(Cassette cassette) {
        cassette.setCassetteNumber(cassettes.size() + 1);
        cassettes.add(cassette);
    }

    public int size() {
        return cassettes.size();
    }

    public Cassette get(int i) {
        return cassettes.get(i);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Cassette cassette : cassettes) {
            sb.append(cassette.toString());
        }
        return sb.toString();
    }

    @Override
    public Iterator<Cassette> iterator() {
        return cassettes.iterator();
    }

    public void reinitialize() {
        for (int i = 0; i < cassettes.size(); i++) {
            cassettes.get(i).setCassetteNumber(i + 1);
        }
    }
}
