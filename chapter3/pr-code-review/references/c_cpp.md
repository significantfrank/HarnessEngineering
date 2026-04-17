# C++ Code Review Guidelines

## 1. Correctness

- [ ] Null pointer dereference: Check pointers before dereferencing. Prefer references or `std::optional`.
- [ ] Uninitialized variables: All variables must be initialized. Use `-Werror=uninitialized` and clang-tidy.
- [ ] Out-of-bounds access: Use `at()` instead of `[]` for container access when bounds are uncertain.
- [ ] Use-after-move: After `std::move(x)`, `x` is in a valid-but-unspecified state. Don't read from it.
- [ ] Integer overflow/underflow: Check bounds before arithmetic. Use `size_t` consistently for sizes.
- [ ] Signed/unsigned comparison: Mixing `int` and `size_t` in comparisons. Use `std::ssize()` (C++20) or casts.
- [ ] Dangling references: Don't return references to local variables or temporaries.

## 2. Memory Management

- [ ] Ownership: Use `std::unique_ptr` for exclusive ownership, `std::shared_ptr` for shared ownership. Avoid raw `new`/`delete`.
- [ ] Smart pointer overhead: `std::shared_ptr` has atomic refcount overhead. Prefer `std::unique_ptr` when sharing isn't needed.
- [ ] Circular references: `std::shared_ptr` cycles cause memory leaks. Break with `std::weak_ptr`.
- [ ] RAII: Resources (files, locks, sockets) must be RAII-wrapped. No manual `close()` or `unlock()`.
- [ ] Rule of Five: If you define destructor, copy constructor, copy assignment, move constructor, or move assignment — define all five. Or use `= default` / `= delete`.
- [ ] Memory leaks: Run with ASan (`-fsanitize=address`) and Valgrind in CI.

## 3. Concurrency

- [ ] Data races: All shared mutable state must be protected by `std::mutex` or atomic operations.
- [ ] Lock_guard vs unique_lock: Use `std::lock_guard` for simple scoped locking. Use `std::unique_lock` for condition variables or deferred locking.
- [ ] Deadlock: Avoid nested locks. Use `std::scoped_lock(m1, m2)` for multiple mutexes (C++17).
- [ ] Atomic operations: Use `std::atomic<T>` for lock-free patterns. Ensure `T` is trivially copyable.
- [ ] Thread-safe initialization: Use `std::call_once` or function-local statics (Meyers' Singleton).
- [ ] Condition variables: Always use a predicate with `wait()` to prevent spurious wakeups.

## 4. API Design

- [ ] Const correctness: Mark methods `const` if they don't modify state. Pass parameters as `const T&` for read-only.
- [ ] Move semantics: Provide move constructor/assignment for types holding resources. Use `std::move` when transferring ownership.
- [ ] noexcept: Mark functions `noexcept` if they truly can't throw (destructors, move operations for simple types).
- [ ] Return types: Return by value (RVO/NRVO optimizes copies). Return `std::optional<T>` for may-absent values.
- [ ] Templates: Prefer concepts (C++20) or SFINAE to constrain template parameters. Don't create overly generic interfaces.
- [ ] Virtual destructors: Base classes with virtual methods MUST have virtual destructors.

## 5. Performance

- [ ] Unnecessary copies: Pass large objects by `const T&` or `T&&`. Watch for implicit copies in `auto` = `const auto&`.
- [ ] Emplace vs push: Use `emplace_back()` instead of `push_back()` to avoid temporary construction.
- [ ] Reserve: Call `reserve()` for vectors when size is known to avoid reallocation.
- [ ] Cache friendliness: Prefer contiguous memory (`std::vector`) over node-based (`std::list`, `std::map`).
- [ ] String views: Use `std::string_view` for read-only string parameters to avoid allocations.
- [ ] Hot paths: Avoid heap allocations in tight loops. Pre-allocate or use stack-based containers.

## 6. Security

- [ ] Buffer overflows: Use `std::array`, `std::vector`, `std::string` instead of C arrays and `char*`.
- [ ] Format strings: Use `std::format` (C++20) or `fmt::format`, never `printf` with user input.
- [ ] Input validation: Validate all external input. Use bounded operations (`strncpy` → `std::string::copy`).
- [ ] Hardcoded secrets: No credentials in source code. Use environment variables or configuration files.
- [ ] Random numbers: Use `std::random_device` + `std::mt19937`, never `rand()`.

## 7. Error Handling

- [ ] Exceptions vs error codes: Use exceptions for exceptional conditions, error codes (`std::expected` in C++23, `absl::StatusOr`) for expected failures.
- [ ] Exception safety: Aim for at least basic guarantee. Prefer strong guarantee for public APIs.
- [ ] No exceptions in destructors: Destructors must not throw. Wrap in `try/catch` if needed.
- [ ] Assert vs exceptions: Use `assert()` for programmer errors (impossible conditions), exceptions for runtime errors.

## 8. Style & Conventions

- [ ] Naming: `PascalCase` for classes, `camelCase` or `snake_case` for functions/variables (pick one, be consistent).
- [ ] Include guards: Use `#pragma once` or traditional `#ifndef` guards. Forward-declare when possible.
- [ ] Header organization: Declarations in `.h`, definitions in `.cpp`. Minimize includes in headers.
- [ ] `auto`: Use `auto` when type is obvious from context. Avoid `auto` when it obscures the type.
- [ ] `constexpr`: Mark compile-time computable values as `constexpr`.
- [ ] C++ standard: Know the project's minimum C++ version. Don't use features beyond it.
