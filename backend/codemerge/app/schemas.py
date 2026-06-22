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
