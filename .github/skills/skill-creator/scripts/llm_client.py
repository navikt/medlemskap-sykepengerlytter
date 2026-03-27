"""Multi-backend LLM client for skill-creator scripts.

Supports OpenAI, Azure OpenAI, Anthropic, and GitHub Copilot CLI backends.
Configuration is via environment variables (see SKILL.md for details).
"""

import json
import os
import subprocess
import sys
from pathlib import Path


def get_backend() -> str:
    """Return the configured LLM backend name."""
    return os.environ.get("SKILL_LLM_BACKEND", "openai")


def get_model() -> str:
    """Return the configured model identifier."""
    return os.environ.get("SKILL_LLM_MODEL", "gpt-4o")


def call_llm(prompt: str, model: str | None = None, timeout: int = 300) -> str:
    """Send a prompt to the configured LLM backend and return the text response.

    Args:
        prompt: The full prompt text.
        model: Model override (uses SKILL_LLM_MODEL env var if None).
        timeout: Request timeout in seconds.

    Returns:
        The model's text response.

    Raises:
        RuntimeError: If the call fails.
    """
    backend = get_backend()
    model = model or get_model()

    if backend == "openai":
        return _call_openai(prompt, model, timeout)
    elif backend == "azure":
        return _call_azure(prompt, model, timeout)
    elif backend == "anthropic":
        return _call_anthropic(prompt, model, timeout)
    elif backend == "copilot":
        return _call_copilot(prompt, model, timeout)
    else:
        raise RuntimeError(f"Unknown LLM backend: {backend}. Use: openai, azure, anthropic, copilot")


def _call_openai(prompt: str, model: str, timeout: int) -> str:
    """Call OpenAI-compatible API."""
    try:
        import openai
    except ImportError:
        raise RuntimeError(
            "openai package not installed. Run: pip install openai"
        )

    api_key = os.environ.get("OPENAI_API_KEY")
    base_url = os.environ.get("OPENAI_BASE_URL")

    if not api_key:
        raise RuntimeError("OPENAI_API_KEY environment variable not set")

    kwargs = {"api_key": api_key}
    if base_url:
        kwargs["base_url"] = base_url

    client = openai.OpenAI(**kwargs)

    response = client.chat.completions.create(
        model=model,
        messages=[{"role": "user", "content": prompt}],
        timeout=timeout,
    )

    return response.choices[0].message.content or ""


def _call_azure(prompt: str, model: str, timeout: int) -> str:
    """Call Azure OpenAI API."""
    try:
        import openai
    except ImportError:
        raise RuntimeError(
            "openai package not installed. Run: pip install openai"
        )

    endpoint = os.environ.get("AZURE_OPENAI_ENDPOINT")
    api_key = os.environ.get("AZURE_OPENAI_API_KEY")
    deployment = os.environ.get("AZURE_OPENAI_DEPLOYMENT", model)

    if not endpoint:
        raise RuntimeError("AZURE_OPENAI_ENDPOINT environment variable not set")
    if not api_key:
        raise RuntimeError("AZURE_OPENAI_API_KEY environment variable not set")

    client = openai.AzureOpenAI(
        azure_endpoint=endpoint,
        api_key=api_key,
        api_version="2024-02-01",
    )

    response = client.chat.completions.create(
        model=deployment,
        messages=[{"role": "user", "content": prompt}],
        timeout=timeout,
    )

    return response.choices[0].message.content or ""


def _call_anthropic(prompt: str, model: str, timeout: int) -> str:
    """Call Anthropic API."""
    try:
        import anthropic
    except ImportError:
        raise RuntimeError(
            "anthropic package not installed. Run: pip install anthropic"
        )

    api_key = os.environ.get("ANTHROPIC_API_KEY")
    if not api_key:
        raise RuntimeError("ANTHROPIC_API_KEY environment variable not set")

    client = anthropic.Anthropic(api_key=api_key)

    response = client.messages.create(
        model=model or "claude-sonnet-4-20250514",
        max_tokens=4096,
        messages=[{"role": "user", "content": prompt}],
    )

    # Extract text from content blocks
    text_parts = []
    for block in response.content:
        if hasattr(block, "text"):
            text_parts.append(block.text)
    return "\n".join(text_parts)


def _call_copilot(prompt: str, model: str, timeout: int) -> str:
    """Call via GitHub Copilot CLI (gh copilot).

    Falls back to gh api with copilot models endpoint if available.
    """
    # Try gh copilot CLI first
    try:
        result = subprocess.run(
            ["gh", "copilot", "suggest", "-t", "shell", prompt],
            capture_output=True,
            text=True,
            timeout=timeout,
        )
        if result.returncode == 0 and result.stdout.strip():
            return result.stdout.strip()
    except (FileNotFoundError, subprocess.TimeoutExpired):
        pass

    # Fall back to gh api with GitHub Models
    try:
        payload = json.dumps({
            "model": model or "gpt-4o",
            "messages": [{"role": "user", "content": prompt}],
        })

        result = subprocess.run(
            [
                "gh", "api",
                "--method", "POST",
                "-H", "Accept: application/json",
                "https://models.inference.ai.azure.com/chat/completions",
                "--input", "-",
            ],
            input=payload,
            capture_output=True,
            text=True,
            timeout=timeout,
        )

        if result.returncode == 0:
            data = json.loads(result.stdout)
            return data.get("choices", [{}])[0].get("message", {}).get("content", "")
    except (FileNotFoundError, subprocess.TimeoutExpired, json.JSONDecodeError):
        pass

    raise RuntimeError(
        "GitHub Copilot CLI not available. Install with: gh extension install github/gh-copilot\n"
        "Or switch backend: export SKILL_LLM_BACKEND=openai"
    )

