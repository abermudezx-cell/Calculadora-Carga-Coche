# Tesla Carga (Android)

Aplicación Android nativa en español y tema oscuro para estimar la carga doméstica monofásica de un Tesla Model 3 RWD.

## Qué hace
- Introducir el porcentaje de batería actual.
- Elegir amperios con el deslizador (1–26 A) o escribir el valor manualmente.
- Estimar el tiempo hasta el 80 % y el 100 %, la energía aproximada y la hora de finalización.
- Funciona sin conexión y no solicita permisos ni datos personales.

## Supuestos de cálculo
- 230 V monofásicos.
- 60 kWh de capacidad útil estimada.
- 90 % de eficiencia de carga.
- Pequeño margen estimado en el cálculo hasta el 100 %.

Son estimaciones; la capacidad exacta y el tiempo real pueden variar según la versión concreta del vehículo, temperatura y condiciones de carga.

## Generar el APK
Este entorno de trabajo no dispone de Android SDK/Gradle ni de acceso a Internet para descargar las herramientas, por lo que no he podido compilar ni verificar aquí un APK instalable.

La forma más sencilla es subir esta carpeta a un repositorio de GitHub y ejecutar la acción **Construir APK Android** desde la pestaña **Actions**. El archivo `app-debug.apk` aparecerá como artefacto descargable `Tesla-Carga-APK`. También puedes abrir el proyecto en Android Studio y compilarlo con **Build > Build APK(s)**.
