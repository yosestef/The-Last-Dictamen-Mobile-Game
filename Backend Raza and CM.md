# PROMPT PARA AGENTE: Backends FastAPI + MySQL + Docker
## Proyecto: PROYECTO-FINAL (Android Multijuego)

---

## 🎯 MISIÓN

Debes implementar los backends de FastAPI para los juegos **La Raza Run** y **Code Merge**, junto con los clientes Retrofit en Android para conectar ambos juegos al servidor real. También debes unificar el `docker-compose.yml` para que todos los servicios (Code Slasher, La Raza Run y Code Merge) corran juntos.

**NO tocar ningún archivo dentro de los paquetes `fruitninja` ni `catchgame`.**
**NO modificar ningún archivo de lógica de juego ya existente (engines, canvas, etc.).**
**SOLO crear archivos nuevos y editar los indicados explícitamente.**

---

## 📁 ESTRUCTURA DEL PROYECTO

```
c:\Proyecto Moviles\PROYECTO-FINAL\
├── backend\                          ← Backend existente (Code Slasher, puerto 8000)
│   ├── app\
│   │   ├── main.py
│   │   ├── database.py
│   │   ├── models.py
│   │   ├── schemas.py
│   │   └── crud.py
│   ├── Dockerfile
│   ├── requirements.txt
│   ├── docker-compose.yml            ← ESTE ARCHIVO SERÁ REEMPLAZADO
│   └── .env
└── app\src\main\java\com\android\mobile\games\app\games\
    ├── fruitninja\                   ← NO TOCAR
    ├── catchgame\                    ← NO TOCAR
    ├── razarun\data\                 ← Agregar RetrofitRazaService.kt aquí
    └── codemerge\data\               ← Agregar archivos Retrofit aquí
```

---

## 📋 PASO 1: Reorganizar la carpeta backend

Mueve (renombra) la carpeta actual `backend\app\` para que quede bajo `backend\codeslasher\app\`.
La nueva estructura será:

```
backend\
├── codeslasher\           ← Contenido actual de backend\
│   ├── Dockerfile
│   ├── requirements.txt
│   └── app\
│       ├── __init__.py
│       ├── main.py
│       ├── database.py
│       ├── models.py
│       ├── schemas.py
│       └── crud.py
├── razarun\               ← NUEVO - crear todo
│   ├── Dockerfile
│   ├── requirements.txt
│   └── app\
│       ├── __init__.py
│       ├── main.py
│       ├── database.py
│       ├── models.py
│       ├── schemas.py
│       └── crud.py
├── codemerge\             ← NUEVO - crear todo
│   ├── Dockerfile
│   ├── requirements.txt
│   └── app\
│       ├── __init__.py
│       ├── main.py
│       ├── database.py
│       ├── models.py
│       ├── schemas.py
│       └── crud.py
├── mysql\
│   └── init.sql           ← NUEVO - script de inicialización MySQL
├── docker-compose.yml     ← NUEVO - reemplaza el actual, unificado
└── .env                   ← NUEVO - variables globales
```

---

## 📋 PASO 2: Script de inicialización MySQL

**Crear:** `backend\mysql\init.sql`

```sql
-- Crear las 3 bases de datos del proyecto
CREATE DATABASE IF NOT EXISTS codeslasher CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS razarun CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS codemerge CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Otorgar permisos al usuario de la app
GRANT ALL PRIVILEGES ON codeslasher.* TO 'user'@'%';
GRANT ALL PRIVILEGES ON razarun.* TO 'user'@'%';
GRANT ALL PRIVILEGES ON codemerge.* TO 'user'@'%';
FLUSH PRIVILEGES;
```

---

## 📋 PASO 3: Variables de entorno globales

**Crear:** `backend\.env`

```env
# MySQL
MYSQL_ROOT_PASSWORD=rootpassword
MYSQL_USER=user
MYSQL_PASSWORD=password

