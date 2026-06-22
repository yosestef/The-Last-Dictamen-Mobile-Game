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
