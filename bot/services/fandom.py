import aiohttp
from bs4 import BeautifulSoup
import logging
from typing import Optional
from urllib.parse import unquote

logger = logging.getLogger(__name__)

HEADERS = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
    'Accept-Language': 'ru-RU,ru;q=0.9',
    'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9',
}

async def fetch_wiki_page(url: str) -> Optional[dict]:
    try:
        decoded_url = unquote(url)
        async with aiohttp.ClientSession(headers=HEADERS) as session:
            async with session.get(decoded_url, timeout=15) as response:
                if response.status != 200:
                    logger.error(f"HTTP {response.status} для {decoded_url}")
                    return None
                html = await response.text()
                soup = BeautifulSoup(html, 'lxml')
                
                # Заголовок
                title_tag = soup.find('h1', class_='page-header__title')
                if not title_tag:
                    title_tag = soup.find('h1')
                title = title_tag.get_text(strip=True) if title_tag else "Без названия"
                
                # Контент
                content_div = soup.find('div', class_='mw-parser-output')
                if not content_div:
                    content_div = soup.find('div', id='mw-content-text')
                
                description = ""
                if content_div:
                    # Берём первый непустой параграф
                    for p in content_div.find_all('p', recursive=True):
                        text = p.get_text(' ', strip=True)
                        if len(text) > 50 and not text.startswith('['):
                            description = text
                            break
                if not description and content_div:
                    description = content_div.get_text(' ', strip=True)[:500]
                if not description:
                    description = "Описание не найдено."
                
                if len(description) > 800:
                    description = description[:800] + "..."
                
                return {"title": title, "content": description}
    except Exception as e:
        logger.error(f"Ошибка парсинга Fandom: {e}")
        return None
