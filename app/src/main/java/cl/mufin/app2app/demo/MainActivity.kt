package cl.mufin.app2app.demo

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.os.Parcel
import android.os.RemoteException
import android.text.InputType
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import org.json.JSONObject
import java.util.UUID

/**
 * Cliente mínimo del contrato App2App de DTEx® (https://www.dtex.cl/developers/app2app). Sirve para
 * probar la integración en el terminal y como referencia para integradores.
 */
class MainActivity : Activity() {
    private val documentos = listOf("ticket", "none", "voucher", "cl_sii_boleta", "cl_sii_boleta_exenta")

    private lateinit var monto: EditText
    private lateinit var propina: EditText
    private lateinit var vuelto: EditText
    private lateinit var documento: Spinner
    private lateinit var imprimir: Switch
    private lateinit var respuesta: TextView
    private lateinit var rutComercio: EditText

    /**
     * Se persiste ANTES de llamar a DTEx®: si esta app muere mientras DTEx® cobra,
     * al volver es la única forma de consultar en qué quedó.
     */
    private var ultimoRequestId: String?
        get() = getSharedPreferences("demo", MODE_PRIVATE).getString("request_id", null)
        set(value) {
            getSharedPreferences("demo", MODE_PRIVATE).edit().putString("request_id", value).commit()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        monto = EditText(this).apply {
            hint = "Monto (CLP)"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText("1500")
        }
        propina = EditText(this).apply {
            hint = "Propina"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText("0")
        }
        vuelto = EditText(this).apply {
            hint = "Vuelto (cashback)"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText("0")
        }
        documento = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, documentos)
        }
        imprimir = Switch(this).apply { text = "DTEx® imprime"; isChecked = true }
        respuesta = TextView(this).apply { setTextIsSelectable(true) }

        val cobrar = Button(this).apply {
            text = "Cobrar"
            setOnClickListener { cobrar(UUID.randomUUID().toString()) }
        }
        val reintentar = Button(this).apply {
            text = "Reintentar mismo request_id"
            setOnClickListener { ultimoRequestId?.let { cobrar(it) } }
        }
        val estado = Button(this).apply {
            text = "Consultar estado"
            setOnClickListener { consultarEstado() }
        }
        val despertar = Button(this).apply {
            text = "Despertar DTEx®"
            setOnClickListener {
                autoDespertar = true
                despertar()
            }
        }
        // En producción lo pone la app padre con el RUT del comercio que opera
        // el equipo.
        rutComercio = EditText(this).apply {
            hint = "RUT del comercio (12345678-9)"
        }
        val activar = Button(this).apply {
            text = "Activar equipo"
            setOnClickListener { activar() }
        }
        // Un botón por tipo: el selector de arriba también tiene none y voucher,
        // que no llevan folios.
        val foliosTicket = Button(this).apply {
            text = "Descargar folios ticket"
            setOnClickListener { descargarFolios("ticket") }
        }
        val folios = Button(this).apply {
            text = "Descargar folios boleta"
            setOnClickListener { descargarFolios("cl_sii_boleta") }
        }
        val foliosExenta = Button(this).apply {
            text = "Descargar folios boleta exenta"
            setOnClickListener { descargarFolios("cl_sii_boleta_exenta") }
        }
        val configuracion = Button(this).apply {
            text = "Configuración DTEx®"
            setOnClickListener { configuracion() }
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
            listOf(rutComercio, activar, monto, propina, vuelto, documento, imprimir, cobrar, reintentar, estado, despertar, foliosTicket, folios, foliosExenta, configuracion, respuesta).forEach { addView(it) }
        }
        setContentView(ScrollView(this).apply { addView(layout) })
    }

    /**
     * Mientras esta app está al frente queda conectada a DTEx®: Android no lo
     * cierra por memoria y el cobro no espera el arranque. Al conectar, DTEx®
     * dice si está listo; si no (recién arrancado o lo cerró Android), se
     * despierta ahora y no en el próximo cobro.
     */
    private val keepAlive = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (autoDespertar && !dtexListo(service)) despertar()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            if (autoDespertar) despertar()
        }
    }

    /**
     * Se apaga si un WAKE_UP no respondió READY (equipo sin activar, sin conexión):
     * cada vuelta a esta pantalla reconecta, y sin esto quedaría despertando en
     * ciclo. Se vuelve a encender con el botón Despertar.
     */
    private var autoDespertar = true

    private fun dtexListo(service: IBinder?): Boolean {
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        return try {
            service?.transact(IBinder.FIRST_CALL_TRANSACTION, data, reply, 0) == true && reply.readInt() == 1
        } catch (e: RemoteException) {
            false
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    override fun onStart() {
        super.onStart()
        val paquete = paqueteDtex() ?: return
        bindService(Intent("dev.mufin.dtex.action.KEEP_ALIVE").setPackage(paquete), keepAlive, BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        // Un cobro también detiene esta pantalla, pero ahí DTEx® está al frente
        // y no necesita la conexión.
        try {
            unbindService(keepAlive)
        } catch (e: IllegalArgumentException) {
            // No estaba conectado (DTEx® no instalado).
        }
    }

    private fun activar() {
        val paquete = paqueteDtex() ?: return
        val request = JSONObject()
            .put("version", 1)
            .put("commerce_rut", rutComercio.text.toString().trim())
        val intent = Intent("dev.mufin.dtex.action.ACTIVATE")
            .setPackage(paquete)
            .putExtra("request", request.toString())
        @Suppress("DEPRECATION")
        startActivityForResult(intent, REQ_DTEX)
    }

    private fun descargarFolios(documento: String) {
        val paquete = paqueteDtex() ?: return
        val request = JSONObject()
            .put("version", 1)
            .put("document", documento)
        val intent = Intent("dev.mufin.dtex.action.DOWNLOAD_FOLIOS")
            .setPackage(paquete)
            .putExtra("request", request.toString())
        @Suppress("DEPRECATION")
        startActivityForResult(intent, REQ_DTEX)
    }

    private fun configuracion() {
        val paquete = paqueteDtex() ?: return
        @Suppress("DEPRECATION")
        startActivityForResult(Intent("dev.mufin.dtex.action.SETTINGS").setPackage(paquete), REQ_DTEX)
    }

    private fun despertar() {
        val paquete = paqueteDtex() ?: return
        @Suppress("DEPRECATION")
        startActivityForResult(Intent("dev.mufin.dtex.action.WAKE_UP").setPackage(paquete), REQ_WAKE_UP)
    }

    private fun paqueteDtex(): String? = listOf("dev.mufin.dtex", "dev.mufin.dtex.tuu_pro2").firstOrNull {
        try {
            packageManager.getPackageInfo(it, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun cobrar(requestId: String) {
        val paquete = paqueteDtex()
        if (paquete == null) {
            respuesta.text = "DTEx® no está instalado"
            return
        }
        ultimoRequestId = requestId

        val request = JSONObject()
            .put("version", 1)
            .put("request_id", requestId)
            .put("amount", monto.text.toString().toLongOrNull() ?: 0)
            .put("tip", propina.text.toString().toLongOrNull() ?: 0)
            .put("cashback", vuelto.text.toString().toLongOrNull() ?: 0)
            .put("currency", "CLP")
            .put("document", documentos[documento.selectedItemPosition])
            .put("print", imprimir.isChecked)

        val intent = Intent("dev.mufin.dtex.action.PAYMENT")
            .setPackage(paquete)
            .putExtra("request", request.toString())
        @Suppress("DEPRECATION")
        startActivityForResult(intent, REQ_DTEX)
    }

    private fun consultarEstado() {
        val paquete = paqueteDtex() ?: return
        val requestId = ultimoRequestId ?: return
        val request = JSONObject().put("version", 1).put("request_id", requestId)
        val intent = Intent("dev.mufin.dtex.action.STATUS")
            .setPackage(paquete)
            .putExtra("request", request.toString())
        @Suppress("DEPRECATION")
        startActivityForResult(intent, REQ_DTEX)
    }

    @Deprecated("Ejemplo sin AndroidX a propósito")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_WAKE_UP) {
            autoDespertar = data?.getStringExtra("response")?.let { JSONObject(it).optString("status") } == "READY"
        } else if (requestCode != REQ_DTEX) {
            return
        }

        val json = data?.getStringExtra("response")
        Log.i("App2AppDemo", "resultCode=$resultCode response=$json")
        respuesta.text = if (json == null) {
            // DTEx® murió a mitad de camino: no se sabe si cobró. La consulta de
            // estado dice en qué etapa quedó.
            "Sin respuesta (resultCode=$resultCode). Usa «Consultar estado»."
        } else {
            JSONObject(json).toString(2)
        }
    }

    private companion object {
        const val REQ_DTEX = 4242
        const val REQ_WAKE_UP = 4243
    }
}
