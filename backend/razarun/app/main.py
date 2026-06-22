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
