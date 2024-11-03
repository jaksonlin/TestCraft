import com.github.jaksonlin.pitestintellij.util.Mutations

class MutationScoreReporter {
    data class FunctionScore(
        val className: String,
        val methodName: String,
        val methodDescription: String,
        val totalMutants: Int,
        val killedMutants: Int,
        val survivedMutants: Int,
        val score: Double
    ) {
        override fun toString(): String = buildString {
            append("Function: $className#$methodName$methodDescription\n")
            append("  Mutation Score: ${String.format("%.2f", score * 100)}%\n")
            append("  Total Mutants: $totalMutants\n")
            append("  Killed: $killedMutants\n")
            append("  Survived: $survivedMutants\n")
        }
    }

    fun generateReport(mutations: Mutations): String {
        val functionScores = mutations.mutation
            .groupBy { "${it.mutatedClass}#${it.mutatedMethod}${it.methodDescription}" }
            .map { (_, mutants) ->
                val totalMutants = mutants.size
                val killedMutants = mutants.count { it.status == "KILLED" }
                val survivedMutants = mutants.count { it.status == "SURVIVED" }

                FunctionScore(
                    className = mutants.first().mutatedClass,
                    methodName = mutants.first().mutatedMethod,
                    methodDescription = mutants.first().methodDescription,
                    totalMutants = totalMutants,
                    killedMutants = killedMutants,
                    survivedMutants = survivedMutants,
                    score = killedMutants.toDouble() / totalMutants
                )
            }
            .sortedByDescending { it.score }

        return buildString {
            appendLine("=== Mutation Testing Report ===")
            appendLine()

            // Overall project score
            val totalMutants = functionScores.sumOf { it.totalMutants }
            val totalKilled = functionScores.sumOf { it.killedMutants }
            val overallScore = totalKilled.toDouble() / totalMutants
            appendLine("Overall Project Score: ${String.format("%.2f", overallScore * 100)}%")
            appendLine("Total Mutants: $totalMutants")
            appendLine("Total Killed: $totalKilled")
            appendLine()

            appendLine("=== Function-Level Scores ===")
            functionScores.forEach { score ->
                appendLine(score.toString())
                appendLine()
            }
        }
    }
}