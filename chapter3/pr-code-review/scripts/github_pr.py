#!/usr/bin/env python3
"""
GitHub PR Code Review Tool

Usage:
  # Fetch PR diff only
  python3 github_pr.py --owner alibaba --repo COLA --pr 518

  # Submit review comments
  python3 github_pr.py --owner alibaba --repo COLA --pr 518 \
    --submit \
    --comments '[{"path":"src/Foo.java","line":10,"body":"bug here"}]' \
    --body '## Review Summary\nFound 1 issue.'
"""

import argparse
import json
import os
import sys
from pathlib import Path

import requests

LANG_MAP = {
    ".java": "java",
    ".kt": "java",
    ".scala": "java",
    ".go": "go",
    ".py": "python",
    ".pyx": "python",
    ".cpp": "cpp",
    ".cc": "cpp",
    ".cxx": "cpp",
    ".h": "cpp",
    ".hpp": "cpp",
    ".hxx": "cpp",
    ".c": "cpp",
    ".cs": "csharp",
    ".rs": "rust",
    ".ts": "typescript",
    ".tsx": "typescript",
    ".js": "javascript",
    ".jsx": "javascript",
    ".rb": "ruby",
    ".php": "php",
    ".swift": "swift",
}

SEVERITY_LABELS = {
    "critical": "🔴 **Critical**",
    "major": "🟠 **Major**",
    "minor": "🟡 **Minor**",
    "suggestion": "🔵 **Suggestion**",
}


def load_env():
    env_paths = [
        Path.cwd() / ".env",
        Path.cwd().parent / ".env",
        Path.home() / ".env",
    ]
    for env_path in env_paths:
        if env_path.exists():
            with open(env_path) as f:
                for line in f:
                    line = line.strip()
                    if line and not line.startswith("#") and "=" in line:
                        key, _, value = line.partition("=")
                        os.environ.setdefault(key.strip(), value.strip())
            return
    print("Warning: No .env file found. GITHUB_TOKEN may not be set.", file=sys.stderr)


def get_github_token():
    token = os.environ.get("GITHUB_TOKEN", "")
    if not token:
        print("Error: GITHUB_TOKEN not set. Add it to .env or environment.", file=sys.stderr)
        sys.exit(1)
    return token


def api_headers(token):
    return {
        "Authorization": f"token {token}",
        "Accept": "application/vnd.github.v3+json",
    }


def fetch_pr_files(owner, repo, pr_number, token):
    url = f"https://api.github.com/repos/{owner}/{repo}/pulls/{pr_number}/files"
    headers = api_headers(token)
    all_files = []
    page = 1

    while True:
        resp = requests.get(url, params={"page": page, "per_page": 100}, headers=headers)
        if resp.status_code == 403 and "rate limit" in resp.text.lower():
            print("Error: GitHub API rate limit exceeded.", file=sys.stderr)
            sys.exit(1)
        resp.raise_for_status()
        files = resp.json()
        if not files:
            break
        all_files.extend(files)
        page += 1

    return all_files


def fetch_pr_info(owner, repo, pr_number, token):
    url = f"https://api.github.com/repos/{owner}/{repo}/pulls/{pr_number}"
    headers = api_headers(token)
    resp = requests.get(url, headers=headers)
    resp.raise_for_status()
    return resp.json()


def detect_language(filename):
    suffix = Path(filename).suffix.lower()
    return LANG_MAP.get(suffix, "unknown")


def load_review_guide(language, reference_dir=None):
    if reference_dir is None:
        reference_dir = Path(__file__).parent.parent / "references"
    else:
        reference_dir = Path(reference_dir)

    guide_map = {
        "java": "java-review-guide.md",
        "go": "go-review-guide.md",
        "python": "python-review-guide.md",
        "cpp": "cpp-review-guide.md",
    }

    guide_file = guide_map.get(language)
    if guide_file:
        guide_path = reference_dir / guide_file
        if guide_path.exists():
            return guide_path.read_text(encoding="utf-8")
    return None