# Code Slasher
CODESLASHER_DB_URL=mysql+pymysql://user:password@db:3306/codeslasher

# La Raza Run
RAZARUN_DB_URL=mysql+pymysql://user:password@db:3306/razarun

# Code Merge
CODEMERGE_DB_URL=mysql+pymysql://user:password@db:3306/codemerge
```

---

## 📋 PASO 4: Docker Compose Unificado

**Crear/Reemplazar:** `backend\docker-compose.yml`

```yaml
version: '3.8'

services:
  db:
    image: mysql:8.0
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_USER: ${MYSQL_USER}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./mysql/init.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-p${MYSQL_ROOT_PASSWORD}"]
      interval: 10s
      timeout: 5s
      retries: 10
      start_period: 30s

  api_codeslasher:
    build: ./codeslasher
    ports:
      - "8000:8000"
    environment:
      - DATABASE_URL=${CODESLASHER_DB_URL}
    depends_on:
      db:
        condition: service_healthy
    restart: on-failure

  api_razarun:
    build: ./razarun
    ports:
      - "8001:8000"
    environment:
      - DATABASE_URL=${RAZARUN_DB_URL}
    depends_on:
      db:
        condition: service_healthy
    restart: on-failure

  api_codemerge:
    build: ./codemerge
    ports:
      - "8002:8000"
    environment:
      - DATABASE_URL=${CODEMERGE_DB_URL}
    depends_on:
      db:
        condition: service_healthy
    restart: on-failure

volumes:
  mysql_data:
```

---

## 📋 PASO 5: Reorganizar Code Slasher

Mueve los archivos actuales de `backend\` a su nueva subcarpeta `backend\codeslasher\`:

**Crear:** `backend\codeslasher\Dockerfile`
(Contenido idéntico al `Dockerfile` actual — solo cambia el contexto de build)

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

**Crear:** `backend\codeslasher\requirements.txt`
```
fastapi
uvicorn[standard]
sqlalchemy
pymysql
pydantic
pydantic-settings
python-dotenv
cryptography
```

**Crear:** `backend\codeslasher\app\__init__.py` (archivo vacío)

**Copiar** los archivos existentes `main.py`, `database.py`, `models.py`, `schemas.py`, `crud.py` a `backend\codeslasher\app\`.

**IMPORTANTE:** En `backend\codeslasher\app\database.py`, asegúrate de que la URL por defecto sea:
```python
SQLALCHEMY_DATABASE_URL = os.getenv(
    "DATABASE_URL", "mysql+pymysql://user:password@localhost:3306/codeslasher"
)
```

---

## 📋 PASO 6: Backend La Raza Run

### 6.1 Dockerfile

**Crear:** `backend\razarun\Dockerfile`

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

### 6.2 Requirements

**Crear:** `backend\razarun\requirements.txt`

```
fastapi
uvicorn[standard]
sqlalchemy
pymysql
pydantic
python-dotenv
cryptography
```

### 6.3 `__init__.py`

**Crear:** `backend\razarun\app\__init__.py` (vacío)

### 6.4 Database

**Crear:** `backend\razarun\app\database.py`

```python
import os
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, declarative_base
from dotenv import load_dotenv

load_dotenv()

