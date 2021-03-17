#!/bin/bash



NDK=/opt/software/android-ndk-r21b
TOOLCHAIN=$NDK/toolchains/llvm/prebuilt/linux-x86_64

function build_android {

echo "开始编译 $SO_TYPE"

./configure \
--enable-shared \
--enable-jni \
--enable-cross-compile \
--target-os=android \
--prefix=$PREFIX \
--arch=$ARCH \
--cpu=$CPU \
--cc=$CC \
--cxx=$CXX \
--cross-prefix=$CROSS_PREFIX \
--disable-avdevice \
--disable-programs \
--disable-asm \
--disable-ffplay \
--disable-ffmpeg \
--disable-ffprobe \
--disable-doc \
--disable-symver \
--disable-postproc \
--disable-neon \
--disable-hwaccels \
--disable-static \
--disable-gpl 

make clean
make
make install

echo "编译成功 $SO_TYPE"

}

function v7() {

ARCH=arm
CPU=armv7-a
SO_TYPE=armeabi-v7a
API=21
PREFIX=$(pwd)/android/$SO_TYPE
CC=$TOOLCHAIN/bin/armv7a-linux-androideabi$API-clang
CXX=$TOOLCHAIN/bin/armv7a-linux-androideabi$API-clang++
CROSS_PREFIX=$TOOLCHAIN/bin/arm-linux-androideabi-
	
build_android

}

function v8() {

ARCH=arm64
CPU=armv8-a
SO_TYPE=armeabi-v8a
API=21
PREFIX=$(pwd)/android/$SO_TYPE
CC=$TOOLCHAIN/bin/aarch64-linux-androideabi$API-clang
CXX=$TOOLCHAIN/bin/aarch64-linux-androideabi$API-clang++
CROSS_PREFIX=$TOOLCHAIN/bin/aarch64-linux-android-

build_android
}

function x86() {

ARCH=x86
CPU=x86
SO_TYPE=x86
API=21
PREFIX=$(pwd)/android/$SO_TYPE
CC=$TOOLCHAIN/bin/i686-linux-androideabi$API-clang
CXX=$TOOLCHAIN/bin/i686-linux-androideabi$API-clang++
CROSS_PREFIX=$TOOLCHAIN/bin/i686-linux-android-

build_android
}

function x86_64() {
	
ARCH=x86_64
CPU=x86-64
SO_TYPE=x86-64
API=21
PREFIX=$(pwd)/android/$SO_TYPE
CC=$TOOLCHAIN/bin/x86_64-linux-androideabi$API-clang
CXX=$TOOLCHAIN/bin/x86_64-linux-androideabi$API-clang++
CROSS_PREFIX=$TOOLCHAIN/bin/x86_64-linux-android-

build_android

}

if [ $1 = "v7" ]
then
	v7
fi

