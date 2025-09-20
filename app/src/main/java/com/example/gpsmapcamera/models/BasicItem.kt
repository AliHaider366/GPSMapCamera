package com.example.gpsmapcamera.models

sealed class TemplateModificationItem {
    data class Header(val title: String) : TemplateModificationItem()
    data class Option(val title: String, var isChecked: Boolean? = null, var isShowArrow: Boolean = true) : TemplateModificationItem()
}
