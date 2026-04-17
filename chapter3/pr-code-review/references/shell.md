# Shell Code Review Guidelines

## 1. Correctness

- [ ] Word splitting: Always quote variable expansions (`"$var"`, not `$var`) unless word splitting is intentional.
- [ ] Globs: Unquoted variables are subject to glob expansion. Use `"$@"` not `$@` in most cases.
- [ ] `set -euo pipefail`: Use strict mode in production scripts. Understand what each flag does.
- [ ] Exit codes: Check return values of commands (`if ! command; then ...`). Use `set -e` or explicit checks.
- [ ] Shebang: Use `#!/usr/bin/env bash` for portability. Use `#!/bin/sh` only if POSIX-compliant.
- [ ] `[[` vs `[`: In bash, prefer `[[ ]]` over `[ ]` — no word splitting, no pathname expansion, supports `&&`/`||` and pattern matching.

## 2. Error Handling

- [ ] `set -e`: Script exits on any command failure. Be aware it doesn't work inside pipes (use `set -o pipefail`).
- [ ] Trap: Use `trap 'cleanup' EXIT` for cleanup. Use `trap 'err_handler $LINENO' ERR` for error reporting.
- [ ] Pipe failures: `command1 | command2` — by default, only the last command's exit code matters. Use `set -o pipefail` or `PIPESTATUS`.
- [ ] Command substitution: Check `$?` or use `if !` after critical commands. `var=$(command)` silently fails if `set -e` is off.

## 3. Security

- [ ] Injection: Never `eval` or `source` untrusted input. Avoid `eval "$var"`.
- [ ] Temp files: Use `mktemp` for temp files, not predictable names like `/tmp/myapp.$$`.
- [ ] Permissions: Set restrictive permissions (`chmod 600`) for files containing sensitive data.
- [ ] Secrets: Don't hardcode passwords. Use environment variables or secret management tools.
- [ ] Path traversal: Validate paths. Don't interpolate user input into file paths without sanitization.
- [ ] Sudo: Avoid `sudo` inside scripts. Prefer running the whole script with appropriate privileges.

## 4. Portability

- [ ] Bashisms: If using `#!/bin/sh`, avoid bash-specific features (`[[`, `(( ))`, arrays, `${var//pattern/replacement}`, process substitution).
- [ ] GNU vs BSD: Commands like `sed`, `date`, `xargs` differ between GNU and BSD (macOS). Use `gsed`/`gdate` or write portable alternatives.
- [ ] Line endings: Ensure LF line endings. CRLF causes `'\r': command not found`.
- [ ] Locale: Set `export LC_ALL=C` for predictable sorting and text processing.

## 5. Performance

- [ ] Avoid subshells: `var=$(cat file)` spawns a subshell. Use `read -r var < file` for single lines.
- [ ] Avoid pipes in loops: `while read -r line; do ...; done < file` is faster than `cat file | while read -r line`.
- [ ] Builtins: Prefer shell builtins (`[[ ]]`, `${var#pattern}`, `${var%pattern}`) over external commands (`grep`, `sed`, `awk`) for simple operations.
- [ ] `printf` over `echo`: `echo` behavior varies across implementations. `printf` is portable and predictable.

## 6. Style & Conventions

- [ ] ShellCheck: Run `shellcheck` on all shell scripts. Fix all warnings.
- [ ] Naming: `UPPER_SNAKE_CASE` for constants and environment variables, `lower_snake_case` for local variables and functions.
- [ ] Functions: Extract repeated logic into functions. Use `local` for function-scoped variables.
- [ ] Indentation: Use 2 or 4 spaces consistently. Never mix tabs and spaces.
- [ ] Comments: Explain WHY, not WHAT. Document non-obvious behavior, edge cases, and workarounds.
- [ ] Long lines: Break with `\` at logical points. Keep lines under 80-100 characters.
- [ ] `readonly` / `declare -r`: Use for constants that should not be reassigned.
