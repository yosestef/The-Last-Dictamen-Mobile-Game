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
