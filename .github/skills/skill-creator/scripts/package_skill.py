#!/usr/bin/env python3
"""
Skill Packager - Creates a distributable .skill file and/or deploys to
GitHub Copilot instruction formats.

Usage:
    python -m scripts.package_skill <path/to/skill-folder> [output-directory]
    python -m scripts.package_skill <path/to/skill-folder> --deploy-copilot <repo-root>

Examples:
    python -m scripts.package_skill skills/my-skill
    python -m scripts.package_skill skills/my-skill ./dist
    python -m scripts.package_skill skills/my-skill --deploy-copilot /path/to/repo
"""

import fnmatch
import re
import shutil
import sys
import zipfile
from pathlib import Path

from scripts.quick_validate import validate_skill
from scripts.utils import parse_skill_md

# Patterns to exclude when packaging skills.
EXCLUDE_DIRS = {"__pycache__", "node_modules", ".venv", ".git", "venv", "env"}
EXCLUDE_GLOBS = {"*.pyc"}
EXCLUDE_FILES = {".DS_Store", ".gitignore"}
# Directories excluded only at the skill root (not when nested deeper).
ROOT_EXCLUDE_DIRS = {"evals"}


def should_exclude(rel_path: Path) -> bool:
    """Check if a path should be excluded from packaging."""
    parts = rel_path.parts
    for part in parts:
        if part in EXCLUDE_DIRS:
            return True
        # Also exclude any hidden directories (starting with .) except the skill root
        if part.startswith('.') and part not in ('.', '..'):
            return True
    # rel_path is relative to skill_path.parent, so parts[0] is the skill
    # folder name and parts[1] (if present) is the first subdir.
    if len(parts) > 1 and parts[1] in ROOT_EXCLUDE_DIRS:
        return True
    name = rel_path.name
    if name in EXCLUDE_FILES:
        return True
    return any(fnmatch.fnmatch(name, pat) for pat in EXCLUDE_GLOBS)


def package_skill(skill_path, output_dir=None):
    """
    Package a skill folder into a .skill file.

    Args:
        skill_path: Path to the skill folder
        output_dir: Optional output directory for the .skill file

    Returns:
        Path to the created .skill file, or None if error
    """
    skill_path = Path(skill_path).resolve()

    if not skill_path.exists():
        print(f"❌ Error: Skill folder not found: {skill_path}")
        return None

    if not skill_path.is_dir():
        print(f"❌ Error: Path is not a directory: {skill_path}")
        return None

    skill_md = skill_path / "SKILL.md"
    if not skill_md.exists():
        print(f"❌ Error: SKILL.md not found in {skill_path}")
        return None

    # Run validation before packaging
    print("🔍 Validating skill...")
    valid, message = validate_skill(skill_path)
    if not valid:
        print(f"❌ Validation failed: {message}")
        print("   Please fix the validation errors before packaging.")
        return None
    print(f"✅ {message}\n")

    # Determine output location
    skill_name = skill_path.name
    if output_dir:
        output_path = Path(output_dir).resolve()
        output_path.mkdir(parents=True, exist_ok=True)
    else:
        output_path = Path.cwd()

    skill_filename = output_path / f"{skill_name}.skill"

    # Create the .skill file (zip format)
    try:
        with zipfile.ZipFile(skill_filename, 'w', zipfile.ZIP_DEFLATED) as zipf:
            for file_path in skill_path.rglob('*'):
                if not file_path.is_file():
                    continue
                arcname = file_path.relative_to(skill_path.parent)
                if should_exclude(arcname):
                    print(f"  Skipped: {arcname}")
                    continue
                zipf.write(file_path, arcname)
                print(f"  Added: {arcname}")

        print(f"\n✅ Successfully packaged skill to: {skill_filename}")
        return skill_filename

    except Exception as e:
        print(f"❌ Error creating .skill file: {e}")
        return None


