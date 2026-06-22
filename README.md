# THE LAST DICTAMEN 🎮

**The Last Dictamen** es una aplicación Android multijuego con temática politécnica (ESCOM / IPN). Reúne cuatro minijuegos originales, cada uno con su propio backend independiente para el registro de puntuaciones y rankings globales.

---

## 🎮 Juegos

### 1. Code Slasher 🗡️
Versión del clásico Fruit Ninja ambientada en un laboratorio de programación. Corta los bugs, errores y nulos para ganar puntos; evita las bombas y aprovecha el café y las credenciales IPN.

**Modos de juego:**

| Modo | Descripción | Tiempo | Vidas |
|---|---|---|---|
| Clásico | Supervivencia pura — se pierde al fallar 3 cortes | Sin límite | 3 |
| Salvar el Semestre | Contrarreloj de 60 s con bonus por combos | 60 s | 3 |
| Relax | Práctica libre sin penalización por caídas | 90 s | ∞ |

**Elementos en pantalla:** Bugs, Errores, Nulos (cortar = puntos), Café / IPN Card (bonus), Bombas (penalización).

---

### 2. La Raza Run 🏃
Runner infinito ambientado en el Transbordo de la Ciencia del IPN. El jugador debe recorrer 600 m antes de que se agoten los 90 segundos esquivando obstáculos con salto o deslizamiento.

**Obstáculos:** Carreto, Mochila, Charco.  
**Acciones del jugador:** RUN · JUMP · SLIDE · CRASH · WIN.  
**Dificultad progresiva:** la velocidad aumenta 3 % cada 100 m recorridos.  
**Resultados posibles:** `VICTORY`, `CRASHED`, `TIME_OUT`.

---

### 3. The Last Dictamen 📜
Juego de atrapa-objetos: sobrevive las 18 semanas en la ESCOM recolectando café y útiles mientras evitas las materias y ETS que caen del cielo. Incluye un sistema de preguntas de trivia que puede salvarte una vida.

**Dificultades:**

| Dificultad | Label | Descripción |
|---|---|---|
| EASY | 1er Semestre | Pocas tareas, más café |
| MEDIUM | Medio Semestre | Más reportes y menos tiempo |
| HARD | Semestre Final | Lluvia de ETS y departamentales |

---

### 4. Code Merge 💻
Inspirado en Suika Game: suelta elementos de código que caen y se fusionan al toparse dos del mismo nivel, subiendo de categoría hasta compilar el proyecto final.

**Jerarquía de elementos:**

| Nivel | Elemento | Puntos |
|---|---|---|
| 1 | NULO | 10 |
| 2 | BUG | 20 |
| 3 | ERROR | 30 |
| 4 | CAFÉ | 50 |
| 5 | IPN CARD | 100 |
| 6 | PROJECT COMPLETE | 500 |

Motor de física propio con gravedad, rebote y fricción. Se gana al alcanzar PROJECT COMPLETE; se pierde si los elementos superan la línea límite superior.

---

##  Arquitectura del Proyecto

```
THE LAST DICTAMEN
├── Frontend  →  Android (Kotlin + Jetpack Compose)
└── Backend   →  4 microservicios FastAPI + MySQL 8.0 + Docker
```

### Frontend — Patrones por juego

| Juego | Patrón principal |
|---|---|
| Code Slasher | GameEngine + Canvas + Repository |
| La Raza Run | GameEngine + Canvas |
| The Last Dictamen | Controller + Canvas + Trivia system |
| Code Merge | ViewModel (MVI) + Physics Engine |

### Backend — Microservicios

| Servicio | Puerto | Base de datos | Endpoints principales |
|---|---|---|---|
| `api_codeslasher` | 8000 | `codeslasher` | `POST /scores/` · `GET /rankings/` |
| `api_razarun` | 8001 | `razarun` | `POST /results/` · `GET /rankings/` |
| `api_codemerge` | 8002 | `codemerge` | `POST /scores/` · `GET /highscores/` |
| `api_lastdictamen` | 8003 | `lastdictamen` | `POST /scores/` · `GET /rankings/?difficulty=` |

---

##  Tecnologías

### Frontend (Android)
- **Lenguaje:** Kotlin 1.9+
- **UI:** Jetpack Compose + Canvas API
- **Red:** Retrofit 2 / OkHttp
- **Arquitectura:** Repository Pattern · MVI · ViewModel

### Backend (Python)
- **Framework:** FastAPI
- **ORM:** SQLAlchemy 2.x
- **Base de datos:** MySQL 8.0
- **Servidor:** Uvicorn
- **Infraestructura:** Docker / Docker Compose

---

## 📂 Estructura del Proyecto

