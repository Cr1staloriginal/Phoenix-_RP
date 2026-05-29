from aiogram import Router, F
from aiogram.types import Message
from sqlalchemy import select
from bot.services.llm import llm
from bot.database import async_session
from bot.models import User, Character, ChatMessage
from datetime import datetime
import logging

router = Router()
logger = logging.getLogger(__name__)

@router.message(F.text == "📋 Мои персонажи")
async def my_characters(message: Message):
    async with async_session() as session:
        result = await session.execute(
            select(Character).where(Character.user_id == message.from_user.id)
        )
        chars = result.scalars().all()
        if not chars:
            await message.answer("У вас пока нет персонажей. Создайте или импортируйте!")
        else:
            text = "\n".join([f"🔹 {c.name}" for c in chars])
            await message.answer(f"Ваши персонажи:\n{text}")

@router.message()
async def chat_with_character(message: Message):
    # Проверяем, есть ли персонаж у пользователя
    async with async_session() as session:
        result = await session.execute(
            select(Character).where(Character.user_id == message.from_user.id)
        )
        character = result.scalars().first()
        if not character:
            await message.answer("Сначала создай или импортируй персонажа.")
            return
        
        # Получаем последние 10 сообщений для контекста, сортируя по дате
        history_result = await session.execute(
            select(ChatMessage)
            .where(ChatMessage.character_id == character.id)
            .order_by(ChatMessage.created_at.asc())
            .limit(10)
        )
        history = history_result.scalars().all()
        
        # Сохраняем сообщение пользователя
        user_msg = ChatMessage(
            user_id=message.from_user.id,
            character_id=character.id,
            role="user",
            content=message.text,
            created_at=datetime.utcnow()
        )
        session.add(user_msg)
        
        # Формируем промпт
        messages = []
        if character.description:
            messages.append({"role": "system", "content": f"Ты — {character.name}. {character.description}"})
        else:
            messages.append({"role": "system", "content": f"Ты — {character.name}. Отвечай в его стиле, коротко."})
        
        # Добавляем историю
        for msg in history:
            role = "assistant" if msg.role == "assistant" else "user"
            messages.append({"role": role, "content": msg.content})
        
        messages.append({"role": "user", "content": message.text})
        
        try:
            # Запрос к LLM
            response = await llm.ainvoke(messages)
            answer = response.content
            
            # Сохраняем ответ бота
            bot_msg = ChatMessage(
                user_id=message.from_user.id,
                character_id=character.id,
                role="assistant",
                content=answer,
                created_at=datetime.utcnow()
            )
            session.add(bot_msg)
            await session.commit()
            
            await message.answer(answer)
        except Exception as e:
            logger.error(f"Ошибка при обращении к LLM: {e}")
            await message.answer("Извини, произошла ошибка. Попробуй позже.")
