#!/local/bin/perl
@args = @ARGV; @ARGV = ();
system("/private/apps/alchemy/solaris/alchemy", @args);
system("chmod 644 $args[1]");
