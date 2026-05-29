from aiogram import Router, F
from aiogram.types import Message
from sqlalchemy import select
from bot.models import async_session, Character, ChatMessage
from bot.services.llm import get_character_response

router = Router()

# Храним текущего персонажа пользователя
user_current_char = {}   # {user_id: character_id}

@router.message(F.text == "👤 Мои персонажи")
async def my_characters(message: Message):
    async with async_session() as session:
        result = await session.execute(select(Character))
        chars = result.scalars().all()
        
        if not chars:
            await message.answer("У тебя пока нет персонажей. Импортируй кого-нибудь из Fandom!")
            return
        
        text = "📋 **Твои персонажи:**\n\n"
        for c in chars:
            text += f"• {c.name}\n"
        text += "\nНапиши имя персонажа, чтобы начать с ним чат."
        await message.answer(text)

@router.message()
async def handle_chat(message: Message):
    user_id = message.from_user.id
    user_text = message.text.strip()

    # Проверяем, выбрал ли пользователь персонажа по имени
    async with async_session() as session:
        result = await session.execute(
            select(Character).where(Character.name.ilike(f"%{user_text}%"))
        )
        char = result.scalar_one_or_none()

        if char:
            user_current_char[user_id] = char.id
            greeting = char.greeting or f"Привет! Я {char.name}."
            await message.answer(f"✅ **{char.name}**\n\n{greeting}")
            return

    # Если персонаж выбран — общаемся
    if user_id not in user_current_char:
        await message.answer("Сначала выбери персонажа (напиши его имя) или импортируй нового.")
        return

    char_id = user_current_char[user_id]

    # Загружаем персонажа
    async with async_session() as session:
        result = await session.execute(select(Character).where(Character.id == char_id))
        character = result.scalar_one_or_none()

        if not character:
            await message.answer("Персонаж не найден.")
            return

        # Загружаем историю
        history_result = await session.execute(
            select(ChatMessage)
            .where(ChatMessage.user_id == user_id, ChatMessage.character_id == char_id)
            .order_by(ChatMessage.id.asc())
        )
        history = [{"role": m.role, "content": m.content} for m in history_result.scalars().all()]

        # Сохраняем сообщение пользователя
        user_msg = ChatMessage(user_id=user_id, character_id=char_id, role="user", content=user_text)
        session.add(user_msg)
        await session.commit()

    await message.answer("💭 Персонаж думает...")

    # Получаем ответ от LLM
    response_text = await get_character_response(character, history, user_text)

    # Сохраняем ответ
    async with async_session() as session:
        assistant_msg = ChatMessage(
            user_id=user_id, 
            character_id=char_id, 
            role="assistant", 
            content=response_text
        )
        session.add(assistant_msg)
        await session.commit()

    await message.answer(response_text)
