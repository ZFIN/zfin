package cgi;

// This appears in Core Web Programming from
// Prentice Hall Publishers, and may be freely used
// or adapted. 1997 Marty Hall, hall@apl.jhu.edu.

public class LookupTable {
  private String[] nameArray, valueArray;
  private String[][] fullValueArray;

  public LookupTable(String[] nameArray,
                     String[] valueArray,
                     String[][] fullValueArray) {
    this.nameArray = nameArray;
    this.valueArray = valueArray;
    this.fullValueArray = fullValueArray;
  }

  public String[] getNames() {
    return(nameArray);
  }

  public String[] getValues() {
    return(valueArray);
  }

  public String[][] getFullValues() {
    return(fullValueArray);
  }

  // Although this is O(N) and HashTable would
  // be O(1), N is typically so small that a HashTable
  // is the same or slower.
  // null -- no such name. Empty string -- name
  // supplied with empty value.
  public String getValue(String name) {
    for(int i=0; i<nameArray.length; i++)
      if (nameArray[i].equals(name))
        return(valueArray[i]);
    return(null);
  }

  public String[] getFullValue(String name) {
    for(int i=0; i<nameArray.length; i++)
      if (nameArray[i].equals(name))
        return(fullValueArray[i]);
    return(null);
  }

  public int numValues(String name) {
    String[] values = getFullValue(name);
    if (values == null)
      return(0);
    else
      return(values.length);
  }
}
