#!/usr/bin/env sh

VERSION=1.0.0
DEB_OUT=../..

DEB_NAME="blue-charm-gnome-applet_$VERSION.deb"
ROOT=`pwd`

DEB_ROOT=$ROOT/build/deb
DEB_DEBIAN=$DEB_ROOT/DEBIAN
DEB_CONTROL=$DEB_DEBIAN/control
DEB_RC=$DEB_ROOT/usr/share/bluecharm/rc
DEB_BIN=$DEB_ROOT/usr/bin
DEB_UTL=$DEB_BIN/bluecharm
DEB_ETC=$DEB_ROOT/ets/xdg/autostart

SRC_ROOT=../../applets
SRC_COMMON=$SRC_ROOT/common
SRC_GNOME=$SRC_ROOT/gnome
SRC_RC=$SRC_ROOT/rc

rm -rf $DEB_ROOT
mkdir -p $DEB_DEBIAN || exit 1
mkdir -p $DEB_RC || exit 1
mkdir -p $DEB_BIN || exit 1
mkdir -p $DEB_ETC || exit 1

touch $DEB_ETC/indicator-bluecharm.desktop
touch $DEB_BIN/bluecharm-applet.sh

echo "Package: bluecharm-indicator-applet
Version: $VERSION
Architecture: all
Maintainer: Krinkin Mike <krinkin.m.u@gmail.com>
Depends: python
Section: python
Priority: extra
Description: Unity applet for BlueCharm notifier" > $DEB_CONTROL || exit 1

echo "[Desktop Entry]
Name=Blue Charm Indicator
Comment=An indicator of missed calls and sms
Exec=bluecharm-applet.sh
Type=Application" > $DEB_ETC/indicator-bluecharm.desktop || exit 1

echo "#!/bin/bash
python /usr/bin/bluecharm/main.py" > $DEB_BIN/bluecharm-applet.sh || exit 1

cp $SRC_COMMON/* $DEB_BIN
cp $SRC_GNOME/* $DEB_BIN
cp $SRC_RC/* $DEB_RC

dpkg -b $DEB_ROOT $DEB_OUT/$DEB_NAME