import aiohttp
from bs4 import BeautifulSoup
import logging
from typing import Optional
from urllib.parse import unquote

logger = logging.getLogger(__name__)

# Максимально похожи на реальный браузер
HEADERS = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
    'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8',
    'Accept-Language': 'ru-RU,ru;q=0.9,en;q=0.8',
    'Accept-Encoding': 'gzip, deflate, br',
    'Connection': 'keep-alive',
    'Upgrade-Insecure-Requests': '1',
    'Sec-Fetch-Dest': 'document',
    'Sec-Fetch-Mode': 'navigate',
    'Sec-Fetch-Site': 'none',
    'Sec-Fetch-User': '?1',
    'Cache-Control': 'max-age=0',
}

async def fetch_wiki_page(url: str) -> Optional[dict]:
    decoded_url = unquote(url)
    logger.info(f"Загружаем: {decoded_url}")
    
    # Пробуем разные варианты заголовков, если первый не сработает
    for attempt in range(2):
        try:
            # Иногда помогает смена User-Agent на более старый
            if attempt == 1:
                HEADERS['User-Agent'] = 'Mozilla/5.0 (Windows NT 10.0; rv:91.0) Gecko/20100101 Firefox/91.0'
            
            async with aiohttp.ClientSession(headers=HEADERS) as session:
                async with session.get(decoded_url, timeout=15) as response:
                    if response.status == 200:
                        html = await response.text()
                        break
                    else:
                        logger.error(f"Попытка {attempt+1}: статус {response.status}")
                        if attempt == 1:
                            return None
                        continue
        except Exception as e:
            logger.error(f"Попытка {attempt+1} ошибка: {e}")
            if attempt == 1:
                return None
    
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
