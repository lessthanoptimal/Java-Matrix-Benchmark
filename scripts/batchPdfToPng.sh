#/bin/sh
for i in *.pdf; do convert $i `echo $i|cut -f1 -d'.'`.png; done
