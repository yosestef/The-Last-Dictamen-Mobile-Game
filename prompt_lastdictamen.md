# PROMPT PARA AGENTE: Backend The Last Dictamen (CatchGame)
## Proyecto: PROYECTO-FINAL — Android Multijuego

---

## 🎯 MISIÓN

Implementar el backend **FastAPI + MySQL + Docker** para el juego **The Last Dictamen** (paquete `catchgame`), integrarlo al `docker-compose.yml` unificado ya existente, y agregar el cliente Retrofit en Android.

Este juego es el más delicado porque **actualmente no tiene NINGUNA capa de red** — solo almacenamiento local con DataStore. La integración debe ser quirúrgica: agregar lo nuevo sin tocar nada de lo que ya funciona.

---

## 📋 CONTEXTO CRÍTICO — Lee esto antes de tocar cualquier archivo

### Lo que YA existe y funciona (NO TOCAR)
```
catchgame/
  config/CatchGameConfig.kt         ← NO TOCAR
  data/CatchGameScoreRepository.kt  ← NO TOCAR (DataStore local — sigue funcionando)
  data/TriviaJsonLoader.kt          ← NO TOCAR
  data/TriviaRepository.kt          ← NO TOCAR
  engine/CatchGameController.kt     ← NO TOCAR
  model/CatchGameDifficulty.kt      ← NO TOCAR
  model/CatchGameItem.kt            ← NO TOCAR
  model/CatchGameUiState.kt         ← NO TOCAR
  model/TriviaCategory.kt           ← NO TOCAR
  model/TriviaQuestion.kt           ← NO TOCAR
  ui/CatchGameHud.kt                ← NO TOCAR
  ui/CatchTriviaDialog.kt           ← NO TOCAR
```

### Archivos que SÍ se editarán (mínimo indispensable)
```
catchgame/ui/CatchGameScreen.kt     ← Agregar llamada al servicio en Game Over
catchgame/ui/CatchGameMenuScreen.kt ← Agregar botón de ranking
navigation/AppNavigation.kt         ← Actualizar instanciación del servicio
backend/docker-compose.yml          ← Agregar servicio api_lastdictamen
backend/.env                        ← Agregar variable LASTDICTAMEN_DB_URL
```

### Archivos nuevos a crear
```
catchgame/data/ICatchGameService.kt             ← Interfaz del servicio de red
catchgame/data/CatchGameApiService.kt           ← Interfaz Retrofit
catchgame/data/RetrofitCatchGameService.kt      ← Implementación real con Retrofit
backend/lastdictamen/Dockerfile
backend/lastdictamen/requirements.txt
backend/lastdictamen/app/__init__.py
backend/lastdictamen/app/main.py
backend/lastdictamen/app/database.py
backend/lastdictamen/app/models.py
backend/lastdictamen/app/schemas.py
backend/lastdictamen/app/crud.py
```

---

## 🔍 Datos de dominio del juego

### Dificultades (enum ya existente en Android):
- `EASY` → label "1er Semestre"
- `MEDIUM` → label "Medio Semestre"
- `HARD` → label "Semestre Final"

### Qué se guarda al terminar una partida:
- Nombre del jugador (username — debe pedirse en el menú)
- Puntuación (Int: score)
- Dificultad (String: "EASY", "MEDIUM", "HARD")
- Timestamp (auto-generado por el servidor)

### Flujo de juego actual (importante entender para no romper nada):
1. `CatchGameMenuScreen` → selecciona dificultad → presiona "✨ ¡A clases! ✨"
2. `CatchGameScreen` → juega → `isGameOver = true`
3. Al `isGameOver`: `CatchGameScoreRepository.saveBestScoreIfNeeded()` guarda localmente
4. `CatchGameOverPanel` aparece con botones "Reinscripción" y "Ir a Dirección"

**La integración con el backend se hará en el punto 3, SIN reemplazar el guardado local.**

---

## 📋 PASO 1: Backend Python — The Last Dictamen (puerto 8003)

### 1.1 Dockerfile

**Crear:** `backend\lastdictamen\Dockerfile`

```dockerfile
FROM python:3.11-slim

WORKDIR /app

RUN apt-get update && apt-get install -y \
    build-essential \
    libmariadb-dev \
    pkg-config \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
```

### 1.2 Requirements

**Crear:** `backend\lastdictamen\requirements.txt`