def format_diff_output(files):
    output = []
    language_summary = {}

    for f in files:
        lang = detect_language(f["filename"])
        language_summary[lang] = language_summary.get(lang, 0) + 1

        output.append("=" * 80)
        output.append(f"File: {f['filename']}")
        output.append(f"Language: {lang}")
        output.append(f"Status: {f['status']}  |  Additions: +{f['additions']}  Deletions: -{f['deletions']}")
        output.append("=" * 80)
        if f.get("patch"):
            output.append(f["patch"])
        else:
            output.append("(No patch content — file may be too large)")
        output.append("")

    output.insert(0, "## Language Summary")
    for lang, count in sorted(language_summary.items(), key=lambda x: -x[1]):
        guide = load_review_guide(lang)
        guide_note = " (guide available)" if guide else " (no guide)"
        output.insert(1, f"  {lang}: {count} file(s){guide_note}")
    output.insert(2, "")

    return "\n".join(output)


def format_review_guide_output(files):
    languages = set()
    for f in files:
        lang = detect_language(f["filename"])
        if lang != "unknown":
            languages.add(lang)

    output = []
    for lang in sorted(languages):
        guide = load_review_guide(lang)
        if guide:
            output.append(f"\n{'#' * 80}")
            output.append(f"# Review Guide: {lang.upper()}")
            output.append(f"{'#' * 80}\n")
            output.append(guide)

    return "\n".join(output)


def submit_review(owner, repo, pr_number, token, comments, body):
    pr_info = fetch_pr_info(owner, repo, pr_number, token)
    head_sha = pr_info["head"]["sha"]

    payload = {
        "commit_id": head_sha,
        "body": body,
        "event": "COMMENT",
        "comments": comments,
    }

    url = f"https://api.github.com/repos/{owner}/{repo}/pulls/{pr_number}/reviews"
    resp = requests.post(url, headers=api_headers(token), json=payload)

    if resp.status_code in (200, 201):
        data = resp.json()
        print(f"Review submitted successfully!")
        print(f"  Review ID: {data['id']}")
        print(f"  Review URL: {data['html_url']}")
        print(f"  Comments posted: {len(comments)}")
        return data
    else:
        print(f"Error submitting review: {resp.status_code}", file=sys.stderr)
        print(resp.text, file=sys.stderr)
        sys.exit(1)


def main():
    load_env()

    parser = argparse.ArgumentParser(description="PR Code Review Tool")
    parser.add_argument("--owner", required=True, help="Repository owner")
    parser.add_argument("--repo", required=True, help="Repository name")
    parser.add_argument("--pr", type=int, required=True, help="Pull request number")
    parser.add_argument("--reference-dir", default=None, help="Path to reference guides directory")

    sub = parser.add_mutually_exclusive_group()
    sub.add_argument("--submit", action="store_true", help="Submit review to GitHub")
    sub.add_argument("--guides", action="store_true", help="Print language review guides only")

    parser.add_argument("--comments", default=None, help="JSON array of review comments (for --submit)")
    parser.add_argument("--comments-file", default=None, help="File containing JSON comments array (for --submit)")
    parser.add_argument("--body", default="Code review submitted via pr-code-review skill.", help="Review summary body (for --submit)")
    parser.add_argument("--body-file", default=None, help="File containing review summary body (for --submit)")

    args = parser.parse_args()
    token = get_github_token()

    if args.guides:
        files = fetch_pr_files(args.owner, args.repo, args.pr, token)
        output = format_review_guide_output(files)
        print(output)
        return

    if args.submit:
        if args.comments_file:
            with open(args.comments_file) as f:
                comments = json.load(f)
        elif args.comments:
            comments = json.loads(args.comments)
        else:
            print("Error: --submit requires --comments or --comments-file", file=sys.stderr)
            sys.exit(1)

        if args.body_file:
            with open(args.body_file) as f:
                body = f.read()
        else:
            body = args.body

        submit_review(args.owner, args.repo, args.pr, token, comments, body)
        return

    files = fetch_pr_files(args.owner, args.repo, args.pr, token)
    pr_info = fetch_pr_info(args.owner, args.repo, args.pr, token)

    print(f"PR: {pr_info['title']}")
    print(f"Author: {pr_info['user']['login']}")
    print(f"State: {pr_info['state']}")
    print(f"Head: {pr_info['head']['sha'][:12]}")
    print(f"Files changed: {len(files)}")
    print()

    print(format_diff_output(files))

    languages = set()
    for f in files:
        lang = detect_language(f["filename"])
        if lang != "unknown":
            languages.add(lang)

    if languages:
        print("\n📋 Applicable review guides:")
        for lang in sorted(languages):
            guide = load_review_guide(lang, args.reference_dir)
            status = "✓ loaded" if guide else "✗ not found"
            print(f"  {lang}: {status}")


if __name__ == "__main__":
    main()
