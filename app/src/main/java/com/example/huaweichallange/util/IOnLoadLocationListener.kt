package com.example.huaweichallange.util

interface IOnLoadLocationListener {
    fun onLocationLoadSuccess(latLng: List<MyLatlng>)
    fun onLocationLoadFailed(message: String)
}