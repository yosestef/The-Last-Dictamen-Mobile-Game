Prompt: Arquitectura de Identidad Global y Autenticación Híbrida (Play Store Ready)
Misión: Implementar un sistema de gestión de identidad centralizado que elimine los formularios manuales de "Nombre de Programador" en los juegos individuales y prepare la app para la Google Play Store mediante un flujo de inicio de sesión moderno (Google, Correo o Anónimo).
1. Capa de Identidad y Persistencia (IdentityManager)
Crea un componente singleton que gestione la sesión global del usuario:
•
Tecnología: Utiliza Jetpack DataStore para persistir el programmerId y el authProvider.
•
Enum de Proveedores: Define AuthProvider { ANONIMO, GOOGLE, CORREO, NINGUNO }.
•
Lógica de Identidad Anónima: Si el usuario elige "Continuar sin iniciar sesión", genera automáticamente un ID con el formato PROG-XXXX (donde X son caracteres alfanuméricos aleatorios) y establece el proveedor como ANONIMO.
•
Contratos para el Futuro:
◦
linkAccount(provider: AuthProvider): Placeholder para vincular la cuenta anónima con una real (Google/Email).
◦
syncWithCloud(): Función que prepare la migración de datos de MySQL (Retrofit) a Firebase Firestore.
2. Flujo de Navegación y UI de Bienvenida (AuthScreen)
Rediseña el inicio de la aplicación en AppNavigation.kt:
•
Destino Inicial: Una nueva AuthScreen con estética Cyber-Hacker.
•
Lógica de Verificación: Al abrir, la app debe comprobar si ya existe un ID en DataStore.
◦
Si existe: Navegar directamente al MainMenuScreen tras un Splash de branding de 1.5 segundos.
◦
Si NO existe: Mostrar opciones de inicio de sesión:
a.
Botón "[ CONTINUAR COMO INVITADO ]" -> Genera el ID PROG-XXXX.
b.
Botón "GOOGLE SIGN-IN" (Placeholder visual).
c.
Botón "CORREO ELECTRÓNICO" (Placeholder visual).
•
Feedback Visual: Al generar el ID anónimo, muestra una animación de terminal: "Asignando Credenciales de Acceso... ID: [ID] GENERADO".
3. Refactorización Integral de Juegos (Limpieza de Regresiones)
Elimina la redundancia de datos en los menús de juego existentes:
•
Limpieza de UI: Remueve definitivamente los TextField y validaciones de nombre en:
◦
FruitNinjaMenuScreen.kt (Code Slasher)
◦
CatchGameMenuScreen.kt (The Last Dictamen)
•
Inyección Automática: Modifica los ViewModels y Servicios de todos los juegos (RetrofitGameService, CatchGameService, etc.) para que obtengan el ID directamente desde el IdentityManager global. El usuario ya no debe escribir su nombre para jugar o registrar su score.
•
Menú Principal: Muestra el ID activo en una esquina superior con la etiqueta: SESSION_ID: [ID].
4. Estándares de QA y Buenas Prácticas
•
Flujo Unidireccional: La UI debe observar el estado de la sesión mediante un StateFlow.
•
Inmutabilidad: Una vez generado o asignado el ID, este no debe poder editarse manualmente dentro de los juegos.
•
Preparación para Firebase: Asegura que el código sea modular para que, al añadir google-services.json, solo sea necesario cambiar la implementación de linkAccount sin tocar la lógica de los juegos.
•
Logs de Depuración: Añade un log: [QA-AUTH]: Active Identity initialized as [ID] via [PROVIDER]

Evitar regeneración accidental: "Asegurar que la lógica de generación aleatoria verifique primero que el ID no exista en DataStore, evitando que se sobreescriba un ID ya existente en el dispositivo al reiniciar la app."

Inyección en la API actual: "El ID debe enviarse en el cuerpo o los headers de las peticiones de Retrofit actuales a MySQL en sustitución del antiguo campo 'name'."

Asegurate de integrarlo correctamente al proyecto, haz el proceso de QA lo mejor que puedas, integralo bien con el sistema actual, sin que se rompa nada de lo que ya funciona, si captas alguna inconsistencia o algo que rompa el proyecto avisame