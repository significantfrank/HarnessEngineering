# Go Code Review Guidelines

## 1. Correctness

- [ ] Nil checks: Are pointer/receiver nil checks in place? Remember nil map write panics.
- [ ] Slice bounds: Check for out-of-range access. `len(slice)` before indexing.
- [ ] Range semantics: `for i, v := range slice` — `v` is a copy. Taking address of `&v` gives same pointer each iteration.
- [ ] Goroutine closures: Loop variables captured in goroutines need explicit parameter passing or shadowing (Go 1.22+ fixes this, but verify version).
- [ ] Interface nil: A typed nil (`var t *T; var i interface{} = t; i != nil` is true). Use explicit nil check on concrete type if needed.
- [ ] Map concurrency: Maps are NOT safe for concurrent read/write. Use `sync.RWMutex` or `sync.Map`.
- [ ] Error handling: Don't discard errors (`_ = doSomething()`). Handle or propagate every error.

## 2. Concurrency

- [ ] Goroutine leaks: Are all goroutines guaranteed to exit? Use `context.Context` for cancellation.
- [ ] Channel direction: Specify channel direction in function signatures (`chan<- T` for send-only, `<-chan T` for receive-only).
- [ ] Select default: `select { default: }` is a non-blocking operation. Ensure this is intentional.
- [ ] WaitGroup: `wg.Add()` must be called before goroutine starts, not inside it.
- [ ] Mutex: Avoid copying mutexes. Structs with mutexes should be passed by pointer.
- [ ] Deadlock: Lock ordering must be consistent. Avoid holding multiple locks simultaneously.

## 3. API Design

- [ ] Exported names: Only export what external packages need. Unexport internal helpers.
- [ ] Receiver names: Use short, consistent receiver names (typically first letter of type: `func (s *Server)` not `func (server *Server)`).
- [ ] Return structs or multiple values: For 3+ return values, consider returning a struct.
- [ ] Errors as values: Return `error` as the last return value. Use `fmt.Errorf("context: %w", err)` for wrapping.
- [ ] Interfaces: Define interfaces where they are consumed, not where they are implemented. Keep interfaces small.

## 4. Performance

- [ ] Slice pre-allocation: Use `make([]T, 0, n)` when size is known to avoid reallocation.
- [ ] String concatenation: Use `strings.Builder` for multiple concatenations.
- [ ] Avoid unnecessary `[]byte`↔`string` conversions: Each conversion allocates.
- [ ] Struct alignment: Order struct fields by size (largest first) to minimize padding.
- [ ] Defer in loops: `defer` in loops defers until function end, not loop iteration. Consider anonymous function wrapper.

## 5. Security

- [ ] SQL injection: Use `database/sql` parameterized queries, never `fmt.Sprintf`.
- [ ] Path traversal: Validate and sanitize file paths. Use `filepath.Clean`.
- [ ] Command injection: Avoid `exec.Command("sh", "-c", userInput)`. Use explicit args.
- [ ] Secrets: Never hardcode credentials. Use environment variables or secret managers.
- [ ] TLS: Use `crypto/tls` with minimum TLS 1.2. Set `MinVersion` explicitly.

## 6. Error Handling

- [ ] Sentinel errors: Use `errors.New("message")` for sentinel values. Check with `errors.Is()`.
- [ ] Custom error types: Implement `error` interface. Support `errors.As()` for type assertion.
- [ ] Wrap errors: Always add context with `fmt.Errorf("doing X: %w", err)` to preserve the chain.
- [ ] Panic: Only for truly unrecoverable programmer errors. Libraries should never panic.

## 7. Testing

- [ ] Table-driven tests: Use `t.Run` for subtests. Define test cases as struct slices.
- [ ] Test files: Place `_test.go` files in the same package for white-box testing.
- [ ] Golden files: For complex output, use golden file pattern with `testdata/`.
- [ ] Race detector: Run tests with `-race` flag. Ensure CI uses it.
- [ ] Benchmarks: Use `BenchmarkXxx` with `b.N` for performance-critical code.

## 8. Style & Conventions

- [ ] `gofmt` / `goimports`: Code must be formatted. No exceptions.
- [ ] Naming: Short names for short-lived variables, descriptive for long-lived ones. `i` for loop index is fine; `userIndex` for function scope.
- [ ] Comments: Start with the name of the thing being commented. `// Execute runs the job.` not `// This function runs the job.`
- [ ] Package names: Lowercase, single word, no underscores. `http` not `httputil` (unless needed for disambiguation).
- [ ] Error messages: Lowercase, no trailing period. `fmt.Errorf("failed to connect to %s", addr)`
