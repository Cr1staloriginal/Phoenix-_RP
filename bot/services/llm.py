import logging
from langchain_openai import ChatOpenAI
from bot.config import settings

logger = logging.getLogger(__name__)

# Бесплатная LLM для RP (Mistral Small 3.1 24B Instruct)
llm = ChatOpenAI(
    base_url="https://openrouter.ai/api/v1",
    api_key=settings.OPENROUTER_API_KEY,
    model="mistralai/mistral-small-3.1-24b-instruct:free",
    temperature=0.85,          # креативность
    max_tokens=1024,
)

async def generate_character_card(page_data: dict) -> str:
    """
    Генерирует карточку персонажа из данных Fandom.
    """
    title = page_data.get('title', 'Неизвестный персонаж')
    description = page_data.get('content', 'Нет описания.')
    return f"📖 **{title}**\n\n📝 {description}\n\n✨ Импортировано с Fandom"