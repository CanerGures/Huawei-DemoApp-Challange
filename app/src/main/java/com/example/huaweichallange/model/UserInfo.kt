package com.example.huaweichallange.model

import java.io.Serializable

data class UserInfoModel(
    var personName: String? = null,
    var personPhoto: String? = null
) : Serializable