```
fastapi
uvicorn[standard]
sqlalchemy
pymysql
pydantic
python-dotenv
cryptography
```

### 1.3 `__init__.py`

**Crear:** `backend\lastdictamen\app\__init__.py`

```python
```
(archivo vacío — obligatorio para que los imports relativos funcionen)

### 1.4 Database

**Crear:** `backend\lastdictamen\app\database.py`

```python
import os
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, declarative_base
from dotenv import load_dotenv

load_dotenv()

SQLALCHEMY_DATABASE_URL = os.getenv(
    "DATABASE_URL", "mysql+pymysql://user:password@localhost:3306/lastdictamen"
)

engine = create_engine(SQLALCHEMY_DATABASE_URL)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
```

### 1.5 Models

**Crear:** `backend\lastdictamen\app\models.py`

```python
from sqlalchemy import Column, Integer, String, DateTime, ForeignKey
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from .database import Base


class Player(Base):
    __tablename__ = "players"

    id = Column(Integer, primary_key=True, index=True)
    username = Column(String(50), unique=True, index=True, nullable=False)

    scores = relationship("Score", back_populates="player")


class Score(Base):
    __tablename__ = "scores"

    id = Column(Integer, primary_key=True, index=True)
    player_id = Column(Integer, ForeignKey("players.id"), nullable=False)
    score = Column(Integer, nullable=False)
    difficulty = Column(String(10), nullable=False)  # EASY | MEDIUM | HARD
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    player = relationship("Player", back_populates="scores")
```

### 1.6 Schemas

**Crear:** `backend\lastdictamen\app\schemas.py`

```python
from pydantic import BaseModel, field_validator
from datetime import datetime
from typing import List


VALID_DIFFICULTIES = {"EASY", "MEDIUM", "HARD"}


class ScoreCreate(BaseModel):
    username: str
    score: int
    difficulty: str

    @field_validator("difficulty")
    @classmethod
    def difficulty_must_be_valid(cls, v: str) -> str:
        if v not in VALID_DIFFICULTIES:
            raise ValueError(f"difficulty debe ser uno de: {VALID_DIFFICULTIES}")
        return v

    @field_validator("score")
    @classmethod
    def score_must_be_non_negative(cls, v: int) -> int:
        if v < 0:
            raise ValueError("score no puede ser negativo")
        return v


class ScoreResponse(BaseModel):
    id: int
    username: str
    score: int
    difficulty: str
    created_at: datetime

    model_config = {"from_attributes": True}


class RankingEntry(BaseModel):
    username: str
    score: int
    difficulty: str
    created_at: datetime

    model_config = {"from_attributes": True}
```

### 1.7 CRUD

**Crear:** `backend\lastdictamen\app\crud.py`

```python
from sqlalchemy.orm import Session
from . import models, schemas


def get_player_by_username(db: Session, username: str):
    return db.query(models.Player).filter(
        models.Player.username == username
    ).first()


def create_player(db: Session, username: str):
    db_player = models.Player(username=username)
    db.add(db_player)
    db.commit()
    db.refresh(db_player)
    return db_player


def create_score(db: Session, data: schemas.ScoreCreate):
    # Find or create player
    player = get_player_by_username(db, data.username)
    if not player:
        player = create_player(db, data.username)

    db_score = models.Score(
        player_id=player.id,
        score=data.score,
        difficulty=data.difficulty,
    )
    db.add(db_score)
    db.commit()
    db.refresh(db_score)
    return db_score


def get_rankings(db: Session, difficulty: str = None, limit: int = 10):
    query = (
        db.query(models.Score, models.Player.username)
        .join(models.Player)
    )
    if difficulty:
        query = query.filter(models.Score.difficulty == difficulty)

    rows = query.order_by(models.Score.score.desc()).limit(limit).all()

    return [
        schemas.RankingEntry(
            username=username,
            score=score.score,
            difficulty=score.difficulty,
            created_at=score.created_at,
        )
        for score, username in rows
    ]
```

### 1.8 Main

**Crear:** `backend\lastdictamen\app\main.py`

