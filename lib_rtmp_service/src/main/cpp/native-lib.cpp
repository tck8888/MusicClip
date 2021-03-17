#include <jni.h>
#include <jni.h>

//
// Created by tck88 on 2021/3/7.
//

extern "C"
JNIEXPORT jboolean

 JNICALL
Java_com_tck_av_audio_rtmp_AudioLive_connect(JNIEnv *env, jobject thiz, jstring url) {
    // TODO: implement connect()
}

extern "C"
JNIEXPORT jboolean

JNICALL
Java_com_tck_av_audio_rtmp_AudioLive_sendData(JNIEnv *env, jobject thiz, jbyteArray data, jint len,
                                              jlong tms, jint type) {
    // TODO: implement sendData()
}