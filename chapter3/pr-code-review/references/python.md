# Python Code Review Guidelines

## 1. Correctness

- [ ] None checks: Verify `None` is handled before method/attribute access. Use `is None` / `is not None`, not `== None`.
- [ ] Mutable default arguments: `def foo(items=[])` is a classic bug — the list is shared across calls. Use `None` default + `items = items or []` inside.
- [ ] Type coercion: Watch for implicit conversions between `int`/`float`/`str`. `0 == False` is True.
- [ ] Index errors: Check bounds before indexing. Use slicing or `.get()` for safe access.
- [ ] Exception swallowing: Avoid bare `except:` or `except Exception:` that silently catch and discard errors.
- [ ] Generator exhaustion: Generators can only be iterated once. Convert to list if reuse is needed.

## 2. Concurrency & Thread Safety

- [ ] GIL limitations: The GIL prevents true parallelism for CPU-bound code. Use `multiprocessing` instead of `threading` for CPU work.
- [ ] Shared state: `threading.Lock` / `threading.RLock` for shared mutable state across threads.
- [ ] asyncio: Mixing sync and async code? Use `asyncio.to_thread()` for blocking calls. Never call `time.sleep()` in async code.
- [ ] Race conditions: Check-then-act patterns need locking (e.g., `if key not in cache: cache[key] = value`).
- [ ] Process-safe data structures: `multiprocessing.Manager` for shared state across processes.

## 3. API Design

- [ ] Type hints: Use type hints for public APIs. `def get_user(user_id: int) -> Optional[User]:`.
- [ ] Keyword-only arguments: Use `*` separator for clarity: `def query(sql, *, params=None)`.
- [ ] Return types: Be consistent. Don't return `None` sometimes and raise exception other times.
- [ ] Protocol vs ABC: Use `typing.Protocol` for structural subtyping, `abc.ABC` for nominal subtyping.
- [ ] Dataclasses: Prefer `@dataclass` for simple data containers. Use `frozen=True` for immutability.

## 4. Performance

- [ ] List comprehension vs loop: Prefer list/dict/set comprehensions over loops with `.append()`.
- [ ] Generator expressions: Use `(...)` instead of `[...]` for memory efficiency when iterating once.
- [ ] String concatenation: Use `f-strings` or `str.join()`, not `+` in loops.
- [ ] Dictionary lookups: Use `dict.get(key, default)` instead of `if key in dict: dict[key]`.
- [ ] Avoid repeated lookups: Cache `len(list)` or attribute lookups outside tight loops.

## 5. Security

- [ ] Input validation: Validate and sanitize all external input. Use libraries like `pydantic` for schema validation.
- [ ] SQL injection: Use parameterized queries with ORM or `cursor.execute("SELECT ? FROM t", (val,))`.
- [ ] Code injection: Never use `eval()`, `exec()`, or `__import__()` with untrusted input.
- [ ] Pickle: Never unpickle untrusted data. Use `json`, `msgpack`, or `protobuf`.
- [ ] Hardcoded secrets: No credentials in source code. Use environment variables or secret managers.
- [ ] Path traversal: Use `pathlib.Path.resolve()` and verify the path stays within expected directories.

## 6. Error Handling

- [ ] Specific exceptions: Catch specific exceptions (`except ValueError:`), not bare `except:`.
- [ ] Exception chaining: Use `raise NewError(...) from original_error` to preserve stack trace.
- [ ] Custom exceptions: Inherit from appropriate base (`ValueError`, `RuntimeError`, etc.), not bare `Exception`.
- [ ] Logging: Use `logging` module, not `print()`. Configure log levels appropriately.

## 7. Testing

- [ ] Test framework: Use `pytest`. Avoid `unittest` unless compatibility is required.
- [ ] Fixtures: Use `@pytest.fixture` for setup/teardown. Scope appropriately (`function`, `module`, `session`).
- [ ] Parametrize: Use `@pytest.mark.parametrize` for data-driven tests instead of loops.
- [ ] Mocking: Use `unittest.mock`. Prefer `patch` as context manager or decorator. Don't over-mock.
- [ ] Coverage: Aim for meaningful coverage, not 100%. Test edge cases, not just happy paths.

## 8. Style & Conventions

- [ ] PEP 8: Follow PEP 8. Use `ruff` or `black` for formatting, `isort` for import sorting.
- [ ] Naming: `snake_case` for functions/variables, `PascalCase` for classes, `UPPER_SNAKE_CASE` for constants.
- [ ] Docstrings: Use Google style or NumPy style. Public functions/classes must have docstrings.
- [ ] Imports: Absolute imports preferred. Avoid wildcard imports (`from module import *`).
- [ ] Line length: 88 (black default) or 79 (PEP 8). Pick one and enforce with linter.
- [ ] f-strings: Prefer f-strings over `%` or `.format()` for string formatting (Python 3.6+).
