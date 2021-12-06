# Generate beamer pdf slieshow using pandoc

.PHONY: all
.SUFFIXES: .pdf .md

all: pres.pdf

pres.md: ../README.md header.yml
	cat header.yml ../README.md > $@

.md.pdf:
	pandoc -t beamer -o $@ $<
