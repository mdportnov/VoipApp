package ru.mephi.voip.call

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.abtollc.sdk.AbtoPhone
import ru.mephi.shared.data.model.Account
import ru.mephi.voip.ui.utils.EncryptUtils

fun parseRemoteContact(remoteContact: String): Pair<String, String> { // return Name and Number
    val name = remoteContact.substringAfter("\"").substringBefore("\"")
    val sipNumber = remoteContact.substringAfter(":").substringBefore("@")
    return Pair(name, sipNumber)
}

fun AbtoPhone.getSipUsername(accId: Long): String? = this.config.getAccount(accId)?.sipUserName

private fun decryptAccountJson() = EncryptUtils.decrypt()
private fun encryptAccountJson(jsonForEncrypt: String) = EncryptUtils.encrypt(jsonForEncrypt)

fun getAccountsList(): MutableList<Account> {
    val jsonDecrypted = decryptAccountJson()
    return if (jsonDecrypted.isNullOrEmpty())
        mutableListOf()
    else Json.decodeFromString(jsonDecrypted)
}

fun getActiveAccount(): Account? = getAccountsList().firstOrNull { it.isActive }

fun getAccountsJson(list: List<Account>?) = Json.encodeToJsonElement(list).toString()

fun saveAccounts(list: List<Account>) = encryptAccountJson(getAccountsJson(list))