```
PROYECTO-FINAL/
├── app/
│   └── src/main/java/com/android/mobile/games/app/
│       ├── MainActivity.kt
│       ├── navigation/
│       │   ├── AppRoute.kt
│       │   └── AppNavigation.kt
│       ├── ui/
│       │   ├── screens/MainMenuScreen.kt
│       │   ├── theme/
│       │   └── util/ImmersiveMode.kt
│       └── games/
│           ├── fruitninja/          # Code Slasher
│           │   ├── assets/
│           │   ├── data/            # GameService, RetrofitGameService
│           │   ├── engine/          # FruitNinjaGameEngine
│           │   ├── model/           # GameState, Item, Effect, Difficulty
│           │   ├── ui/              # Canvas, HUD, Screens, GameOverPanel
│           │   └── util/
│           ├── razarun/             # La Raza Run
│           │   ├── data/            # RazaGameService, RetrofitRazaService
│           │   ├── engine/          # RazaGameEngine
│           │   ├── model/           # RazaGameState, Obstacle
│           │   ├── ui/              # RazaCanvas, RazaHud, RazaScreen
│           │   └── util/
│           ├── catchgame/           # The Last Dictamen
│           │   ├── config/
│           │   ├── data/            # ICatchGameService, RetrofitCatchGameService, TriviaRepository
│           │   ├── engine/          # CatchGameController
│           │   ├── model/           # UiState, Item, Difficulty, TriviaQuestion
│           │   └── ui/              # Canvas, HUD, Screens, TriviaDialog
│           └── codemerge/           # Code Merge
│               ├── data/            # ICodeMergeGameService, RetrofitCodeMergeService
│               ├── engine/          # CodeMergeViewModel (MVI)
│               ├── model/           # GameState, CodeElement, CodeLevel, MergeRunResult
│               └── ui/              # CodeMergeScreen
├── backend/
│   ├── codeslasher/                 # API Code Slasher — puerto 8000
│   │   ├── app/ (main, models, schemas, crud, database)
│   │   ├── Dockerfile
│   │   └── requirements.txt
│   ├── razarun/                     # API La Raza Run — puerto 8001
│   │   ├── app/ (main, models, schemas, crud, database)
│   │   ├── Dockerfile
│   │   └── requirements.txt
│   ├── codemerge/                   # API Code Merge — puerto 8002
│   │   ├── app/ (main, models, schemas, crud, database)
│   │   ├── Dockerfile
│   │   └── requirements.txt
│   ├── lastdictamen/                # API The Last Dictamen — puerto 8003
│   │   ├── app/ (main, models, schemas, crud, database)
│   │   ├── Dockerfile
│   │   └── requirements.txt
│   ├── mysql/
│   │   └── init.sql                 # Crea las 4 bases de datos
│   ├── docker-compose.yml           # Orquesta los 5 servicios (db + 4 APIs)
│   └── .env                         # Variables de entorno globales
└── README.md
```

---

##  Instalación y Configuración

### Backend

1. Navegar a la carpeta `backend/`.
2. Levantar todos los servicios con Docker Compose:
   ```bash
   docker-compose up --build
   ```
3. Los servicios quedarán disponibles en:
   - Code Slasher API → `http://localhost:8000`
   - La Raza Run API → `http://localhost:8001`
   - Code Merge API → `http://localhost:8002`
   - The Last Dictamen API → `http://localhost:8003`

> El script `mysql/init.sql` crea automáticamente las cuatro bases de datos al primer arranque del contenedor MySQL.

### Frontend (Android)

1. Abrir el proyecto en **Android Studio**.
2. Sincronizar Gradle.
3. Ajustar la constante `BASE_URL` en cada cliente Retrofit si se prueba en dispositivo físico (por defecto apunta a la IP de WSL `172.22.80.1`):

| Archivo | Puerto |
|---|---|
| `fruitninja/data/RetrofitGameService.kt` | 8000 |
| `razarun/data/RetrofitRazaService.kt` | 8001 |
| `codemerge/data/RetrofitCodeMergeService.kt` | 8002 |
| `catchgame/data/RetrofitCatchGameService.kt` | 8003 |

4. Ejecutar en un emulador o dispositivo físico (**API 26+ recomendado**).

---

##  API Reference

### Code Slasher (`:8000`)
```
POST /scores/        body: { username, score, difficulty }
GET  /rankings/      query: ?limit=10
```

### La Raza Run (`:8001`)
```
POST /results/       body: { name, distance, status }
GET  /rankings/      query: ?limit=10
```

### Code Merge (`:8002`)
```
POST /scores/        body: { player_name, score }
GET  /highscores/    query: ?limit=10
```

### The Last Dictamen (`:8003`)
```
POST /scores/        body: { username, score, difficulty }
GET  /rankings/      query: ?difficulty=EASY|MEDIUM|HARD&limit=10
```

---

Desarrollado como proyecto final de Programación Móvil — ESCOM, IPN. 💻📱
