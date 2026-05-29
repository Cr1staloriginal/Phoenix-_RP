import asyncio
import logging
from aiogram import Bot, Dispatcher
from aiogram.fsm.storage.memory import MemoryStorage
from bot.config import settings
from bot.models import engine, Base
from bot.handlers.start import router as start_router
from bot.handlers.character import router as character_router
from bot.handlers.chat import router as chat_router

logging.basicConfig(level=logging.INFO)

bot = Bot(token=settings.BOT_TOKEN)
dp = Dispatcher(storage=MemoryStorage())

dp.include_router(start_router)
dp.include_router(character_router)
dp.include_router(chat_router)

async def main():
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    
    print("🚀 RP Telegram Bot успешно запущен!")
    await dp.start_polling(bot)

if __name__ == "__main__":
    asyncio.run(main())