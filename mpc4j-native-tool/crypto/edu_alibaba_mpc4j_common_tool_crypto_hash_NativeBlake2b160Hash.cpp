//
// Created by Weiran Liu on 2021/12/31.
//

#include "edu_alibaba_mpc4j_common_tool_crypto_hash_NativeBlake2b160Hash.h"
#include "blake2b_hash.h"
#include <cstdint>

JNIEXPORT jbyteArray JNICALL Java_edu_alibaba_mpc4j_common_tool_crypto_hash_NativeBlake2b160Hash_digest
    (JNIEnv *env, jobject context, jbyteArray jmessage) {
    // 读取输入
    jsize length = (*env).GetArrayLength(jmessage);
    jbyte* jmessageBuffer = (*env).GetByteArrayElements(jmessage, nullptr);
    auto * input = (uint8_t*) jmessageBuffer;
    // 计算哈希
    auto * output = new uint8_t[BLAKE_2B_160_DIGEST_LENGTH];
    blake2b_160_hash(output, input, length);
    // 释放资源并返回结果
    (*env).ReleaseByteArrayElements(jmessage, jmessageBuffer, 0);
    jbyteArray jhash = (*env).NewByteArray(BLAKE_2B_160_DIGEST_LENGTH);
    (*env).SetByteArrayRegion(jhash, 0, BLAKE_2B_160_DIGEST_LENGTH, (const jbyte*)output);
    delete[] output;

    return jhash;
}
