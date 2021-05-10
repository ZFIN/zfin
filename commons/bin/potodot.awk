#! /local/bin/gawk -f 

# does not work in shebang line, move to outside call
# -v pwd=`pwd` 
# -v pwd=${PWD##*/} 

# usage: potodot.awk -v pwd=`pwd` <file.po>

# convert a partial order (edge list) into a graphviz dot digraph
BEGIN {
	# I'm choosing to pass the current directory as variable pwd, 
	# but whatever works, the name is not really used here
	# but would help keep track of where something came from if it was moved
	z=split(pwd, gname,"/");
	print "digraph " gname[z] " {\n/*";
	system("date");
	print pwd  "\n*/" 
	r=0;l=0
}
!/.*PHONY|clean/      { # avoid house keeping dependencies
        node1 = $1;
        node2 = substr($2, 1, match($2, /:/)-1);
        gsub(/[^a-zA-Z_0-9]+/, "_", node1);
        gsub(/[^a-zA-Z_0-9]+/, "_", node2);
        # strip leading/trailing underscore
        if (1 == index(node1,"_")) node1 = substr(node1,2);
        if (1 == index(node2,"_")) node2 = substr(node2,2);  
        if (match(node1,/_$/)) node1 = substr(node1,1,length(node1)-1);
        if (match(node2,/_$/)) node2 = substr(node2,1,length(node1)-1); 
        print "\t" node1 " -> " node2 ";";
		leaf[l++] = node1
		root[r++] = node2;
}
END   {

  i=0;
  for(r in root) {
	for(l in leaf) {
	  if (root[r] == leaf[l]) {
		node[i++] = root[r];
		delete root[r];
		delete leaf[l]
	  }
	}
  }
# there may be duplicate interior nodes so double check
  for (i in node) {
	for(r in root) if (root[r] == node[i]) delete root[r];
	for(l in leaf) if (leaf[l] == node[i]) delete leaf[l]
  }

# there may be duplicate root/leaf nodes so double check
  #asort(root); asort(leaf);
  for(r in root) {
	  if (root[r] == NULL ) delete root[r];
	  if (root[r] == root[r+1] ) delete root[r];
  }
  for(l in leaf) {
	if (leaf[l] == NULL) delete leaf[l];
	if (leaf[l] == leaf[l+1]) delete leaf[l]
  }

  # give the roots and leafs nodes different attributes
  for(r in root) if (root[r] != NULL ){print "\t" root[r] " [penwidth=4];" };
  for(l in leaf) if (leaf[l] != NULL ){print "\t" leaf[l] " [shape=box];" };

  print "}"
}

#dotid(str){
# Any string of alphnumeric not beginning with a digit;
#       str
#       a numeral [-]?(.[0-9]+ | [0-9]+(.[0-9]*)? );
#       any double-quoted string ("...") possibly containing escaped quotes ('");
#       an HTML tag (<...>).
