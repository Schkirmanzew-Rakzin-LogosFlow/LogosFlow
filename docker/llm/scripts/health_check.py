#!/usr/bin/env python3
"""
Health check script for Ollama server.
This script checks if the Ollama API is responding properly.
"""

import sys
import urllib.request
import urllib.error
import json
import time

def check_ollama_health(host="localhost", port=11434, timeout=3):
    """
    Check if Ollama server is healthy by testing the API endpoint.
    
    Args:
        host: Ollama server host
        port: Ollama server port
        timeout: Request timeout in seconds
    
    Returns:
        bool: True if healthy, False otherwise
    """
    try:
        # Try to connect to the Ollama API version endpoint
        url = f"http://{host}:{port}/api/version"
        
        req = urllib.request.Request(url)
        req.add_header('User-Agent', 'Ollama-Health-Check/1.0')
        
        with urllib.request.urlopen(req, timeout=timeout) as response:
            if response.status == 200:
                # Try to parse the response to ensure it's valid JSON
                data = json.loads(response.read().decode('utf-8'))
                if 'version' in data:
                    print(f"Ollama server is healthy - version: {data.get('version', 'unknown')}")
                    return True
                else:
                    print("Ollama server responded but without version info")
                    return False
            else:
                print(f"Ollama server responded with status: {response.status}")
                return False
                
    except urllib.error.HTTPError as e:
        print(f"HTTP error connecting to Ollama: {e.code} - {e.reason}")
        return False
    except urllib.error.URLError as e:
        print(f"URL error connecting to Ollama: {e.reason}")
        return False
    except json.JSONDecodeError as e:
        print(f"Invalid JSON response from Ollama: {e}")
        return False
    except Exception as e:
        print(f"Unexpected error checking Ollama health: {e}")
        return False

def main():
    """Main health check function."""
    # Allow some time for the server to start up
    max_attempts = 3
    for attempt in range(max_attempts):
        if check_ollama_health():
            sys.exit(0)  # Healthy
        
        if attempt < max_attempts - 1:
            print(f"Health check attempt {attempt + 1} failed, retrying...")
            time.sleep(1)
    
    print("All health check attempts failed")
    sys.exit(1)  # Unhealthy

if __name__ == "__main__":
    main()
