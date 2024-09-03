package br.com.lsl.ead52

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.honeywell.aidc.*


class MainActivity : AppCompatActivity() {

    private lateinit var textView: TextView

    companion object {
        private const val TAG = "IntentApiSample"
        private const val ACTION_BARCODE_DATA = "com.honeywell.sample.action.BARCODE_DATA"
        private const val ACTION_CLAIM_SCANNER = "com.honeywell.aidc.action.ACTION_CLAIM_SCANNER"
        private const val ACTION_RELEASE_SCANNER = "com.honeywell.aidc.action.ACTION_RELEASE_SCANNER"
        private const val EXTRA_SCANNER = "com.honeywell.aidc.extra.EXTRA_SCANNER"
        private const val EXTRA_PROFILE = "com.honeywell.aidc.extra.EXTRA_PROFILE"
        private const val EXTRA_PROPERTIES = "com.honeywell.aidc.extra.EXTRA_PROPERTIES"
    }

    private val barcodeDataReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_BARCODE_DATA == intent.action) {
                val version = intent.getIntExtra("version", 0)
                if (version >= 1) {
                    val aimId = intent.getStringExtra("aimId")
                    val charset = intent.getStringExtra("charset")
                    val codeId = intent.getStringExtra("codeId")
                    val data = intent.getStringExtra("data")
                    val dataBytes = intent.getByteArrayExtra("dataBytes")
                    val dataBytesStr = bytesToHexString(dataBytes)
                    val timestamp = intent.getStringExtra("timestamp")
                    val text = String.format(
                        "Data:%s\nCharset:%s\nBytes:%s\nAimId:%s\nCodeId:%s\nTimestamp:%s\n",
                        data, charset, dataBytesStr, aimId, codeId, timestamp
                    )
                    setText(text)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.txtlabel)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(barcodeDataReceiver, IntentFilter(ACTION_BARCODE_DATA))
        claimScanner()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(barcodeDataReceiver)
        releaseScanner()
    }

    private fun claimScanner() {
        val properties = Bundle().apply {
            putBoolean("DPR_DATA_INTENT", true)
            putString("DPR_DATA_INTENT_ACTION", ACTION_BARCODE_DATA)
        }
        sendBroadcast(
            Intent(ACTION_CLAIM_SCANNER).apply {
                setPackage("com.intermec.datacollectionservice")
                putExtra(EXTRA_SCANNER, "dcs.scanner.imager")
                putExtra(EXTRA_PROFILE, "MyProfile1")
                putExtra(EXTRA_PROPERTIES, properties)
            }
        )
    }

    private fun releaseScanner() {
        sendBroadcast(
            Intent(ACTION_RELEASE_SCANNER).apply {
                setPackage("com.intermec.datacollectionservice")
            }
        )
    }

    private fun setText(text: String) {
        textView?.let {
            runOnUiThread { it.text = text }
        }
    }

    private fun bytesToHexString(arr: ByteArray?): String {
        return arr?.joinToString(", ", "[", "]") { "0x${Integer.toHexString(it.toInt())}" } ?: "[]"
    }

}