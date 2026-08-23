---
name: compile
description: Build the FigTree fork in this repo (C:\Users\Magdalena Dusza\workspeace\my_figtree) into runnable jar files using the locally-installed JDK 8 and Apache Ant. Use this whenever the user asks to compile, build, skompiluj, zbuduj, or wants to check whether their code changes work / see if the program still runs, especially after they've made several edits and are ready to test them together. Do NOT trigger this after every single small edit — the user prefers batching changes and compiling once at the end of a work session.
---

# Compile FigTree

Builds the project with Apache Ant. There is no system-wide JDK or Ant installed —
both were extracted locally into a `tools` folder, so the build must point at them
explicitly via environment variables rather than relying on anything already on PATH.

## Steps

1. Run the build:

```bash
export JAVA_HOME="/c/Users/Magdalena Dusza/tools/jdk8u502-b07"
export ANT_HOME="/c/Users/Magdalena Dusza/tools/apache-ant-1.10.15"
cd "/c/Users/Magdalena Dusza/workspeace/my_figtree"
"$ANT_HOME/bin/ant"
```

2. Check the tail of the output for `BUILD SUCCESSFUL` or `BUILD FAILED`.

## On success

Report success in plain, non-technical language (the user is new to coding) and point
to the built jar:

```
C:\Users\Magdalena Dusza\workspeace\my_figtree\dist\figtree.jar
```

(Ant also produces `figtreepanel.jar` and `figtree-pdf.jar` alongside it — these are
sub-components, not separate versions of the program; `figtree.jar` is the one to run.)

Offer to launch it so the user can see the change in action:

```bash
export JAVA_HOME="/c/Users/Magdalena Dusza/tools/jdk8u502-b07"
"$JAVA_HOME/bin/java" -jar "/c/Users/Magdalena Dusza/workspeace/my_figtree/dist/figtree.jar"
```

## On failure

`BUILD FAILED` means there's a mistake in the code (most often a typo or a syntax
error introduced by an edit) — this is normal while iterating, not a sign the setup
broke. Show the relevant error lines from the Ant/javac output (the ones naming a
file and line number, e.g. `FigTreePanel.java:42: error: ...`) rather than the full
verbose log, and explain in plain terms what's wrong before attempting a fix.
