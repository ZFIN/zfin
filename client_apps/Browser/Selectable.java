import java.util.*;

public interface Selectable {
	public String get_data();
        public String get_label();
	public String expand(String results,String pre,String post,String sep);
	public Vector get_children();
}
