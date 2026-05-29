from aiogram.types import ReplyKeyboardMarkup, KeyboardButton, InlineKeyboardMarkup, InlineKeyboardButton

main_keyboard = ReplyKeyboardMarkup(
    keyboard=[
        [KeyboardButton(text="📋 Мои персонажи")],
        [KeyboardButton(text="➕ Создать персонажа")],
        [KeyboardButton(text="🌐 Импорт из Fandom")]
    ],
    resize_keyboard=True
)

# Клавиатура для выбора персонажа (инлайн)
def characters_keyboard(characters):
    buttons = []
    for char in characters:
        buttons.append([InlineKeyboardButton(text=char.name, callback_data=f"select_char_{char.id}")])
    return InlineKeyboardMarkup(inline_keyboard=buttons)

# Кнопка отмены
cancel_keyboard = ReplyKeyboardMarkup(
    keyboard=[[KeyboardButton(text="❌ Отмена")]],
    resize_keyboard=True
)
