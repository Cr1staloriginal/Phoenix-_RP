import asyncio
asyncio.set_event_loop_policy(asyncio.WindowsSelectorEventLoopPolicy())
import asyncio
from bot.main import main

if __name__ == "__main__":
    asyncio.run(main())
