import java.io.*;

// This appears in Core Web Programming from
// Prentice Hall Publishers, and may be freely used
// or adapted. 1997 Marty Hall, hall@apl.jhu.edu.

public class ShowParse extends CgiShow {
  public static void main(String[] args) {
    String method = System.getProperty("REQUEST_METHOD");
    String[] data = new String[1];
    if ("GET".equalsIgnoreCase(method))
      data[0] = System.getProperty("QUERY_STRING");
    else {
      try {
        DataInputStream in =
          new DataInputStream(System.in);
        data[0] = in.readLine();
      } catch(IOException ioe) {
        System.out.println("IOException: " + ioe);
        System.exit(-1);
      }
    }
    ShowParse app = new ShowParse("ShowParse", data,
                                  method);
    app.printFile();
  }

  public ShowParse(String name, String[] queryData,
                   String requestMethod) {
    super(name, queryData, requestMethod);
  }
  
  protected void printBody(String[] queryData) {
    QueryStringParser parser =
      new QueryStringParser(queryData[0]);
    LookupTable table = parser.parse();
    String[] names = table.getNames();
    String[] values = table.getValues();
    System.out.println("Request method:  <CODE>" +
                       type + "</CODE>.<BR>");
    if (names.length > 0)
      System.out.println("Data supplied:\n" +
                         "<CENTER>\n" +
                         "<TABLE BORDER=1>\n" +
                         "  <TR><TH>Name<TH>Value(s)");
    else
      System.out.println("<H2>No data supplied.</H2>");
    String name, value;
    String[] fullValue;
    for(int i=0; i<names.length; i++) {
      name = names[i];
      System.out.println("  <TR><TD>" + name);
      if (table.numValues(name) > 1) {
        fullValue = table.getFullValue(name);
        System.out.println
          ("      <TD>Multiple values supplied:\n" +
           "          <UL>");
        for(int j=0; j<fullValue.length; j++)
          System.out.println("            <LI>" +
                             fullValue[j]);
        System.out.println("          </UL>");
      } else {
        value = values[i];
        if (value.equals(""))
          System.out.println
            ("      <TD><I>No Value Supplied</I>");
        else
          System.out.println
            ("      <TD>" + value);
      }
    }
    System.out.println("</TABLE>\n</CENTER>");
  }

  protected void printStyleRules() {
    super.printStyleRules();
    System.out.println
      ("TH { background: black;\n" +
       "     color: white }\n" +
       "UL { margin-top: -10pt }");
  }
}
