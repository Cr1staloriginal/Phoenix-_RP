import logging
from langchain_openai import ChatOpenAI
from bot.config import settings

logger = logging.getLogger(__name__)

# Бесплатная модель через OpenRouter
llm = ChatOpenAI(
    base_url="https://openrouter.ai/api/v1",
    api_key=settings.OPENROUTER_API_KEY,
    model="openrouter/free",
    temperature=0.8,
)

async def generate_character_card(page_data: dict) -> str:
    """
    Генерирует карточку персонажа на основе данных с Fandom.
    Можно заменить на LLM-генерацию, но для скорости – форматирование.
    """
    title = page_data.get('title', 'Неизвестный персонаж')
    description = page_data.get('content', 'Нет описания.')
    return f"📖 **{title}**\n\n📝 {description}\n\n✨ Импортировано с Fandom"

async def generate_character_description(name: str, user_input: str = "") -> str:
    """
    Опционально: LLM генерирует описание по имени. Но мы не будем навязывать.
    """
    # Можно вернуть заглушку или вызвать LLM
    return f"Это {name}. {user_input}" if user_input else f"Персонаж по имени {name}."
