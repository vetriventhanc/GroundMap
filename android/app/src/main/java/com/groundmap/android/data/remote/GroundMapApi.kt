
package com.groundmap.android.data.remote

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class DashboardData(
    val workspaceName: String,
    val totalAssets: Int,
    val operationalAssets: Int,
    val maintenanceAssets: Int,
    val damagedAssets: Int,
    val message: String? = null
)

data class WorkspaceData(
    val id: String,
    val name: String,
    val description: String?
)

data class AssetData(
    val id: String,
    val workspaceId: String,
    val roomId: String?,
    val assetCode: String,
    val name: String,
    val category: String,
    val manufacturer: String?,
    val modelNumber: String?,
    val serialNumber: String?,
    val purchaseDate: String?,
    val status: String,
    val condition: String,
    val notes: String?
)

data class AssetHistoryData(
    val id: String,
    val assetId: String,
    val eventType: String,
    val description: String,
    val oldValue: JSONObject?,
    val newValue: JSONObject?,
    val createdAt: String
)


data class FaultReportData(
    val id: String,
    val assetId: String,
    val title: String,
    val description: String,
    val severity: String,
    val status: String,
    val resolutionNotes: String?,
    val createdAt: String,
    val updatedAt: String
)

data class RoomLocationData(
    val id: String,
    val name: String,
    val description: String?
)

data class FloorLocationData(
    val id: String,
    val name: String,
    val floorNumber: Int?,
    val rooms: List<RoomLocationData>
)

data class BuildingLocationData(
    val id: String,
    val name: String,
    val description: String?,
    val floors: List<FloorLocationData>
)

data class SiteLocationData(
    val id: String,
    val name: String,
    val address: String?,
    val buildings: List<BuildingLocationData>
)

data class WorkspaceLocationTreeData(
    val id: String,
    val name: String,
    val description: String?,
    val sites: List<SiteLocationData>
)

object GroundMapApi {

    private const val BASE_URL = "http://10.0.2.2:8000"

