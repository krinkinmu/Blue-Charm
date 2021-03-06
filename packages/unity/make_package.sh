#!/usr/bin/env sh

VERSION=1.0.0
DEB_OUT=../..

DEB_NAME="blue-charm-indicator_$VERSION.deb"
ROOT=`pwd`

DEB_ROOT=$ROOT/build/deb
DEB_DEBIAN=$DEB_ROOT/DEBIAN
DEB_CONTROL=$DEB_DEBIAN/control
DEB_POSTINST=$DEB_DEBIAN/postinst
DEB_RC=$DEB_ROOT/usr/share/bluecharm/rc
DEB_BIN=$DEB_ROOT/usr/bin
DEB_UTL=$DEB_BIN/bluecharm

SRC_ROOT=../../applets
SRC_COMMON=$SRC_ROOT/common
SRC_UNITY=$SRC_ROOT/unity
SRC_RC=$SRC_ROOT/rc

rm -rf $DEB_ROOT

mkdir -p $DEB_DEBIAN || exit 1
mkdir -p $DEB_RC || exit 1
mkdir -p $DEB_UTL || exit 1

touch $DEB_BIN/bluecharm-applet.sh

echo "Package: bluecharm-indicator-applet
Version: $VERSION
Architecture: all
Maintainer: Krinkin Mike <krinkin.m.u@gmail.com>
Depends: python, python-bluez, python-gtk2
Section: python
Priority: extra
Description: Unity applet for BlueCharm notifier" > $DEB_CONTROL || exit 1

echo "#!/bin/bash
chmod +x /usr/bin/bluecharm-applet.sh" > $DEB_POSTINST || exit 1
chmod 0775 $DEB_POSTINST

echo "#!/bin/bash
python /usr/bin/bluecharm/main.py" > $DEB_BIN/bluecharm-applet.sh || exit 1

cp $SRC_COMMON/* $DEB_UTL
cp $SRC_UNITY/* $DEB_UTL
cp $SRC_RC/* $DEB_RC

dpkg -b $DEB_ROOT $DEB_OUT/$DEB_NAME
