# Copyright © 2013  Mattias Andrée (maandree@member.fsf.org)
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
BINJAR=$(DATA)/$(PKGNAME)
LICENSES=$(DATA)/licenses

PROGRAM=ponypipe
FLAGS=-Xlint:all -O
JAR=jar
JAVAC=javac
CLASS=$(shell find src | grep '\.java$$' | sed -e 's/\.java$$/\.class/g' -e 's/^src\//bin\//g')
SOURCE=$(shell find src | grep '\.java$$' | sed -e -e 's/^src\//bin\//g')
CLASS_JAR=$$(find . | grep '\.class$$')
BOOK=$(PROGRAM)
BOOKDIR=info/


all: code info

code: class jar launcher


launcher: $(PROGRAM).install

$(PROGRAM).install: $(PROGRAM)
	cp "$<" "$@"
	sed -i 's:"$$0.jar":"$(PREFIX)$(BINJAR)/$(COMMAND).jar":g' "$@"


bin/%.java: src/%.java
	@mkdir -p "$${dirname "$@"}"
	cp "$<" "$@"
	sed -i 's:"./rules":"$(PREFIX)$(DATA)/$(PKGNAME)/rules":g' "$@"

class: $(SOURCE) $(CLASS)
bin/%.class: bin/%.java
	$(JAVAC) -cp bin -s bin -d bin $(FLAGS) "$<"

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


install: install-cmd install-license install-info

install-cmd:
	mkdir -p "$(DESTDIR)$(PREFIX)$(BIN)"
	mkdir -p "$(DESTDIR)$(PREFIX)$(BINJAR)"
	install -m 755 "$(PROGRAM).install" "$(DESTDIR)$(PREFIX)$(BIN)/$(COMMAND)"
	install -m 755 "$(PROGRAM).jar" "$(DESTDIR)$(PREFIX)$(BINJAR)/$(COMMAND).jar"
	mkdir -p "$(DESTDIR)$(PREFIX)$(DATA)/$(PKGNAME)"
	install -m 644 "rules" "$(DESTDIR)$(PREFIX)$(DATA)/$(PKGNAME)/rules"

install-info:
	mkdir -p "$(DESTDIR)$(PREFIX)$(DATA)/info"
	install -m 644 "$(BOOK).info.gz" "$(DESTDIR)$(PREFIX)$(DATA)/info/$(PKGNAME).info.gz"

install-license:
	mkdir -p "$(DESTDIR)$(PREFIX)$(LICENSES)/$(PKGNAME)"
	install -m 644 COPYING "$(DESTDIR)$(PREFIX)$(LICENSES)/$(PKGNAME)/COPYING"


uninstall:
	rm -- "$(DESTDIR)$(PREFIX)$(BIN)/$(COMMAND)"
	rm -- "$(DESTDIR)$(PREFIX)$(BINJAR)/$(COMMAND).jar"
	rm -r -- "$(DESTDIR)$(PREFIX)$(DATA)/$(PROGRAM)"
	rm -r -- "$(DESTDIR)$(PREFIX)$(LICENSES)/$(PKGNAME)"
	rm -- "$(DESTDIR)$(PREFIX)$(DATA)/info/$(PKGNAME).info.gz"

clean:
	-rm -r *.{t2d,aux,{cp,pg,op,vr}{,s},fn,ky,log,toc,tp,bak,info,pdf,ps,dvi,gz,install} bin "$(PROGRAM).jar" 2>/dev/null

