package com.ogken.safeorbit.model

/**
 * Data model for a circle member's published location.
 * Stored under /circles/{circleId}/locations/{userId}.
 */
data class UserLocation(
    val userId: String = "",
    val circleId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val accuracy: Float = 0.0f,
    val isLive: Boolean = false,
    val isApproximate: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val batteryPercent: Int = 100
) {
    fun toMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "circleId" to circleId,
        "latitude" to latitude,
        "longitude" to longitude,
        "accuracy" to accuracy.toDouble(),
        "isLive" to isLive,
        "isApproximate" to isApproximate,
        "timestamp" to timestamp,
        "batteryPercent" to batteryPercent
    )

    companion object {
        fun fromMap(map: Map<String, Any>): UserLocation = UserLocation(
            userId = (map["userId"] as? String).orEmpty(),
            circleId = (map["circleId"] as? String).orEmpty(),
            latitude = (map["latitude"] as? Number)?.toDouble() ?: 0.0,
            longitude = (map["longitude"] as? Number)?.toDouble() ?: 0.0,
            accuracy = (map["accuracy"] as? Number)?.toFloat() ?: 0.0f,
            isLive = (map["isLive"] as? Boolean) ?: false,
            isApproximate = (map["isApproximate"] as? Boolean) ?: false,
            timestamp = (map["timestamp"] as? Number)?.toLong() ?: 0L,
            batteryPercent = (map["batteryPercent"] as? Number)?.toInt() ?: 100
        )
    }
}
