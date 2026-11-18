
from fastapi import FastAPI

from app.core.config import settings

from app.api.routes.health import router as health_router
from app.api.routes.locations import router as locations_router
from app.api.routes.assets import router as assets_router


app = FastAPI(
    title=settings.app_name,
    description="Offline-first Spatial Intelligence Platform",
    version="0.4.0"
)

app.include_router(health_router)
app.include_router(locations_router)
app.include_router(assets_router)


@app.get("/")
def root():
    return {
        "application": settings.app_name,
        "version": "0.4.0",
        "status": "running",
        "modules": [
            "health",
            "spatial_locations",
            "asset_management"
        ]
    }
