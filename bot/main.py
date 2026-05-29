import asyncio
import logging
from aiogram import Bot, Dispatcher
from bot.config import settings
from bot.handlers import start_router, character_router, chat_router

logging.basicConfig(level=logging.INFO)

async def main():
    bot = Bot(token=settings.BOT_TOKEN)
    dp = Dispatcher()
    dp.include_routers(start_router, character_router, chat_router)
    await dp.start_polling(bot)

if __name__ == "__main__":
    asyncio.run(main())
