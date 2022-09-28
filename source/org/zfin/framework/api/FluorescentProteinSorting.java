package org.zfin.framework.api;

import org.apache.commons.collections4.CollectionUtils;
import org.zfin.marker.fluorescence.FluorescentProtein;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class FluorescentProteinSorting implements Sorting<FluorescentProtein> {

    private List<Comparator<FluorescentProtein>> defaultList;
    private List<Comparator<FluorescentProtein>> efgListUp;
    private List<Comparator<FluorescentProtein>> efgListDown;
    private List<Comparator<FluorescentProtein>> emissionListUp;
    private List<Comparator<FluorescentProtein>> emissionListDown;
    private List<Comparator<FluorescentProtein>> excitationListUp;
    private List<Comparator<FluorescentProtein>> excitationListDown;

    public FluorescentProteinSorting() {
        super();

        defaultList = new ArrayList<>(3);
        defaultList.add(efgExistsOrder);
        defaultList.add(proteinOrder);

        efgListUp = new ArrayList<>(3);
        efgListUp.add(efgExistsOrder);
        efgListUp.add(efgOrder);

        efgListDown = new ArrayList<>(3);
        efgListDown.add(efgExistsOrder);
        efgListDown.add(efgOrder.reversed());

        emissionListUp = new ArrayList<>(3);
        emissionListUp.add(efgExistsOrder);
        emissionListUp.add(emissionExistsOrder);
        emissionListUp.add(emissionOrder);

        emissionListDown = new ArrayList<>(3);
        emissionListDown.add(efgExistsOrder);
        emissionListDown.add(emissionExistsOrder);
        emissionListDown.add(emissionOrder.reversed());

        excitationListUp = new ArrayList<>(3);
        excitationListUp.add(efgExistsOrder);
        excitationListUp.add(excitationExistsOrder);
        excitationListUp.add(excitationOrder);

        excitationListDown = new ArrayList<>(3);
        excitationListDown.add(efgExistsOrder);
        excitationListDown.add(excitationExistsOrder);
        excitationListDown.add(excitationOrder.reversed());

    }

    private static Comparator<FluorescentProtein> proteinOrder =
            Comparator.comparing(protein -> protein.getName().toLowerCase());

    private static Comparator<FluorescentProtein> efgExistsOrder =
            Comparator.comparing(protein -> CollectionUtils.isEmpty(protein.getEfgs()));

    private static Comparator<FluorescentProtein> emissionExistsOrder =
            Comparator.comparing(protein -> protein.getEmissionLength() == null);

    private static Comparator<FluorescentProtein> excitationExistsOrder =
            Comparator.comparing(protein -> protein.getExcitationLength() == null);

    private static Comparator<FluorescentProtein> emissionOrder =
            Comparator.comparing(FluorescentProtein::getEmissionLength, Comparator.nullsLast(Comparator.naturalOrder()));

    private static Comparator<FluorescentProtein> excitationOrder =
            Comparator.comparing(FluorescentProtein::getExcitationLength, Comparator.nullsLast(Comparator.naturalOrder()));

    private static Comparator<FluorescentProtein> efgOrder =
            Comparator.comparing(protein -> {
                if (CollectionUtils.isEmpty(protein.getEfgs()))
                    return null;
                return protein.getEfgs().stream()
                        .map(marker -> marker.getAbbreviation().toLowerCase())
                        .sorted(Comparator.naturalOrder())
                        .collect(joining());
            }, Comparator.nullsLast(Comparator.naturalOrder()));

    public Comparator<FluorescentProtein> getComparator(String value) {
        Field field = Field.getField(value);
        if (field == null)
            return getJoinedComparator(defaultList);
        //throw new RuntimeException("Cannot find a sorting algorithm for name: " + value);

        return switch (field) {
            case PROTEIN_UP -> getJoinedComparator(defaultList);
            case PROTEIN_DOWN -> getJoinedComparator(defaultList).reversed();
            case EFG_UP -> getJoinedComparator(efgListUp);
            case EFG_DOWN -> getJoinedComparator(efgListDown);
            case EMISSION_UP -> getJoinedComparator(emissionListUp);
            case EMISSION_DOWN -> getJoinedComparator(emissionListDown);
            case EXCITATION_UP -> getJoinedComparator(excitationListUp);
            case EXCITATION_DOWN -> getJoinedComparator(excitationListDown);
            default -> getJoinedComparator(defaultList);
        };
    }

    enum Field {
        PROTEIN_UP("proteinUp"), PROTEIN_DOWN("proteinDown"),
        EFG_UP("efgUp"), EFG_DOWN("efgDown"),
        EMISSION_UP("emissionUp"), EMISSION_DOWN("emissionDown"),
        EXCITATION_UP("excitationUp"), EXCITATION_DOWN("excitationDown"),
        SUPPLIER_UP("supplierUp"),
        ALLELE_UP("createdAlleleUp");

        private String val;

        Field(String value) {
            this.val = value;
        }

        public static Field getField(String value) {
            return Arrays.stream(values()).filter(field -> field.getVal().equals(value)).findFirst().orElse(null);
        }

        public String getVal() {
            return val;
        }
    }
}
