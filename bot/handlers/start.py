from aiogram import Router
from aiogram.filters import Command
from aiogram.types import Message
from bot.keyboards import main_keyboard
from bot.database import async_session
from bot.models import User

router = Router()

@router.message(Command("start"))
async def start_command(message: Message):
    # Сохраняем пользователя в БД
    async with async_session() as session:
        user = await session.get(User, message.from_user.id)
        if not user:
            user = User(telegram_id=message.from_user.id)
            session.add(user)
            await session.commit()
    await message.answer(
        "🌟 Добро пожаловать в RP-бота!\n\n"
        "Ты можешь:\n"
        "📋 Список своих персонажей\n"
        "➕ Создать персонажа вручную\n"
        "🌐 Импортировать персонажа с Fandom\n\n"
        "После создания выбери персонажа для общения.",
        reply_markup=main_keyboard
    )
