## Process to convert

### Remove absolute paths for binaries:
/bin/rm
/local/bin/wget
/bin/cp
/local/bin/gunzip 

```
perl -i -pe 's!/bin/rm!rm!' *
perl -i -pe 's!/local/bin/wget!wget!' *
perl -i -pe 's!/local/bin/rsync!rsync!' *
perl -i -pe 's!/bin/cp!cp!' *
perl -i -pe 's!/bin/mv!mv!' *
perl -i -pe 's!/bin/chgrp!chgrp!' *
perl -i -pe 's!/bin/chmod!chmod!' *
perl -i -pe 's!/local/bin/gunzip!gunzip!' *

```

### Remove absolute paths for perl shebangs:
#!/private/bin/perl -> #!/usr/bin/env perl

### Replace templates for commands:
@BLASTSERVER_XDFORMAT@ becomes xdformat

 