from sqlalchemy.orm import Session
from . import models, schemas


def get_player_by_name(db: Session, player_name: str):
    return db.query(models.Player).filter(models.Player.player_name == player_name).first()


def create_player(db: Session, player_name: str):
    db_player = models.Player(player_name=player_name)
    db.add(db_player)
    db.commit()
    db.refresh(db_player)
    return db_player


def create_score(db: Session, data: schemas.ScoreCreate):
    player = get_player_by_name(db, data.player_name)
    if not player:
        player = create_player(db, data.player_name)

    db_score = models.Score(player_id=player.id, score=data.score)
    db.add(db_score)
    db.commit()
    db.refresh(db_score)
    return db_score


def get_high_scores(db: Session, limit: int = 10):
    rows = (
        db.query(models.Score, models.Player.player_name)
        .join(models.Player)
        .order_by(models.Score.score.desc())
        .limit(limit)
        .all()
    )
    return [
        schemas.ScoreResponse(
            id=score.id,
            player_name=player_name,
            score=score.score,
            created_at=score.created_at,
        )
        for score, player_name in rows
    ]