    private fun request(
        path: String,
        method: String = "GET",
        body: JSONObject? = null
    ): String {

        val connection = URL("$BASE_URL$path")
            .openConnection() as HttpURLConnection

        try {
            connection.requestMethod = method
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            connection.setRequestProperty("Accept", "application/json")

            if (body != null) {
                connection.doOutput = true
                connection.setRequestProperty(
                    "Content-Type",
                    "application/json"
                )

                connection.outputStream.use {
                    it.write(body.toString().toByteArray(Charsets.UTF_8))
                }
            }

            val code = connection.responseCode

            val stream = if (code in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val response = stream?.bufferedReader()?.use {
                it.readText()
            } ?: ""

            if (code !in 200..299) {
                throw Exception("API error $code: $response")
            }

            return response

        } finally {
            connection.disconnect()
        }
    }

    private fun JSONObject.optionalString(key: String): String? {
        return if (isNull(key)) null else optString(key).takeIf {
            it.isNotBlank()
        }
    }

    private fun nullableJsonValue(value: String?): Any {
        return value?.takeIf { it.isNotBlank() } ?: JSONObject.NULL
    }

    private fun parseAsset(item: JSONObject): AssetData {
        return AssetData(
            id = item.getString("id"),
            workspaceId = item.getString("workspace_id"),
            roomId = item.optionalString("room_id"),
            assetCode = item.getString("asset_code"),
            name = item.getString("name"),
            category = item.getString("category"),
            manufacturer = item.optionalString("manufacturer"),
            modelNumber = item.optionalString("model_number"),
            serialNumber = item.optionalString("serial_number"),
            purchaseDate = item.optionalString("purchase_date"),
            status = item.getString("status"),
            condition = item.getString("condition"),
            notes = item.optionalString("notes")
        )
    }

    // WORKSPACES

    fun loadWorkspaces(): List<WorkspaceData> {
        val response = JSONArray(
            request("/api/v1/workspaces")
        )

        return buildList {
            for (i in 0 until response.length()) {
                val item = response.getJSONObject(i)

                add(
                    WorkspaceData(
                        id = item.getString("id"),
                        name = item.getString("name"),
                        description = item.optionalString("description")
                    )
                )
            }
        }
    }

    // ASSET LIST

    fun loadAssets(workspaceId: String): List<AssetData> {
        val response = JSONArray(
            request("/api/v1/assets?workspace_id=$workspaceId")
        )

        return buildList {
            for (i in 0 until response.length()) {
                add(parseAsset(response.getJSONObject(i)))
            }
        }
    }

    // LOCATION TREE

    fun loadLocationTree(workspaceId: String): WorkspaceLocationTreeData {
        val rawResponse = request(
            "/api/v1/locations/tree?workspace_id=$workspaceId"
        ).trim()

        val workspace = when {
            rawResponse.startsWith("[") -> {
                val items = JSONArray(rawResponse)
                if (items.length() == 0) {
                    throw Exception("Location tree returned no workspaces.")
                }
                items.getJSONObject(0)
            }

            else -> {
                val response = JSONObject(rawResponse)
                val items = response.optJSONArray("value")

                if (items != null) {
                    if (items.length() == 0) {
                        throw Exception("Location tree returned no workspaces.")
                    }
                    items.getJSONObject(0)
                } else {
                    response
                }
            }
        }

        val sitesJson = workspace.optJSONArray("sites") ?: JSONArray()

        val sites = buildList {
            for (siteIndex in 0 until sitesJson.length()) {
                val site = sitesJson.getJSONObject(siteIndex)
                val buildingsJson = site.optJSONArray("buildings") ?: JSONArray()

                val buildings = buildList {
                    for (buildingIndex in 0 until buildingsJson.length()) {
                        val building = buildingsJson.getJSONObject(buildingIndex)
                        val floorsJson = building.optJSONArray("floors") ?: JSONArray()

                        val floors = buildList {
                            for (floorIndex in 0 until floorsJson.length()) {
                                val floor = floorsJson.getJSONObject(floorIndex)
                                val roomsJson = floor.optJSONArray("rooms") ?: JSONArray()

                                val rooms = buildList {
                                    for (roomIndex in 0 until roomsJson.length()) {
                                        val room = roomsJson.getJSONObject(roomIndex)
                                        add(
                                            RoomLocationData(
                                                id = room.getString("id"),
                                                name = room.getString("name"),
                                                description = room.optionalString("description")
                                            )
                                        )
                                    }
                                }

                                add(
                                    FloorLocationData(
                                        id = floor.getString("id"),
                                        name = floor.getString("name"),
                                        floorNumber = if (floor.isNull("floor_number")) {
                                            null
                                        } else {
                                            floor.optInt("floor_number")
                                        },
                                        rooms = rooms
                                    )
                                )
                            }
                        }

                        add(
                            BuildingLocationData(
                                id = building.getString("id"),
                                name = building.getString("name"),
                                description = building.optionalString("description"),
                                floors = floors
                            )
                        )
                    }
                }

                add(
                    SiteLocationData(
                        id = site.getString("id"),
                        name = site.getString("name"),
                        address = site.optionalString("address"),
                        buildings = buildings
                    )
                )
            }
        }

        return WorkspaceLocationTreeData(
            id = workspace.getString("id"),
            name = workspace.getString("name"),
            description = workspace.optionalString("description"),
            sites = sites
        )
    }

    // ASSIGN ASSET LOCATION

    fun updateAssetLocation(
        assetId: String,
        roomId: String?
    ): AssetData {
        val body = JSONObject().apply {
            put("room_id", nullableJsonValue(roomId))
        }

        return parseAsset(
            JSONObject(
                request(
                    "/api/v1/assets/$assetId/location",
                    "PATCH",
                    body
                )
            )
        )
    }

    // SINGLE ASSET

    fun loadAsset(assetId: String): AssetData {
        return parseAsset(
            JSONObject(request("/api/v1/assets/$assetId"))
        )
    }

    // REGISTER ASSET

    fun createAsset(
        workspaceId: String,
        roomId: String?,
        assetCode: String,
        name: String,
        category: String,
        manufacturer: String?,
        modelNumber: String?,
        serialNumber: String?,
        purchaseDate: String?,
        status: String = "active",
        condition: String = "good",
        notes: String?
    ): AssetData {

        val body = JSONObject().apply {
            put("workspace_id", workspaceId)
            put("room_id", nullableJsonValue(roomId))
            put("asset_code", assetCode.trim())
            put("name", name.trim())
            put("category", category.trim())
            put("manufacturer", nullableJsonValue(manufacturer))
            put("model_number", nullableJsonValue(modelNumber))
            put("serial_number", nullableJsonValue(serialNumber))
            put("purchase_date", nullableJsonValue(purchaseDate))
            put("status", status)
            put("condition", condition)
            put("notes", nullableJsonValue(notes))
        }

        return parseAsset(
            JSONObject(
                request("/api/v1/assets", "POST", body)
            )
        )
    }

    // UPDATE ASSET INFORMATION

    fun updateAsset(
        assetId: String,
        name: String,
        category: String,
        manufacturer: String?,
        modelNumber: String?,
        serialNumber: String?,
        purchaseDate: String?,
        notes: String?
    ): AssetData {

        val body = JSONObject().apply {
            put("name", name.trim())
            put("category", category.trim())
            put("manufacturer", nullableJsonValue(manufacturer))
            put("model_number", nullableJsonValue(modelNumber))
            put("serial_number", nullableJsonValue(serialNumber))
            put("purchase_date", nullableJsonValue(purchaseDate))
            put("notes", nullableJsonValue(notes))
        }

        return parseAsset(
            JSONObject(
                request("/api/v1/assets/$assetId", "PATCH", body)
            )
        )
    }

    // UPDATE ASSET STATUS

    fun updateAssetStatus(
        assetId: String,
        status: String
    ): AssetData {

        require(
            status in listOf("active", "maintenance", "inactive", "retired")
        ) {
            "Invalid asset status: $status"
        }

        val body = JSONObject().apply {
            put("status", status)
        }

        return parseAsset(
            JSONObject(
                request(
                    "/api/v1/assets/$assetId/status",
                    "PATCH",
                    body
                )
            )
        )
    }

    // UPDATE ASSET CONDITION

    fun updateAssetCondition(
        assetId: String,
        condition: String
    ): AssetData {

        require(
            condition in listOf(
                "excellent",
                "good",
                "fair",
                "poor",
                "damaged"
            )
        ) {
            "Invalid asset condition: $condition"
        }

        val body = JSONObject().apply {
            put("condition", condition)
        }

        return parseAsset(
            JSONObject(
                request(
                    "/api/v1/assets/$assetId/condition",
                    "PATCH",
                    body
                )
            )
        )
    }

    // ASSET HISTORY

    fun loadAssetHistory(assetId: String): List<AssetHistoryData> {
        val response = JSONArray(
            request("/api/v1/assets/$assetId/history")
        )

        return buildList {
            for (i in 0 until response.length()) {
                val item = response.getJSONObject(i)

                add(
                    AssetHistoryData(
                        id = item.getString("id"),
                        assetId = item.getString("asset_id"),
                        eventType = item.getString("event_type"),
                        description = item.getString("description"),
                        oldValue = if (
                            item.isNull("old_value")
                        ) null else item.getJSONObject("old_value"),
                        newValue = if (
                            item.isNull("new_value")
                        ) null else item.getJSONObject("new_value"),
                        createdAt = item.getString("created_at")
                    )
                )
            }
        }
    }


    // FAULT REPORTING

    fun createFaultReport(
        assetId: String,
        title: String,
        description: String,
        severity: String
    ): FaultReportData {

        require(severity in listOf("low", "medium", "high", "critical")) {
            "Invalid fault severity: $severity"
        }

        val body = JSONObject().apply {
            put("title", title.trim())
            put("description", description.trim())
            put("severity", severity)
        }

        val response = JSONObject(
            request(
                "/api/v1/assets/$assetId/fault-reports",
                "POST",
                body
            )
        )

        return parseFaultReport(response)
    }

    fun loadFaultReports(
        assetId: String
    ): List<FaultReportData> {

        val response = JSONArray(
            request("/api/v1/assets/$assetId/fault-reports")
        )

        return buildList {
            for (i in 0 until response.length()) {
                add(parseFaultReport(response.getJSONObject(i)))
            }
        }
    }

    fun updateFaultReport(
        assetId: String,
        reportId: String,
        status: String,
        resolutionNotes: String?
    ): FaultReportData {
        require(status in listOf("open", "in_progress", "resolved")) {
            "Invalid fault report status: $status"
        }

        val body = JSONObject().apply {
            put("status", status)
            put(
                "resolution_notes",
                resolutionNotes?.trim()?.takeIf { it.isNotBlank() } ?: JSONObject.NULL
            )
        }

        val response = JSONObject(
            request(
                "/api/v1/assets/$assetId/fault-reports/$reportId",
                "PATCH",
                body
            )
        )

        return parseFaultReport(response)
    }

    private fun parseFaultReport(
        item: JSONObject
    ): FaultReportData {
        return FaultReportData(
            id = item.getString("id"),
            assetId = item.getString("asset_id"),
            title = item.getString("title"),
            description = item.getString("description"),
            severity = item.getString("severity"),
            status = item.getString("status"),
            resolutionNotes = item.optString("resolution_notes")
                .takeIf { it.isNotBlank() && it != "null" },
            createdAt = item.getString("created_at"),
            updatedAt = item.getString("updated_at")
        )
    }



    // ASSET QR CODE

    fun loadAssetQr(assetId: String): ByteArray {
        val connection = URL(
            "$BASE_URL/api/v1/assets/$assetId/qr"
        ).openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.setRequestProperty("Accept", "image/png")

            val responseCode = connection.responseCode

            if (responseCode !in 200..299) {
                val errorMessage = connection.errorStream
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    ?: "Unable to retrieve QR code."

                throw Exception("API error $responseCode: $errorMessage")
            }

            val contentType = connection.contentType ?: ""

            if (!contentType.contains("image/png", ignoreCase = true)) {
                throw Exception("Unexpected QR response type: $contentType")
            }

            return connection.inputStream.use {
                it.readBytes()
            }
        } finally {
            connection.disconnect()
        }
    }


    // DASHBOARD

    fun loadDashboard(): DashboardData {
        val workspaces = loadWorkspaces()

        if (workspaces.isEmpty()) {
            return DashboardData(
                workspaceName = "No workspace",
                totalAssets = 0,
                operationalAssets = 0,
                maintenanceAssets = 0,
                damagedAssets = 0,
                message = "Create a workspace to get started."
            )
        }

        val workspace = workspaces.first()
        val assets = loadAssets(workspace.id)

        return DashboardData(
            workspaceName = workspace.name,
            totalAssets = assets.size,
            operationalAssets = assets.count {
                it.status == "active"
            },
            maintenanceAssets = assets.count {
                it.status == "maintenance"
            },
            damagedAssets = assets.count {
                it.condition == "damaged"
            }
        )
    }
}