```python
from fastapi import FastAPI, Depends, Query
from sqlalchemy.orm import Session
from typing import List, Optional
from . import crud, models, schemas
from .database import SessionLocal, engine, get_db

models.Base.metadata.create_all(bind=engine)

app = FastAPI(title="The Last Dictamen API")


@app.get("/")
def read_root():
    return {"message": "Welcome to The Last Dictamen API"}


@app.post("/scores/", response_model=schemas.ScoreResponse)
def create_score(data: schemas.ScoreCreate, db: Session = Depends(get_db)):
    return crud.create_score(db=db, data=data)


@app.get("/rankings/", response_model=List[schemas.RankingEntry])
def read_rankings(
    difficulty: Optional[str] = Query(
        default=None,
        description="Filtrar por dificultad: EASY, MEDIUM o HARD"
    ),
    limit: int = Query(default=10, ge=1, le=100),
    db: Session = Depends(get_db),
):
    return crud.get_rankings(db, difficulty=difficulty, limit=limit)
```

---

## 📋 PASO 2: Actualizar Docker Compose y `.env`

### 2.1 Agregar base de datos al script init.sql

**Editar:** `backend\mysql\init.sql`

Agregar al final del archivo (después de las líneas de `codemerge`):

```sql
CREATE DATABASE IF NOT EXISTS lastdictamen CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
GRANT ALL PRIVILEGES ON lastdictamen.* TO 'user'@'%';
FLUSH PRIVILEGES;
```

**IMPORTANTE:** Si el archivo ya tiene `FLUSH PRIVILEGES;` al final, agregar las líneas ANTES del último `FLUSH PRIVILEGES;`, no duplicarlo.

### 2.2 Agregar variable de entorno

**Editar:** `backend\.env`

Agregar al final:
```env
LASTDICTAMEN_DB_URL=mysql+pymysql://user:password@db:3306/lastdictamen
```

### 2.3 Agregar servicio al Docker Compose

**Editar:** `backend\docker-compose.yml`

En la sección `services:`, agregar el nuevo servicio `api_lastdictamen` DESPUÉS de `api_codemerge` y ANTES de la sección `volumes:`:

```yaml
  api_lastdictamen:
    build: ./lastdictamen
    ports:
      - "8003:8000"
    environment:
      - DATABASE_URL=${LASTDICTAMEN_DB_URL}
    depends_on:
      db:
        condition: service_healthy
    restart: on-failure
```

---

## 📋 PASO 3: Capa de datos Android — Nuevos archivos

### 3.1 Interfaz del servicio

**Crear:** `app\src\main\java\com\android\mobile\games\app\games\catchgame\data\ICatchGameService.kt`

```kotlin
package com.android.mobile.games.app.games.catchgame.data

data class CatchGameRankingEntry(
    val username: String,
    val score: Int,
    val difficulty: String,
    val createdAt: String
)

interface ICatchGameService {
    /**
     * Envía el resultado de una partida al servidor.
     * @return true si se guardó correctamente, false si hubo error de red.
     */
    suspend fun submitScore(username: String, score: Int, difficulty: String): Boolean

    /**
     * Obtiene el ranking global o filtrado por dificultad.
     * @param difficulty null para ranking global, "EASY"/"MEDIUM"/"HARD" para filtrar.
     * @return lista vacía si hay error de red (nunca lanza excepción).
     */
    suspend fun getRankings(difficulty: String? = null, limit: Int = 10): List<CatchGameRankingEntry>
}
```

### 3.2 Interfaz Retrofit

**Crear:** `app\src\main\java\com\android\mobile\games\app\games\catchgame\data\CatchGameApiService.kt`

```kotlin
package com.android.mobile.games.app.games.catchgame.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class CatchScoreRequest(
    val username: String,
    val score: Int,
    val difficulty: String
)

data class CatchScoreResponse(
    val id: Int,
    val username: String,
    val score: Int,
    val difficulty: String,
    val created_at: String
)

data class CatchRankingResponse(
    val username: String,
    val score: Int,
    val difficulty: String,
    val created_at: String
)

interface CatchGameApiService {
    @POST("scores/")
    suspend fun submitScore(@Body score: CatchScoreRequest): CatchScoreResponse

    @GET("rankings/")
    suspend fun getRankings(
        @Query("difficulty") difficulty: String? = null,
        @Query("limit") limit: Int = 10
    ): List<CatchRankingResponse>
}
```

### 3.3 Cliente Retrofit + implementación real

**Crear:** `app\src\main\java\com\android\mobile\games\app\games\catchgame\data\RetrofitCatchGameService.kt`

