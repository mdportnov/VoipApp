package ru.mephi.shared.data.network

const val REQUEST_TIMEOUT = 5000L
const val VOIP_MEPHI_URL = "https://sd.mephi.ru/api/6/"
const val API_KEY = "theiTh0Wohtho\$uquie)kooc"

const val GET_PROFILE_PIC_URL_BY_SIP = "${VOIP_MEPHI_URL}get_photo_mobile.jpg?api_key=${API_KEY}&phone="
const val GET_PROFILE_PIC_URL_BY_GUID = "${VOIP_MEPHI_URL}get_photo_mobile.jpg?api_key=${API_KEY}&EmpGUID="