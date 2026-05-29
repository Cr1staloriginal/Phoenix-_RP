from aiogram import Router
from aiogram.filters import Command
from aiogram.types import Message
from bot.keyboards import main_keyboard

router = Router()

@router.message(Command("start"))
async def start_command(message: Message):
    await message.answer(
        "Привет! Я бот для RP-игр с LLM. Создавай персонажей и общайся с ними!\nИспользуй кнопки ниже:",
        reply_markup=main_keyboard
    )
