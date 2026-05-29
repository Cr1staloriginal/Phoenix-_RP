from aiogram import Router
from aiogram.filters import Command
from aiogram.types import Message
from bot.keyboards import main_menu

router = Router()

@router.message(Command("start"))
async def cmd_start(message: Message):
    await message.answer(
        "👋 Добро пожаловать в **RP Бот**!\n\n"
        "Здесь можно создавать персонажей из Fandom и общаться с ними без цензуры.\n\n"
        "Выбери действие ниже 👇",
        reply_markup=main_menu
    )