import aiohttp
from bs4 import BeautifulSoup
import logging
from typing import Optional

logger = logging.getLogger(__name__)

async def fetch_wiki_page(url: str) -> Optional[dict]:
    """
    Парсит страницу Fandom, возвращает заголовок и первый абзац текста.
    """
    try:
        async with aiohttp.ClientSession() as session:
            async with session.get(url, timeout=10) as response:
                if response.status != 200:
                    logger.error(f"Ошибка {response.status} при запросе {url}")
                    return None
                html = await response.text()
                soup = BeautifulSoup(html, 'lxml')
                
                # Заголовок
                title_tag = soup.find('h1', class_='page-header__title')
                if not title_tag:
                    title_tag = soup.find('h1')
                title = title_tag.get_text(strip=True) if title_tag else "Без названия"
                
                # Ищем основной контент – часто в <div class="mw-parser-output">
                content_div = soup.find('div', class_='mw-parser-output')
                if not content_div:
                    content_div = soup.find('div', {'id': 'mw-content-text'})
                if content_div:
                    # Берём первый абзац текста
                    first_p = content_div.find('p')
                    if first_p:
                        text = first_p.get_text(strip=True)
                    else:
                        text = "Описание не найдено."
                else:
                    text = "Не удалось извлечь описание."
                
                # Ограничим длину
                if len(text) > 800:
                    text = text[:800] + "..."
                
                return {"title": title, "content": text}
    except Exception as e:
        logger.error(f"Ошибка парсинга Fandom: {e}")
        return None