SQLALCHEMY_DATABASE_URL = os.getenv(
    "DATABASE_URL", "mysql+pymysql://user:password@localhost:3306/razarun"
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

### 6.5 Models

**Crear:** `backend\razarun\app\models.py`

```python
from sqlalchemy import Column, Integer, String, Float, DateTime, ForeignKey
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from .database import Base


class Player(Base):
    __tablename__ = "players"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(50), unique=True, index=True, nullable=False)

    results = relationship("Result", back_populates="player")


class Result(Base):
    __tablename__ = "results"

    id = Column(Integer, primary_key=True, index=True)
    player_id = Column(Integer, ForeignKey("players.id"), nullable=False)
    distance = Column(Float, nullable=False)
    status = Column(String(20), nullable=False)  # VICTORY | CRASHED | TIME_OUT
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    player = relationship("Player", back_populates="results")
```

### 6.6 Schemas

**Crear:** `backend\razarun\app\schemas.py`

```python
from pydantic import BaseModel, field_validator
from datetime import datetime
from typing import List


class ResultCreate(BaseModel):
    name: str
    distance: float
    status: str

    @field_validator("status")
    @classmethod
    def status_must_be_valid(cls, v):
        allowed = {"VICTORY", "CRASHED", "TIME_OUT"}
        if v not in allowed:
            raise ValueError(f"status debe ser uno de: {allowed}")
        return v


class ResultResponse(BaseModel):
    id: int
    name: str
    distance: float
    status: str
    created_at: datetime

    model_config = {"from_attributes": True}


class RankingEntry(BaseModel):
    name: str
    distance: float
    status: str
    created_at: datetime

    model_config = {"from_attributes": True}
```

### 6.7 CRUD

**Crear:** `backend\razarun\app\crud.py`

```python
from sqlalchemy.orm import Session
from . import models, schemas


def get_player_by_name(db: Session, name: str):
    return db.query(models.Player).filter(models.Player.name == name).first()


def create_player(db: Session, name: str):
    db_player = models.Player(name=name)
    db.add(db_player)
    db.commit()
    db.refresh(db_player)
    return db_player


def create_result(db: Session, result: schemas.ResultCreate):
    player = get_player_by_name(db, result.name)
    if not player:
        player = create_player(db, result.name)

    db_result = models.Result(
        player_id=player.id,
        distance=result.distance,
        status=result.status,
    )
    db.add(db_result)
    db.commit()
    db.refresh(db_result)
    return db_result


def get_rankings(db: Session, limit: int = 10):
    rows = (
        db.query(models.Result, models.Player.name)
        .join(models.Player)
        .order_by(models.Result.distance.desc())
        .limit(limit)
        .all()
    )
    return [
        schemas.RankingEntry(
            name=name,
            distance=result.distance,
            status=result.status,
            created_at=result.created_at,
        )
        for result, name in rows
    ]
```

### 6.8 Main

**Crear:** `backend\razarun\app\main.py`

```python
from fastapi import FastAPI, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List
from . import crud, models, schemas
from .database import SessionLocal, engine, get_db

models.Base.metadata.create_all(bind=engine)

app = FastAPI(title="La Raza Run API")


@app.get("/")
def read_root():
    return {"message": "Welcome to La Raza Run API"}


@app.post("/results/", response_model=schemas.ResultResponse)
def create_result(result: schemas.ResultCreate, db: Session = Depends(get_db)):
    return crud.create_result(db=db, result=result)


@app.get("/rankings/", response_model=List[schemas.RankingEntry])
def read_rankings(limit: int = 10, db: Session = Depends(get_db)):
    return crud.get_rankings(db, limit=limit)
```

---

## 📋 PASO 7: Backend Code Merge

### 7.1 Dockerfile

**Crear:** `backend\codemerge\Dockerfile`

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

### 7.2 Requirements

**Crear:** `backend\codemerge\requirements.txt`

```
fastapi
uvicorn[standard]
sqlalchemy
pymysql
pydantic
python-dotenv
cryptography
```

### 7.3 `__init__.py`

**Crear:** `backend\codemerge\app\__init__.py` (vacío)

### 7.4 Database

**Crear:** `backend\codemerge\app\database.py`

```python
import os
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, declarative_base
from dotenv import load_dotenv

load_dotenv()

SQLALCHEMY_DATABASE_URL = os.getenv(
    "DATABASE_URL", "mysql+pymysql://user:password@localhost:3306/codemerge"
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

### 7.5 Models

**Crear:** `backend\codemerge\app\models.py`

```python
from sqlalchemy import Column, Integer, String, DateTime, ForeignKey
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from .database import Base


class Player(Base):
    __tablename__ = "players"

    id = Column(Integer, primary_key=True, index=True)
    player_name = Column(String(50), unique=True, index=True, nullable=False)

    scores = relationship("Score", back_populates="player")


class Score(Base):
    __tablename__ = "scores"

    id = Column(Integer, primary_key=True, index=True)
    player_id = Column(Integer, ForeignKey("players.id"), nullable=False)
    score = Column(Integer, nullable=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    player = relationship("Player", back_populates="scores")
```

### 7.6 Schemas

**Crear:** `backend\codemerge\app\schemas.py`

```python
from pydantic import BaseModel
from datetime import datetime
from typing import List


class ScoreCreate(BaseModel):
    player_name: str
    score: int


class ScoreResponse(BaseModel):
    id: int
    player_name: str
    score: int
    created_at: datetime

    model_config = {"from_attributes": True}
```

### 7.7 CRUD

**Crear:** `backend\codemerge\app\crud.py`

```python
from sqlalchemy.orm import Session
from . import models, schemas


def get_player_by_name(db: Session, player_name: str):
    return db.query(models.Player).filter(models.Player.player_name == player_name).first()


def create_player(db: Session, player_name: str):
    db_player = models.Player(player_name=player_name)
    db.add(db_player)
    db.commit()
    db.refresh(db_player)
    return db_player


def create_score(db: Session, data: schemas.ScoreCreate):
    player = get_player_by_name(db, data.player_name)
    if not player:
        player = create_player(db, data.player_name)

    db_score = models.Score(player_id=player.id, score=data.score)
    db.add(db_score)
    db.commit()
    db.refresh(db_score)
    return db_score


def get_high_scores(db: Session, limit: int = 10):
    rows = (
        db.query(models.Score, models.Player.player_name)
        .join(models.Player)
        .order_by(models.Score.score.desc())
        .limit(limit)
        .all()
    )
    return [
        schemas.ScoreResponse(
            id=score.id,
            player_name=player_name,
            score=score.score,
            created_at=score.created_at,
        )
        for score, player_name in rows
    ]
```

### 7.8 Main

**Crear:** `backend\codemerge\app\main.py`

```python
from fastapi import FastAPI, Depends
from sqlalchemy.orm import Session
from typing import List
from . import crud, models, schemas
from .database import SessionLocal, engine, get_db

models.Base.metadata.create_all(bind=engine)

app = FastAPI(title="Code Merge API")


@app.get("/")
def read_root():
    return {"message": "Welcome to Code Merge API"}


@app.post("/scores/", response_model=schemas.ScoreResponse)
def create_score(data: schemas.ScoreCreate, db: Session = Depends(get_db)):
    return crud.create_score(db=db, data=data)


@app.get("/highscores/", response_model=List[schemas.ScoreResponse])
def read_high_scores(limit: int = 10, db: Session = Depends(get_db)):
    return crud.get_high_scores(db, limit=limit)
```

---

## 📋 PASO 8: Cliente Android — La Raza Run

### 8.1 Interfaz Retrofit

**Crear:** `app\src\main\java\com\android\mobile\games\app\games\razarun\data\RazaApiService.kt`

```kotlin
package com.android.mobile.games.app.games.razarun.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class RazaResultRequest(
    val name: String,
    val distance: Float,
    val status: String
)

data class RazaRankingResponse(
    val name: String,
    val distance: Float,
    val status: String,
    val created_at: String
)

interface RazaApiService {
    @POST("results/")
    suspend fun submitResult(@Body result: RazaResultRequest): RazaRankingResponse

    @GET("rankings/")
    suspend fun getRankings(@Query("limit") limit: Int = 10): List<RazaRankingResponse>
}
```

### 8.2 Cliente Retrofit

**Crear:** `app\src\main\java\com\android\mobile\games\app\games\razarun\data\RetrofitRazaService.kt`

```kotlin
package com.android.mobile.games.app.games.razarun.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RazaRetrofitClient {
    // Usa la misma IP que Code Slasher (WSL). Puerto 8001 para La Raza Run.
    private const val BASE_URL = "http://172.22.80.1:8001/"

    val instance: RazaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RazaApiService::class.java)
    }
}

