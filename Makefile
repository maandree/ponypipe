# Copyright © 2013, 2017  Mattias Andrée (m@maandree.se)
# 
# Copying and distribution of this file, with or without modification,
# are permitted in any medium without royalty provided the copyright
# notice and this notice are preserved.  This file is offered as-is,
# without any warranty.
# 
# [GNU All Permissive License]

COMMAND=ponypipe
PKGNAME=ponypipe
PREFIX=/usr
BIN=/bin
DATA=/share
LICENSES=$(DATA)/licenses

FLAGS=-Xlint:all -O
JAR=jar
JAVAC=javac

SOURCE =\
	bin/se/kth/maandree/ponypipe/DecodeStream.java\
	bin/se/kth/maandree/ponypipe/EncodeStream.java\
	bin/se/kth/maandree/ponypipe/Ponypipe.java\
	bin/se/kth/maandree/ponypipe/TranslateStream.java

CLASS =\
	bin/se/kth/maandree/ponypipe/DecodeStream.class\
	bin/se/kth/maandree/ponypipe/EncodeStream.class\
	bin/se/kth/maandree/ponypipe/Ponypipe.class\
	bin/se/kth/maandree/ponypipe/TranslateStream.class

CLASS_JAR =\
	se/kth/maandree/ponypipe/DecodeStream.class\
	se/kth/maandree/ponypipe/EncodeStream.class\
	se/kth/maandree/ponypipe/Ponypipe.class\
	se/kth/maandree/ponypipe/Ponypipe\$$DeponifyStream.class\
	se/kth/maandree/ponypipe/Ponypipe\$$PonifyStream.class\
	se/kth/maandree/ponypipe/Ponypipe\$$RulesStream.class\
	se/kth/maandree/ponypipe/TranslateStream.class\
	se/kth/maandree/ponypipe/TranslateStream\$$Match.class


all: code info
code: class jar

bin/%.java: src/%.java
	@mkdir -p -- "$$(dirname -- "$@")"
	cp "$<" "$@"
	sed -i 's:"./rules":"$(PREFIX)$(DATA)/$(PKGNAME)/rules":g' "$@"

class: $(SOURCE) $(CLASS)
bin/%.class: bin/%.java
	$(JAVAC) -encoding UTF-8 -cp bin -s bin -d bin $(FLAGS) "$<"

jar: ponypipe.jar
ponypipe.jar: META-INF/MANIFEST.MF $(CLASS)
	mkdir -p bin/META-INF
	cp META-INF/MANIFEST.MF bin/META-INF/MANIFEST.MF
	cd bin && $(JAR) -cfm "../$@" "META-INF/MANIFEST.MF" $(CLASS_JAR)
	sed -i '1s|^.*|#!/usr/bin/java -jar\n&|' "$@"
	chmod 755 "$@"


info: ponypipe.info
%.info: info/%.texinfo
	$(MAKEINFO) "$<"

pdf: ponypipe.pdf
%.pdf: info/%.texinfo
	texi2pdf "$<"

dvi: ponypipe.dvi
%.dvi: info/%.texinfo
	$(TEXI2DVI) "$<"


install: install-cmd install-license install-info

install-cmd:
	mkdir -p "$(DESTDIR)$(PREFIX)$(BIN)"
	cp ponypipe.jar "$(DESTDIR)$(PREFIX)$(BIN)/$(COMMAND)"
	chmod 755 "$(DESTDIR)$(PREFIX)$(BIN)/$(COMMAND)"
	mkdir -p "$(DESTDIR)$(PREFIX)$(DATA)/$(PKGNAME)"
	cp rules "$(DESTDIR)$(PREFIX)$(DATA)/$(PKGNAME)/rules"
	chmod 644 "$(DESTDIR)$(PREFIX)$(DATA)/$(PKGNAME)/rules"

install-info:
	mkdir -p "$(DESTDIR)$(PREFIX)$(DATA)/info"
	cp ponypipe.info "$(DESTDIR)$(PREFIX)$(DATA)/info/$(PKGNAME).info"
	chmod 644 "$(DESTDIR)$(PREFIX)$(DATA)/info/$(PKGNAME).info"

install-license:
	mkdir -p "$(DESTDIR)$(PREFIX)$(LICENSES)/$(PKGNAME)"
	cp COPYING "$(DESTDIR)$(PREFIX)$(LICENSES)/$(PKGNAME)/COPYING"
	chmod 644 "$(DESTDIR)$(PREFIX)$(LICENSES)/$(PKGNAME)/COPYING"


uninstall:
	rm -- "$(DESTDIR)$(PREFIX)$(BIN)/$(COMMAND)"
	rm -r -- "$(DESTDIR)$(PREFIX)$(DATA)/$(PKGNAME)"
	rm -r -- "$(DESTDIR)$(PREFIX)$(LICENSES)/$(PKGNAME)"
	rm -- "$(DESTDIR)$(PREFIX)$(DATA)/info/$(PKGNAME).info"

clean:
	-rm -r -- *.t2d *.aux *.cp *.cps *.pg *.pgs *.op *.ops *.vr *.vrs *.fn *.ky
	-rm -r -- *.log *.toc *.tp *.bak *.info *.pdf *.ps *.dvi *.install bin ponypipe.jar

.PHONY: clean uninstall install-license install-info install-cmd install jar class code all info pdf dvi
