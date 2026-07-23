package com.example.agentic.llmsim

import com.alai.llmsim.{Script, ScriptSource}
import com.alai.llmsim.Script.*

object ChainWorkflowFlow extends ScriptSource {

  val script: Script = Script.exactly(
    reply(
      """92: customer satisfaction
        |45%: revenue growth
        |23%: market share
        |5%: customer churn
        |$43: new user acquisition cost
        |78%: product adoption rate
        |87: employee satisfaction
        |34%: operating margin""".stripMargin
    ),
    reply(
      """92%: customer satisfaction
        |45%: revenue growth
        |23%: market share
        |5%: customer churn
        |$43: new user acquisition cost
        |78%: product adoption rate
        |87%: employee satisfaction
        |34%: operating margin""".stripMargin
    ),
    reply(
      """92%: customer satisfaction
        |87%: employee satisfaction
        |78%: product adoption rate
        |45%: revenue growth
        |$43: new user acquisition cost
        |34%: operating margin
        |23%: market share
        |5%: customer churn""".stripMargin
    ),
    reply(
      """|| Metric | Value |
        ||:--|--:|
        || Customer Satisfaction | 92% |
        || Employee Satisfaction | 87% |
        || Product Adoption Rate | 78% |
        || Revenue Growth | 45% |
        || New User Acquisition Cost | $43 |
        || Operating Margin | 34% |
        || Market Share | 23% |
        || Customer Churn | 5% |""".stripMargin
    )
  )
}
