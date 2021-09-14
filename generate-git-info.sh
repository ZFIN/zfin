rm -f git-info.txt
touch git-info.txt
git rev-parse HEAD >> git-info.txt
git rev-parse --abbrev-ref HEAD >> git-info.txt