#!/usr/bin/env python3
"""
Entry point for the Social Media Content Scorer API.
This file serves as the application entry point.
"""

import os

# Import the FastAPI app from the app package
from app.main import app

# Export the app for ASGI servers
__all__ = ['app']

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=int(os.getenv("PORT", 8080)))