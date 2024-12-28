package com.github.jaksonlin.pitestintellij.context

data class UnittestCase(
    val values: Map<String, Any?>
) {
    fun getString(key: String): String = values[key] as? String ?: ""
    fun getStringList(key: String): List<String> = values[key] as? List<String> ?: emptyList()
    fun getStatus(key: String): String = values[key] as? String ?: "TODO"
}