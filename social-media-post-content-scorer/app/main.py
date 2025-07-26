import logging
import sys
from contextlib import asynccontextmanager
from datetime import datetime
from fastapi import FastAPI, HTTPException, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from .models import AnalyzeRequest, AnalyzeResponse, HealthResponse
from .openai_client import OpenAIClient
from .analyzer import GenericRelevanceAnalyzer

# Simple logging for K8s
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    stream=sys.stdout
)

logger = logging.getLogger(__name__)

# Global instances
openai_client = None
analyzer = None


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Manage application lifecycle."""
    global openai_client, analyzer
    logger.info("Starting Social Media Content Relevance API")
    try:
        openai_client = OpenAIClient()
        analyzer = GenericRelevanceAnalyzer(openai_client)
        if await openai_client.test_connection():
            logger.info("OpenAI connection successful")
        else:
            logger.warning("OpenAI connection test failed")
    except ValueError as e:
        logger.error(f"Configuration error: {e}")
        openai_client = None
        analyzer = None
        logger.warning("OpenAI connection failed - service may not work properly")
    yield
    logger.info("Shutting down Social Media Content Relevance API")
    if openai_client:
        await openai_client.close()


# FastAPI app
app = FastAPI(
    title="Social Media Content Relevance API",
    description="AI-powered service to analyze social media posts and score topic relevance (e.g., raincoat, sunscreen, umbrella, etc.)",
    version="1.1.0",
    lifespan=lifespan
)

# CORS middleware - simplified for API service
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["GET", "POST"],
    allow_headers=["*"],
)


@app.get("/")
async def root():
    """Root endpoint - API information."""
    return {
        "name": "Social Media Content Scoring API",
        "version": "1.0.0",
        "description": "AI-powered service to analyze social media posts and score topic relevance (e.g., raincoat, sunscreen, umbrella, etc.)",
        "endpoints": {
            "health": "/health",
            "analyze": "/analyze",
            "docs": "/docs",
            "openapi": "/openapi.json"
        }
    }


@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception):
    """Global exception handler."""
    logger.error(f"Unhandled exception on {request.url.path}: {str(exc)}")
    return JSONResponse(
        status_code=500,
        content={"detail": "Internal server error"}
    )


@app.get("/health", response_model=HealthResponse)
async def health_check():
    """Health check endpoint - fast response for K8s."""
    # Check if OpenAI client is properly initialized
    if openai_client is None:
        openai_status = "configuration_error"
    else:
        openai_status = "ready"

    return HealthResponse(
        status="healthy",
        timestamp=datetime.utcnow().isoformat(),
        version="1.0.0",
        openai_status=openai_status
    )



@app.post("/analyze", response_model=AnalyzeResponse)
async def analyze_post(request: AnalyzeRequest):
    """Analyze social media post content for topic relevance."""
    if analyzer is None or openai_client is None:
        raise HTTPException(
            status_code=503,
            detail="Service not ready: OPENAI_API_KEY environment variable is required"
        )
    try:
        logger.info(f"Analysis request: text={bool(request.content.caption)}, "
                   f"hashtags={len(request.content.hashtags or [])}, "
                   f"images={len(request.content.image_urls or [])}, "
                   f"topic={request.topic}")
        if not analyzer:
            raise HTTPException(status_code=503, detail="Service not ready")
        # Use provided topic or fallback to default
        topic = request.topic or openai_client.settings.default_topic
        score, confidence, detailed_analysis, processing_time = await analyzer.analyze_post(request.content, topic)
        return AnalyzeResponse(
            relevance_score=score,
            confidence=confidence,
            analysis=detailed_analysis,
            processing_time_ms=processing_time
        )
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Analysis failed: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Analysis failed: {str(e)}")


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8080)
