# Agent Instructions & Workflow Rules
## Siva Machine Works Smart Manufacturing Platform

These rules apply to all automated agents, subagents, and LLMs working on this codebase.

### 1. Code Modification Rules
- **Inspect Before Editing:** Always read existing code, documentation, and related files before making changes.
- **Preserve Working Functionality:** Never overwrite working logic unless explicitly required by the prompt or to fix a bug.
- **No Fake Logic:** Do not use mock implementations or "fake logic" when real business rules are expected.
- **Feature Completion:** Do not mark features as complete until they have been fully implemented and actually work.
- **Run Tests:** Execute relevant tests after modifying code to verify functionality and catch regressions.
- **Architectural Changes:** Explain major architectural changes in your thought process or communications before executing them.

### 2. Security Mandates
- **No Hardcoded Secrets:** Never include raw passwords, keys, or tokens in the code.
- **Do Not Disable Security:** Never disable authentication, RBAC, or CSRF/CORS protections to bypass failing tests. Tests must adapt to security.
- **Restricted AI Database Access:** LLMs and agents must not provision direct unrestricted SQL access to the database. All AI data access must flow through secure RAG pipelines.

### 3. Dependency Management
- **Justify Dependencies:** Do not add new external libraries or dependencies (Maven/npm) without explicit justification and ensuring they align with the project stack.

### 4. Communication & Context
- **Modular Awareness:** Be aware of the modular monolith structure. Ensure changes in one module do not illegally directly reference internal services of another module.
- **Leverage Docs:** Frequently consult the `docs/` directory (`01-project-overview.md`, `03-system-architecture.md`, `10-development-rules.md`, etc.) for domain context.
