package ru.mephi.voip.abto

import org.abtollc.sdk.AbtoPhone
import ru.mephi.voip.utils.EncryptUtils

fun parseRemoteContact(remoteContact: String): Pair<String, String> { // return Name and Number
    val name = remoteContact.substringAfter("\"").substringBefore("\"")
    val sipNumber = remoteContact.substringAfter(":").substringBefore("@")
    return Pair(name, sipNumber)
}

fun AbtoPhone.getSipUsername(accId: Long): String? = this.config.getAccount(accId)?.sipUserName

fun decryptAccountJson() = EncryptUtils.decrypt()
fun encryptAccountJson(jsonForEncrypt: String) = EncryptUtils.encrypt(jsonForEncrypt)