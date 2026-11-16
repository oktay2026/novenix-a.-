package com.example.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.NovenixCatalog

object IntentHelpers {

    fun openExternalUrl(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "Bağlantıyı açacak uygulama bulunamadı.", Toast.LENGTH_SHORT).show()
        }
    }

    fun dialPhone(context: Context, phoneUri: String = NovenixCatalog.PHONE_URI) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse(phoneUri))
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "Arama uygulaması bulunamadı.", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendEmail(
        context: Context,
        email: String = NovenixCatalog.EMAIL,
        subject: String = "",
        body: String = ""
    ) {
        try {
            val uriBuilder = StringBuilder("mailto:")
                .append(Uri.encode(email))
            val params = mutableListOf<String>()
            if (subject.isNotBlank()) params.add("subject=${Uri.encode(subject)}")
            if (body.isNotBlank()) params.add("body=${Uri.encode(body)}")
            if (params.isNotEmpty()) {
                uriBuilder.append("?").append(params.joinToString("&"))
            }
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(uriBuilder.toString()))
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "E-posta uygulaması bulunamadı.", Toast.LENGTH_SHORT).show()
        }
    }

    fun openMaps(context: Context, query: String = NovenixCatalog.MAP_QUERY) {
        try {
            val geoUri = Uri.parse("geo:0,0?q=${Uri.encode(query)}")
            val intent = Intent(Intent.ACTION_VIEW, geoUri)
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            openExternalUrl(
                context,
                "https://www.google.com/maps/search/?api=1&query=${Uri.encode(query)}"
            )
        }
    }

    fun shareLink(context: Context, url: String, title: String) {
        val shareText = if (title.isNotBlank()) "$title\n$url" else url
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title.ifBlank { "Novenix Teknoloji Grubu" })
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        val chooser = Intent.createChooser(sendIntent, "Bağlantıyı Paylaş")
        context.startActivity(chooser)
    }
}
