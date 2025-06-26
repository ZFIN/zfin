## Process to convert

### Remove absolute paths for binaries:
/bin/rm
/local/bin/wget
/bin/cp
/local/bin/gunzip 

```
perl -i -pe 's!^/bin/rm!rm!' *
perl -i -pe 's!^/local/bin/wget!wget!' *
perl -i -pe 's!^/bin/cp!cp!' *
perl -i -pe 's!^/local/bin/gunzip!gunzip!' *
```

### Remove absolute paths for perl shebangs:
#!/private/bin/perl -> #!/usr/bin/env perl


 