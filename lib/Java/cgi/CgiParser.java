package cgi;

import java.util.*;

// This appears in Core Web Programming from
// Prentice Hall Publishers, and may be freely used
// or adapted. 1997 Marty Hall, hall@apl.jhu.edu.

public class CgiParser {
  private String data, delims1, delims2;
  private String[] nameArray, valueArray;
  private String[][] fullValueArray;
  
  public CgiParser(String data,
                   String delims1,
                   String delims2) {
    this.data = data;
    this.delims1 = delims1;
    this.delims2 = delims2;
  }

  public LookupTable parse() {
    StringVector nameVector = new StringVector(); 
    Vector valueVector = new Vector();
    if (data == null)
      return(buildTable(nameVector, valueVector, 0));
    StringTokenizer tok =
      new StringTokenizer(data, delims1);
    String nameValuePair, name, value;
    StringTokenizer tempTok;
    int index, numNames=0;
    StringVector values;
    while(tok.hasMoreTokens()) {
      nameValuePair = tok.nextToken();
      tempTok = new StringTokenizer(nameValuePair,
                                    delims2);
      name = URLDecoder.decode(tempTok.nextToken());
      if (tempTok.hasMoreTokens())
        value = URLDecoder.decode(tempTok.nextToken());
      else
        value = "";
      index = nameVector.indexOf(name);
      if (index == -1) {
        nameVector.addElement(name);
        values = new StringVector();
        values.addElement(value);
        valueVector.addElement(values);
        numNames++;
      } else {
        values =
          (StringVector)valueVector.elementAt(index);
        values.addElement(value);
      }
    }
    return(buildTable(nameVector,
                      valueVector,
                      numNames));
  }

  private LookupTable buildTable(StringVector nameVector,
                                 Vector valueVector,
                                 int numNames) {
    nameArray = new String[numNames];
    valueArray = new String[numNames];
    fullValueArray = new String[numNames][];
    LookupTable table = new LookupTable(nameArray,
                                        valueArray,
                                        fullValueArray);
    String[] fullValues;
    StringVector values;
    for(int i=0; i<nameVector.size(); i++) {
      nameArray[i] = nameVector.elementAt(i);
      values = (StringVector)valueVector.elementAt(i);
      valueArray[i] = values.firstElement();
      fullValues = new String[values.size()];
      values.copyInto(fullValues);
      fullValueArray[i] = fullValues;
    }
    return(table);
  }
}