```kotlin
package com.android.mobile.games.app.games.catchgame.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object CatchGameRetrofitClient {
    // Misma IP que Code Slasher (WSL). Puerto 8003 para The Last Dictamen.
    private const val BASE_URL = "http://172.22.80.1:8003/"

    val instance: CatchGameApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CatchGameApiService::class.java)
    }
}

class RetrofitCatchGameService : ICatchGameService {

    override suspend fun submitScore(username: String, score: Int, difficulty: String): Boolean {
        return try {
            CatchGameRetrofitClient.instance.submitScore(
                CatchScoreRequest(
                    username = username,
                    score = score,
                    difficulty = difficulty
                )
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallo silencioso: el juego sigue funcionando aunque no haya red
            false
        }
    }

    override suspend fun getRankings(difficulty: String?, limit: Int): List<CatchGameRankingEntry> {
        return try {
            CatchGameRetrofitClient.instance.getRankings(
                difficulty = difficulty,
                limit = limit
            ).map { response ->
                CatchGameRankingEntry(
                    username = response.username,
                    score = response.score,
                    difficulty = response.difficulty,
                    createdAt = response.created_at
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallo silencioso: retorna lista vacía
            emptyList()
        }
    }
}
```

---

## 📋 PASO 4: Modificar CatchGameMenuScreen para pedir username y mostrar ranking

**Editar:** `app\src\main\java\com\android\mobile\games\app\games\catchgame\ui\CatchGameMenuScreen.kt`

**Cambios requeridos:**
1. Agregar parámetros `onStartGameClick: (username: String) -> Unit` (el difficulty ya se maneja en el padre)
2. Agregar campo de texto para el username
3. Agregar botón de 🏆 Ranking (igual que Code Slasher)
4. El botón de inicio solo se habilita si username no está vacío

**Reemplaza el archivo completo con este contenido:**

```kotlin
package com.android.mobile.games.app.games.catchgame.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.mobile.games.app.R
import com.android.mobile.games.app.games.catchgame.data.CatchGameRankingEntry
import com.android.mobile.games.app.games.catchgame.model.CatchGameDifficulty
import com.android.mobile.games.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun CatchGameMenuScreen(
    selectedDifficulty: CatchGameDifficulty,
    onDifficultySelected: (CatchGameDifficulty) -> Unit,
    onStartGameClick: (username: String) -> Unit,
    onBackClick: () -> Unit,
    rankingLoader: suspend (difficulty: String?, limit: Int) -> List<CatchGameRankingEntry>
) {
    var username by remember { mutableStateOf("") }
    var showRankingModal by remember { mutableStateOf(false) }
    var rankingData by remember { mutableStateOf<List<CatchGameRankingEntry>>(emptyList()) }
    var isLoadingRanking by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_menu),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Overlay pink
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CutePink.copy(alpha = 0.15f))
        )

        // Botón Ranking (top-right)
        IconButton(
            onClick = {
                showRankingModal = true
                isLoadingRanking = true
                coroutineScope.launch {
                    rankingData = rankingLoader(selectedDifficulty.name, 10)
                    isLoadingRanking = false
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(52.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = CuteYellow,
                border = BorderStroke(2.dp, Color.White)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text("🏆", fontSize = 22.sp)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CuteCream.copy(alpha = 0.9f),
                shape = RoundedCornerShape(32.dp),
                border = BorderStroke(3.dp, CuteLavender),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📜 THE LAST DICTAMEN ⚖️",
                        color = TextDark,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Elige tu nivel de sufrimiento académico",
                        color = TextDark.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Campo de nombre del jugador
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Tu nombre de estudiante", color = TextDark.copy(alpha = 0.7f)) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = TextDark,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        ),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CutePink,
                            unfocusedBorderColor = CutePink.copy(alpha = 0.5f),
                            cursorColor = CutePink,
                            focusedLabelColor = CutePink,
                            unfocusedLabelColor = TextDark.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CatchDifficultyCard(
                            difficulty = CatchGameDifficulty.EASY,
                            selectedDifficulty = selectedDifficulty,
                            onClick = onDifficultySelected,
                            color = CuteMint,
                            modifier = Modifier.weight(1f)
                        )
                        CatchDifficultyCard(
                            difficulty = CatchGameDifficulty.MEDIUM,
                            selectedDifficulty = selectedDifficulty,
                            onClick = onDifficultySelected,
                            color = CuteYellow,
                            modifier = Modifier.weight(1f)
                        )
                        CatchDifficultyCard(
                            difficulty = CatchGameDifficulty.HARD,
                            selectedDifficulty = selectedDifficulty,
                            onClick = onDifficultySelected,
                            color = CutePink,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = { onStartGameClick(username.trim()) },
                        enabled = username.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CutePink,
                            contentColor = TextDark,
                            disabledContainerColor = Color.LightGray
                        ),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(text = "✨ ¡A clases! ✨", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onBackClick,
                        border = BorderStroke(2.dp, CuteLavender),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDark),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(text = "Salir de la ESCOM 🎓", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Modal de Ranking
        if (showRankingModal) {
            AlertDialog(
                onDismissRequest = { showRankingModal = false },
                containerColor = CuteCream,
                title = {
                    Text(
                        "👑 MEJORES DICTÁMENES (${selectedDifficulty.label})",
                        color = TextDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        if (isLoadingRanking) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                color = CutePink
                            )
                        } else if (rankingData.isEmpty()) {
                            Text("Sin registros aún. ¡Sé el primero! 📚", color = TextDark)
                        } else {
                            rankingData.forEachIndexed { index, entry ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val medal = when (index) {
                                        0 -> "🥇 "
                                        1 -> "🥈 "
                                        2 -> "🥉 "
                                        else -> "${index + 1}. "
                                    }
                                    Text(
                                        text = "$medal${entry.username}",
                                        color = TextDark,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${entry.score} pts",
                                        color = CutePink,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showRankingModal = false }) {
                        Text("CERRAR 🎀", color = CutePink, fontWeight = FontWeight.Bold)
                    }
                },
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

@Composable
private fun CatchDifficultyCard(
    difficulty: CatchGameDifficulty,
    selectedDifficulty: CatchGameDifficulty,
    onClick: (CatchGameDifficulty) -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    val isSelected = difficulty == selectedDifficulty

    Card(
        onClick = { onClick(difficulty) },
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = if (isSelected) 3.dp else 1.5.dp,
                color = if (isSelected) color else color.copy(alpha = 0.5f),
                shape = RoundedCornerShape(18.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.4f) else Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val emoji = when (difficulty) {
                CatchGameDifficulty.EASY -> "😊"
                CatchGameDifficulty.MEDIUM -> "😐"
                CatchGameDifficulty.HARD -> "💀"
            }
            Text(emoji, fontSize = 24.sp)
            Text(
                text = difficulty.label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        }
    }
}
```

