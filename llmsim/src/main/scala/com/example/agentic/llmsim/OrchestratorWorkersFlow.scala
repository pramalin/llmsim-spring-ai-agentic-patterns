package com.example.agentic.llmsim

import com.alai.llmsim.{Script, ScriptSource}
import com.alai.llmsim.Script.*

object OrchestratorWorkersFlow extends ScriptSource {

  private val orchestratorResponse =
    """{"analysis":"The announcement needs two audiences: a formal policy-oriented version and a conversational employee-facing version.","tasks":[{"type":"formal","description":"Write a precise, technical announcement for formal distribution."},{"type":"conversational","description":"Write an engaging, friendly announcement for employees."}]}"""

  private val formalResponse =
    """Formal Announcement:
      |The company will conduct a three-month pilot of a
      |four-day workweek to evaluate employee well-being
      |and sustained productivity.""".stripMargin

  private val conversationalResponse =
    """Team Announcement:
      |We are trying a four-day workweek for three months
      |to support well-being while keeping productivity
      |strong.""".stripMargin

  val script: Script = Script.exactly(
    reply(orchestratorResponse),
    reply(formalResponse),
    reply(conversationalResponse)
  )
}
