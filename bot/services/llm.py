import logging
from langchain_openai import ChatOpenAI
from bot.config import settings

logger = logging.getLogger(__name__)

# Актуальная бесплатная модель
llm = ChatOpenAI(
    base_url="https://openrouter.ai/api/v1",
    api_key=settings.OPENROUTER_API_KEY,
    model="mistralai/mistral-small-3.1-24b-instruct:free",
    temperature=0.85,
    max_tokens=1024,
    timeout=60,
)

async def generate_character_card(page_data: dict) -> str:
    title = page_data.get('title', 'Неизвестный персонаж')
    description = page_data.get('content', 'Нет описания.')
    return f"📖 **{title}**\n\n📝 {description}\n\n✨ Импортировано с Fandom"