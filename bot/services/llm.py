from langchain_openai import ChatOpenAI
from langchain_core.messages import HumanMessage
from bot.config import settings

# Бесплатные модели OpenRouter
llm = ChatOpenAI(
    base_url="https://openrouter.ai/api/v1",
    api_key=settings.OPENROUTER_API_KEY,
    model="deepseek/deepseek-r1:free",   # Хорошее качество и бесплатно
    temperature=0.85,
    max_tokens=1500,
)

async def generate_character_card(page_data: dict):
    prompt = f"""Создай качественную карточку персонажа для ролевой игры.

Название: {page_data['title']}
Содержание страницы: {page_data['content'][:9000]}

Верни **только** JSON в следующем формате:
{{
  "name": "Имя персонажа",
  "greeting": "Первое сообщение от персонажа",
  "description": "Краткое описание",
  "personality": "Характер и особенности",
  "system_prompt": "Полная инструкция для ИИ как вести себя от лица этого персонажа"
}}

JSON должен быть валидным."""
    
    try:
        response = await llm.ainvoke([HumanMessage(content=prompt)])
        return response.content
    except Exception as e:
        print(f"LLM Error: {e}")
        return None