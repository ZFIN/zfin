import java.awt.*;
import java.awt.image.*;
import java.applet.*;
import java.util.*;
import java.lang.*;

// CLASS   : TreeNode
// PURPOSE : Represent a single node in the anatomic tree
public class TreeNode implements Selectable {
  public  Vector  children;  
  private  Integer seq_num;
  private  TreeNode parent;
  private  String  data;
  private  int height;
  public  boolean isClosed;

  public TreeNode(String str, TreeNode Parent, Integer snum) {
    data = str;
    parent = Parent;
    height = -1;
    children = new Vector();
    isClosed = true;
    seq_num = snum;
    data.trim();
  }

  public TreeNode(TreeNode tn) {
    data = tn.get_data();
    parent = tn.get_parent();
    height = -1;
    children = new Vector();
    isClosed = true;
    seq_num = tn.get_seq_num();
    data.trim();
    
  }
   

  public TreeNode addChild(String str, Integer snum) {

    TreeNode tn = new TreeNode(str, this, snum);
    children.addElement(tn);
    children.trimToSize();  // Save memory!
    return tn;
  }

  public String get_label() {
    return get_data();  //compatability for Selectable interface
  }

  public int getLevel() {
    TreeNode tmp = this;
    int i=-1;
    while (tmp != null) {
      tmp=tmp.get_parent();
      i++;
    }
    return i;
  }

  public String expand(String result, String format_pre, String format_post, String separator) {
    System.out.println("expand" + get_data());
    if (children.size() > 0)
      return OrExpand(result, format_pre, format_post, separator);
    else
      return "";
  }

  public String OrExpand(String result, String format_pre, String format_post, String separator) {
    //this is new for the mini version of the browser,
    //the big idea is that when you select an organ with children,
    //everything that's inside counts.. 
    
    System.out.println("OrExpand" + get_data());

    int i = 0;
    for(i = 0 ; i < children.size() ; i++) {
      result = ((TreeNode)children.elementAt(i)).OrExpand(result, format_pre,format_post, separator);
    }
    
    if (result.length() > 0)
      result = result + " " + separator + " " + format_pre + this.data + format_post;
    else
      result = result + format_pre + this.data + format_post ;
  
    return result;

  }

  public Vector get_children() {
    return children;
  }


  public  TreeNode get_parent() {
    return parent;
  }

  public void set_parent(TreeNode p) {
    parent = p;
  } 

  public  String  get_data() {
    return data;
  }

  public void set_data(String d) {
    data = d;
  }

  public  int get_height() {
    return height;
  }

  public void set_height(int h) {
    height = h;
  }

  public Integer get_seq_num() {
    return seq_num;
  }

	public String toString() {
		return get_data();
	}

}
