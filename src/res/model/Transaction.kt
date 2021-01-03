package res.model

import com.google.gson.annotations.SerializedName

data class Transaction(
    val uuid: Long,
    val description: String,
    val category: String,
    val operation: String,
    val amount: Double,
    val status: String,
    val creation_date: String
)