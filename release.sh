#!/bin/bash

VERSION=$1

if [ "$VERSION" == "" ]; then
	exit
else 
	svn rm --force distr/latest_stable/*
	mkdir -p distr/latest_stable
	svn add distr/latest_stable
	cp -r trunk/netbeans-protobuf-plugin-suite/build/updates distr/$VERSION
	rm -rf distr/$VERSION/.svn
	cp trunk/netbeans-protobuf-plugin-suite/build/updates/* distr/latest_stable/
	svn add distr/$VERSION
	svn add distr/latest_stable/*

	echo "Remember to tag: $VERSION"
fi
