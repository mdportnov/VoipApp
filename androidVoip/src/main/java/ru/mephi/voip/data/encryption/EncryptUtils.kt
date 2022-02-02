package ru.mephi.voip.data.encryption

import com.github.leonardoxh.keystore.CipherStorage
import com.github.leonardoxh.keystore.CipherStorageFactory
import ru.mephi.shared.appContext

object EncryptUtils {
    private var cipherStorage: CipherStorage = CipherStorageFactory.newInstance(appContext)
    fun encrypt(input: String) = cipherStorage.encrypt("passwords", input)
    fun decrypt() = cipherStorage.decrypt("passwords")
}