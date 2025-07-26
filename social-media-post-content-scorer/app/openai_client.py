import httpx
import logging
from openai import AsyncOpenAI
from .config import settings

logger = logging.getLogger(__name__)



class OpenAIClient:
    def __init__(self):
        if not settings.openai_api_key:
            raise ValueError(
                "OPENAI_API_KEY environment variable is required. "
                "Please set it in your environment or .env file."
            )
        self.settings = settings
        self.client = AsyncOpenAI(
            api_key=settings.openai_api_key,
            timeout=settings.request_timeout_seconds
        )
        self.http_client = httpx.AsyncClient(timeout=settings.request_timeout_seconds)

    async def analyze_text(self, prompt: str) -> str:
        """Analyze text using GPT-3.5-turbo."""
        try:
            response = await self.client.chat.completions.create(
                model="gpt-3.5-turbo",
                messages=[
                    {
                        "role": "system",
                "content": f"Analyze social media content for the configured topic. Respond with JSON only."
                    },
                    {
                        "role": "user",
                        "content": prompt
                    }
                ],
                max_tokens=100,  # Minimal tokens for testing
                temperature=0.1
            )

            content = response.choices[0].message.content
            logger.info(f"Text analysis completed, tokens: {response.usage.total_tokens}")
            return content

        except Exception as e:
            logger.error(f"OpenAI text analysis failed: {str(e)}")
            raise Exception(f"Text analysis failed: {str(e)}")

    async def analyze_image(self, image_url: str, prompt: str) -> str:
        """Analyze image using GPT-4 Vision."""
        try:
            # First, validate the image URL is accessible
            await self._validate_image_url(image_url)

            response = await self.client.chat.completions.create(
                model="gpt-4o-mini",
                messages=[
                    {
                        "role": "system",
                        "content": "You are an expert at analyzing images for specific content. Always respond with valid JSON as requested."
                    },
                    {
                        "role": "user",
                        "content": [
                            {
                                "type": "text",
                                "text": prompt
                            },
                            {
                                "type": "image_url",
                                "image_url": {
                                    "url": image_url,
                                    "detail": "low"
                                }
                            }
                        ]
                    }
                ],
                max_tokens=150,  # Allow enough tokens for complete JSON response
                temperature=0.1
            )

            content = response.choices[0].message.content
            logger.info(f"Image analysis completed, tokens: {response.usage.total_tokens}")
            return content

        except Exception as e:
            logger.error(f"OpenAI image analysis failed: {str(e)}")
            raise Exception(f"Image analysis failed: {str(e)}")

    async def _validate_image_url(self, image_url: str) -> None:
        """Validate that the image URL is accessible and within size limits."""
        try:
            async with self.http_client.stream("GET", image_url) as response:
                if response.status_code != 200:
                    raise Exception(f"Image URL returned status {response.status_code}")

                content_type = response.headers.get("content-type", "")
                if not content_type.startswith("image/"):
                    raise Exception(f"URL does not point to an image (content-type: {content_type})")

                content_length = response.headers.get("content-length")
                if content_length:
                    size_mb = int(content_length) / (1024 * 1024)
                    if size_mb > settings.max_image_size_mb:
                        raise Exception(f"Image too large: {size_mb:.1f}MB > {settings.max_image_size_mb}MB")

        except httpx.RequestError as e:
            raise Exception(f"Failed to access image URL: {str(e)}")

    async def test_connection(self) -> bool:
        """Test if OpenAI API is accessible."""
        try:
            response = await self.client.chat.completions.create(
                model="gpt-3.5-turbo",
                messages=[{"role": "user", "content": "Hello"}],
                max_tokens=5
            )
            return True
        except Exception as e:
            logger.error(f"OpenAI connection test failed: {str(e)}")
            return False

    async def close(self):
        """Close the HTTP client."""
        await self.http_client.aclose()
