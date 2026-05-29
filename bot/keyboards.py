from aiogram.types import ReplyKeyboardMarkup, KeyboardButton

main_menu = ReplyKeyboardMarkup(
    keyboard=[
        [KeyboardButton(text="👤 Мои персонажи")],
        [KeyboardButton(text="➕ Создать персонажа")],
        [KeyboardButton(text="🌐 Импорт из Fandom")],
    ],
    resize_keyboard=True,
    input_field_placeholder="Выберите действие..."
)