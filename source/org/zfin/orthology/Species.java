package org.zfin.orthology;

/**
 * Typesafe enumerated class that represents a species used in the orthology domain.
 */
// ToDo: Turn this into a Java5 enumeration!
public enum Species {

    ZEBRAFISH("Zebrafish", 1),
    HUMAN("Human", 2),
    MOUSE("Mouse", 3),
    FLY("Fly", 4),
    YEAST("Yeast", 5);

    private String value;
    private int index;

    Species(String value, int index) {
      this.value = value;
      this.index = index;
    }

    boolean matches(String value) {
      return this.value.equals(value);
    }

    public String toString() {
        return this.value;
    }

    public int getIndex() {
      return this.index;
    }

    static public Species getSpecies(String value) {
      for (Species item : values()) {
        if (item.matches(value))
          return item;
      }
      return null;
    }


}
