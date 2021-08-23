@file:Suppress("UnstableApiUsage")

package org.sandboxpowered.fabric.util

import com.google.common.hash.HashFunction
import com.google.common.hash.Hashing
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

fun String.hash(hash: Hash, charset: Charset = StandardCharsets.UTF_8): String {
    return hash.hashFunction.hashString(this, charset).toString()
}

@Suppress("unused")
enum class Hash(val hashFunction: HashFunction) {
    MD5(Hashing.md5()),
    SHA256(Hashing.sha256()),
    SHA512(Hashing.sha512()),
}