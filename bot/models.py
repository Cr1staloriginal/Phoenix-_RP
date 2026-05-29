from sqlalchemy import Column, Integer, String, Text, JSON
from sqlalchemy.ext.asyncio import create_async_engine, async_sessionmaker, AsyncAttrs
from sqlalchemy.orm import DeclarativeBase

engine = create_async_engine("sqlite+aiosqlite:///./rp_bot.db", echo=False)
async_session = async_sessionmaker(engine, expire_on_commit=False)

class Base(AsyncAttrs, DeclarativeBase):
    pass

class Character(Base):
    __tablename__ = "characters"
    
    id = Column(Integer, primary_key=True)
    name = Column(String, nullable=False)
    avatar_url = Column(String, nullable=True)
    greeting = Column(Text, nullable=True)
    description = Column(Text, nullable=True)
    personality = Column(Text, nullable=True)
    system_prompt = Column(Text, nullable=True)
    example_dialogue = Column(Text, nullable=True)
    lorebook = Column(JSON, default=list)
    fandom_wiki = Column(String, nullable=True)