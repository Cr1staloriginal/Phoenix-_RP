import aiohttp
import logging
import re
from typing import Optional
from urllib.parse import unquote

logger = logging.getLogger(__name__)

async def fetch_wiki_page(url: str) -> Optional[dict]:
    decoded_url = unquote(url)
    logger.info(f"Загружаем через API: {decoded_url}")
    
    # Извлекаем имя статьи и домен
    match = re.search(r'https?://([^/]+)/wiki/(.+)', decoded_url)
    if not match:
        logger.error("Неверный формат ссылки Fandom")
        return None
    domain = match.group(1)
    article = match.group(2)
    
    api_url = f"https://{domain}/api.php"
    params = {
        "action": "parse",
        "page": article,
        "format": "json",
        "prop": "text|displaytitle",
        "redirects": "1"
    }
    
    try:
        async with aiohttp.ClientSession() as session:
            async with session.get(api_url, params=params, timeout=15) as resp:
                if resp.status != 200:
                    logger.error(f"API ответил {resp.status}")
                    return None
                data = await resp.json()
                if "error" in data:
                    logger.error(f"API ошибка: {data['error']}")
                    return None
                title = data.get("parse", {}).get("title", "Без названия")
                html_content = data.get("parse", {}).get("text", {}).get("*", "")
                # Очищаем от HTML-тегов
                clean = re.sub(r'<[^>]+>', ' ', html_content)
                clean = re.sub(r'\s+', ' ', clean).strip()
                description = clean[:800] if len(clean) > 800 else clean
                if not description:
                    description = "Описание не найдено."
                return {"title": title, "content": description}
    except Exception as e:
        logger.error(f"Ошибка API: {e}")
        return None