---

## 📋 PASO 5: Modificar CatchGameScreen para enviar score al servidor

**Editar:** `app\src\main\java\com\android\mobile\games\app\games\catchgame\ui\CatchGameScreen.kt`

**Solo se modifica la firma del Composable y el `LaunchedEffect(uiState.isGameOver)`.**
El resto del archivo queda IDÉNTICO.

**Cambio 1:** Agregar parámetros `username: String` y `gameService: ICatchGameService` a la firma:

```kotlin
// ANTES:
@Composable
fun CatchGameScreen(
    difficulty: CatchGameDifficulty,
    onBackToMenuClick: () -> Unit
)

// DESPUÉS:
@Composable
fun CatchGameScreen(
    difficulty: CatchGameDifficulty,
    username: String,
    gameService: ICatchGameService,
    onBackToMenuClick: () -> Unit
)
```

**Cambio 2:** El import — agregar al bloque de imports:
```kotlin
import com.android.mobile.games.app.games.catchgame.data.ICatchGameService
```

**Cambio 3:** Reemplazar únicamente el bloque `LaunchedEffect(uiState.isGameOver)` existente:

```kotlin
// ANTES (líneas ~144-151 en el archivo original):
LaunchedEffect(uiState.isGameOver) {
    if (uiState.isGameOver) {
        scoreRepository.saveBestScoreIfNeeded(
            difficulty = difficulty,
            score = uiState.score
        )
    }
}

// DESPUÉS — agrega el envío al servidor SIN eliminar el guardado local:
LaunchedEffect(uiState.isGameOver) {
    if (uiState.isGameOver) {
        // 1. Guardado local — sigue funcionando igual que antes
        scoreRepository.saveBestScoreIfNeeded(
            difficulty = difficulty,
            score = uiState.score
        )
        // 2. Envío al servidor — fallo silencioso si no hay red
        if (username.isNotBlank()) {
            gameService.submitScore(
                username = username,
                score = uiState.score,
                difficulty = difficulty.name
            )
        }
    }
}
```

---

## 📋 PASO 6: Actualizar AppNavigation.kt

