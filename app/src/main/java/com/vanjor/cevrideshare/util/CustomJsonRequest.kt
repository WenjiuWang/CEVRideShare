package com.vanjor.cevrideshare.util

import com.android.volley.ParseError
import org.json.JSONException
import com.android.volley.toolbox.HttpHeaderParser
import org.json.JSONArray
import com.android.volley.NetworkResponse
import com.android.volley.Response
import org.json.JSONObject
import com.android.volley.toolbox.JsonRequest
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

class CustomJsonRequest(
    method: Int, url: String?, jsonRequest: JSONObject?,
    listener: Response.Listener<JSONArray?>?, errorListener: Response.ErrorListener?
) :
    JsonRequest<JSONArray>(
        method, url, jsonRequest?.toString(), listener,
        errorListener
    ) {
    override fun parseNetworkResponse(response: NetworkResponse): Response<JSONArray> {
        return try {
            val jsonString = String(
                response.data,
                Charset.forName(HttpHeaderParser.parseCharset(response.headers))
            )
            Response.success(
                JSONArray(jsonString),
                HttpHeaderParser.parseCacheHeaders(response)
            )
        } catch (e: UnsupportedEncodingException) {
            Response.error(ParseError(e))
        } catch (je: JSONException) {
            Response.error(ParseError(je))
        }
    }
}
