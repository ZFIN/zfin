package org.zfin.antibody;

/**
 * Main domain object for antibodies.
 */
public class Isotype {

    private HeavyChain heavyChain;
    private LightChain lightChain;
    

    public HeavyChain getHeavyChain() {
        return heavyChain;
    }

    public void setHeavyChain(HeavyChain heavyChain) {
        this.heavyChain = heavyChain;
    }

    public LightChain getLightChain() {
        return lightChain;
    }

    public void setLightChain(LightChain lightChain) {
        this.lightChain = lightChain;
    }

    /**
     * This method formats the isotype in standard format:
     * <heavy chain>, <light chain>
     *
     * @return String
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (heavyChain != null)
            sb.append(heavyChain.toString());
        if (lightChain != null) {
            if (heavyChain != null) {
                sb.append(", ");
                sb.append(heavyChain.toString());
            }
        }
        return sb.toString();
    }

    public enum HeavyChain {
        IGA("IgA"),
        IGA1("IgA1"),
        IGA2("IgA2"),
        IGD("IgD"),
        IGE("IgE"),
        IGG("IgG"),
        IGG1("IgG1"),
        IGG2("IgG2"),
        IGG2a("IgG2a"),
        IGG2b("IgG2b"),
        IGG3("IgG3"),
        IGG4("IgG4"),
        IGM("IgM"),
        IGN("IgN"),
        IGR("IgR"),
        IGW("IgW"),
        IGX("IgX"),
        IGY("IgY");

        private String value;

        private HeavyChain(String value) {
            this.value = value;
        }

        public String toString() {
            return this.value;
        }
    }

    public enum LightChain {
        K("k"),
        L("l"),
        L1("l1"),
        L2("l2"),
        L3("l3"),
        L4("l4"),
        R("r"),
        S("s"),
        I("i"),
        I1("i1"),
        I2("i2"),
        I3("i3"),
        I4("i4");

        private String value;

        private LightChain(String value) {
            this.value = value;
        }

        public String toString() {
            return this.value;
        }

    }
}
