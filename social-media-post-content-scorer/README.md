# Social Media Content Scoring API

A stateless API service that analyzes social media post content and provides relevance scores for specific topics using OpenAI's GPT-4 and GPT-4 Vision models.

## Overview

This service provides a single endpoint that accepts social media post content (text, hashtags, and images) and returns a score indicating how likely the post is about a specific topic. The analysis is powered by OpenAI's advanced language and vision models.

## Architecture

```
┌─────────────────┐    ┌──────────────────┐
│   Client App    │────│   Load Balancer  │
└─────────────────┘    └──────────────────┘
                                │
         ┌──────────────────────▼──────────────────────┐
         │       Social Media Content Scoring API Service        │
         │  ┌────────────────────────────────────────┐ │
         │  │          API Controller             │ │
         │  │  • Input Validation                │ │
         │  │  • Response Formatting             │ │
         │  └────────────────────────────────────────┘ │
         │  ┌────────────────────────────────────────┐ │
         │  │        Analysis Engine              │ │
         │  │  • Content Preprocessor            │ │
         │  │  • OpenAI Integration              │ │
         │  │  • Score Calculator                │ │
         │  └────────────────────────────────────────┘ │
         └─────────────────────────────────────────────┘
                                │
         ┌──────────────────────┼──────────────────────┐
         │                      │                      │
         ▼                      ▼                      ▼
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   OpenAI API    │    │   Cache Layer    │    │    Logging      │
│                 │    │    (Optional)    │    │   (Optional)    │
│ • GPT-4 Vision  │    │                  │    │                 │
│ • GPT-4 Text    │    │ • Redis Cache    │    │ • Request Logs  │
│                 │    │ • Rate Limiting  │    │ • Error Logs    │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

## Features

- **Text Analysis**: Analyzes captions and hashtags for topic-related content
- **Visual Analysis**: Uses GPT-4 Vision to detect relevant content in images
- **Combined Scoring**: Provides an overall score (0-100) with confidence level
- **Detailed Reasoning**: Explains why the score was assigned
- **Stateless Design**: No data persistence, purely analytical
- **Fast Response**: Optimized for quick analysis and response

## API Specification

### Endpoint

```
POST /analyze
Content-Type: application/json
```

### Request Format

```json
    "content": {
      "caption": "Just got my new yellow raincoat! Perfect for Seattle weather ☔",
      "hashtags": ["#raincoat", "#weather", "#fashion", "#seattle"],
      "image_urls": ["https://example.com/image1.jpg", "https://example.com/image2.jpg"]
    }
