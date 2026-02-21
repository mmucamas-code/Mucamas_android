package com.movil.mucamas.data.model

data class EmailJsRequest(
    val service_id: String,
    val template_id: String,
    val user_id: String,
    val template_params: TemplateParams
)

data class TemplateParams(
    val account_name: String,
    val passcode: String,
    val name: String
)
