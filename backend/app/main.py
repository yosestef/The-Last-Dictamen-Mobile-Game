from fastapi import FastAPI, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List
from . import crud, models, schemas
from .database import SessionLocal, engine, get_db

models.Base.metadata.create_all(bind=engine)

app = FastAPI(title="Code Slasher API")

@app.get("/")
def read_root():
    return {"message": "Welcome to Code Slasher API"}

@app.post("/scores/", response_model=schemas.ScoreResponse)
def create_score(score: schemas.ScoreCreate, db: Session = Depends(get_db)):
    return crud.create_score(db=db, score=score)

@app.get("/rankings/", response_model=List[schemas.RankingEntry])
def read_rankings(limit: int = 10, db: Session = Depends(get_db)):
    return crud.get_rankings(db, limit=limit)
