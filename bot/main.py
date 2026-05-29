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
    # Создаём сессию с большими таймаутами и keepalive
    session = AiohttpSession(
        timeout=60,
        keep_alive=True,
    )
    bot = Bot(token=settings.BOT_TOKEN, session=session)
    storage = MemoryStorage()
    dp = Dispatcher(storage=storage)
    dp.include_routers(start_router, character_router, chat_router)
    
    # Запускаем polling с автоматическим переподключением
    await dp.start_polling(
        bot,
        allowed_updates=["message", "callback_query"],
        skip_updates=True,
        polling_timeout=60,
    )

if __name__ == "__main__":
    try:
        asyncio.run(main())
    except (KeyboardInterrupt, SystemExit):
        logging.info("Бот остановлен")
