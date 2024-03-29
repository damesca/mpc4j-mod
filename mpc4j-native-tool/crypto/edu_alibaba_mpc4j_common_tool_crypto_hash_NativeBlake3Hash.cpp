//
// Created by Weiran Liu on 2022/1/7.
//

#include "edu_alibaba_mpc4j_common_tool_crypto_hash_NativeBlake3Hash.h"
#include "blake3_hash.h"
#include <cstdint>

JNIEXPORT jbyteArray JNICALL Java_edu_alibaba_mpc4j_common_tool_crypto_hash_NativeBlake3Hash_digest
    (JNIEnv *env, jobject context, jbyteArray jmessage) {
// 读取输入
    jsize length = (*env).GetArrayLength(jmessage);
    jbyte* jmessageBuffer = (*env).GetByteArrayElements(jmessage, nullptr);
    auto * input = (uint8_t*) jmessageBuffer;
    // 计算哈希
    auto * output = new uint8_t[BLAKE3_OUT_LEN];
    blake3_hash(output, input, length);
    // 释放资源并返回结果
    (*env).ReleaseByteArrayElements(jmessage, jmessageBuffer, 0);
    jbyteArray jhash = (*env).NewByteArray(BLAKE3_OUT_LEN);
    (*env).SetByteArrayRegion(jhash, 0, BLAKE3_OUT_LEN, (const jbyte*)output);
    delete[] output;

    return jhash;
}