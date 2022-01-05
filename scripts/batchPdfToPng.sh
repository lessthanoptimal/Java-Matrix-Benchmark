#/bin/sh
#for i in *.pdf; do convert $i `echo $i|cut -f1 -d'.'`.png; done
for i in *.pdf; do pdftoppm $i `echo $i|cut -f1 -d'.'` -png -f 1 -singlefile; done