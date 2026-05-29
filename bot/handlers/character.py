import logging
from aiogram import Router, F
from aiogram.types import Message, ReplyKeyboardRemove
from aiogram.fsm.context import FSMContext
from aiogram.fsm.state import State, StatesGroup
from bot.services.fandom import fetch_wiki_page
from bot.services.llm import generate_character_card
from bot.database import async_session
from bot.models import User, Character
from bot.keyboards import main_keyboard, cancel_keyboard

router = Router()
logger = logging.getLogger(__name__)

# Состояния для импорта из Fandom
class ImportFandom(StatesGroup):
    waiting_for_url = State()

# Состояния для ручного создания
class CreateCharacter(StatesGroup):
    waiting_for_name = State()
    waiting_for_description = State()

# --- Импорт из Fandom ---
@router.message(F.text == "🌐 Импорт из Fandom")
async def import_fandom_start(message: Message, state: FSMContext):
    await state.set_state(ImportFandom.waiting_for_url)
    await message.answer(
        "Отправь ссылку на страницу Fandom.\nПример: `https://character.fandom.com/ru/wiki/Гарри_Поттер`\n\n"
        "Или нажми ❌ Отмена",
        reply_markup=cancel_keyboard,
        parse_mode="Markdown"
    )

@router.message(ImportFandom.waiting_for_url, F.text == "❌ Отмена")
async def cancel_import(message: Message, state: FSMContext):
    await state.clear()
    await message.answer("Импорт отменён.", reply_markup=main_keyboard)

@router.message(ImportFandom.waiting_for_url)
async def process_fandom_url(message: Message, state: FSMContext):
    url = message.text.strip()
    if not url.startswith("http"):
        await message.answer("Пожалуйста, отправь корректную ссылку, начинающуюся с http:// или https://")
        return
    
    await message.answer("⏳ Парсинг страницы, подожди...")
    page_data = await fetch_wiki_page(url)
    if not page_data:
        await message.answer("Не удалось загрузить страницу. Проверь ссылку и попробуй снова.")
        return
    
    card = await generate_character_card(page_data)
    # Сохраняем персонажа в БД
    async with async_session() as session:
        user = await session.execute(
            select(User).where(User.telegram_id == message.from_user.id)
        )
        user = user.scalar_one_or_none()
        if not user:
            # на всякий случай создаём
            user = User(telegram_id=message.from_user.id)
            session.add(user)
            await session.commit()
        
        character = Character(
            user_id=user.id,
            name=page_data['title'],
            description=page_data['content']
        )
        session.add(character)
        await session.commit()
    
    await state.clear()
    await message.answer(
        f"✅ Персонаж **{page_data['title']}** успешно импортирован!\n\n{card}",
        reply_markup=main_keyboard,
        parse_mode="Markdown"
    )

# --- Ручное создание персонажа ---
@router.message(F.text == "➕ Создать персонажа")
async def create_character_start(message: Message, state: FSMContext):
    await state.set_state(CreateCharacter.waiting_for_name)
    await message.answer(
        "Введи **имя** персонажа:",
        reply_markup=cancel_keyboard,
        parse_mode="Markdown"
    )

@router.message(CreateCharacter.waiting_for_name, F.text == "❌ Отмена")
async def cancel_create(message: Message, state: FSMContext):
    await state.clear()
    await message.answer("Создание персонажа отменено.", reply_markup=main_keyboard)

@router.message(CreateCharacter.waiting_for_name)
async def process_character_name(message: Message, state: FSMContext):
    name = message.text.strip()
    if len(name) > 50:
        await message.answer("Имя слишком длинное (макс 50 символов). Попробуй снова.")
        return
    await state.update_data(name=name)
    await state.set_state(CreateCharacter.waiting_for_description)
    await message.answer(
        "Теперь введи **описание** персонажа (или отправь '-' для пустого):",
        parse_mode="Markdown"
    )

@router.message(CreateCharacter.waiting_for_description, F.text == "❌ Отмена")
async def cancel_create_desc(message: Message, state: FSMContext):
    await state.clear()
    await message.answer("Создание отменено.", reply_markup=main_keyboard)

@router.message(CreateCharacter.waiting_for_description)
async def process_character_description(message: Message, state: FSMContext):
    data = await state.get_data()
    name = data['name']
    description = message.text.strip()
    if description == "-":
        description = None
    
    async with async_session() as session:
        user = await session.execute(
            select(User).where(User.telegram_id == message.from_user.id)
        )
        user = user.scalar_one_or_none()
        if not user:
            user = User(telegram_id=message.from_user.id)
            session.add(user)
            await session.commit()
        
        character = Character(
            user_id=user.id,
            name=name,
            description=description
        )
        session.add(character)
        await session.commit()
    
    await state.clear()
    await message.answer(
        f"✅ Персонаж **{name}** создан!\n\nОписание: {description if description else 'Отсутствует'}",
        reply_markup=main_keyboard,
        parse_mode="Markdown"
    )
