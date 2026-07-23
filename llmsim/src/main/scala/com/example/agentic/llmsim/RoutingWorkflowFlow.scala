package com.example.agentic.llmsim

import com.alai.llmsim.{Script, ScriptSource}
import com.alai.llmsim.Script.*

object RoutingWorkflowFlow extends ScriptSource {

  private val routeSelection =
    """{"reasoning":"The ticket reports a post-install crash and requests logs and recovery steps, so it requires technical support.","selection":"technical"}"""

  private val technicalResponse =
    """Technical Support Response:
      |1. Roll back to version 5.1 to restore service.
      |2. Collect the startup log and crash dump.
      |3. Escalate with the artifacts if version 5.2 still fails.""".stripMargin

  val script: Script = Script.exactly(
    reply(routeSelection),
    reply(technicalResponse)
  )
}
