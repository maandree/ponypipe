install:
	./build.sh -pkg
	./build.sh -jar -pkg
	install -d "${DESTDIR}/usr/bin"
	install -m 755 ponypipe.jar "${DESTDIR}/usr/bin"
	install -m 755 ponypipe "${DESTDIR}/usr/bin"
	install -d "${DESTDIR}/usr/share/ponypipe"
	install -m 644 share/ponypipe/rules "${DESTDIR}/usr/share/ponypipe/rules"

uninstall:
	unlink "${DESTDIR}/usr/bin/ponypipe"
	unlink "${DESTDIR}/usr/bin/ponypipe.jar"
	rm -rf "${DESTDIR}/usr/share/ponypipe"
