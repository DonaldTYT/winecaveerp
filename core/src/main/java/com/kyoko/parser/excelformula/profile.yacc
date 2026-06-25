#!/bin/sh
l() {
/usr/app/jflex/bin/jflex $*
}
y() {
/usr/local/bin/byacc -Jpackage=com.kyoko.parser.excelformula -Jextends=com.kyoko.parser.Parser $*
}
