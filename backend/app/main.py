
from fastapi import FastAPI
from datetime import datetime, timezone

app = FastAPI(
    title="GroundMap API",
    description="Offline-first Spatial Intelligence Platform",
    version="0.1.0"
)


@app.get("/")
def root():
    return {
        "application": "GroundMap",
        "message": "Spatial Intelligence Platform API",
        "version": "0.1.0",
        "status": "running"
    }


@app.get("/health")
def health():
    return {
        "status": "healthy",
        "application": "GroundMap",
        "timestamp": datetime.now(timezone.utc).isoformat()
    }
