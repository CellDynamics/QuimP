#!/bin/sh
# This script transfers files between old pi and new pilip server

rsync -lrtzv -e "ssh -i ~/.ssh/pi -p 10222" --delete --stats --progress pi@quimp.linkpc.net:/var/www/quimp-update-site/ /tmp/u
rsync -lrtzv --delete --stats --progress /tmp/u/ admin@pilip.lnx.warwick.ac.uk:/data/www/html/quimp-update-site