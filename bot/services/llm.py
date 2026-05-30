import logging
from langchain_openai import ChatOpenAI
from bot.config import settings

logger = logging.getLogger(__name__)

# Динамический роутер: сам подставит любую работающую бесплатную модель
llm = ChatOpenAI(
    base_url="https://openrouter.ai/api/v1",
    api_key=settings.OPENROUTER_API_KEY,
    model="openrouter/free",   # ← главное изменение
    temperature=0.85,
    max_tokens=1024,
    timeout=60,
)

async def generate_character_card(page_data: dict) -> str:
    title = page_data.get('title', 'Неизвестный персонаж')
    description = page_data.get('content', 'Нет описания.')
    return f"📖 **{title}**\n\n📝 {description}\n\n✨ Импортировано с Fandom"