**Editar:** `app\src\main\java\com\android\mobile\games\app\games\navigation\AppNavigation.kt`

**Solo se modifican 3 secciones. El resto del archivo queda IDÉNTICO.**

**Cambio 1:** Agregar imports (agregar junto a los otros imports del paquete catchgame):
```kotlin
import com.android.mobile.games.app.games.catchgame.data.ICatchGameService
import com.android.mobile.games.app.games.catchgame.data.RetrofitCatchGameService
```

**Cambio 2:** Agregar instancia del servicio (junto a `val codeMergeService`):
```kotlin
// ANTES — dentro de AppNavigation(), junto al codeMergeService:
val codeMergeService = remember { MockCodeMergeGameService() }

// DESPUÉS — agregar la línea del catchGame service justo debajo:
val codeMergeService = remember { MockCodeMergeGameService() }
val catchGameService: ICatchGameService = remember { RetrofitCatchGameService() }
```

**Cambio 3:** Agregar `catchGameUsername` state y actualizar los dos composables de CatchGame:

```kotlin
// ANTES — justo después del catchGameDifficulty state:
var catchGameDifficulty by remember {
    mutableStateOf(CatchGameDifficulty.EASY)
}

// DESPUÉS:
var catchGameDifficulty by remember {
    mutableStateOf(CatchGameDifficulty.EASY)
}
var catchGameUsername by remember {
    mutableStateOf("")
}
```

**Cambio 4:** Actualizar el composable de `CatchGameMenu`:
```kotlin
// ANTES:
composable(AppRoute.CatchGameMenu.route) {
    CatchGameMenuScreen(
        selectedDifficulty = catchGameDifficulty,
        onDifficultySelected = { difficulty ->
            catchGameDifficulty = difficulty
        },
        onStartGameClick = {
            navController.navigate(AppRoute.CatchGame.route)
        },
        onBackClick = {
            navController.popBackStack()
        }
    )
}

// DESPUÉS:
composable(AppRoute.CatchGameMenu.route) {
    CatchGameMenuScreen(
        selectedDifficulty = catchGameDifficulty,
        onDifficultySelected = { difficulty ->
            catchGameDifficulty = difficulty
        },
        onStartGameClick = { username ->
            catchGameUsername = username
            navController.navigate(AppRoute.CatchGame.route)
        },
        onBackClick = {
            navController.popBackStack()
        },
        rankingLoader = { difficulty, limit ->
            catchGameService.getRankings(difficulty = difficulty, limit = limit)
        }
    )
}
```

**Cambio 5:** Actualizar el composable de `CatchGame`:
```kotlin
// ANTES:
composable(AppRoute.CatchGame.route) {
    CatchGameScreen(
        difficulty = catchGameDifficulty,
        onBackToMenuClick = {
            navController.popBackStack()
        }
    )
}

// DESPUÉS:
composable(AppRoute.CatchGame.route) {
    CatchGameScreen(
        difficulty = catchGameDifficulty,
        username = catchGameUsername,
        gameService = catchGameService,
        onBackToMenuClick = {
            navController.popBackStack()
        }
    )
}
```

---

## 📋 PASO 7: QA — Verificación completa

### 7.1 Verificar todos los archivos nuevos

Confirma que existen exactamente estos archivos nuevos:
```
backend\lastdictamen\Dockerfile
backend\lastdictamen\requirements.txt
backend\lastdictamen\app\__init__.py
backend\lastdictamen\app\main.py
backend\lastdictamen\app\database.py
backend\lastdictamen\app\models.py
backend\lastdictamen\app\schemas.py
backend\lastdictamen\app\crud.py
catchgame\data\ICatchGameService.kt
catchgame\data\CatchGameApiService.kt
catchgame\data\RetrofitCatchGameService.kt
```

### 7.2 Verificar que no se tocaron archivos prohibidos

```
# Estos archivos NO deben tener ninguna modificación:
catchgame/config/CatchGameConfig.kt
catchgame/data/CatchGameScoreRepository.kt
catchgame/data/TriviaJsonLoader.kt
catchgame/data/TriviaRepository.kt
catchgame/engine/CatchGameController.kt
catchgame/model/CatchGameDifficulty.kt
catchgame/model/CatchGameItem.kt
catchgame/model/CatchGameUiState.kt
catchgame/model/TriviaCategory.kt
catchgame/model/TriviaQuestion.kt
catchgame/ui/CatchGameHud.kt
catchgame/ui/CatchTriviaDialog.kt
catchgame/ui/CatchGameOverPanel.kt
fruitninja/* (todos)
razarun/* (todos — excepto si el prompt anterior ya hizo cambios ahí)
```

