import httpx
from bs4 import BeautifulSoup

class FandomService:
    async def get_page(self, wiki_base: str, page_title: str):
        url = f"{wiki_base.rstrip('/')}/api.php"
        params = {
            "action": "parse",
            "page": page_title,
            "prop": "text",
            "format": "json",
            "formatversion": 2
        }
        
        try:
            async with httpx.AsyncClient(timeout=30.0) as client:
                resp = await client.get(url, params=params)
                resp.raise_for_status()
                data = resp.json()
                
                if "parse" not in data:
                    return None
                    
                html = data["parse"]["text"]
                soup = BeautifulSoup(html, "html.parser")
                
                # Убираем мусор
                for tag in soup(["script", "style", "nav", "footer", "table", "aside"]):
                    tag.decompose()
                    
                text = soup.get_text(separator="\n", strip=True)
                return {
                    "title": data["parse"]["title"],
                    "content": text[:13000]
                }
        except Exception as e:
            print(f"Fandom error: {e}")
            return None