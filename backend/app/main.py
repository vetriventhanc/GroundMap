
from fastapi import FastAPI

from app.core.config import settings
from app.api.routes.health import router as health_router
from app.api.routes.locations import router as locations_router


app = FastAPI(
    title=settings.app_name,
    description="Offline-first Spatial Intelligence Platform",
    version="0.3.0"
)

app.include_router(health_router)
app.include_router(locations_router)


@app.get("/")
def root():
    return {
        "application": settings.app_name,
        "description": "Spatial Intelligence Platform API",
        "version": "0.3.0",
        "status": "running"
    }
