import aiohttp
import logging
from typing import Optional

logger = logging.getLogger(__name__)

async def fetch_wiki_page(url: str) -> Optional[dict]:
    """
    Парсит страницу Fandom и возвращает заголовок и текст.
    """
    try:
        async with aiohttp.ClientSession() as session:
            async with session.get(url) as response:
                if response.status != 200:
                    logger.error(f"Ошибка загрузки страницы: {response.status}")
                    return None
                html = await response.text()
                # TODO: реальный парсинг с BeautifulSoup
                return {"title": "Test", "content": html[:500]}
    except Exception as e:
        logger.error(f"Ошибка при запросе к Fandom: {e}")
        return None
