# Issues - Sofia Foundation

## 2026-03-03 Task 3: Hexagonal Skeleton + Clock/Exception Base

- `lsp_diagnostics` could not run because `kotlin-lsp` is not installed in this environment.
- Initial `ktlintCheck` failed on import order and constructor formatting; resolved by running `./gradlew ktlintFormat --no-daemon` and re-running quality gates.

## 2026-03-03 Task 4: ArchUnit Guardrails (Hexagonal + Timestamps)

- `lsp_diagnostics` still cannot validate `.kt`/`.kts` files because `kotlin-lsp` is not installed (`Command not found: kotlin-lsp`).
