import logging
from langchain_openai import ChatOpenAI
from bot.config import settings

logger = logging.getLogger(__name__)

# Проверяем, есть ли ключ
if not settings.OPENROUTER_API_KEY:
    logger.error("OPENROUTER_API_KEY не задан в .env файле!")

llm = ChatOpenAI(
    base_url="https://openrouter.ai/api/v1",
    api_key=settings.OPENROUTER_API_KEY,
    model="google/gemini-2.0-flash-exp:free",  # стабильная бесплатная модель
    temperature=0.85,
    max_tokens=1024,
    timeout=60,
)

async def generate_character_card(page_data: dict) -> str:
    title = page_data.get('title', 'Неизвестный персонаж')
    description = page_data.get('content', 'Нет описания.')
    return f"📖 **{title}**\n\n📝 {description}\n\n✨ Импортировано с Fandom"