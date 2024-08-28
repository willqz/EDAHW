package br.com.lsl.ead52

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.honeywell.aidc.*


class MainActivity : AppCompatActivity(), BarcodeReader.BarcodeListener, BarcodeReader.TriggerListener {

    private var barcodeReader: BarcodeReader? = null
    private var manager: AidcManager? = null

    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.txtlabel)

        AidcManager.create(this) { aidcManager ->
            manager = aidcManager
            try {
                barcodeReader = manager?.createBarcodeReader()

                // Verifica se o barcodeReader foi criado corretamente
                barcodeReader?.let {
                    it.addBarcodeListener(this)

                    val properties = mapOf<String, Any>(
                        BarcodeReader.PROPERTY_CODE_128_ENABLED to true,  // Habilita Code 128
                        BarcodeReader.PROPERTY_CODE_39_ENABLED to true,   // Habilita Code 39
                        BarcodeReader.PROPERTY_QR_CODE_ENABLED to true,   // Habilita QR Code
                        //BarcodeReader.PROPERTY_DATAMATRIX_ENABLED to true, // Habilita Data Matrix
                        //BarcodeReader.PROPERTY_AZTEC_ENABLED to true,     // Habilita Aztec
                        //BarcodeReader.PROPERTY_EAN_13_ENABLED to true,    // Habilita EAN-13
                        BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE to BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL, // Modo de controle automático
                        BarcodeReader.PROPERTY_NOTIFICATION_BAD_READ_ENABLED to true // Habilita notificação de leitura ruim
                    )
                    it.setProperties(properties)

                    it.addTriggerListener(this)


                    Log.d("Scan", "BarcodeReader claimed")
                } ?: run {
                    Toast.makeText(
                        this@MainActivity,
                        "Erro ao criar o leitor de código de barras",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: InvalidScannerNameException) {
                Toast.makeText(
                    this@MainActivity,
                    "Invalid Scanner Name Exception: " + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Exception: " + e.message, Toast.LENGTH_SHORT)
                    .show()
            }
        }

    }

    // When using Automatic Trigger control do not need to implement the
    // onTriggerEvent function
    override fun onTriggerEvent(event: TriggerStateChangeEvent) {
        try {
            // only handle trigger presses
            // turn on/off aimer, illumination and decoding
            barcodeReader!!.aim(event.state)
            barcodeReader!!.light(event.state)
            barcodeReader!!.decode(event.state)
        } catch (e: ScannerNotClaimedException) {
//            e.printStackTrace();
            Toast.makeText(this, "Scanner is not claimed", Toast.LENGTH_SHORT).show()
        } catch (e: ScannerUnavailableException) {
//            e.printStackTrace();
            Toast.makeText(this, "Scanner unavailable", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onBarcodeEvent(event: BarcodeReadEvent?) {
        event?.let {
            val scannedData = it.barcodeData

            Log.d("Scan", "Código de barras: $scannedData")

            runOnUiThread {
                textView.text = scannedData.toString()
            }
        }
    }

    override fun onFailureEvent(p0: BarcodeFailureEvent?) {
        Log.e("Scan", "Falha na leitura do código de barras")
        runOnUiThread {
            textView.text = "Falha na leitura"
        }
    }

    override fun onPause() {
        super.onPause()
        if (barcodeReader != null) {
            // release the scanner claim so we don't get any scanner
            // notifications while paused.
            barcodeReader?.release()
        }
    }

    override fun onResume() {
        super.onResume()
        if (barcodeReader != null) {
            try {
                barcodeReader?.claim()
            } catch (e: ScannerUnavailableException) {
//                e.printStackTrace();
                Toast.makeText(this, "Scanner unavailable", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (barcodeReader != null) {
            barcodeReader?.close();
            barcodeReader = null;
        }

        if (manager != null) {
            manager?.close();
        }
    }
}