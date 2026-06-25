#!/bin/sh
l() {
/usr/app/jflex/bin/jflex $*
}
y() {
/usr/local/bin/byacc -Jpackage=com.kyoko.parser.whereclpar -Jextends=com.kyoko.parser.Parser $*
}
