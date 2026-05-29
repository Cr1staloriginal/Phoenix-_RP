import logging
from aiogram import Router, F
from aiogram.types import Message, CallbackQuery
from sqlalchemy import select, update
from bot.services.llm import llm
from bot.database import async_session
from bot.models import User, Character, ChatMessage
from bot.keyboards import main_keyboard, characters_keyboard
from datetime import datetime

router = Router()
logger = logging.getLogger(__name__)

# --- Список моих персонажей ---
@router.message(F.text == "📋 Мои персонажи")
async def my_characters(message: Message):
    async with async_session() as session:
        user = await session.execute(
            select(User).where(User.telegram_id == message.from_user.id)
        )
        user = user.scalar_one_or_none()
        if not user:
            await message.answer("Сначала нажми /start")
            return
        chars = await session.execute(
            select(Character).where(Character.user_id == user.id)
        )
        chars = chars.scalars().all()
        if not chars:
            await message.answer("У тебя пока нет персонажей. Создай или импортируй.")
            return
        # Показываем клавиатуру для выбора
        await message.answer(
            "Выбери персонажа для общения:",
            reply_markup=characters_keyboard(chars)
        )

# --- Обработка выбора персонажа (инлайн) ---
@router.callback_query(lambda c: c.data.startswith("select_char_"))
async def select_character(callback: CallbackQuery):
    char_id = int(callback.data.split("_")[-1])
    async with async_session() as session:
        # Обновляем active_character_id у пользователя
        user = await session.execute(
            select(User).where(User.telegram_id == callback.from_user.id)
        )
        user = user.scalar_one_or_none()
        if user:
            user.active_character_id = char_id
            await session.commit()
    await callback.answer(f"✅ Персонаж выбран!")
    await callback.message.answer("Теперь можешь писать сообщения – я отвечу от лица персонажа.", reply_markup=main_keyboard)
    await callback.message.delete()  # убираем клавиатуру выбора

# --- Чат с выбранным персонажем ---
@router.message(F.text)
async def chat_with_character(message: Message):
    if message.text in ["📋 Мои персонажи", "➕ Создать персонажа", "🌐 Импорт из Fandom"]:
        return  # эти сообщения обрабатываются другими роутерами
    
    async with async_session() as session:
        # Находим пользователя
        user = await session.execute(
            select(User).where(User.telegram_id == message.from_user.id)
        )
        user = user.scalar_one_or_none()
        if not user or not user.active_character_id:
            await message.answer(
                "Сначала выбери персонажа через команду 📋 Мои персонажи"
            )
            return
        
        # Загружаем персонажа
        character = await session.get(Character, user.active_character_id)
        if not character:
            await message.answer("Персонаж не найден. Выбери другого.")
            return
        
        # Сохраняем сообщение пользователя
        user_msg = ChatMessage(
            user_id=user.id,
            character_id=character.id,
            role="user",
            content=message.text,
            created_at=datetime.utcnow()
        )
        session.add(user_msg)
        
        # Получаем последние 15 сообщений истории
        history_result = await session.execute(
            select(ChatMessage)
            .where(ChatMessage.character_id == character.id)
            .order_by(ChatMessage.created_at.asc())
            .limit(15)
        )
        history = history_result.scalars().all()
        
        # Формируем список для LLM
        messages = []
        if character.description:
            messages.append({"role": "system", "content": f"Ты — {character.name}. {character.description} Отвечай от первого лица, в роли персонажа."})
        else:
            messages.append({"role": "system", "content": f"Ты — {character.name}. Отвечай в его стиле, коротко и естественно."})
        
        for msg in history:
            role = "assistant" if msg.role == "assistant" else "user"
            messages.append({"role": role, "content": msg.content})
        
        # Добавляем текущее сообщение (оно ещё не в истории)
        messages.append({"role": "user", "content": message.text})
        
        try:
            # Запрос к бесплатной LLM
            response = await llm.ainvoke(messages)
            answer = response.content
            
            # Сохраняем ответ бота
            bot_msg = ChatMessage(
                user_id=user.id,
                character_id=character.id,
                role="assistant",
                content=answer,
                created_at=datetime.utcnow()
            )
            session.add(bot_msg)
            await session.commit()
            
            await message.answer(answer)
        except Exception as e:
            logger.error(f"LLM ошибка: {e}")
            await message.answer("❌ Ошибка при генерации ответа. Попробуй позже.")
