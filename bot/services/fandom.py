import aiohttp
import logging
from typing import Optional
from urllib.parse import unquote

logger = logging.getLogger(__name__)

async def fetch_wiki_page(url: str) -> Optional[dict]:
    """
    Извлекает заголовок и описание через Fandom API.
    """
    decoded_url = unquote(url)
    logger.info(f"Загружаем через API: {decoded_url}")
    
    # Извлекаем название статьи из URL
    # Пример: https://tiny-bunny.fandom.com/ru/wiki/Алиса
    if '/wiki/' not in decoded_url:
        logger.error("Неверный формат ссылки. Должно быть .../wiki/Имя")
        return None
    
    article_name = decoded_url.split('/wiki/')[-1]
    
    # API endpoint для Fandom
    api_url = f"https://tiny-bunny.fandom.com/api.php"
    params = {
        "action": "parse",
        "page": article_name,
        "format": "json",
        "prop": "text|displaytitle",
        "redirects": "1"
    }
    
    try:
        async with aiohttp.ClientSession() as session:
            async with session.get(api_url, params=params, timeout=15) as resp:
                if resp.status != 200:
                    logger.error(f"API вернул {resp.status}")
                    return None
                data = await resp.json()
                if "error" in data:
                    logger.error(f"API ошибка: {data['error']}")
                    return None
                
                title = data.get("parse", {}).get("title", "Без названия")
                html_content = data.get("parse", {}).get("text", {}).get("*", "")
                
                # Простой парсинг HTML для извлечения текста (без BeautifulSoup)
                from html.parser import HTMLParser
                class TextExtractor(HTMLParser):
                    def __init__(self):
                        super().__init__()
                        self.text = []
                    def handle_data(self, data):
                        if data.strip():
                            self.text.append(data.strip())
                parser = TextExtractor()
                parser.feed(html_content)
                full_text = " ".join(parser.text)
                # Берём первые ~800 символов
                description = full_text[:800] if len(full_text) > 800 else full_text
                if not description:
                    description = "Описание не найдено."
                
                return {"title": title, "content": description}
    except Exception as e:
        logger.error(f"Ошибка API Fandom: {e}")
        return None