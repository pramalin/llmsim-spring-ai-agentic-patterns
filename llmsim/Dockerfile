FROM ghcr.io/pramalin/llmsim-build:0.2.1 AS build

COPY src/main/scala/com/example/agentic/llmsim/ChainWorkflowFlow.scala       /build/src/main/scala/com/example/agentic/llmsim/ChainWorkflowFlow.scala

COPY src/main/scala/com/example/agentic/llmsim/EvaluatorOptimizerFlow.scala       /build/src/main/scala/com/example/agentic/llmsim/EvaluatorOptimizerFlow.scala

RUN sbt assembly

FROM eclipse-temurin:21-jre-jammy

COPY --from=build       /build/target/scala-3.3.3/llmsim.jar       /app/llmsim.jar

ENV LLMSIM_SCRIPT=com.example.agentic.llmsim.ChainWorkflowFlow

EXPOSE 8089

ENTRYPOINT ["java", "-jar", "/app/llmsim.jar"]
