<p align="center">
  <a href="https://www.dtex.cl">
    <img src=".github/dtex-icon.png" alt="DTEx®" width="96" height="96">
  </a>
</p>

<h1 align="center">DTEx® App2App demo</h1>

<p align="center">
  <strong>Convierte tu negocio en un punto de pagos y servicios.</strong><br>
  Cliente de referencia para que tu app Android cobre con tarjeta a través de DTEx®.
</p>

<p align="center">
  <a href="https://www.dtex.cl/developers/app2app"><img alt="Contrato App2App" src="https://img.shields.io/badge/contrato-App2App%20v1-1f55bd?style=flat-square"></a>
  <img alt="Android 6+" src="https://img.shields.io/badge/Android-6.0%2B-0a7b69?style=flat-square&logo=android&logoColor=white">
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-2.1-143f95?style=flat-square&logo=kotlin&logoColor=white">
  <a href="LICENSE"><img alt="Licencia MIT" src="https://img.shields.io/badge/licencia-MIT-0d2e72?style=flat-square"></a>
  <a href="https://github.com/morello-cl/dtex_app2app_demo/pulls"><img alt="PR bienvenidos" src="https://img.shields.io/badge/PR-bienvenidos-3ec5ad?style=flat-square"></a>
</p>

<p align="center">
  <a href="https://www.dtex.cl/developers/app2app"><b>Documentación</b></a> ·
  <a href="https://www.dtex.cl"><b>dtex.cl</b></a> ·
  <a href="#contribuir"><b>Contribuir</b></a>
</p>

---

## ¿Qué es App2App?

DTEx® convierte un terminal Android en un punto de pagos: cobro con tarjeta, boleta y ticket electrónico, todo desde un solo equipo. Con **App2App**, tu app (ERP, restaurante, caja, ISV) instalada en el mismo terminal le pide el cobro a DTEx® y recibe el resultado, sin integrar adquirentes ni emisión de documentos por tu cuenta.

```text
 Tu app ──PAYMENT──▶ DTEx® ──▶ Adquirente (cobro con tarjeta)
    ▲                  │
    └────respuesta─────┘   APPROVED · DECLINED · CANCELED · ERROR
```

Esta demo implementa el contrato completo en un solo archivo, [`MainActivity.kt`](app/src/main/java/cl/mufin/app2app/demo/MainActivity.kt), sin AndroidX: queda solo lo que necesita cualquier integrador.

## Qué incluye

| Acción | Para qué sirve |
|---|---|
| **Cobrar** · `PAYMENT` | Monto, propina, vuelto y documento (`ticket`, `none`, `cl_sii_boleta`, `cl_sii_boleta_exenta`), con o sin impresión. |
| **Reintentar** · mismo `request_id` | DTEx® devuelve el resultado guardado y no vuelve a cobrar. |
| **Consultar estado** · `STATUS` | En qué etapa quedó un cobro, aunque tu app haya muerto mientras cobraba. |
| **Despertar** · `WAKE_UP` | Deja DTEx® listo antes del cobro. |
| **Mantener listo** · `KEEP_ALIVE` | Conexión en `onStart`/`onStop` para que Android no cierre DTEx®. |
| **Activar equipo** · `ACTIVATE` | Activación de emergencia con el RUT del comercio. |
| **Descargar folios** · `DOWNLOAD_FOLIOS` | Ticket, boleta o boleta exenta, sin traer DTEx® al frente. |
| **Configuración** · `SETTINGS` | Abre la configuración de DTEx® para el operador. |

## Empezar

**Requisitos:** un terminal Android con DTEx® instalado (Sunmi, PAX, TUU y otros) y JDK 17.

```bash
git clone https://github.com/morello-cl/dtex_app2app_demo.git
cd dtex_app2app_demo
./gradlew installDebug
```

1. Abre **App2App demo** en el terminal.
2. Elige el documento y presiona **Cobrar**. La respuesta JSON aparece al final de la pantalla.

> [!IMPORTANT]
> DTEx® solo atiende apps autorizadas en cada equipo. El paquete de esta demo (`cl.mufin.app2app.demo`) está autorizado únicamente en equipos de prueba de Mufin. Para tu app, cambia `applicationId` y solicita la autorización según la [documentación](https://www.dtex.cl/developers/app2app).

## Contribuir

**Los pull requests son bienvenidos.** Si encuentras un error o tienes una idea para que la demo sea más clara o más útil para otros integradores, adelante:

1. Haz un fork y crea una rama desde `main`.
2. Mantén el espíritu del ejemplo: un solo archivo, sin AndroidX ni dependencias nuevas, y código fácil de leer.
3. Comprueba que compila (`./gradlew assembleDebug`) y, si puedes, pruébalo en un terminal con DTEx®.
4. Abre el PR y cuenta qué cambia y por qué.

¿Dudas sobre el contrato o un comportamiento de DTEx®? Abre un [issue](https://github.com/morello-cl/dtex_app2app_demo/issues).

## Licencia

[MIT](LICENSE) © Mufin SpA. DTEx® es marca registrada de Mufin SpA.
