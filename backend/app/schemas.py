from pydantic import BaseModel
from datetime import datetime
from typing import List, Optional

class ScoreBase(BaseModel):
    score: int
    difficulty: str

class ScoreCreate(ScoreBase):
    username: str

class Score(ScoreBase):
    id: int
    user_id: int
    created_at: datetime

    class Config:
        from_attributes = True

class ScoreResponse(ScoreBase):
    id: int
    username: str
    created_at: datetime

    class Config:
        from_attributes = True

class UserBase(BaseModel):
    username: str

class UserCreate(UserBase):
    pass

class User(UserBase):
    id: int
    scores: List[Score] = []

    class Config:
        from_attributes = True

class RankingEntry(BaseModel):
    username: str
    score: int
    difficulty: str
    created_at: datetime
