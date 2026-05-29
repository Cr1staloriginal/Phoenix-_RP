from aiogram.types import ReplyKeyboardMarkup, KeyboardButton

main_keyboard = ReplyKeyboardMarkup(
    keyboard=[
        [KeyboardButton(text="📋 Мои персонажи")],
        [KeyboardButton(text="➕ Создать персонажа")],
        [KeyboardButton(text="🌐 Импорт из Fandom")]
    ],
    resize_keyboard=True
)