class RetrofitRazaService : RazaGameService {

    override suspend fun getTopRankings(): List<RankingEntry> {
        return try {
            RazaRetrofitClient.instance.getRankings().map {
                RankingEntry(
                    name = it.name,
                    distance = it.distance,
                    status = it.status
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun submitResult(name: String, distance: Float, status: String): Boolean {
        return try {
            RazaRetrofitClient.instance.submitResult(
                RazaResultRequest(name = name, distance = distance, status = status)
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
```

---

## 📋 PASO 9: Cliente Android — Code Merge

### 9.1 Interfaz Retrofit

**Crear:** `app\src\main\java\com\android\mobile\games\app\games\codemerge\data\CodeMergeApiService.kt`

```kotlin
package com.android.mobile.games.app.games.codemerge.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class CodeMergeScoreRequest(
    val player_name: String,
    val score: Int
)

data class CodeMergeScoreResponse(
    val id: Int,
    val player_name: String,
    val score: Int,
    val created_at: String
)

interface CodeMergeApiService {
    @POST("scores/")
    suspend fun uploadScore(@Body score: CodeMergeScoreRequest): CodeMergeScoreResponse

    @GET("highscores/")
    suspend fun getHighScores(@Query("limit") limit: Int = 10): List<CodeMergeScoreResponse>
}
```

### 9.2 Cliente Retrofit + Implementación

**Crear:** `app\src\main\java\com\android\mobile\games\app\games\codemerge\data\RetrofitCodeMergeService.kt`

```kotlin
package com.android.mobile.games.app.games.codemerge.data

import com.android.mobile.games.app.games.codemerge.model.MergeRunResult
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object CodeMergeRetrofitClient {
    // Puerto 8002 para Code Merge
    private const val BASE_URL = "http://172.22.80.1:8002/"

    val instance: CodeMergeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CodeMergeApiService::class.java)
    }
}

class RetrofitCodeMergeGameService : ICodeMergeGameService {

    override suspend fun saveResult(result: MergeRunResult): Boolean {
        return try {
            CodeMergeRetrofitClient.instance.uploadScore(
                CodeMergeScoreRequest(
                    player_name = result.playerName,
                    score = result.score
                )
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun getHighScores(): List<MergeRunResult> {
        return try {
            CodeMergeRetrofitClient.instance.getHighScores().map {
                MergeRunResult(
                    id = it.id,
                    playerName = it.player_name,
                    score = it.score,
                    timestamp = it.created_at
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
```

---

## 📋 PASO 10: Verificar modelo MergeRunResult

**Verificar que** `app\src\main\java\com\android\mobile\games\app\games\codemerge\model\CodeMergeModel.kt` (o el archivo donde está definido `MergeRunResult`) tenga exactamente:

```kotlin
data class MergeRunResult(
    val id: Int = 0,
    val playerName: String,
    val score: Int,
    val timestamp: String = ""
)
```

Si el campo `id` o `timestamp` no existen, agrégalos con valores por defecto para no romper el código existente.

---

## 📋 PASO 11: QA — Verificación completa

### 11.1 Verificar estructura de archivos creados

Antes de continuar, verifica que existan todos estos archivos:
```
backend\mysql\init.sql
backend\.env
backend\docker-compose.yml
backend\codeslasher\Dockerfile
backend\codeslasher\requirements.txt
backend\codeslasher\app\__init__.py
backend\razarun\Dockerfile
backend\razarun\requirements.txt
backend\razarun\app\__init__.py
backend\razarun\app\main.py
backend\razarun\app\database.py
backend\razarun\app\models.py
backend\razarun\app\schemas.py
backend\razarun\app\crud.py
backend\codemerge\Dockerfile
backend\codemerge\requirements.txt
backend\codemerge\app\__init__.py
backend\codemerge\app\main.py
backend\codemerge\app\database.py
backend\codemerge\app\models.py
backend\codemerge\app\schemas.py
backend\codemerge\app\crud.py
```

Y en Android:
```
razarun\data\RazaApiService.kt
razarun\data\RetrofitRazaService.kt
codemerge\data\CodeMergeApiService.kt
codemerge\data\RetrofitCodeMergeService.kt
```

### 11.2 Verificar sintaxis Python (sin ejecutar Docker)

Para cada backend nuevo, verifica que Python puede importar los módulos sin errores:
```bash
# Desde backend\razarun\
python -c "from app import main, crud, models, schemas, database; print('OK razarun')"

# Desde backend\codemerge\
python -c "from app import main, crud, models, schemas, database; print('OK codemerge')"
```

Si hay errores de importación, corrígelos antes de continuar.

### 11.3 Verificar que el código Android compila

Ejecuta desde la raíz del proyecto Android:
```bash
.\gradlew assembleDebug
```

El build debe terminar con `BUILD SUCCESSFUL`. Si hay errores de compilación en Kotlin, corrígelos.

### 11.4 Checklist final de calidad

Antes de declarar la tarea completa, verifica manualmente:

- [ ] `backend\docker-compose.yml` tiene 4 servicios: `db`, `api_codeslasher`, `api_razarun`, `api_codemerge`
- [ ] El servicio `db` tiene `healthcheck` configurado
- [ ] Los servicios de API tienen `depends_on: db: condition: service_healthy`
- [ ] El `init.sql` crea las 3 bases de datos
- [ ] Cada `app\__init__.py` existe (aunque esté vacío) — sin esto FastAPI falla al importar
- [ ] Cada `database.py` lee `DATABASE_URL` desde variables de entorno con `.getenv()`
- [ ] Cada `main.py` llama `models.Base.metadata.create_all(bind=engine)` al inicio
- [ ] Los schemas usan `model_config = {"from_attributes": True}` (Pydantic v2) — NO `class Config`
- [ ] Los `crud.py` hacen `find-or-create` del player antes de insertar el resultado/score
- [ ] Los `RetrofitRazaService` y `RetrofitCodeMergeGameService` tienen `try/catch` en cada método
- [ ] NO se modificó ningún archivo dentro de `fruitninja\` ni `catchgame\`
- [ ] NO se modificaron archivos de engine, canvas ni lógica de juego existente

---

## ⚠️ RESTRICCIONES ABSOLUTAS

1. **NUNCA** hardcodear credenciales de base de datos en código Python — siempre usar `os.getenv()`
2. **NUNCA** modificar archivos dentro de `fruitninja\` o `catchgame\`
3. **NUNCA** usar `class Config` en schemas Pydantic — usar `model_config = {"from_attributes": True}`
4. **NUNCA** omitir el `__init__.py` en las carpetas `app\` — sin él, los imports relativos fallan
5. **NUNCA** usar `depends_on: db` sin `condition: service_healthy` — causa race condition y errores de conexión
6. Si `MergeRunResult` ya existe y tiene campos distintos, **adaptar** el `RetrofitCodeMergeGameService` para que mapee correctamente SIN cambiar los campos existentes del modelo

---

## 📝 NOTAS TÉCNICAS IMPORTANTES

- La IP `172.22.80.1` es la IP de WSL que ya usa Code Slasher en producción. Usar la misma para todos.
- El puerto interno del contenedor siempre es `8000` (uvicorn). El mapeo externo cambia: 8000, 8001, 8002.
- MySQL 8.0 con `init.sql` solo ejecuta el script la primera vez que se crea el volumen. Si el volumen ya existe, el script NO se vuelve a ejecutar.
- SQLAlchemy `create_all()` es idempotente — crea tablas si no existen, no borra datos.
- El `declarative_base()` está en `sqlalchemy.orm` desde SQLAlchemy 1.4+. Usar esa importación, no `sqlalchemy.ext.declarative`.
