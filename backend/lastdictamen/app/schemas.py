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
