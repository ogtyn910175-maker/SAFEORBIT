package com.ogken.safeorbit.map

/**
 * Encapsulates a configurable map tile provider definition.
 * Allows swapping or adding alternative raster tile providers (e.g. self-hosted, Stadia, etc.)
 * without modifying the map screens or view models.
 */
data class MapTileProvider(
    val id: String,
    val name: String,
    val tileUrlTemplate: String,
    val tileSize: Int = 256,
    val minZoom: Int = 0,
    val maxZoom: Int = 19,
    val attribution: String,
    val userAgent: String
)

object MapTileConfig {
    /**
     * Default OpenStreetMap raster tile configuration.
     * Compliant with OSM Tile Usage Policy:
     * - Uses https://tile.openstreetmap.org/{z}/{x}/{y}.png
     * - Configured with identifiable SafeOrbit User-Agent
     * - Visible attribution: © OpenStreetMap contributors
     */
    val OpenStreetMap = MapTileProvider(
        id = "osm-raster-tiles",
        name = "OpenStreetMap",
        tileUrlTemplate = "https://tile.openstreetmap.org/{z}/{x}/{y}.png",
        tileSize = 256,
        minZoom = 0,
        maxZoom = 19,
        attribution = "© OpenStreetMap contributors",
        userAgent = "SafeOrbit/1.1.1 (Android; com.ogken.safeorbit)"
    )

    /**
     * Currently active tile provider. Can be changed at runtime or through app configuration.
     */
    var currentProvider: MapTileProvider = OpenStreetMap

    /**
     * Generates a MapLibre Style JSON string referencing the active tile provider.
     */
    fun createStyleJson(provider: MapTileProvider = currentProvider): String {
        return """
        {
          "version": 8,
          "sources": {
            "${provider.id}": {
              "type": "raster",
              "tiles": [
                "${provider.tileUrlTemplate}"
              ],
              "tileSize": ${provider.tileSize},
              "minzoom": ${provider.minZoom},
              "maxzoom": ${provider.maxZoom},
              "attribution": "${provider.attribution}"
            }
          },
          "layers": [
            {
              "id": "${provider.id}-layer",
              "type": "raster",
              "source": "${provider.id}"
            }
          ]
        }
        """.trimIndent()
    }
}
