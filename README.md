# DTEx® App2App demo

Cliente mínimo del contrato **App2App de DTEx®**: una app Android que, instalada en el mismo terminal que DTEx®, le pide cobros con tarjeta y recibe el resultado. Sirve para probar la integración y como referencia de código.

Contrato completo: **https://www.dtex.cl/developers/app2app**

No usa AndroidX a propósito: el ejemplo se limita a lo que necesita cualquier integrador. Todo está en [`MainActivity.kt`](app/src/main/java/cl/mufin/app2app/demo/MainActivity.kt).

## Qué muestra

- **Cobrar** (`PAYMENT`): monto, propina, vuelto, documento (`ticket`, `none`, `voucher`, `cl_sii_boleta`, `cl_sii_boleta_exenta`) y si DTEx® imprime.
- **Reintentar mismo request_id**: DTEx® devuelve el resultado guardado y no vuelve a cobrar.
- **Consultar estado** (`STATUS`): en qué etapa quedó el último `request_id`, aunque esta app haya muerto durante el cobro.
- **Despertar DTEx®** (`WAKE_UP`) y la conexión `KEEP_ALIVE` en `onStart`/`onStop`.
- **Activar equipo** (`ACTIVATE`), **Descargar folios** (`DOWNLOAD_FOLIOS`) y **Configuración DTEx®** (`SETTINGS`).

## Probarlo

1. Compila e instala en el terminal donde está DTEx®:
   ```bash
   ./gradlew installDebug
   ```
2. DTEx® solo atiende paquetes autorizados en el equipo. El paquete de esta demo (`cl.mufin.app2app.demo`) está autorizado únicamente en equipos de prueba de Mufin. Para tu app, cambia `applicationId` y solicita la autorización a Mufin (ver la documentación).
3. Elige el documento y presiona **Cobrar**. La respuesta JSON aparece al final de la pantalla.

## Licencia

MIT. DTEx® es marca registrada de Mufin SpA.
