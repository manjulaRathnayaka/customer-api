from typing import List, Optional
from pydantic import BaseModel, Field, HttpUrl


class PostContent(BaseModel):
    caption: Optional[str] = Field(None, description="Instagram post caption text")
    hashtags: Optional[List[str]] = Field(default=[], description="List of hashtags from the post")
    image_urls: Optional[List[HttpUrl]] = Field(default=[], description="URLs of images to analyze")


class AnalyzeRequest(BaseModel):
    content: PostContent = Field(..., description="Instagram post content to analyze")


class TextAnalysis(BaseModel):
    score: int = Field(..., ge=0, le=100, description="Text analysis score (0-100)")
    detected_elements: List[str] = Field(default=[], description="Elements detected in text")
    reasoning: str = Field(..., description="Reasoning for the text score")


class VisualAnalysis(BaseModel):
    score: int = Field(..., ge=0, le=100, description="Visual analysis score (0-100)")
    detected_elements: List[str] = Field(default=[], description="Elements detected in images")
    reasoning: str = Field(..., description="Reasoning for the visual score")


class DetailedAnalysis(BaseModel):
    text_score: int = Field(..., ge=0, le=100, description="Score based on text analysis only")
    visual_score: int = Field(..., ge=0, le=100, description="Score based on image analysis only")
    reasoning: str = Field(..., description="Explanation of how the score was determined")
    detected_elements: List[str] = Field(..., description="List of elements that contributed to the score")


class AnalyzeResponse(BaseModel):
    raincoat_score: int = Field(..., ge=0, le=100, description="Overall score indicating raincoat relevance")
    confidence: float = Field(..., ge=0.0, le=1.0, description="Confidence level of the analysis")
    analysis: DetailedAnalysis = Field(..., description="Detailed analysis breakdown")
    processing_time_ms: int = Field(..., description="Time taken to process the request in milliseconds")


class HealthResponse(BaseModel):
    status: str = Field(..., description="Health status")
    timestamp: str = Field(..., description="Current timestamp")
    version: str = Field(..., description="API version")
    openai_status: str = Field(..., description="OpenAI API connection status")
