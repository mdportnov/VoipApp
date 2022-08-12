package ru.mephi.voip.utils

import com.github.leonardoxh.keystore.CipherStorage
import com.github.leonardoxh.keystore.CipherStorageFactory
import ru.mephi.shared.utils.appContext

object EncryptUtils {
    private var cipherStorage: CipherStorage = CipherStorageFactory.newInstance(appContext)
    fun encrypt(input: String) = cipherStorage.encrypt("passwords", input)
    fun decrypt() = cipherStorage.decrypt("passwords")
}