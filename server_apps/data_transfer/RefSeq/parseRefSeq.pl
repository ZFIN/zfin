#! /local/bin/perl

system("parseLLintoUNL.pl");
system("parseLL_HSintoUNL.pl");
system("parseLL_MMintoUNL.pl");
system("parseloc2UG.pl");
system("parseLoc2RefintoUNL.pl");

exit;
