package cgi;

// This appears in Core Web Programming from
// Prentice Hall Publishers, and may be freely used
// or adapted. 1997 Marty Hall, hall@apl.jhu.edu.

public class QueryStringParser extends CgiParser {
  public QueryStringParser(String queryString) {
    super(queryString, "&", "=");
  }
}
