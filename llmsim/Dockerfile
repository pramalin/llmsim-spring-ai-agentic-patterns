FROM ghcr.io/pramalin/llmsim-build:0.3.0 AS build

COPY src/main/scala/com/example/agentic/llmsim/ChainWorkflowFlow.scala       /build/src/main/scala/com/example/agentic/llmsim/ChainWorkflowFlow.scala

COPY src/main/scala/com/example/agentic/llmsim/ParallelizationWorkflowFlow.scala       /build/src/main/scala/com/example/agentic/llmsim/ParallelizationWorkflowFlow.scala

COPY src/main/scala/com/example/agentic/llmsim/RoutingWorkflowFlow.scala       /build/src/main/scala/com/example/agentic/llmsim/RoutingWorkflowFlow.scala

COPY src/main/scala/com/example/agentic/llmsim/OrchestratorWorkersFlow.scala       /build/src/main/scala/com/example/agentic/llmsim/OrchestratorWorkersFlow.scala

COPY src/main/scala/com/example/agentic/llmsim/EvaluatorOptimizerFlow.scala       /build/src/main/scala/com/example/agentic/llmsim/EvaluatorOptimizerFlow.scala

RUN sbt assembly

FROM eclipse-temurin:21-jre-jammy

COPY --from=build       /build/target/scala-3.3.3/llmsim.jar       /app/llmsim.jar

ENV LLMSIM_SCRIPT=com.example.agentic.llmsim.ChainWorkflowFlow

EXPOSE 8089

ENTRYPOINT ["java", "-jar", "/app/llmsim.jar"]
