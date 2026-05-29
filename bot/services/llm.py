from langchain_openai import ChatOpenAI
from langchain_core.messages import SystemMessage, HumanMessage, AIMessage
from bot.config import settings

llm = ChatOpenAI(
    base_url="https://openrouter.ai/api/v1",
    api_key=settings.OPENROUTER_API_KEY,
    model="deepseek/deepseek-r1:free",   # бесплатная и довольно умная
    temperature=0.85,
    max_tokens=1200,
)

async def get_character_response(character, history: list, user_message: str):
    """Генерирует ответ от лица персонажа"""
    system_content = character.system_prompt or f"Ты — {character.name}. Отвечай естественно."
    
    messages = [SystemMessage(content=system_content)]
    
    # Добавляем историю
    for msg in history[-10:]:  # последние 10 сообщений
        if msg["role"] == "user":
            messages.append(HumanMessage(content=msg["content"]))
        else:
            messages.append(AIMessage(content=msg["content"]))
    
    messages.append(HumanMessage(content=user_message))
    
    try:
        response = await llm.ainvoke(messages)
        return response.content
    except Exception as e:
        print(f"LLM Error: {e}")
        return "Извини, я сейчас немного перегружен... Попробуй позже."
