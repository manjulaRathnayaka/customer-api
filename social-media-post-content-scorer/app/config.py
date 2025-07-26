from pydantic_settings import BaseSettings
from typing import Optional


class Settings(BaseSettings):
    openai_api_key: Optional[str] = None
    max_image_size_mb: int = 10
    request_timeout_seconds: int = 30
    enable_caching: bool = False
    log_level: str = "INFO"
    default_topic: str = "raincoat"

    class Config:
        env_file = ".env"


settings = Settings()
