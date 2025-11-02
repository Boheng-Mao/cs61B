# Gitlet (CS 61B Project 2)

Gitlet is a teaching-scale version control system inspired by Git and built in Java for UC Berkeley's CS 61B course. The goal of the project is to model how distributed version control works from the internals out: commits, branching, checkout, reset, and merge are all implemented from scratch using plain file system persistence and Java serialization.

Project spec: https://sp21.datastructur.es/materials/proj/proj2/proj2

## Repository Structure

- `gitlet/Main.java` – CLI entry point. Parses command-line arguments and dispatches to repository operations.
- `gitlet/Repository.java` – Core command implementation. Coordinates staging, commit creation, branch updates, checkout, reset, and merge logic.
- `gitlet/Commit.java` – Immutable commit objects (message, timestamp, parent pointers, tracked blob IDs). Handles persistence and lookup utilities.
- `gitlet/Blob.java` – Wraps tracked file snapshots. Stores file contents, file name, and SHA-1 identifier; persisted under `.gitlet/objects/blobs`.
- `gitlet/Branch.java` – Represents branch heads and writes their commit pointers under `.gitlet/branches`.
- `gitlet/Stage.java` – Models the add/remove staging areas used between commits. Serialized files live under `.gitlet/stages`.
- `gitlet/Utils.java` – Common helpers for serialization, SHA-1 hashing, file I/O, and argument validation.
- `gitlet/GitletException.java`, `gitlet/Dumpable.java`, `gitlet/DumpObj.java` – Lightweight utilities for error handling and debugging object state.
- `testing/` – Provided harness and integration tests from the course.
- `pom.xml`, `Makefile` – Build tooling (Maven and a lightweight Makefile wrapper for automated grading scripts).

The compiled binaries land in `bin/` during development, and `target/` is used by Maven.

## On-Disk Layout

Initializing a repository creates a `.gitlet` directory mirroring Git's loose-object structure:

```
.gitlet/
  HEAD                  // current commit ID
  BRANCH                // current branch name
  branches/             // one file per branch storing its head commit ID
  stages/
    addStage            // files staged for addition (path -> blob ID)
    removeStage         // files staged for removal
  objects/
    commits/            // serialized commit objects
    blobs/              // serialized file snapshots
```

Every tracked file is materialized as a `Blob` (content + SHA-1) so commits can reference immutable snapshots. Commits form a DAG via parent pointers (single parent for normal commits, two parents for merges). Branches simply name commit IDs, making checkout and reset operations pointer updates plus working-directory restoration.

## Command Set

`Main` wires the spec-mandated commands:
- `init` – Create the `.gitlet` directory structure, the initial commit, the `master` branch, and empty staging areas.
- `add <file>` – Hash file contents, stage for addition if modified relative to the current commit, and persist the blob.
- `commit <message>` – Materialize a new commit from the staging areas, update the current branch head, and clear staging.
- `rm <file>` – Unstage a file or stage it for removal (and delete from working dir when appropriate).
- `log` / `global-log` – Traverse commit history (current branch or all commits) for human-readable inspection.
- `find <message>` – Locate commits by commit message.
- `status` – Summarize branch heads, staging area contents, and file state (like `git status`).
- `branch <name>` / `rm-branch <name>` – Create or delete branch references.
- `checkout` – Restore files or switch branches via the three spec-defined forms (`-- <file>`, `<commit> -- <file>`, `<branch>`).
- `reset <commit>` – Move the current branch head to a specific commit and reconcile the working directory.
- `merge <branch>` – Perform a three-way merge, writing conflicts to working files with conflict markers when needed.

All commands follow the CS 61B specification, including exit-on-error semantics and precise failure messages to satisfy the autograder.

## Development Notes

- Requires Java 11+ (course infrastructure targets OpenJDK 11).
- Build with `mvn package` or use the provided `Makefile` targets (e.g., `make check`).
- Run directly via `java gitlet.Main <command>`. The spec assumes invoking from the repository root.
- Tests under `testing/` mirror the official staff tests.
