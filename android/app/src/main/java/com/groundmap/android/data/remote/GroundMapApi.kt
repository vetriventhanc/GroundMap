
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

            connection.setRequestProperty(
                "Accept",
                "application/json"
            )

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

    private fun JSONObject.optionalString(
        key: String
    ): String? {
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

    // SINGLE ASSET

    fun loadAsset(assetId: String): AssetData {
        val item = JSONObject(
            request("/api/v1/assets/$assetId")
        )

        return parseAsset(item)
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

        val response = request(
            path = "/api/v1/assets",
            method = "POST",
            body = body
        )

        return parseAsset(JSONObject(response))
    }

    // UPDATE ASSET

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

        val response = request(
            path = "/api/v1/assets/$assetId",
            method = "PATCH",
            body = body
        )

        return parseAsset(JSONObject(response))
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