def deploy_to_copilot(skill_path, repo_root, target=None):
    """
    Deploy a skill to GitHub Copilot instruction format in a repository.

    Args:
        skill_path: Path to the skill folder
        repo_root: Path to the repository root
        target: Deployment target ('instructions', 'prompt-file', 'agent', 'agents-md')
                If None, reads from frontmatter 'copilot-target' or defaults to 'prompt-file'
    """
    skill_path = Path(skill_path).resolve()
    repo_root = Path(repo_root).resolve()

    if not skill_path.exists() or not (skill_path / "SKILL.md").exists():
        print(f"❌ Error: Valid skill not found at {skill_path}")
        return False

    name, description, content = parse_skill_md(skill_path)

    # Strip frontmatter from content for deployment
    lines = content.split('\n')
    body_start = 0
    if lines[0].strip() == '---':
        for i, line in enumerate(lines[1:], start=1):
            if line.strip() == '---':
                body_start = i + 1
                break
    body = '\n'.join(lines[body_start:]).strip()

    # Determine target from frontmatter if not specified
    if target is None:
        match = re.search(r'^copilot-target:\s*(.+)$', content, re.MULTILINE)
        target = match.group(1).strip() if match else 'prompt-file'

    github_dir = repo_root / ".github"
    github_dir.mkdir(exist_ok=True)

    if target == 'instructions':
        # Deploy as always-on project instructions
        dest = github_dir / "copilot-instructions.md"
        header = f"<!-- Skill: {name} -->\n<!-- {description} -->\n\n"
        dest.write_text(header + body)
        print(f"✅ Deployed to {dest}")
        print("   This skill will be always-on for all Copilot interactions in this repo.")
        return True

    elif target == 'prompt-file':
        # Deploy as on-demand prompt file
        prompts_dir = github_dir / "prompts"
        prompts_dir.mkdir(exist_ok=True)
        dest = prompts_dir / f"{name}.prompt.md"
        dest.write_text(body)
        print(f"✅ Deployed to {dest}")
        print(f"   Users can invoke this skill in Copilot Chat.")
        return True

    elif target == 'agents-md':
        # Deploy as a section in AGENTS.md (legacy format)
        dest = repo_root / "AGENTS.md"
        section = f"\n\n## {name}\n\n{description}\n\n{body}\n"
        if dest.exists():
            existing = dest.read_text()
            # Check if section already exists
            if f"## {name}" in existing:
                # Replace existing section
                pattern = rf'(## {re.escape(name)}\n)(.*?)(?=\n## |\Z)'
                updated = re.sub(pattern, f'## {name}\n\n{description}\n\n{body}\n',
                                 existing, flags=re.DOTALL)
                dest.write_text(updated)
                print(f"✅ Updated section '{name}' in {dest}")
            else:
                dest.write_text(existing + section)
                print(f"✅ Appended section '{name}' to {dest}")
        else:
            header = f"# AGENTS.md\n\nCoding agent instructions for this project.\n"
            dest.write_text(header + section)
            print(f"✅ Created {dest} with section '{name}'")
        print("   ⚠️  Note: .github/agents/<name>.agent.md is the preferred format. Use --target agent instead.")
        return True

    elif target == 'agent':
        # Deploy as a custom agent (.github/agents/<name>.agent.md)
        agents_dir = github_dir / "agents"
        agents_dir.mkdir(exist_ok=True)
        dest = agents_dir / f"{name}.agent.md"
        agent_content = (
            f"---\n"
            f"name: {name}\n"
            f"description: >\n"
            f"  {description}\n"
            f"tools:\n"
            f"  - execute\n"
            f"  - read\n"
            f"  - edit\n"
            f"  - search\n"
            f"---\n\n"
            f"{body}\n"
        )
        dest.write_text(agent_content)
        print(f"✅ Deployed to {dest}")
        print(f"   Users can invoke this agent via @{name} in Copilot Chat.")
        print("   Edit the 'tools' list in the YAML frontmatter to adjust tool access.")
        return True

    elif target == 'standalone':
        # Deploy as a standalone skill directory
        dest_dir = github_dir / "skills" / name
        if dest_dir.exists():
            shutil.rmtree(dest_dir)
        shutil.copytree(skill_path, dest_dir,
                        ignore=shutil.ignore_patterns('__pycache__', '*.pyc', '.DS_Store', 'evals'))
        print(f"✅ Deployed standalone skill to {dest_dir}")
        return True

    else:
        print(f"❌ Unknown target: {target}")
        return False


def main():
    if len(sys.argv) < 2:
        print("Usage:")
        print("  python -m scripts.package_skill <path/to/skill-folder> [output-directory]")
        print("  python -m scripts.package_skill <path/to/skill-folder> --deploy-copilot <repo-root> [--target <target>]")
        print()
        print("Targets: instructions, prompt-file, agent, agents-md (legacy), standalone")
        print()
        print("Examples:")
        print("  python -m scripts.package_skill skills/my-skill")
        print("  python -m scripts.package_skill skills/my-skill ./dist")
        print("  python -m scripts.package_skill skills/my-skill --deploy-copilot /path/to/repo")
        print("  python -m scripts.package_skill skills/my-skill --deploy-copilot /path/to/repo --target instructions")
        sys.exit(1)

    skill_path = sys.argv[1]

    # Check for deploy mode
    if "--deploy-copilot" in sys.argv:
        idx = sys.argv.index("--deploy-copilot")
        if idx + 1 >= len(sys.argv):
            print("❌ Error: --deploy-copilot requires a repo root path")
            sys.exit(1)
        repo_root = sys.argv[idx + 1]

        target = None
        if "--target" in sys.argv:
            tidx = sys.argv.index("--target")
            if tidx + 1 < len(sys.argv):
                target = sys.argv[tidx + 1]

        print(f"🚀 Deploying skill to GitHub Copilot format: {skill_path}")
        print(f"   Repository: {repo_root}")
        if target:
            print(f"   Target: {target}")
        print()

        success = deploy_to_copilot(skill_path, repo_root, target)
        sys.exit(0 if success else 1)

    # Default: package mode
    output_dir = sys.argv[2] if len(sys.argv) > 2 else None

    print(f"📦 Packaging skill: {skill_path}")
    if output_dir:
        print(f"   Output directory: {output_dir}")
    print()

    result = package_skill(skill_path, output_dir)
    sys.exit(0 if result else 1)


if __name__ == "__main__":
    main()



