package com.example.util

import android.content.Context
import com.example.data.model.Member
import com.example.data.model.Payment
import com.example.data.repository.SomityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object GoogleSheetSyncManager {

    private const val PREFS_NAME = "scsm_settings_prefs"
    private const val KEY_SHEET_URL = "key_google_sheet_url"
    private const val KEY_LAST_SYNC = "key_last_sync_timestamp"
    
    private const val DEFAULT_WEB_APP_URL = "https://script.google.com/macros/s/AKfycbw4Wrap3_Sd2au2ouBmM9ntBFXM6K5aqRELs_dVqv9n9tiWBs5APMYL-gaqbF0BGKpr/exec"

    fun getSheetUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_SHEET_URL, "")
        return if (!saved.isNullOrBlank()) saved else DEFAULT_WEB_APP_URL
    }

    fun saveSheetUrl(context: Context, url: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SHEET_URL, url.trim()).apply()
    }

    fun getLastSyncTime(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_LAST_SYNC, 0L)
    }

    fun saveLastSyncTime(context: Context, timestamp: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_SYNC, timestamp).apply()
    }

    /**
     * Build JSON payload containing all members and payment history
     */
    suspend fun buildSyncJson(repository: SomityRepository): String {
        val members = repository.getMembersDirect()
        val payments = repository.getAllPaymentsDirect()

        val root = JSONObject()
        root.put("app", "Shinglab Charpotan Somity Manager")
        root.put("version", "1.0")
        root.put("timestamp", System.currentTimeMillis())

        val membersArray = JSONArray()
        members.forEach { m ->
            val mObj = JSONObject()
            mObj.put("id", m.id)
            mObj.put("memberNo", m.memberNo)
            mObj.put("name", m.name)
            mObj.put("phone", m.phone)
            mObj.put("address", m.address)
            mObj.put("joinDate", m.joinDate)
            mObj.put("note", m.note)
            membersArray.put(mObj)
        }
        root.put("members", membersArray)

        val paymentsArray = JSONArray()
        payments.forEach { p ->
            val pObj = JSONObject()
            pObj.put("id", p.id)
            pObj.put("memberId", p.memberId)
            pObj.put("paymentDate", p.paymentDate)
            pObj.put("monthYear", p.monthYear)
            pObj.put("installmentCount", p.installmentCount)
            pObj.put("amount", p.amount)
            pObj.put("receiptNo", p.receiptNo)
            pObj.put("remarks", p.remarks)
            paymentsArray.put(pObj)
        }
        root.put("payments", paymentsArray)

        return root.toString(2)
    }

    /**
     * Perform HTTP POST sync to user's Google Apps Script URL
     */
    suspend fun syncToGoogleSheet(context: Context, repository: SomityRepository): Result<String> {
        val urlString = getSheetUrl(context)
        if (urlString.isEmpty()) {
            return Result.failure(Exception("গুগল শিট ওয়েব অ্যাপ ইউআরএল সংসংযুক্ত করা নেই। দয়া করে নিচে ইউআরএল সেট করুন।"))
        }

        return withContext(Dispatchers.IO) {
            try {
                val jsonPayload = buildSyncJson(repository)
                var currentUrl = urlString
                var connection: HttpURLConnection
                var responseCode: Int
                var redirects = 0

                do {
                    val url = URL(currentUrl)
                    connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "POST"
                    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    connection.doOutput = true
                    connection.connectTimeout = 15000
                    connection.readTimeout = 15000
                    connection.instanceFollowRedirects = true

                    val writer = OutputStreamWriter(connection.outputStream, "UTF-8")
                    writer.write(jsonPayload)
                    writer.flush()
                    writer.close()

                    responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP || 
                        responseCode == HttpURLConnection.HTTP_MOVED_PERM || 
                        responseCode == 307 || responseCode == 308) {
                        val newUrl = connection.getHeaderField("Location")
                        if (newUrl != null) {
                            currentUrl = newUrl
                            redirects++
                        } else {
                            break
                        }
                    } else {
                        break
                    }
                } while (redirects < 5)

                val reader = BufferedReader(InputStreamReader(
                    if (responseCode in 200..299) connection.inputStream else connection.errorStream
                ))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                if (responseCode in 200..299) {
                    saveLastSyncTime(context, System.currentTimeMillis())
                    Result.success("গুগল শিটে ডাটা সফলভাবে সংরক্ষিত হয়েছে!")
                } else {
                    Result.failure(Exception("গুগল শিট সার্ভার রেসপন্স কোড: $responseCode"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Restore database from JSON String (e.g. from Google Sheet or Cloud Backup)
     */
    suspend fun restoreFromJson(jsonString: String, repository: SomityRepository): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val root = JSONObject(jsonString)
                val membersArray = root.getJSONArray("members")
                val paymentsArray = root.getJSONArray("payments")

                val memberList = mutableListOf<Member>()
                for (i in 0 until membersArray.length()) {
                    val mObj = membersArray.getJSONObject(i)
                    memberList.add(
                        Member(
                            id = mObj.optLong("id", 0L),
                            memberNo = mObj.optString("memberNo", "000"),
                            name = mObj.optString("name", "Unknown"),
                            phone = mObj.optString("phone", ""),
                            address = mObj.optString("address", ""),
                            joinDate = mObj.optString("joinDate", "01/01/2025"),
                            note = mObj.optString("note", "")
                        )
                    )
                }

                val paymentList = mutableListOf<Payment>()
                for (i in 0 until paymentsArray.length()) {
                    val pObj = paymentsArray.getJSONObject(i)
                    paymentList.add(
                        Payment(
                            id = pObj.optLong("id", 0L),
                            memberId = pObj.optLong("memberId", 0L),
                            paymentDate = pObj.optString("paymentDate", ""),
                            monthYear = pObj.optString("monthYear", ""),
                            installmentCount = pObj.optInt("installmentCount", 1),
                            amount = pObj.optDouble("amount", 2000.0),
                            receiptNo = pObj.optString("receiptNo", ""),
                            remarks = pObj.optString("remarks", "")
                        )
                    )
                }

                repository.restoreData(memberList, paymentList)
                Result.success("সফলভাবে ${memberList.size} জন সদস্য ও ${paymentList.size} টি জমার তথ্য রিস্টোর করা হয়েছে।")
            } catch (e: Exception) {
                Result.failure(Exception("ডাটা রিস্টোর করতে সমস্যা হয়েছে: ${e.message}"))
            }
        }
    }
}
