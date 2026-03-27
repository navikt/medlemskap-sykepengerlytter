#!/usr/bin/env python3
"""
Quick validation script for skills - minimal version.
Compatible with both Claude Code SKILL.md and GitHub Copilot instruction formats.
"""

import sys
import re
import yaml
from pathlib import Path


def validate_skill(skill_path):
    """Basic validation of a skill."""
    skill_path = Path(skill_path)

    # Check SKILL.md exists
    skill_md = skill_path / 'SKILL.md'
    if not skill_md.exists():
        return False, "SKILL.md not found"

    # Read and validate frontmatter
    content = skill_md.read_text()
    if not content.startswith('---'):
        return False, "No YAML frontmatter found"

    # Extract frontmatter
    match = re.match(r'^---\n(.*?)\n---', content, re.DOTALL)
    if not match:
        return False, "Invalid frontmatter format"

    frontmatter_text = match.group(1)

    # Parse YAML frontmatter
    try:
        frontmatter = yaml.safe_load(frontmatter_text)
        if not isinstance(frontmatter, dict):
            return False, "Frontmatter must be a YAML dictionary"
    except yaml.YAMLError as e:
        return False, f"Invalid YAML in frontmatter: {e}"

    # Define allowed properties
    ALLOWED_PROPERTIES = {
        'name', 'description', 'license', 'allowed-tools',
        'metadata', 'compatibility', 'copilot-target',
    }

    # Check for unexpected properties
    unexpected_keys = set(frontmatter.keys()) - ALLOWED_PROPERTIES
    if unexpected_keys:
        return False, (
            f"Unexpected key(s) in SKILL.md frontmatter: {', '.join(sorted(unexpected_keys))}. "
            f"Allowed properties are: {', '.join(sorted(ALLOWED_PROPERTIES))}"
        )

    # Check required fields
    if 'name' not in frontmatter:
        return False, "Missing 'name' in frontmatter"
    if 'description' not in frontmatter:
        return False, "Missing 'description' in frontmatter"

    # Extract name for validation
    name = frontmatter.get('name', '')
    if not isinstance(name, str):
        return False, f"Name must be a string, got {type(name).__name__}"
    name = name.strip()
    if name:
        # Check naming convention (kebab-case: lowercase with hyphens)
        if not re.match(r'^[a-z0-9-]+$', name):
            return False, f"Name '{name}' should be kebab-case (lowercase letters, digits, and hyphens only)"
        if name.startswith('-') or name.endswith('-') or '--' in name:
            return False, f"Name '{name}' cannot start/end with hyphen or contain consecutive hyphens"
        # Check name length (max 64 characters)
        if len(name) > 64:
            return False, f"Name is too long ({len(name)} characters). Maximum is 64 characters."

    # Extract and validate description
    description = frontmatter.get('description', '')
    if not isinstance(description, str):
        return False, f"Description must be a string, got {type(description).__name__}"
    description = description.strip()
    if description:
        # Check for angle brackets
        if '<' in description or '>' in description:
            return False, "Description cannot contain angle brackets (< or >)"
        # Check description length (max 1024 characters)
        if len(description) > 1024:
            return False, f"Description is too long ({len(description)} characters). Maximum is 1024 characters."

    # Validate compatibility field if present (optional)
    compatibility = frontmatter.get('compatibility', '')
    if compatibility:
        if not isinstance(compatibility, str):
            return False, f"Compatibility must be a string, got {type(compatibility).__name__}"
        if len(compatibility) > 500:
            return False, f"Compatibility is too long ({len(compatibility)} characters). Maximum is 500 characters."

    # Validate copilot-target if present (optional)
    copilot_target = frontmatter.get('copilot-target', '')
    if copilot_target:
        valid_targets = {'instructions', 'prompt-file', 'agents-md', 'standalone'}
        if copilot_target not in valid_targets:
            return False, (
                f"copilot-target '{copilot_target}' is not valid. "
                f"Must be one of: {', '.join(sorted(valid_targets))}"
            )

    return True, "Skill is valid!"


def validate_copilot_instructions(path):
    """Validate a .github/copilot-instructions.md file."""
    path = Path(path)
    if not path.exists():
        return False, f"File not found: {path}"

    content = path.read_text()
    if len(content.strip()) == 0:
        return False, "File is empty"

    # Check for reasonable length (warn, don't fail)
    lines = content.split('\n')
    if len(lines) > 500:
        return True, f"Warning: File has {len(lines)} lines. Consider splitting into referenced files."

    return True, "Copilot instructions file is valid!"


def validate_prompt_file(path):
    """Validate a .github/prompts/*.prompt.md file."""
    path = Path(path)
    if not path.exists():
        return False, f"File not found: {path}"

    if not path.name.endswith('.prompt.md'):
        return False, f"Prompt file must end with .prompt.md, got: {path.name}"

    content = path.read_text()
    if len(content.strip()) == 0:
        return False, "File is empty"

    return True, "Prompt file is valid!"


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python quick_validate.py <skill_directory_or_file>")
        sys.exit(1)

    target = Path(sys.argv[1])

    if target.is_dir():
        valid, message = validate_skill(target)
    elif target.name == 'copilot-instructions.md':
        valid, message = validate_copilot_instructions(target)
    elif target.name.endswith('.prompt.md'):
        valid, message = validate_prompt_file(target)
    else:
        valid, message = False, f"Unknown file type: {target}"

    print(message)
    sys.exit(0 if valid else 1)

