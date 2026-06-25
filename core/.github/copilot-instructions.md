This repository: concise guidance for AI coding agents

Purpose
- Help an AI contributor become productive quickly by describing the codebase structure, primary components, build/debug flows, and repository-specific conventions.

Big picture
- Java Maven project laid out under `src/main/java/com/kyoko` and `src/test/java`.
- Major packages:
  - `com.kyoko.common` — core streams, utilities, logging, text/date helpers (see [src/main/java/com/kyoko/common/StreamMultiplexer.java](src/main/java/com/kyoko/common/StreamMultiplexer.java)).
  - `com.kyoko.parser` — expression/parser logic, Excel-formula handling, function interfaces (see [src/main/java/com/kyoko/parser/Parser.java](src/main/java/com/kyoko/parser/Parser.java)).
  - `com.kyoko.decumulator` — numeric/decumulator logic and error handling (see [src/main/java/com/kyoko/decumulator/BaseDecumulator.java](src/main/java/com/kyoko/decumulator/BaseDecumulator.java)).
  - `com.kyoko.crypto`, `rpccall`, `tables`, `uniinformation` — ancillary subsystems; inspect when making cross-cutting changes.

Key integration patterns
- Streams and multiplexing is central: `SocketByteStream`, `BufferedByteStream`, `StreamMultiplexer`, `StreamPipe` form a pipeline pattern for IO.
- Parser components use pluggable `FunctionInterface`/`Function` implementations and `Expression` nodes — prefer extending interfaces rather than duplicating parsing logic.
- Look for `*.bak` files (e.g., `StringUtil.java.bak`) — these are developer backups; avoid editing these files unless intentionally restoring history.

Build, test, and debug
- Build: `mvn clean package` at the repo root.
- Run tests: `mvn test` (or `mvn -DskipTests=false test`).
- IDE: project is arranged for Eclipse (workspace path indicates Eclipse); prefer running via Eclipse run configurations for debugging networked components.

Project-specific conventions
- Utilities in `com.kyoko.common` are widely reused; centralize changes there rather than copying helpers into other packages.
- Methods and classes often include small helper classes (e.g., `Sprintf`, `StringUtil`, `ValueUtil`). Search these when fixing string/formatting bugs.
- There are generated artifacts under `target/` — do not edit generated files directly.

Suggested agent behaviors
- When adding or modifying parsing behavior, update `parser/FunctionInterface.java` and `parser/Parser.java` together and add unit tests under `src/test/java`.
- For IO or protocol changes, run or simulate `StreamMultiplexer` and `SocketByteStream` paths; tests and debug sessions should exercise both buffered and socket streams.
- Avoid touching `.bak` files and `target/` contents. Respect existing utility methods — prefer reuse.

Files to inspect first (examples)
- `src/main/java/com/kyoko/common/StreamMultiplexer.java`
- `src/main/java/com/kyoko/common/SocketByteStream.java`
- `src/main/java/com/kyoko/parser/Parser.java`
- `src/main/java/com/kyoko/decumulator/BaseDecumulator.java`

When in doubt
- Run `mvn -q -DskipTests=false test` to verify behaviour after changes.
- If a change affects streaming or parser behaviour, include a short integration test that wires the minimal stream+parser components.

Ask for feedback
- If any part of this summary is unclear or you want more examples (patch examples, tests, or a run harness), tell me which area to expand.
