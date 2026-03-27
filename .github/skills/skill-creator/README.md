# Copilot Skill Creator

A skill for creating new GitHub Copilot skills and iteratively improving them. Feature-compatible port of [Anthropic's skill-creator](https://github.com/anthropics/skills/tree/main/skills/skill-creator), adapted for the GitHub Copilot ecosystem.

## What is a Skill?

A skill is a structured set of instructions that an AI coding assistant follows to accomplish a specific type of task. Think of it as a reusable "recipe" — instead of explaining the same workflow every time, you capture it once and let the AI follow it consistently.

## Quick Start

### Prerequisites

- Python 3.11+
- An LLM API key (OpenAI, Azure, or Anthropic)

### Installation

```bash
cd copilot-skill-creator
pip install -r requirements.txt
```

### Configure LLM Backend

```bash
# Option 1: OpenAI (default)
export SKILL_LLM_BACKEND=openai
export OPENAI_API_KEY=sk-...
export SKILL_LLM_MODEL=gpt-4o

# Option 2: Azure OpenAI
export SKILL_LLM_BACKEND=azure
export AZURE_OPENAI_ENDPOINT=https://your-resource.openai.azure.com
export AZURE_OPENAI_API_KEY=...
export AZURE_OPENAI_DEPLOYMENT=gpt-4o

# Option 3: Anthropic
export SKILL_LLM_BACKEND=anthropic
export ANTHROPIC_API_KEY=sk-ant-...
export SKILL_LLM_MODEL=claude-sonnet-4-20250514
```

## Usage

### Creating a Skill

The primary way to use this is through GitHub Copilot Chat. Load the `SKILL.md` into your conversation context and tell Copilot what kind of skill you want to create. It will guide you through:

1. **Capturing intent** — What should the skill do? When should it trigger?
2. **Writing the draft** — Creating the SKILL.md with proper frontmatter
3. **Testing** — Running eval prompts against the skill
4. **Iterating** — Improving based on feedback
5. **Deploying** — Installing into your repo

### Command-Line Tools

#### Validate a skill
```bash
python -m scripts.quick_validate path/to/skill-folder
```

#### Package a skill
```bash
python -m scripts.package_skill path/to/skill-folder
```

#### Deploy to GitHub Copilot format
```bash
# As always-on project instructions
python -m scripts.package_skill path/to/skill --deploy-copilot /path/to/repo --target instructions

# As an on-demand prompt file
python -m scripts.package_skill path/to/skill --deploy-copilot /path/to/repo --target prompt-file

# As a custom agent (autonomous, tool-using)
python -m scripts.package_skill path/to/skill --deploy-copilot /path/to/repo --target agent

# As a section in AGENTS.md (legacy)
python -m scripts.package_skill path/to/skill --deploy-copilot /path/to/repo --target agents-md
```

#### Run trigger evaluation
```bash
python -m scripts.run_eval \
  --eval-set evals/trigger_eval.json \
  --skill-path path/to/skill \
  --model gpt-4o \
  --verbose
```

#### Run optimization loop
```bash
python -m scripts.run_loop \
  --eval-set evals/trigger_eval.json \
  --skill-path path/to/skill \
  --model gpt-4o \
  --max-iterations 5 \
  --verbose
```

#### Aggregate benchmark results
```bash
python -m scripts.aggregate_benchmark workspace/iteration-1 --skill-name my-skill
```

#### Launch eval viewer
```bash
python eval-viewer/generate_review.py workspace/iteration-1 --skill-name my-skill
```

## Skill Format

```markdown
---
name: my-skill
description: >
  Use this skill when the user wants to... Make sure to use it whenever
  they mention X, Y, or Z.
---

# My Skill

Instructions for the AI go here...
```

### Deployment Targets

| Target | File | When to use |
|--------|------|-------------|
| `instructions` | `.github/copilot-instructions.md` | Always-on for entire repo |
| `prompt-file` | `.github/prompts/<name>.prompt.md` | On-demand invocation |
| `agent` | `.github/agents/<name>.agent.md` | Autonomous agent with tool access |
| `agents-md` | `AGENTS.md` | Legacy coding agent workflows |
| `standalone` | `.github/skills/<name>/` | Complex multi-file skills |

## Project Structure

```
skill-creator/
├── SKILL.md                    # Main skill instructions
├── README.md                   # This file
├── requirements.txt            # Python dependencies
├── agents/                     # Bundled subagent instructions (internal to this skill)
│   ├── grader.md               # Eval grading instructions
│   ├── comparator.md           # Blind A/B comparison
│   └── analyzer.md             # Post-hoc analysis
├── references/
│   └── schemas.md              # JSON schema documentation
├── scripts/
│   ├── llm_client.py           # Multi-backend LLM client
│   ├── quick_validate.py       # Skill validation
│   ├── package_skill.py        # Packaging & deployment
│   ├── run_eval.py             # Trigger evaluation
│   ├── improve_description.py  # Description optimizer
│   ├── run_loop.py             # Eval + improve loop
│   ├── aggregate_benchmark.py  # Benchmark aggregation
│   └── generate_report.py      # HTML report generation
├── eval-viewer/
│   ├── generate_review.py      # Review page server
│   └── viewer.html             # Review page template
└── assets/
    └── eval_review.html        # Eval set review template
```

The `agents/` directory contains internal subagent instructions that the skill-creator reads automatically during the eval workflow. These are **not** top-level GitHub Copilot custom agents — they are bundled resources that the skill uses without requiring explicit user invocation.

## Key Differences from Anthropic's skill-creator

| Feature | Anthropic (Claude Code) | This (GitHub Copilot) |
|---------|------------------------|----------------------|
| LLM backend | `claude -p` only | OpenAI, Azure, Anthropic, GitHub Copilot |
| Eval runner | Uses Claude Code skill commands | Simulated skill-selection prompts |
| Deployment | `.skill` package | `.github/copilot-instructions.md`, prompt files, agents, AGENTS.md |
| Agent format | Plain `.md` in skill bundle | Same — bundled `.md` in `agents/` dir (internal to skill) |
| Built-in tools | Claude Code tools | `execute`, `read`, `edit`, `search`, `web` + MCP |
| Parallelism | `ProcessPoolExecutor` | `ThreadPoolExecutor` (API-based) |
| Skill format | SKILL.md (same) | SKILL.md (same) + Copilot deployment targets |
| Agents/grader | Bundled in skill `agents/` dir | Identical — bundled in skill `agents/` dir |
| Eval viewer | Identical | Identical |
| Benchmark | Identical | Identical |

## License

Apache License 2.0 — see [LICENSE.txt](LICENSE.txt)

