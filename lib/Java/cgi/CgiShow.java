package cgi;

// This appears in Core Web Programming from
// Prentice Hall Publishers, and may be freely used
// or adapted. 1997 Marty Hall, hall@apl.jhu.edu.

public class CgiShow {
  public static void main(String[] args) {
    CgiShow app = new CgiShow("CgiShow", args, "TEST");
    app.printFile();
  }

  protected String name;
  protected String[] args;
  protected String type;

  public CgiShow(String name, String[] args,
                 String type) {
    this.name = name;
    this.args = args;
    this.type = type;
  }
  
  public void printFile() {
    printHeader();
    printBody(args);
    printTrailer();
  }

  protected void printHeader() {
    System.out.println
      ("Content-Type: text/html\n" +
       "\n" +
       "<!DOCTYPE HTML PUBLIC " +
         "\"-//W3C//DTD HTML 3.2//EN\">\n" +
       "<HTML>\n" +
       "<HEAD>\n" +
       "<TITLE>The " + name + " Program</TITLE>\n" +
       "<STYLE>\n" +
       "<!--");
    printStyleRules();
    System.out.println
      ("-->\n" +
       "</STYLE>\n" +
       "</HEAD>\n" +
       "\n" +
       "<BODY>\n" +
       "<H1>The <CODE>" + name + "</CODE> Program" +
       "</H1>");
  }

  protected void printStyleRules() {
    System.out.println
      ("H1 { text-align: center;\n" +
       "     font-family: Arial, sans-serif }");
  }
  
  protected void printBody(String[] data) {
    System.out.println("(Generic CgiShow)");
  }

  protected void printTrailer() {
    System.out.println("</BODY>\n</HTML>");
  }
}
