package org.zfin.mapping;

import org.zfin.sequence.gff.Assembly;
import org.zfin.sequence.gff.AssemblyDAO;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * How the assembly table ranks the assemblies: the lowest {@code a_order} is the newest. Read from the table
 * for each use, so a new assembly counts as soon as it has a row. An assembly the table does not list ranks
 * after every listed one.
 */
public final class AssemblyOrder {

    private final Map<String, Integer> rankByName;

    public AssemblyOrder(Map<String, Integer> rankByName) {
        this.rankByName = Map.copyOf(rankByName);
    }

    public static AssemblyOrder fromDatabase() {
        return new AssemblyOrder(new AssemblyDAO().findAllSortedAssemblies().stream()
                .collect(Collectors.toMap(Assembly::getName, Assembly::getOrder)));
    }

    public int rank(String assemblyName) {
        return rankByName.getOrDefault(assemblyName, Integer.MAX_VALUE);
    }

    /** True if the first assembly is newer than the second. */
    public boolean isNewer(String assemblyName, String otherAssemblyName) {
        return rank(assemblyName) < rank(otherAssemblyName);
    }
}
