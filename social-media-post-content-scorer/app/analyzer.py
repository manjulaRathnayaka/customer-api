import asyncio
import json
import time
import logging
from typing import List, Tuple
from fastapi import HTTPException

from .models import PostContent, TextAnalysis, VisualAnalysis, DetailedAnalysis
from .openai_client import OpenAIClient

logger = logging.getLogger(__name__)



class GenericRelevanceAnalyzer:
    def __init__(self, openai_client: OpenAIClient):
        self.openai_client = openai_client

    async def analyze_post(self, content: PostContent, topic: str) -> Tuple[int, float, DetailedAnalysis, int]:
        """Analyze social media post content for topic relevance."""
        start_time = time.time()
        try:
            tasks = []
            if content.caption or content.hashtags:
                tasks.append(self._analyze_text(content, topic))
            else:
                tasks.append(self._create_empty_text_analysis())
            if content.image_urls:
                tasks.append(self._analyze_images(content.image_urls, topic))
            else:
                tasks.append(self._create_empty_visual_analysis())
            text_analysis, visual_analysis = await asyncio.gather(*tasks)
            score, confidence = self._calculate_combined_score(text_analysis, visual_analysis)
            detailed_analysis = self._create_detailed_analysis(text_analysis, visual_analysis, score)
            processing_time = int((time.time() - start_time) * 1000)
            logger.info(f"Analysis completed: score={score}, confidence={confidence:.2f}, time={processing_time}ms")
            return score, confidence, detailed_analysis, processing_time
        except Exception as e:
            logger.error(f"Post analysis failed: {str(e)}")
            raise HTTPException(status_code=500, detail=f"Analysis failed: {str(e)}")


    async def _analyze_text(self, content: PostContent, topic: str) -> TextAnalysis:
        """Analyze text content for topic relevance."""
        try:
            text_parts = []
            if content.caption:
                text_parts.append(f"Caption: {content.caption}")
            if content.hashtags:
                text_parts.append(f"Hashtags: {', '.join(content.hashtags)}")
            text_content = "\n".join(text_parts)
            prompt = f"""
            Analyze this social media post text for relevance to the topic: '{topic}'.
            - Identify direct mentions, context, and related elements for the topic.
            - Consider context, activities, and any product or event mentions related to the topic.
            Text to analyze:
            {text_content}
            Respond with JSON in this exact format:
            {{
                "score": <integer 0-100>,
                "detected_elements": [<list of specific elements found>],
                "reasoning": "<explanation of score>"
            }}
            """
            response = await self.openai_client.analyze_text(prompt)

            try:
                # Clean the response - remove markdown formatting if present
                cleaned_response = response.strip()
                if cleaned_response.startswith('```json'):
                    cleaned_response = cleaned_response[7:]  # Remove ```json
                if cleaned_response.endswith('```'):
                    cleaned_response = cleaned_response[:-3]  # Remove ```
                cleaned_response = cleaned_response.strip()

                result = json.loads(cleaned_response)
                return TextAnalysis(**result)
            except json.JSONDecodeError:
                logger.warning(f"Failed to parse OpenAI response: {response[:100]}...")
                return TextAnalysis(score=0, detected_elements=[], reasoning="Failed to parse analysis response")

        except Exception as e:
            logger.error(f"Text analysis failed: {str(e)}")
            return TextAnalysis(score=0, detected_elements=[], reasoning=f"Analysis error: {str(e)}")


    async def _analyze_images(self, image_urls: List[str], topic: str) -> VisualAnalysis:
        """Analyze images for topic relevance."""
        try:
            image_url = str(image_urls[0])
            prompt = f"""
            Analyze this image for relevance to the topic: '{topic}'.
            - Identify people, objects, or scenes related to the topic.
            - Consider context, activities, and any product or event mentions related to the topic.
            Respond with JSON in this exact format:
            {{
                "score": <integer 0-100>,
                "detected_elements": [<list of specific visual elements found>],
                "reasoning": "<explanation of what you see and why you gave this score>"
            }}
            """
            response = await self.openai_client.analyze_image(image_url, prompt)

            try:
                # Clean the response - remove markdown formatting if present
                cleaned_response = response.strip()
                if cleaned_response.startswith('```json'):
                    cleaned_response = cleaned_response[7:]  # Remove ```json
                if cleaned_response.endswith('```'):
                    cleaned_response = cleaned_response[:-3]  # Remove ```
                cleaned_response = cleaned_response.strip()

                result = json.loads(cleaned_response)
                return VisualAnalysis(**result)
            except json.JSONDecodeError as e:
                logger.error(f"Failed to parse OpenAI image response. Error: {str(e)}")
                logger.error(f"Response (first 500 chars): {response[:500]}")
                return VisualAnalysis(score=0, detected_elements=[], reasoning="Failed to parse image analysis response")
            except Exception as e:
                logger.error(f"Error processing image analysis response: {str(e)}")
                return VisualAnalysis(score=0, detected_elements=[], reasoning=f"Image analysis error: {str(e)}")

        except Exception as e:
            logger.error(f"Image analysis failed: {str(e)}")
            return VisualAnalysis(score=0, detected_elements=[], reasoning=f"Image analysis error: {str(e)}")

    async def _create_empty_text_analysis(self) -> TextAnalysis:
        """Create empty text analysis when no text content is provided."""
        return TextAnalysis(
            score=0,
            detected_elements=[],
            reasoning="No text content provided for analysis"
        )

    async def _create_empty_visual_analysis(self) -> VisualAnalysis:
        """Create empty visual analysis when no images are provided."""
        return VisualAnalysis(
            score=0,
            detected_elements=[],
            reasoning="No images provided for analysis"
        )

    def _calculate_combined_score(self, text_analysis: TextAnalysis, visual_analysis: VisualAnalysis) -> Tuple[int, float]:
        """Calculate combined score with weighted average."""
        # Weights for different analysis types
        text_weight = 0.4
        visual_weight = 0.6

        # Calculate weighted score
        combined_score = int(
            (text_analysis.score * text_weight) +
            (visual_analysis.score * visual_weight)
        )

        # Calculate confidence based on availability of content
        confidence_factors = []
        if text_analysis.score > 0:
            confidence_factors.append(0.7)  # Text analysis available
        if visual_analysis.score > 0:
            confidence_factors.append(0.8)  # Visual analysis available

        confidence = sum(confidence_factors) / 2 if confidence_factors else 0.3

        return combined_score, min(confidence, 1.0)

    def _create_detailed_analysis(self, text_analysis: TextAnalysis, visual_analysis: VisualAnalysis, score: int) -> DetailedAnalysis:
        """Create detailed analysis combining text and visual results."""
        all_elements = text_analysis.detected_elements + visual_analysis.detected_elements

        reasoning_parts = []
        if text_analysis.reasoning and text_analysis.reasoning != "No text content provided for analysis":
            reasoning_parts.append(f"Text: {text_analysis.reasoning}")
        if visual_analysis.reasoning and visual_analysis.reasoning != "No images provided for analysis":
            reasoning_parts.append(f"Visual: {visual_analysis.reasoning}")

        combined_reasoning = " | ".join(reasoning_parts) if reasoning_parts else "No content available for analysis"

        return DetailedAnalysis(
            text_score=text_analysis.score,
            visual_score=visual_analysis.score,
            reasoning=combined_reasoning,
            detected_elements=all_elements
        )
