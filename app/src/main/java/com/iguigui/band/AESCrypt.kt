package com.iguigui.band

import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object AESCrypt {

    //AES加密
    fun encrypt(input: ByteArray, password: ByteArray): ByteArray {

        //初始化cipher对象
        val cipher = Cipher.getInstance("AES")
        // 生成密钥
        val keySpec: SecretKeySpec? = SecretKeySpec(password, "AES")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec)
        //加密解密
        val encrypt = cipher.doFinal(input)
        val result = Base64.getEncoder().encode(encrypt)

        return result
    }

    //AES解密
    fun decrypt(input: String, password: String): String {

        //初始化cipher对象
        val cipher = Cipher.getInstance("AES")
        // 生成密钥
        val keySpec: SecretKeySpec? = SecretKeySpec(password.toByteArray(), "AES")
        cipher.init(Cipher.DECRYPT_MODE, keySpec)
        //加密解密
        val encrypt = cipher.doFinal(Base64.getDecoder().decode(input.toByteArray()))
        //AES解密不需要用Base64解码
        val result = String(encrypt)

        return result
    }

}