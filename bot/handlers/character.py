import logging
from aiogram import Router, F
from aiogram.types import Message, ReplyKeyboardRemove
from bot.services.fandom import fetch_wiki_page
from bot.services.llm import generate_character_card

router = Router()
logger = logging.getLogger(__name__)

# 🌐 Импорт из Fandom
@router.message(F.text == "🌐 Импорт из Fandom")
async def import_from_fandom(message: Message):
    await message.answer("Отправь мне прямую ссылку на страницу вики (Fandom).\nПример: `https://character.fandom.com/ru/wiki/Имя_персонажа`", reply_markup=ReplyKeyboardRemove())
    # Здесь должна быть логика сохранения состояния, ожидающая ссылку

@router.message(F.text == "➕ Создать персонажа")
async def create_character(message: Message):
    await message.answer("Функция создания персонажа в разработке. Пока можно импортировать из Fandom!")
