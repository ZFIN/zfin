#! /local/bin/perl

system("parseLLintoUNL.pl");
system("parseLL_HSintoUNL.pl");
system("parseLL_MMintoUNL.pl");
system("parseloc2UG.pl");
system("parseloc2acc.pl");
system("parseloc2acclen.pl");
system("parseLoc2RefintoUNL.pl");

exit;
