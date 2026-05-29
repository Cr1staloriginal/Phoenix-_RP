import cloudscraper
from bs4 import BeautifulSoup
import logging
from typing import Optional
from urllib.parse import unquote
import asyncio

logger = logging.getLogger(__name__)

# Создаём scraper один раз (синхронный, но будем запускать в потоке)
scraper = cloudscraper.create_scraper()

async def fetch_wiki_page(url: str) -> Optional[dict]:
    decoded_url = unquote(url)
    logger.info(f"Загружаем через cloudscraper: {decoded_url}")
    
    loop = asyncio.get_event_loop()
    try:
        # Запускаем синхронный запрос в отдельном потоке
        response = await loop.run_in_executor(None, scraper.get, decoded_url)
        if response.status_code != 200:
            logger.error(f"HTTP {response.status_code} для {decoded_url}")
            return None
        
        html = response.text
        soup = BeautifulSoup(html, 'lxml')
        
        # Заголовок
        title_tag = soup.find('h1', class_='page-header__title') or soup.find('h1')
        title = title_tag.get_text(strip=True) if title_tag else "Без названия"
        
        # Контент
        content_div = soup.find('div', class_='mw-parser-output') or soup.find('div', id='mw-content-text')
        description = ""
        if content_div:
            for p in content_div.find_all('p', recursive=True):
                text = p.get_text(' ', strip=True)
                if len(text) > 50 and not text.startswith('['):
                    description = text
                    break
            if not description:
                description = content_div.get_text(' ', strip=True)[:500]
        if not description:
            description = "Описание не найдено."
        
        if len(description) > 800:
            description = description[:800] + "..."
        
        return {"title": title, "content": description}
    except Exception as e:
        logger.error(f"Ошибка cloudscraper: {e}")
        return None