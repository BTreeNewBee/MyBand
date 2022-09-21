package com.iguigui.band

import cn.com.heaton.blelibrary.ble.utils.ByteUtils
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec


object AESCrypt {

    var ALGORITHM = "AES";
    var AES_CBC_PADDING = "AES/CBC/PKCS5Padding";//AES/CBC/PKCS7Padding
    var AES_ECB_PADDING = "AES/ECB/NoPadding";//AES/ECB/PKCS7Padding

    //AES加密
    fun encrypt(input: ByteArray, password: ByteArray): ByteArray {
        //获取SecretKey对象,也可以使用getSecretKey()方法
        val secretKey: SecretKey = SecretKeySpec(password, ALGORITHM)
        //获取指定转换的密码对象Cipher（参数：算法/工作模式/填充模式）
        val cipher = Cipher.getInstance(AES_ECB_PADDING)
        //用密钥和一组算法参数规范初始化此Cipher对象（加密模式）
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        //执行加密操作
        return cipher.doFinal(input)
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