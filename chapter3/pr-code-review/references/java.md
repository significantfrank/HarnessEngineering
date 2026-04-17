# Java Code Review Guidelines

## 1. Correctness

- [ ] Null checks: Are all references checked for null before use? Watch for NPE on method chain calls.
- [ ] Optional usage: Is `Optional` used correctly? Avoid `Optional.get()` without `isPresent()`. Prefer `orElse`, `orElseThrow`, `map`, `flatMap`.
- [ ] Off-by-one errors: Verify loop bounds, substring indices, and array access.
- [ ] Equality: Use `equals()` for object comparison, not `==`. For enums, `==` is acceptable.
- [ ] Integer overflow: Check for potential overflow in arithmetic, especially with `int` for sizes/indices.
- [ ] Exception handling: Are checked exceptions properly caught or declared? Avoid swallowing exceptions with empty catch blocks.
- [ ] Resource leaks: Are streams, connections, and files closed? Prefer try-with-resources.

## 2. Concurrency & Thread Safety

- [ ] Shared mutable state: Is shared state properly synchronized? Check for race conditions.
- [ ] `ConcurrentHashMap`: Check-then-act patterns (`get` then `put`) are NOT atomic. Use `computeIfAbsent`, `putIfAbsent`, or `merge`.
- [ ] `synchronized` blocks: Check for potential deadlocks. Lock ordering must be consistent.
- [ ] Immutable objects: Prefer immutability for shared data. Verify `final` fields, defensive copies.
- [ ] `volatile`: Is `volatile` needed? Does the code rely on visibility guarantees?
- [ ] Thread pools: Are `ExecutorService` instances properly shut down?
- [ ] Date/Time: Use `java.time` (immutable, thread-safe). Avoid `SimpleDateFormat` (not thread-safe).

## 3. API Design

- [ ] Naming: Methods should be verbs (`getTargetStates`), classes should be nouns (`StateChain`).
- [ ] Interface vs implementation: Program to interfaces. Return interface types, not concrete types.
- [ ] Generics: Avoid raw types. Use `List<String>`, not `List`. Check for unchecked casts.
- [ ] Backward compatibility: Are new methods added to interfaces? This breaks implementors. Consider default methods or a new interface.
- [ ] Method signatures: Avoid too many parameters (>4). Consider builder pattern or parameter object.
- [ ] Null return vs Optional: Prefer `Optional<T>` over returning null.

## 4. Performance

- [ ] Unnecessary object creation: Avoid creating objects in tight loops. Prefer object pools or reuse.
- [ ] String concatenation in loops: Use `StringBuilder`.
- [ ] Collection sizing: Provide initial capacity for `ArrayList`, `HashMap` when size is known.
- [ ] Stream vs loop: Streams can be slower for simple operations due to overhead. Prefer loops for hot paths.
- [ ] Boxing/Unboxing: Avoid autoboxing in performance-critical code. Use primitive collections if needed.

## 5. Security

- [ ] Input validation: Validate all external input. Use `Objects.requireNonNull` for parameters.
- [ ] SQL injection: Use `PreparedStatement`, never string concatenation for SQL.
- [ ] Sensitive data: Don't log passwords, tokens, or PII. Use `toString()` that masks sensitive fields.
- [ ] Deserialization: Avoid `ObjectInputStream` for untrusted data. Use safe alternatives.

## 6. Error Handling

- [ ] Specific exceptions: Catch specific exceptions, not `Exception` or `Throwable`.
- [ ] Exception messages: Include context (what failed, with what input).
- [ ] Custom exceptions: Extend `RuntimeException` for unchecked, `Exception` for checked. Follow naming convention (`XxxException`).
- [ ] Logging: Log at appropriate level (ERROR for failures, WARN for recoverable issues, INFO for significant events, DEBUG for details).

## 7. Testing

- [ ] Test coverage: Are new methods tested? Are edge cases covered?
- [ ] Assertions: Use specific assertions (`assertEquals(expected, actual)`, not `assertTrue(a == b)`).
- [ ] Test isolation: Tests should not depend on execution order. Use `@BeforeEach`/`@AfterEach`.
- [ ] Mocking: Prefer real objects over mocks when possible. Don't mock what you don't own.

## 8. Style & Conventions

- [ ] Java naming conventions: `camelCase` for methods/variables, `PascalCase` for classes, `UPPER_SNAKE_CASE` for constants.
- [ ] Javadoc: Public APIs must have Javadoc. Include `@param`, `@return`, `@throws` where applicable.
- [ ] Code organization: One top-level class per file. Inner classes only when strongly coupled.
- [ ] Modifiers order: `public protected private abstract default static final transient volatile synchronized native strictfp`
- [ ] Import order: Avoid wildcard imports (`import java.util.*`).
