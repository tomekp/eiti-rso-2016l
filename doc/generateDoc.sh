#!/bin/bash

FILES=*.tex

for f in $FILES
do
  echo "Processing $f..."
  pdflatex $f > /dev/null 2>&1
  pdflatex $f > /dev/null 2>&1  # run twice to handle references, TOCs etc.
done

echo "Files:"
for f in $FILES
do
  echo "  ${f%.*}.pdf"
done
echo "have been created."