```

#### Request Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `content.caption` | string | No | Social media post caption text |
| `content.hashtags` | array of strings | No | List of hashtags from the post |
| `content.image_urls` | array of strings | No | URLs of images to analyze |

### Response Format

```json
{
  "raincoat_score": 87,
  "confidence": 0.92,
  "analysis": {
    "text_score": 95,
    "visual_score": 78,
    "reasoning": "Strong textual indication with explicit raincoat mention and weather context. Visual analysis confirms presence of raincoat in images.",
    "detected_elements": [
      "Direct raincoat mention in caption",
      "Weather-related hashtags",
      "Yellow raincoat visible in image",
      "Rainy weather context"
    ]
  },
  "processing_time_ms": 1247
}
```

#### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `raincoat_score` | number (0-100) | Overall score indicating topic relevance |
| `confidence` | number (0-1) | Confidence level of the analysis |
| `analysis.text_score` | number (0-100) | Score based on text analysis only |
| `analysis.visual_score` | number (0-100) | Score based on image analysis only |
| `analysis.reasoning` | string | Explanation of how the score was determined |
| `analysis.detected_elements` | array of strings | List of elements that contributed to the score |
| `processing_time_ms` | number | Time taken to process the request in milliseconds |

## Scoring Logic

### Text Analysis (GPT-4)
- **Direct mentions**: "raincoat", "rain jacket", "waterproof jacket" (High impact)
- **Weather context**: Rain, storm, wet weather mentions (Medium impact)
- **Fashion context**: Clothing and style mentions with weather (Medium impact)
- **Seasonal references**: Autumn, spring, rainy season (Low impact)

### Visual Analysis (GPT-4 Vision)
- **Raincoat detection**: Visible raincoats or waterproof garments (High impact)
- **Weather scenes**: Rainy conditions, umbrellas, wet environments (Medium impact)
- **Fashion context**: Clothing posts in weather settings (Medium impact)
- **Outdoor activities**: People in rain gear for activities (Low impact)

### Combined Scoring
The final score is calculated using weighted combination:
- Text analysis: 60% weight
- Visual analysis: 40% weight
- Minimum threshold: 10 (below this, score is 0)
- Maximum boost: +15 points for perfect text + visual alignment

## Technology Stack

### Core Dependencies
- **FastAPI**: High-performance async web framework
- **OpenAI**: Official OpenAI Python client for GPT-4 and GPT-4 Vision
- **Pydantic**: Data validation and serialization
- **Uvicorn**: ASGI server for production deployment
- **HTTPX**: Async HTTP client for image processing

### Optional Dependencies
- **Python-multipart**: File upload support
- **Redis**: Caching for repeated requests (cost optimization)

## Project Structure

```
social-media-content-scorer/
├── app/
│   ├── __init__.py         # Package marker
│   ├── analyzer.py         # Core analysis logic
│   ├── config.py           # Configuration management
│   ├── main.py             # FastAPI application
│   ├── models.py           # Pydantic data models
│   └── openai_client.py    # OpenAI integration
├── .choreo/
│   └── component.yaml      # Choreo service configuration
├── .env.sample             # Environment variables template
├── .gitignore              # Git ignore rules
├── main.py                 # Application entry point
├── openapi.yaml            # API specification & documentation
├── requirements.txt        # Python dependencies
└── README.md               # This file
```## Setup and Installation

### Prerequisites
- Python 3.11+
- OpenAI API key
- Git repository for Choreo deployment

### Local Development

1. **Clone the repository**
   ```bash
   git clone https://github.com/manjulaRathnayaka/social-media-content-scorer.git
   cd social-media-content-scorer
   ```

2. **Create virtual environment**
   ```bash
   python -m venv venv
   source venv/bin/activate  # On Windows: venv\Scripts\activate
   ```

3. **Install dependencies**
   ```bash
   pip install -r requirements.txt
   ```

4. **Set environment variables**
   ```bash
   cp .env.sample .env
   # Edit .env with your OpenAI API key
   ```

5. **Run the application**
   ```bash
   python main.py
   ```

### Choreo Deployment

1. **Push to Git Repository**
   ```bash
   git init
   git add .
   git commit -m "Initial commit: Instagram Raincoat Analysis API"
   git remote add origin <your-repo-url>
   git push -u origin main
   ```

2. **Create Service Component in Choreo**
   - Use the repository URL
   - Select "Service" component type
   - Choreo will automatically detect the buildpack configuration
   - The `.choreo/component.yaml` will be used for configuration

3. **Configure Environment Variables in Choreo**
   - `OPENAI_API_KEY` - Your OpenAI API key (required)
   - `MAX_IMAGE_SIZE_MB` - 10 (optional)
   - `REQUEST_TIMEOUT_SECONDS` - 30 (optional)
   - `LOG_LEVEL` - INFO (optional)

4. **Deploy and Test**
   - Choreo will automatically build and deploy using Python buildpack
   - Test using the `/health` endpoint first
   - Then test `/analyze` with sample data

## Configuration

### Environment Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `OPENAI_API_KEY` | Yes | - | OpenAI API key for GPT-4 access |
| `MAX_IMAGE_SIZE_MB` | No | 10 | Maximum image size for analysis |
| `REQUEST_TIMEOUT_SECONDS` | No | 30 | Timeout for OpenAI API requests |
| `ENABLE_CACHING` | No | false | Enable Redis caching |
| `REDIS_URL` | No | - | Redis connection URL (if caching enabled) |
| `LOG_LEVEL` | No | INFO | Logging level (DEBUG, INFO, WARNING, ERROR) |

### Example .env file
```env
OPENAI_API_KEY=sk-your-openai-api-key-here
MAX_IMAGE_SIZE_MB=10
REQUEST_TIMEOUT_SECONDS=30
ENABLE_CACHING=false
LOG_LEVEL=INFO
```

## Usage Examples

### Basic Text Analysis
```bash
curl -X POST "http://localhost:8080/analyze" \
  -H "Content-Type: application/json" \
  -d '{
    "content": {
      "caption": "Love my new raincoat! Staying dry in this weather.",
      "hashtags": ["#raincoat", "#weather", "#fashion"]
    }
  }'
