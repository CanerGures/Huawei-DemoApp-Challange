package com.example.huaweichallange.model

import java.io.Serializable

data class UserInfoModel(
    var personName: String? = null,
    var familyName: String? = null,
    var personGivenName: String? = null,
    var personEmail: String? = null,
    var personId: String? = null,
    var personPhoto: String? = null
) : Serializable