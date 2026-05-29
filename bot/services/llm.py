import logging
from langchain_openai import ChatOpenAI
from bot.config import settings

logger = logging.getLogger(__name__)

# ✅ БЕСПЛАТНАЯ МОДЕЛЬ: OpenRouter сам подставит лучшую бесплатную
llm = ChatOpenAI(
    base_url="https://openrouter.ai/api/v1",
    api_key=settings.OPENROUTER_API_KEY,
    model="openrouter/free",  # ← КЛЮЧЕВОЕ ИЗМЕНЕНИЕ: бесплатный роутер
    temperature=0.7,
)

async def generate_character_card(page_data: dict) -> str:
    """
    Генерирует карточку персонажа из сырых данных Fandom.
    """
    title = page_data.get('title', 'Без названия')
    content = page_data.get('content', '')
    # Обрезаем текст для красоты
    if len(content) > 500:
        content = content[:500] + '...'
    
    return f"📖 **{title}**\n\n{content}\n\n✨ Импортировано с Fandom"
