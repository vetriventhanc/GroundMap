
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
    created_at: datetime
    updated_at: datetime
