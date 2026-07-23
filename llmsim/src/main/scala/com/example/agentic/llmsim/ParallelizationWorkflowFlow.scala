package com.example.agentic.llmsim

import com.alai.llmsim.{Script, ScriptSource}
import com.alai.llmsim.Script.*

object ParallelizationWorkflowFlow extends ScriptSource {

  private val stakeholderResponse =
    "Stakeholder analysis completed."

  val script: Script = Script.exactly(
    reply(stakeholderResponse),
    reply(stakeholderResponse),
    reply(stakeholderResponse),
    reply(stakeholderResponse)
  )
}
