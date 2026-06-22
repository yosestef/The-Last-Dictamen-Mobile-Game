from sqlalchemy.orm import Session
from . import models, schemas

def get_user_by_username(db: Session, username: str):
    return db.query(models.User).filter(models.User.username == username).first()

def create_user(db: Session, user: schemas.UserCreate):
    db_user = models.User(username=user.username)
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user

def create_score(db: Session, score: schemas.ScoreCreate):
    # Find or create user
    db_user = get_user_by_username(db, score.username)
    if not db_user:
        db_user = create_user(db, schemas.UserCreate(username=score.username))
    
    db_score = models.Score(
        user_id=db_user.id,
        score=score.score,
        difficulty=score.difficulty
    )
    db.add(db_score)
    db.commit()
    db.refresh(db_score)
    
    # Return with username for the schema mapping
    return {
        "id": db_score.id,
        "username": db_user.username,
        "score": db_score.score,
        "difficulty": db_score.difficulty,
        "created_at": db_score.created_at
    }

def get_rankings(db: Session, limit: int = 10):
    results = db.query(models.Score, models.User.username)\
        .join(models.User)\
        .order_by(models.Score.score.desc())\
        .limit(limit)\
        .all()
    
    rankings = []
    for score, username in results:
        rankings.append(schemas.RankingEntry(
            username=username,
            score=score.score,
            difficulty=score.difficulty,
            created_at=score.created_at
        ))
    return rankings
