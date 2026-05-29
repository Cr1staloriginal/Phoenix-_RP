from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    BOT_TOKEN: str
    OPENROUTER_API_KEY: str

    class Config:
        env_file = ".env"

settings = Settings()