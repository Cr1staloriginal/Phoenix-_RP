from aiogram import Router, F
from aiogram.types import Message
from sqlalchemy import select
from bot.models import async_session, Character

router = Router()

user_current_char = {}  # {user_id: character_id}

@router.message(F.text == "👤 Мои персонажи")
async def my_characters(message: Message):
    async with async_session() as session:
        result = await session.execute(select(Character))
        chars = result.scalars().all()
        
        if not chars:
            await message.answer("У тебя пока нет созданных персонажей.")
            return
        
        text = "📋 **Твои персонажи:**\n\n"
        for c in chars:
            text += f"• {c.name}\n"
        text += "\nНапиши имя персонажа, чтобы начать общение."
        await message.answer(text)

@router.message()
async def handle_chat(message: Message):
    user_id = message.from_user.id
    text = message.text.strip()

    # Проверка, выбрал ли пользователь персонажа
    async with async_session() as session:
        result = await session.execute(
            select(Character).where(Character.name.ilike(f"%{text}%"))
        )
        char = result.scalar_one_or_none()

        if char:
            user_current_char[user_id] = char.id
            greeting = char.greeting or f"Привет! Я {char.name}."
            await message.answer(f"✅ Вы выбрали персонажа **{char.name}**\n\n{greeting}")
            return

    # Если уже выбран персонаж — общаемся
    if user_id in user_current_char:
        # Пока заглушка. Позже сюда подключим настоящий LLM
        await message.answer("💬 Персонаж думает над ответом...")
    else:
        await message.answer("Выбери персонажа из списка или импортируй нового через меню.")