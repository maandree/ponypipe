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


all: class jar

class: $(CLASS)
bin/%.class: src/%.java
	@mkdir -p bin
	$(JAVAC) -cp src -s src -d bin $(FLAGS) "$<"

jar: $(PROGRAM).jar
$(PROGRAM).jar: META-INF/MANIFEST.MF $(CLASS)
	mkdir -p bin/META-INF
	cp {,bin/}META-INF/MANIFEST.MF
	cd bin && $(JAR) -cfm "../$@" "META-INF/MANIFEST.MF" $(CLASS_JAR)


install:
	mkdir -p "$(DESTDIR)$(PREFIX)$(BIN)"
	install -m 755 "$(PROGRAM).jar" "$(DESTDIR)$(PREFIX)$(BIN)"
	install -m 755 "$(PROGRAM)" "$(DESTDIR)$(PREFIX)$(BIN)"
	mkdir -p "$(DESTDIR)$(PREFIX)$(DATA)/$(PROGRAM)"
	install -m 644 "rules" "$(DESTDIR)$(PREFIX)$(DATA)/$(PROGRAM)/rules"

uninstall:
	unlink "$(DESTDIR)$(PREFIX)$(BIN)/$(PROGRAM)"
	unlink "$(DESTDIR)$(PREFIX)$(BIN)/$(PROGRAM).jar"
	rm -rf "$(DESTDIR)$(PREFIX)$(DATA)/$(PROGRAM)"

clean:
	rm -r bin "$(PROGRAM).jar" 2>/dev/null || exit 0

