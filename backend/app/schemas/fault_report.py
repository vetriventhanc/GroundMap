
from datetime import datetime
from typing import Literal
from uuid import UUID

from pydantic import BaseModel, ConfigDict, Field


FaultSeverity = Literal[
    "low",
    "medium",
    "high",
    "critical"
]


class FaultReportCreate(BaseModel):
    title: str = Field(min_length=3, max_length=150)
    description: str = Field(min_length=5)
    severity: FaultSeverity = "medium"


class FaultReportResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: UUID
    asset_id: UUID
    title: str
    description: str
    severity: str
    status: str
    resolution_notes: str | None = None
    created_at: datetime
    updated_at: datetime

FaultReportStatus = Literal[
    "open",
    "in_progress",
    "resolved",
]


class FaultReportUpdate(BaseModel):
    status: FaultReportStatus
    resolution_notes: str | None = Field(
        default=None,
        max_length=5000,
    )