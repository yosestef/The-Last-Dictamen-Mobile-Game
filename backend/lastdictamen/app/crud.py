from sqlalchemy.orm import Session
from . import models, schemas


def get_player_by_username(db: Session, username: str):
    return db.query(models.Player).filter(
        models.Player.username == username
    ).first()


def create_player(db: Session, username: str):
    db_player = models.Player(username=username)
    db.add(db_player)
    db.commit()
    db.refresh(db_player)
    return db_player


def create_score(db: Session, data: schemas.ScoreCreate):
    player = get_player_by_username(db, data.username)
    if not player:
        player = create_player(db, data.username)

    db_score = models.Score(
        player_id=player.id,
        score=data.score,
        difficulty=data.difficulty,
    )
    db.add(db_score)
    db.commit()
    db.refresh(db_score)
    return db_score


def get_rankings(db: Session, difficulty: str = None, limit: int = 10):
    query = (
        db.query(models.Score, models.Player.username)
        .join(models.Player)
    )
    if difficulty:
        query = query.filter(models.Score.difficulty == difficulty)

    rows = query.order_by(models.Score.score.desc()).limit(limit).all()

    return [
        schemas.RankingEntry(
            username=username,
            score=score.score,
            difficulty=score.difficulty,
            created_at=score.created_at,
        )
        for score, username in rows
    ]
