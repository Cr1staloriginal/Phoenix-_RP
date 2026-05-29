import asyncio
import logging
from aiogram import Bot, Dispatcher
from aiogram.client.session.aiohttp import AiohttpSession
from aiogram.fsm.storage.memory import MemoryStorage
from bot.config import settings
from bot.handlers import start_router, character_router, chat_router
from bot.database import init_db

logging.basicConfig(level=logging.INFO)

async def main():
    await init_db()
    session = AiohttpSession(timeout=60)
    bot = Bot(token=settings.BOT_TOKEN, session=session)
    storage = MemoryStorage()
    dp = Dispatcher(storage=storage)
    dp.include_routers(start_router, character_router, chat_router)
    await dp.start_polling(bot, allowed_updates=["message", "callback_query"])

if __name__ == "__main__":
    asyncio.run(main())