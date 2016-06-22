#!/bin/sh
for j in {0..20};
do
    echo "round $j"
    for i in csv doc html odg odp ods odt ppt rtf sxc sxi sxw txt xls;
    do
        echo "R$j - processing file type $i"
        curl -F "filename=@../documents/test.$i" -F "mimeType=application/pdf" http://root:root1234@localhost:8071/jahia/cms/convert > /tmp/file.$i.pdf
    done
done