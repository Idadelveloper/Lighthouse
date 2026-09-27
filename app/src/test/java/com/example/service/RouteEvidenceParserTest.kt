package com.example.service

import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Overlay guard for the public source snapshot.
 *
 * The production route-evidence parser and network client stay private. Keeping
 * this test path in the public archive replaces stale private tests in build
 * environments that overlay uploaded source instead of deleting absent files.
 */
class RouteEvidenceParserTest {
    @Test
    fun `public build exposes only the private backend boundary marker`() {
        assertNotNull(PrivateRouteEvidenceServiceBoundary)
    }
}
