# Copyright © 2013  Mattias Andrée (maandree@member.fsf.org)
# 
# Copying and distribution of this file, with or without modification,
# are permitted in any medium without royalty provided the copyright
# notice and this notice are preserved.  This file is offered as-is,
# without any warranty.
# 
# [GNU All Permissive License]

PREFIX=/usr
BIN=/bin
DATA=/share
PROGRAM=ponypipe
FLAGS=-Xlint:all -O
JAR=$(shell if which jar7 >/dev/null; then echo jar7; else echo jar; fi)
JAVAC=$(shell if which javac7 >/dev/null; then echo javac7; else echo javac; fi)
CLASS=$(shell find src | grep '\.java$$' | sed -e 's/\.java$$/\.class/g' -e 's/^src\//bin\//g')
CLASS_JAR=$$(find . | grep '\.class$$')
BOOK=$(PROGRAM)
BOOKDIR=./


all: class jar info

class: $(CLASS)
bin/%.class: src/%.java
	@mkdir -p bin
	$(JAVAC) -cp src -s src -d bin $(FLAGS) "$<"

jar: $(PROGRAM).jar
$(PROGRAM).jar: META-INF/MANIFEST.MF $(CLASS)
	mkdir -p bin/META-INF
	cp {,bin/}META-INF/MANIFEST.MF
	cd bin && $(JAR) -cfm "../$@" "META-INF/MANIFEST.MF" $(CLASS_JAR)


info: $(BOOK).info.gz
%.info: $(BOOKDIR)%.texinfo
	$(MAKEINFO) "$<"
%.info.gz: %.info
	gzip -9c < "$<" > "$@"


pdf: $(BOOK).pdf
%.pdf: $(BOOKDIR)%.texinfo
	texi2pdf "$<"

pdf.gz: $(BOOK).pdf.gz
%.pdf.gz: %.pdf
	gzip -9c < "$<" > "$@"

pdf.xz: $(BOOK).pdf.xz
%.pdf.xz: %.pdf
	xz -e9 < "$<" > "$@"


dvi: $(BOOK).dvi
%.dvi: $(BOOKDIR)%.texinfo
	$(TEXI2DVI) "$<"

dvi.gz: $(BOOK).dvi.gz
%.dvi.gz: %.dvi
	gzip -9c < "$<" > "$@"

dvi.xz: $(BOOK).dvi.xz
%.dvi.xz: %.dvi
	xz -e9 < "$<" > "$@"


install:
	mkdir -p "$(DESTDIR)$(PREFIX)$(BIN)"
	install -m 755 "$(PROGRAM).jar" "$(DESTDIR)$(PREFIX)$(BIN)"
	install -m 755 "$(PROGRAM)" "$(DESTDIR)$(PREFIX)$(BIN)"
	mkdir -p "$(DESTDIR)$(PREFIX)$(DATA)/$(PROGRAM)"
	install -m 644 "rules" "$(DESTDIR)$(PREFIX)$(DATA)/$(PROGRAM)/rules"
	mkdir -p "$(DESTDIR)$(PREFIX)$(DATA)/info"
	install -m 644 "$(BOOK).info.gz" "$(DESTDIR)$(PREFIX)$(DATA)/info"
	mkdir -p "$(DESTDIR)$(PREFIX)$(DATA)/licenses/$(PROGRAM)"
	install -m 644 COPYING "$(DESTDIR)$(PREFIX)$(DATA)/licenses/$(PROGRAM)"

uninstall:
	unlink "$(DESTDIR)$(PREFIX)$(BIN)/$(PROGRAM)"
	unlink "$(DESTDIR)$(PREFIX)$(BIN)/$(PROGRAM).jar"
	rm -rf "$(DESTDIR)$(PREFIX)$(DATA)/$(PROGRAM)"
	rm -rf "$(DESTDIR)$(PREFIX)$(DATA)/licenses/$(PROGRAM)"
	unlink "$(DESTDIR)$(PREFIX)$(DATA)/info/$(BOOK).info.gz"

clean:
	rm -r bin "$(PROGRAM).jar" 2>/dev/null || exit 0
	rm -r *.{t2d,aux,{cp,pg,op,vr}{,s},fn,ky,log,toc,tp,bak,info,pdf,ps,dvi,gz} 2>/dev/null || exit 0

