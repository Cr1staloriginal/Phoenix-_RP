import asyncio
import logging
from aiogram import Bot, Dispatcher
from aiogram.fsm.storage.memory import MemoryStorage
from bot.config import settings
from bot.handlers import start_router, character_router, chat_router
from bot.database import init_db

logging.basicConfig(level=logging.INFO)

async def main():
    await init_db()  # создаём таблицы в БД
    bot = Bot(token=settings.BOT_TOKEN)
    storage = MemoryStorage()
    dp = Dispatcher(storage=storage)
    dp.include_routers(start_router, character_router, chat_router)
    await dp.start_polling(bot)

if __name__ == "__main__":
    asyncio.run(main())
