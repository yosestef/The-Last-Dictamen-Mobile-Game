# Guía de Configuración Firebase — The Last Dictamen Arcade

> **Para el profesor:** Esta guía contiene todo lo necesario para conectar la app a su cuenta de Firebase.
> El proyecto ya está integrado y listo — solo se requiere colocar un archivo de configuración.
> Sin ese archivo, la app sigue funcionando normalmente con el servidor REST local.

---

## Índice

1. [¿Qué hace Firebase en esta app?](#1-qué-hace-firebase-en-esta-app)
2. [Requisitos previos](#2-requisitos-previos)
3. [Paso 1 — Crear el proyecto en Firebase Console](#3-paso-1--crear-el-proyecto-en-firebase-console)
4. [Paso 2 — Registrar la app Android](#4-paso-2--registrar-la-app-android)
5. [Paso 3 — Habilitar Authentication](#5-paso-3--habilitar-authentication)
6. [Paso 4 — Crear la base de datos Firestore](#6-paso-4--crear-la-base-de-datos-firestore)
7. [Paso 5 — Colocar el archivo google-services.json](#7-paso-5--colocar-el-archivo-google-servicesjson)
8. [Paso 6 — Compilar y verificar](#8-paso-6--compilar-y-verificar)
9. [Estructura de datos en Firestore](#9-estructura-de-datos-en-firestore)
10. [Reglas de seguridad sugeridas](#10-reglas-de-seguridad-sugeridas)
11. [Solución de problemas](#11-solución-de-problemas)

---

## 1. ¿Qué hace Firebase en esta app?

La app usa dos servicios de Firebase de forma **automática y silenciosa**:

| Servicio | Función |
|----------|---------|
| **Firebase Authentication** | Asigna un UID anónimo a cada dispositivo al iniciar sesión por primera vez. El usuario nunca ve ni interactúa con este proceso. |
| **Cloud Firestore** | Guarda una copia de cada puntaje en la nube, además del servidor REST local. Se sincroniza automáticamente al terminar cada partida. |

**Arquitectura de identidad:**

```
Dispositivo
    │
    ├── PROG-XXXXXX  →  servidor REST  (evaluación de puntajes)
    │   (ID visible en rankings)
    │
    └── Firebase UID  →  Firestore  (respaldo en la nube)
        (interno, nunca se muestra al usuario)
```

El `PROG-XXXXXX` es el identificador que aparece en los rankings del juego. El Firebase UID es solo la credencial de autenticación para Firestore — ambos coexisten sin interferirse.

---

## 2. Requisitos previos

- Cuenta de Google (cualquier cuenta Gmail sirve)
- Acceso a [Firebase Console](https://console.firebase.google.com/)
- Android Studio con el proyecto abierto
- El proyecto compilando correctamente (sin el JSON, ya compila con un placeholder)

---

## 3. Paso 1 — Crear el proyecto en Firebase Console

1. Ir a [console.firebase.google.com](https://console.firebase.google.com/)
2. Hacer clic en **"Agregar proyecto"**
3. Nombrar el proyecto (por ejemplo: `TLD-Arcade` o `the-last-dictamen`)
4. En la pantalla de Google Analytics: puede habilitarse o deshabilitarse — **no afecta la integración**
5. Hacer clic en **"Crear proyecto"** y esperar a que termine

---

## 4. Paso 2 — Registrar la app Android

Una vez creado el proyecto:

1. En la pantalla de inicio del proyecto, hacer clic en el ícono de Android **`</>`**
2. Completar el formulario con **exactamente** estos datos:

   | Campo | Valor |
   |-------|-------|
   | **Nombre del paquete Android** | `com.android.mobile.games.app` |
   | **Nombre de la app (opcional)** | The Last Dictamen Arcade |
   | **SHA-1 (opcional)** | Dejar vacío por ahora |

   > ⚠️ El nombre del paquete debe ser exactamente `com.android.mobile.games.app` (respetando mayúsculas y puntos). Si no coincide, Firebase no reconocerá la app.

3. Hacer clic en **"Registrar app"**
4. En la siguiente pantalla aparecerá el botón **"Descargar google-services.json"** — **descargarlo ahora**, se usará en el Paso 5
5. Continuar haciendo clic en **"Siguiente"** en los pasos restantes del asistente (las instrucciones de Gradle ya están aplicadas en el proyecto)
6. Finalizar con **"Ir a la consola"**

---

## 5. Paso 3 — Habilitar Authentication

1. En el menú lateral de Firebase Console, ir a **"Compilación" → "Authentication"**
2. Hacer clic en **"Comenzar"**
3. En la pestaña **"Método de acceso"**, buscar **"Anónimo"**
4. Hacer clic en **"Anónimo"** y activar el interruptor **"Habilitar"**
5. Guardar

> Esto permite que la app inicie sesión automáticamente sin pedirle credenciales al usuario. Cada dispositivo recibe un UID único y persistente.

---

## 6. Paso 4 — Crear la base de datos Firestore

1. En el menú lateral, ir a **"Compilación" → "Firestore Database"**
2. Hacer clic en **"Crear base de datos"**
3. Seleccionar la **ubicación** del servidor:
   - Para pruebas en México/LATAM: `us-central1` es suficiente
4. Seleccionar el **modo de inicio**:
   - **Modo de prueba** (recomendado para evaluación): permite lectura y escritura sin restricciones durante 30 días
   - Modo de producción: requiere configurar reglas de seguridad (ver [Sección 10](#10-reglas-de-seguridad-sugeridas))
5. Hacer clic en **"Crear"**

> En modo de prueba no se necesita ninguna configuración adicional. Los documentos se crearán automáticamente cuando los alumnos jueguen.

---

## 7. Paso 5 — Colocar el archivo google-services.json

Este es el único cambio que se hace al código del proyecto:

1. Localizar el archivo `google-services.json` descargado en el Paso 2
2. Copiarlo a la carpeta `/app` del proyecto:

```
The-Last-Dictamen-Mobile-Game/
├── app/
│   ├── google-services.json   ← COLOCAR AQUÍ (reemplaza el placeholder)
│   ├── src/
│   └── build.gradle.kts
├── build.gradle.kts
└── ...
```

3. En Android Studio, hacer clic en **"Sync Now"** si aparece la notificación, o ir a **File → Sync Project with Gradle Files**

> El proyecto ya tiene un `google-services.json` de placeholder que permite compilar sin Firebase. Al reemplazarlo con el real, Firebase se activa automáticamente en la próxima compilación.

---

## 8. Paso 6 — Compilar y verificar

1. Compilar y ejecutar la app en un dispositivo o emulador
2. Jugar cualquier partida hasta el final (Game Over)
3. En Android Studio, abrir **Logcat** y filtrar por el tag `QA-FIREBASE`

**Salida esperada en Logcat:**

```
D/QA-FIREBASE: Identidad generada: PROG-A1B2C3 via ANONIMO
D/QA-FIREBASE: Firebase Auth: inicio anónimo OK uid=WxYzAbCd...
D/QA-FIREBASE: CatchGame: enviando puntaje — id=PROG-A1B2C3 score=840 difficulty=MEDIUM
D/QA-FIREBASE: Firestore sync OK: collection=scores_catch programmerId=PROG-A1B2C3 score=840
```

**Verificación en Firebase Console:**

1. Ir a **Authentication → Usuarios**: debe aparecer un usuario con tipo "Anónimo"
2. Ir a **Firestore Database → Datos**: deben aparecer las colecciones `scores_catch` y/o `scores_slasher` con los documentos de puntaje

---

## 9. Estructura de datos en Firestore

Cada partida terminada genera automáticamente un documento en la colección correspondiente:

### Colección `scores_catch` — The Last Dictamen

```
scores_catch/
└── {documento_autogenerado}/
    ├── programmerId : "PROG-A1B2C3"   ← ID visible en rankings
    ├── uid          : "WxYzAbCd..."   ← Firebase UID interno
    ├── score        : 840
    ├── difficulty   : "MEDIUM"
    └── timestamp    : 1719270000000   ← Unix ms
```

### Colección `scores_slasher` — Code Slasher

```
scores_slasher/
└── {documento_autogenerado}/
    ├── programmerId : "PROG-X9Y8Z7"
    ├── uid          : "AbCdWxYz..."
    ├── score        : 1250
    ├── difficulty   : "CLASSIC"
    └── timestamp    : 1719270060000
```

---

## 10. Reglas de seguridad sugeridas

Si se configura Firestore en **modo de producción** (no modo de prueba), aplicar estas reglas en **Firestore → Reglas**:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Solo usuarios autenticados (incluso anónimos) pueden escribir puntajes
    match /scores_catch/{docId} {
      allow read: if true;
      allow create: if request.auth != null
                    && request.resource.data.uid == request.auth.uid;
      allow update, delete: if false;
    }

    match /scores_slasher/{docId} {
      allow read: if true;
      allow create: if request.auth != null
                    && request.resource.data.uid == request.auth.uid;
      allow update, delete: if false;
    }
  }
}
```

> Estas reglas garantizan que cada usuario solo pueda crear documentos con su propio UID, nadie puede modificar o borrar puntajes ajenos, y los rankings son de lectura pública.

---

## 11. Solución de problemas

### La app no guarda en Firestore pero tampoco crashea

Comportamiento esperado si el `google-services.json` es el placeholder o tiene credenciales incorrectas. Verificar en Logcat:

```
D/QA-FIREBASE: Firebase Auth: no disponible (sin google-services.json?): ...
D/QA-FIREBASE: Firebase Firestore: no disponible: ...
```

**Solución:** Confirmar que el `google-services.json` en `/app` fue descargado del proyecto correcto y que el `package_name` dentro del JSON sea `com.android.mobile.games.app`.

### Error al compilar después de colocar el JSON

Verificar que el archivo esté directamente en `/app/`, no dentro de una subcarpeta.
Luego: **File → Sync Project with Gradle Files**.

### No aparecen usuarios en Firebase Authentication

La sesión anónima se crea la primera vez que el usuario pulsa "Continuar como Invitado". Ejecutar ese flujo completo antes de revisar la consola.

### Los documentos en Firestore no aparecen en tiempo real

Firestore Console puede tardar unos segundos en actualizar. Recargar la página de la consola o navegar a la colección manualmente.

---

*Generado para The Last Dictamen Arcade — ESCOM IPN*