### 7.3 Verificar sintaxis Python (sin Docker)

```bash
# Desde backend\lastdictamen\
python -c "from app import main, crud, models, schemas, database; print('OK lastdictamen')"
```

Si hay errores de importación, corrígelos antes de continuar.

### 7.4 Verificar compilación Android

```bash
.\gradlew assembleDebug
```

El build DEBE terminar con `BUILD SUCCESSFUL`. Si hay errores de Kotlin, corrígelos.

### 7.5 Checklist final de calidad

**Backend Python:**
- [ ] `app\__init__.py` existe y está vacío
- [ ] `database.py` usa `declarative_base` de `sqlalchemy.orm` (NO de `sqlalchemy.ext.declarative`)
- [ ] `main.py` llama `models.Base.metadata.create_all(bind=engine)` al arrancar
- [ ] Schemas usan `model_config = {"from_attributes": True}` (Pydantic v2 — NO `class Config`)
- [ ] `crud.py` hace find-or-create del player antes de insertar score
- [ ] `GET /rankings/` acepta `difficulty` como parámetro opcional (puede ser `None`)
- [ ] `POST /scores/` valida que `difficulty` sea "EASY", "MEDIUM" o "HARD"
- [ ] `POST /scores/` valida que `score` sea >= 0

**Docker:**
- [ ] `docker-compose.yml` ahora tiene 5 servicios: `db`, `api_codeslasher`, `api_razarun`, `api_codemerge`, `api_lastdictamen`
- [ ] `api_lastdictamen` usa puerto `8003:8000`
- [ ] `api_lastdictamen` tiene `depends_on: db: condition: service_healthy`
- [ ] `init.sql` crea la base de datos `lastdictamen`
- [ ] `.env` tiene la variable `LASTDICTAMEN_DB_URL`

**Android:**
- [ ] `CatchGameMenuScreen` tiene nuevo parámetro `rankingLoader`
- [ ] `CatchGameMenuScreen` tiene campo de texto para username
- [ ] El botón "¡A clases!" está deshabilitado si username está vacío
- [ ] `CatchGameScreen` recibe `username` y `gameService` pero sigue recibiendo `difficulty` y `onBackToMenuClick`
- [ ] El `LaunchedEffect(uiState.isGameOver)` llama PRIMERO `saveBestScoreIfNeeded` y DESPUÉS `gameService.submitScore`
- [ ] `AppNavigation` tiene `catchGameUsername` en el state
- [ ] `RetrofitCatchGameService` tiene `try/catch` en AMBOS métodos
- [ ] `CatchGameRetrofitClient.BASE_URL` usa puerto `8003`

---

## ⚠️ RESTRICCIONES ABSOLUTAS

1. **NUNCA** eliminar o reemplazar la llamada a `scoreRepository.saveBestScoreIfNeeded()` — debe mantenerse
2. **NUNCA** modificar `CatchGameController.kt` — la lógica del juego no se toca
3. **NUNCA** modificar `TriviaRepository.kt` ni `TriviaJsonLoader.kt`
4. **NUNCA** usar `class Config` en schemas Pydantic — usar `model_config = {"from_attributes": True}`
5. **NUNCA** omitir el `app\__init__.py` — sin él los imports relativos de FastAPI fallan
6. **NUNCA** usar `depends_on: db` sin `condition: service_healthy` — genera race condition
7. **NUNCA** hardcodear credenciales — siempre `os.getenv()`
8. Si el servidor no responde, la app Android NO debe crashear — `try/catch` en todo método de red

---

## 📝 NOTAS FINALES

- La IP `172.22.80.1` es la IP WSL ya en uso por Code Slasher. Usar la misma.
- Puerto interno del contenedor: siempre `8000`. Mapeo externo: `8003`.
- El `init.sql` solo crea la DB si no existe — es seguro ejecutarlo aunque ya existan las otras DBs.
- `getRankings(difficulty = selectedDifficulty.name)` filtra por la dificultad actualmente seleccionada en el menú, para que el ranking sea relevante al nivel elegido.
- El campo `username` se guarda en `AppNavigation` para pasarlo de la pantalla de menú a la pantalla de juego — patrón idéntico al que usa FruitNinja con su `username`.
