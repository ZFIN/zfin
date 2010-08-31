#!/private/bin/perl 

print "load_all_ontologies.pl started... \n" ;
system("date");

print "**************************************************\n";
print "Start loading the Anatomy Ontology (AO) ...\n";
print "**************************************************\n\n";

system("<!--|ROOT_PATH|-->/server_apps/data_transfer/LoadOntology/loadOntology.pl http://obo.cvs.sourceforge.net/viewvc/obo/obo/ontology/anatomy/gross_anatomy/animal_gross_anatomy/fish/zebrafish_anatomy.obo zebrafish_anatomy.obo");

print "Finished loading Anatomy Ontology \n\n";

system("date");

print "**************************************************\n";
print "Start loading the Spatial Ontology...\n";
print "**************************************************\n\n";

system("<!--|ROOT_PATH|-->/server_apps/data_transfer/LoadOntology/loadOntology.pl http://obo.cvs.sourceforge.net/viewvc/obo/obo/ontology/anatomy/caro/spatial.obo spatial.obo");

print "Finished loading Spatial Ontology \n\n";

system("date");

print "**************************************************\n";
print "Start loading the Quality Ontology (PATO)...\n";
print "**************************************************\n\n";

system("<!--|ROOT_PATH|-->/server_apps/data_transfer/LoadOntology/loadOntology.pl http://obo.cvs.sourceforge.net/viewvc/obo/obo/ontology/phenotype/quality.obo quality.obo");

print "Finished loading Quality Ontology \n\n";

system("date");

print "**************************************************\n";
print "Start loading the Gene Ontology (GO)...\n";
print "**************************************************\n\n";

system("<!--|ROOT_PATH|-->/server_apps/data_transfer/LoadOntology/loadOntology.pl http://www.geneontology.org/ontology/obo_format_1_2/gene_ontology.1_2.obo gene_ontology.obo");

print "Finished loading Quality Ontology \n\n";

print "Finished loading all ontologies\n";
system("date");


