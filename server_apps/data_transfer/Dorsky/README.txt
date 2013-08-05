Load the Dorsky / Chien features

The "dorsky_lines.csv" file is expected to be in /research/zunloads/projects/Dorsky/

Run with:

groovy LoadFeatures.groovy

command line options:
	--explain	prints informix explain output to /tmp/dorsky-features.out
	--mail 		sends email about the load to Yvonne & Kevin
	--commit	commits the transaction instead of rolling back.  

The recipe for running this on pruction will be:

groovy LoadFeatures.groovy --mail --commit

