#!/bin/bash

set -exo

pkg_dir=`mktemp -d`
current_dir=`pwd`
build_dir=$current_dir/../lib-ledger-core-build
libcore_dir=$current_dir/../lib-ledger-core
scala_api_dir=$current_dir/ledger-core-binding/src/main/scala/co/ledger/core
java_api_dir=$current_dir/ledger-core-binding/src/main/java/co/ledger/core
dest_dir=$current_dir/ledger-core-binding/lib

echo "Updating libraries"
# Create structure of directory used to build libledger-core.jar
mkdir -p $pkg_dir/resources/djinni_native_libs/

# Copy osx and linux libs
cp $build_dir/core/src/libledger-core.so $pkg_dir/resources/djinni_native_libs/libledger-core.so
cp $build_dir/core/src/libledger-core.dylib $pkg_dir/resources/djinni_native_libs/libledger-core.dylib

# Build the actual jar
cd $pkg_dir
jar cvf libledger-core.jar resources/
cp libledger-core.jar $dest_dir/libledger-core.jar
cd -
rm -rf $pkg_dir
echo "Libraries updated"

echo "Updating APIs"
rm $java_api_dir/*
rm $scala_api_dir/implicits/*
cp $libcore_dir/api/core/scala/* $scala_api_dir/implicits/
cp $libcore_dir/api/core/java/* $java_api_dir
echo "API updated"
