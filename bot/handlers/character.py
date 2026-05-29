from aiogram import Router, F
from aiogram.types import Message
from aiogram.fsm.context import FSMContext
from aiogram.fsm.state import State, StatesGroup
from sqlalchemy import select
from bot.models import async_session, Character
from bot.services.fandom import FandomService
from bot.services.llm import generate_character_card
from bot.keyboards import main_menu

router = Router()

class ImportFandom(StatesGroup):
    waiting_wiki = State()
    waiting_page = State()

@router.message(F.text == "🌐 Импорт из Fandom")
async def start_import(message: Message, state: FSMContext):
    await message.answer("🔗 Отправь **ссылку на вики** (например: `https://witcher.fandom.com/ru`)")
    await state.set_state(ImportFandom.waiting_wiki)

@router.message(ImportFandom.waiting_wiki)
async def process_wiki(message: Message, state: FSMContext):
    await state.update_data(wiki=message.text.strip())
    await message.answer("📝 Теперь отправь **точное название страницы** персонажа\nПример: `Геральт из Ривии`")
    await state.set_state(ImportFandom.waiting_page)

@router.message(ImportFandom.waiting_page)
async def process_page(message: Message, state: FSMContext):
    data = await state.get_data()
    wiki_url = data['wiki']
    page_title = message.text.strip()

    await message.answer("⏳ Загружаю данные из Fandom и создаю персонажа...")

    fandom = FandomService()
    page = await fandom.get_page(wiki_url, page_title)

    if not page:
        await message.answer("❌ Не удалось получить данные с Fandom. Проверь ссылку и название.")
        await state.clear()
        return

    await message.answer("🤖 Генерирую карточку персонажа через ИИ...")

    card_text = await generate_character_card(page)
    
    async with async_session() as session:
        char = Character(
            name=page['title'],
            greeting=f"Привет! Я {page['title']}. Чем могу помочь?",
            description=page.get('content', '')[:800],
            personality="Создан автоматически из Fandom",
            system_prompt=f"Ты — {page['title']}. Отвечай естественно и в стиле персонажа.",
            fandom_wiki=wiki_url
        )
        session.add(char)
        await session.commit()
        await session.refresh(char)

    await message.answer(
        f"✅ **Персонаж успешно создан!**\n\n"
        f"Имя: **{char.name}**\n"
        f"Можешь начать с ним чат, написав его имя.",
        reply_markup=main_menu
    )
    await state.clear()