#!/bin/sh
rm -f atktuning/*.class
JAVALIB=/segfs/tango/release/java/lib;
MYLIB=$JAVALIB/ATKWidget.jar:$JAVALIB/ATKCore.jar
CLASSPATH=$JAVALIB/TangORB.jar:$MYLIB:$JAVALIB/ATKtools.jar:.
export CLASSPATH
echo $CLASSPATH
echo   Compiling AtkTuning...
javac  -g -deprecation atktuning/MainPanel.java
