import java.util.*;

public class ListItem implements Selectable {
  
  String data;
  String label;
		
  public ListItem(String data) {
    this.data = data;
  }

  public ListItem(String data, String label) {
    this.data = data;
    this.label = label;
  }

  public String get_data() {
    return data;
  }
  
  public String get_label() {
    if (label == null)
      return data;
    else
      return label;
  }

  public String expand(String results,String pre,String post, String separator) {
    return pre + this.data + post ;
  }

  public Vector get_children() {
    return new Vector(); //this is sorta lame - it's just so that the SelectedPanel code
                         //can be easily shared..but it's true, none of them have children..
  }
  
  public String toString() { 
    if (label != null)
      return label;
    else
      return data;
  }

}
