package com.ogken.safeorbit

import com.ogken.safeorbit.model.UserLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationValidationTest {

    @Test
    fun testDefaultUserLocationState() {
        val loc = UserLocation()
        assertEquals("", loc.userId)
        assertEquals("", loc.circleId)
        assertEquals(0.0, loc.latitude, 0.0001)
        assertEquals(0.0, loc.longitude, 0.0001)
        assertFalse(loc.isLive)
        assertFalse(loc.isApproximate)
        assertEquals(100, loc.batteryPercent)
    }

    @Test
    fun testUserLocationSerializationRoundtrip() {
        val original = UserLocation(
            userId = "user_abc",
            circleId = "circle_123",
            latitude = 37.7749,
            longitude = -122.4194,
            accuracy = 8.5f,
            isLive = true,
            isApproximate = false,
            timestamp = 1700000000000L,
            batteryPercent = 82
        )

        val map = original.toMap()
        assertEquals("user_abc", map["userId"])
        assertEquals("circle_123", map["circleId"])
        assertEquals(37.7749, (map["latitude"] as Double), 0.0001)
        assertEquals(-122.4194, (map["longitude"] as Double), 0.0001)
        assertEquals(8.5, (map["accuracy"] as Double), 0.0001)
        assertEquals(true, map["isLive"])
        assertEquals(false, map["isApproximate"])
        assertEquals(82, map["batteryPercent"])

        val restored = UserLocation.fromMap(map)
        assertEquals(original.userId, restored.userId)
        assertEquals(original.circleId, restored.circleId)
        assertEquals(original.latitude, restored.latitude, 0.0001)
        assertEquals(original.longitude, restored.longitude, 0.0001)
        assertEquals(original.accuracy, restored.accuracy, 0.0001f)
        assertEquals(original.isLive, restored.isLive)
        assertEquals(original.isApproximate, restored.isApproximate)
        assertEquals(original.batteryPercent, restored.batteryPercent)
    }

    @Test
    fun testApproximateLocationSerialization() {
        val approx = UserLocation(
            userId = "user_approx",
            circleId = "circle_123",
            latitude = 12.9716,
            longitude = 77.5946,
            accuracy = 2500.0f, // City-level coarse accuracy
            isLive = true,
            isApproximate = true,
            timestamp = System.currentTimeMillis(),
            batteryPercent = 65
        )

        val map = approx.toMap()
        val restored = UserLocation.fromMap(map)
        assertTrue(restored.isApproximate)
        assertTrue(restored.isLive)
        assertEquals(2500.0f, restored.accuracy, 0.1f)
    }

    @Test
    fun testCoordinateBoundsValidation() {
        val validLoc = UserLocation(
            latitude = 45.0,
            longitude = 90.0
        )
        assertTrue(validLoc.latitude in -90.0..90.0)
        assertTrue(validLoc.longitude in -180.0..180.0)

        val invalidLat = 120.0
        val invalidLng = -200.0
        assertFalse(invalidLat in -90.0..90.0)
        assertFalse(invalidLng in -180.0..180.0)
    }

    @Test
    fun testStoppedSharingClearsLiveFlag() {
        val liveLoc = UserLocation(
            userId = "user_1",
            circleId = "circle_1",
            latitude = 28.6139,
            longitude = 77.2090,
            isLive = true
        )
        assertTrue(liveLoc.isLive)

        val stoppedLoc = liveLoc.copy(isLive = false)
        assertFalse("Stopped location must have isLive = false", stoppedLoc.isLive)
    }
}