```

### Text + Image Analysis
```bash
curl -X POST "http://localhost:8080/analyze" \
  -H "Content-Type: application/json" \
  -d '{
    "content": {
      "caption": "Perfect outfit for today!",
      "hashtags": ["#ootd", "#fashion"],
      "image_urls": ["https://example.com/raincoat-photo.jpg"]
    }
  }'
```

### Python Client Example
```python
import requests

def analyze_post(caption, hashtags=None, image_urls=None):
    url = "http://localhost:8080/analyze"

    payload = {
        "content": {
            "caption": caption,
            "hashtags": hashtags or [],
            "image_urls": image_urls or []
        }
    }

    response = requests.post(url, json=payload)
    return response.json()

# Example usage
result = analyze_post(
    caption="Just bought a new yellow raincoat!",
    hashtags=["#raincoat", "#fashion", "#weather"],
    image_urls=["https://example.com/my-raincoat.jpg"]
)

print(f"Topic Relevance Score: {result['raincoat_score']}")
print(f"Confidence: {result['confidence']}")
```

## API Health and Monitoring

### Health Check Endpoint
```
GET /health
```

Response:
```json
{
  "status": "healthy",
  "timestamp": "2025-07-18T10:30:00Z",
  "version": "1.0.0",
  "openai_status": "connected"
}
```

### Metrics Endpoint (Optional)
```
GET /metrics
```

## Error Handling

### Common Error Responses

#### Invalid Request
```json
{
  "detail": "Validation error: content field is required"
}
```

#### OpenAI API Error
```json
{
  "detail": "OpenAI API temporarily unavailable. Please try again later."
}
```

#### Image Processing Error
```json
{
  "detail": "Unable to process image URL: https://example.com/invalid-image.jpg"
}
```

## Cost Considerations

### OpenAI API Costs (Approximate)
- **Text Analysis**: $0.01-0.03 per request (depending on content length)
- **Image Analysis**: $0.01-0.02 per image with GPT-4 Vision
- **Average Cost**: $0.02-0.05 per complete analysis

### Cost Optimization Strategies
1. **Caching**: Cache identical requests to avoid duplicate API calls
2. **Image Preprocessing**: Resize large images before sending to OpenAI
3. **Batch Processing**: Process multiple posts in a single request
4. **Smart Prompting**: Optimize prompts to reduce token usage

## Performance

### Expected Response Times
- **Text-only analysis**: 1-3 seconds
- **Image analysis**: 2-5 seconds per image
- **Combined analysis**: 3-8 seconds total

### Scalability
- **Stateless design**: Horizontal scaling with load balancers
- **Async processing**: Non-blocking I/O for concurrent requests
- **Resource requirements**: 512MB RAM, 1 CPU core per instance
- **Choreo Buildpack**: Automatic Python environment setup and dependency management

## Choreo Deployment Guide

### Repository Structure for Choreo
The project is configured for seamless Choreo deployment with:
- **`runtime.txt`**: Specifies Python 3.11 for buildpack
- **`Procfile`**: Defines the web process command
- **`.choreo/component.yaml`**: Choreo service configuration
- **`openapi.yaml`**: API specification for documentation
- **`requirements.txt`**: Python dependencies

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For questions or issues:
- Create an issue in the GitHub repository
- Contact the development team

## Changelog

### Version 1.0.0 (2025-07-18)
- Initial release
- Text and image analysis capabilities
- OpenAI GPT-4 and GPT-4 Vision integration
- RESTful API with comprehensive documentation
