#!/bin/bash
# Snapshots written via /replication are created by this process (uid 8983).
# umask 002 makes them group-writable (dirs 775, files 664) so the Jenkins
# cleanup job, which shares the fishadmin group, can delete them.
umask 002
exec docker-entrypoint.sh "$@"
