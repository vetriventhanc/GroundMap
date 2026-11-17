
import uuid

from datetime import datetime, timezone

from sqlalchemy import (
    DateTime,
    ForeignKey,
    String,
    Text,
    UniqueConstraint,
    Uuid
)

from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.core.database import Base


def utc_now():
    return datetime.now(timezone.utc)


class TimestampMixin:
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        default=utc_now,
        nullable=False
    )

    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        default=utc_now,
        onupdate=utc_now,
        nullable=False
    )


class Workspace(TimestampMixin, Base):
    __tablename__ = "workspaces"

    id: Mapped[uuid.UUID] = mapped_column(
        Uuid(as_uuid=True),
        primary_key=True,
        default=uuid.uuid4
    )

    name: Mapped[str] = mapped_column(
        String(150),
        nullable=False
    )

    description: Mapped[str | None] = mapped_column(
        Text,
        nullable=True
    )

    sites: Mapped[list["Site"]] = relationship(
        back_populates="workspace",
        cascade="all, delete-orphan"
    )


class Site(TimestampMixin, Base):
    __tablename__ = "sites"

    __table_args__ = (
        UniqueConstraint(
            "workspace_id",
            "name",
            name="uq_site_workspace_name"
        ),
    )

    id: Mapped[uuid.UUID] = mapped_column(
        Uuid(as_uuid=True),
        primary_key=True,
        default=uuid.uuid4
    )

    workspace_id: Mapped[uuid.UUID] = mapped_column(
        Uuid(as_uuid=True),
        ForeignKey("workspaces.id", ondelete="CASCADE"),
        nullable=False,
        index=True
    )

    name: Mapped[str] = mapped_column(
        String(150),
        nullable=False
    )

    address: Mapped[str | None] = mapped_column(
        Text,
        nullable=True
    )

    workspace: Mapped["Workspace"] = relationship(
        back_populates="sites"
    )

    buildings: Mapped[list["Building"]] = relationship(
        back_populates="site",
        cascade="all, delete-orphan"
    )


class Building(TimestampMixin, Base):
    __tablename__ = "buildings"

    __table_args__ = (
        UniqueConstraint(
            "site_id",
            "name",
            name="uq_building_site_name"
        ),
    )

    id: Mapped[uuid.UUID] = mapped_column(
        Uuid(as_uuid=True),
        primary_key=True,
        default=uuid.uuid4
    )

    site_id: Mapped[uuid.UUID] = mapped_column(
        Uuid(as_uuid=True),
        ForeignKey("sites.id", ondelete="CASCADE"),
        nullable=False,
        index=True
    )

    name: Mapped[str] = mapped_column(
        String(150),
        nullable=False
    )

    description: Mapped[str | None] = mapped_column(
        Text,
        nullable=True
    )

    site: Mapped["Site"] = relationship(
        back_populates="buildings"
    )

    floors: Mapped[list["Floor"]] = relationship(
        back_populates="building",
        cascade="all, delete-orphan"
    )


class Floor(TimestampMixin, Base):
    __tablename__ = "floors"

    __table_args__ = (
        UniqueConstraint(
            "building_id",
            "name",
            name="uq_floor_building_name"
        ),
    )

    id: Mapped[uuid.UUID] = mapped_column(
        Uuid(as_uuid=True),
        primary_key=True,
        default=uuid.uuid4
    )

    building_id: Mapped[uuid.UUID] = mapped_column(
        Uuid(as_uuid=True),
        ForeignKey("buildings.id", ondelete="CASCADE"),
        nullable=False,
        index=True
    )

    name: Mapped[str] = mapped_column(
        String(100),
        nullable=False
    )

    floor_number: Mapped[int] = mapped_column(
        nullable=False
    )

    building: Mapped["Building"] = relationship(
        back_populates="floors"
    )

    rooms: Mapped[list["Room"]] = relationship(
        back_populates="floor",
        cascade="all, delete-orphan"
    )


class Room(TimestampMixin, Base):
    __tablename__ = "rooms"

    __table_args__ = (
        UniqueConstraint(
            "floor_id",
            "name",
            name="uq_room_floor_name"
        ),
    )

    id: Mapped[uuid.UUID] = mapped_column(
        Uuid(as_uuid=True),
        primary_key=True,
        default=uuid.uuid4
    )

    floor_id: Mapped[uuid.UUID] = mapped_column(
        Uuid(as_uuid=True),
        ForeignKey("floors.id", ondelete="CASCADE"),
        nullable=False,
        index=True
    )

    name: Mapped[str] = mapped_column(
        String(100),
        nullable=False
    )

    description: Mapped[str | None] = mapped_column(
        Text,
        nullable=True
    )

    floor: Mapped["Floor"] = relationship(
        back_populates="rooms"
    )
