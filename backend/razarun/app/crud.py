from sqlalchemy.orm import Session
from . import models, schemas


def get_player_by_name(db: Session, name: str):
    return db.query(models.Player).filter(models.Player.name == name).first()


def create_player(db: Session, name: str):
    db_player = models.Player(name=name)
    db.add(db_player)
    db.commit()
    db.refresh(db_player)
    return db_player


def create_result(db: Session, result: schemas.ResultCreate):
    player = get_player_by_name(db, result.name)
    if not player:
        player = create_player(db, result.name)

    db_result = models.Result(
        player_id=player.id,
        distance=result.distance,
        status=result.status,
    )
    db.add(db_result)
    db.commit()
    db.refresh(db_result)
    return db_result


def get_rankings(db: Session, limit: int = 10):
    rows = (
        db.query(models.Result, models.Player.name)
        .join(models.Player)
        .order_by(models.Result.distance.desc())
        .limit(limit)
        .all()
    )
    return [
        schemas.RankingEntry(
            name=name,
            distance=result.distance,
            status=result.status,
            created_at=result.created_at,
        )
        for result, name in rows
    ]
