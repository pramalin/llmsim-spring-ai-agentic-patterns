package com.example.agentic.llmsim

import com.alai.llmsim.{Script, ScriptSource}
import com.alai.llmsim.Script.*

object EvaluatorOptimizerFlow extends ScriptSource {

  private val firstGeneration =
    """{"thoughts":"The first draft stores values in one stack but calculates the minimum by scanning all values.","response":"public final class MinStack {\n  private final java.util.Deque<Integer> values = new java.util.ArrayDeque<>();\n\n  /** Pushes a value onto this stack. */\n  public void push(int x) {\n    this.values.push(x);\n  }\n\n  /** Removes and returns the top value. */\n  public int pop() {\n    return this.values.pop();\n  }\n\n  /** Returns the current minimum value. */\n  public int getMin() {\n    return this.values.stream().min(Integer::compareTo).orElseThrow();\n  }\n}"}"""

  private val needsImprovement =
    """{"evaluation":"NEEDS_IMPROVEMENT","feedback":"getMin() is O(n), not O(1). Use a second stack containing running minimum values so push, pop, and getMin remain O(1)."}"""

  private val improvedGeneration =
    """{"thoughts":"Use one stack for values and another for running minimum values so every operation remains O(1).","response":"/** Stack supporting constant-time minimum lookup. */\npublic final class MinStack {\n  private final java.util.Deque<Integer> values = new java.util.ArrayDeque<>();\n  private final java.util.Deque<Integer> minima = new java.util.ArrayDeque<>();\n\n  /** Pushes a value onto this stack. */\n  public void push(int x) {\n    this.values.push(x);\n    if (this.minima.isEmpty() || x <= this.minima.peek()) {\n      this.minima.push(x);\n    }\n  }\n\n  /** Removes and returns the top value. */\n  public int pop() {\n    int value = this.values.pop();\n    if (value == this.minima.peek()) {\n      this.minima.pop();\n    }\n    return value;\n  }\n\n  /** Returns the current minimum value. */\n  public int getMin() {\n    return this.minima.element();\n  }\n}"}"""

  private val passed =
    """{"evaluation":"PASS","feedback":"All operations are O(1), fields are private and accessed with this., duplicate minimum values are handled correctly, and the public API has Javadoc."}"""

  val script: Script = Script.exactly(
    reply(firstGeneration),
    reply(needsImprovement),
    reply(improvedGeneration),
    reply(passed)
  )
}
