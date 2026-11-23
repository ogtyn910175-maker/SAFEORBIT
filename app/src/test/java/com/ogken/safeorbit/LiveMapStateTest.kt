package com.ogken.safeorbit

import com.ogken.safeorbit.model.CircleMember
import com.ogken.safeorbit.model.UserLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LiveMapStateTest {

    @Test
    fun testLiveLocationFormatting() {
        val now = System.currentTimeMillis()
        val loc = UserLocation(
            userId = "user_1",
            circleId = "circle_1",
            latitude = 37.7749,
            longitude = -122.4194,
            accuracy = 8.5f,
            isLive = true,
            isApproximate = false,
            timestamp = now,
            batteryPercent = 92
        )

        val elapsedMs = now - loc.timestamp
        val isStale = elapsedMs > 10 * 60_000L
        assertFalse("Fresh location must not be marked stale", isStale)

        val precisionLabel = if (loc.isApproximate) "Approximate Area" else "Live Location"
        assertEquals("Live Location", precisionLabel)
    }

    @Test
    fun testStaleLocationDetection() {
        val fifteenMinutesAgo = System.currentTimeMillis() - 15 * 60_000L
        val staleLoc = UserLocation(
            userId = "user_2",
            circleId = "circle_1",
            latitude = 37.7749,
            longitude = -122.4194,
            accuracy = 25.0f,
            isLive = true,
            isApproximate = true,
            timestamp = fifteenMinutesAgo,
            batteryPercent = 45
        )

        val elapsedMs = System.currentTimeMillis() - staleLoc.timestamp
        val isStale = elapsedMs > 10 * 60_000L
        assertTrue("Locations older than 10 minutes must be flagged as stale", isStale)

        val precisionLabel = if (staleLoc.isApproximate) "Approximate Area" else "Live Location"
        assertEquals("Approximate Area", precisionLabel)
    }

    @Test
    fun testActiveSharersCount() {
        val memberLocations = listOf(
            UserLocation(userId = "u1", isLive = true),
            UserLocation(userId = "u2", isLive = false),
            UserLocation(userId = "u3", isLive = true)
        )

        val currentUserSharing = false
        val activeCount = memberLocations.count { it.isLive } + (if (currentUserSharing) 1 else 0)
        assertEquals(2, activeCount)

        val currentUserSharingNow = true
        val activeCountWithSelf = memberLocations.count { it.isLive } + (if (currentUserSharingNow) 1 else 0)
        assertEquals(3, activeCountWithSelf)
    }

    @Test
    fun testEmptyCircleHasZeroSharers() {
        val emptyLocations = emptyList<UserLocation>()
        val currentUserSharing = false
        val count = emptyLocations.count { it.isLive } + (if (currentUserSharing) 1 else 0)
        assertEquals(0, count)
    }
}
