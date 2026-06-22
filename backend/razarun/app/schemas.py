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
