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
