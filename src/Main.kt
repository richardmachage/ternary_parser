import java.util.regex.Pattern


fun main() {
    val expressionString = """
        if (var_1 == 2, 0, 
        if (var_2 == 4, 15, 0))
        + if (var_2 == 3, 5, 0)
        - if (var_4 == 2, 0, 5)
        + if (var_3 == 3, 5, 0)
    """.trimIndent()

    val variables = mapOf(
        "var_1" to 1,
        "var_2" to 4,
        "var_3" to 3,
        "var_4" to 5
    )

    var myAnswer = evaluateExpression(
        expression = expressionString,
        variables = variables
    )

    println("The answer is $myAnswer")
}

//evaluateExpression
fun evaluateExpression(expression: String, variables: Map<String, Int>): Int {
    // Helper function to evaluate a ternary condition
    fun evalCondition(match: MatchResult): String {
        // Extract the condition, truthy value, and falsy value from the match
        val condition = match.groupValues[1].trim()
        val truthyValue = match.groupValues[2].trim()
        val falsyValue = match.groupValues[3].trim()

        // Evaluate the condition
        val conditionValue = evaluateCondition(condition, variables)
        // Return the truthy value if the condition is true, otherwise return the falsy value
        return if (conditionValue) truthyValue else falsyValue
    }

    // Regex pattern to find ternary operations
    val pattern = Pattern.compile("""if\s*\(\s*([^,]+)\s*,\s*([^,]+)\s*,\s*([^)]+)\s*\)""").toRegex()

    // Variable to hold the modified expression
    var newExpression = expression
    while (true) {
        val matcher = pattern.find(newExpression)
        if (matcher == null) {
            break
        }
        // Replace the found ternary operation with its evaluated result
        newExpression = pattern.replace(newExpression, ::evalCondition)
    }

    // Evaluate the final arithmetic expression
    return evaluateArithmetic(newExpression, variables)
}

//  evaluating a single condition
fun evaluateCondition(condition: String, variables: Map<String, Int>): Boolean {
    val parts = condition.split("==").map { it.trim() }
    // Get the variable value or use it as a direct integer
    val variableValue = variables[parts[0]] ?: parts[0].toInt()
    val comparisonValue = parts[1].toInt()
    // Check if the condition is true
    return variableValue == comparisonValue
}

// Function to evaluate arithmetic expressions
fun evaluateArithmetic(expression: String, variables: Map<String, Int>): Int {
    var evaluatedExpression = expression
    // Replace variable names with their actual values
    variables.forEach { (key, value) ->
        evaluatedExpression = evaluatedExpression.replace(key, value.toString())
    }

    return evaluateFullExpression(evaluatedExpression)
}

fun evaluateFullExpression(expression: String): Int {
    val cleanedExpression = expression.replace(" ", "").replace("\n", "")
    val operators = Regex("[-+*/]").findAll(cleanedExpression).map { it.value }.toList()
    val numbers = cleanedExpression.split(Regex("[-+*/]")).map { it.toInt() }.toMutableList()

    var result = numbers.removeAt(0)
    for ((index, operator) in operators.withIndex()) {
        when (operator) {
            "+" -> result += numbers[index]
            "-" -> result -= numbers[index]
            "*" -> result *= numbers[index]
            "/" -> result /= numbers[index]
        }
    }
    return result
}


/*
Create a parser that evaluates and executes ternary operations in the form of:

if ( <condition>, <truthy value>, <falsy value> )
Which would normally equate to:

if ( <condition> ) {
    return <truthy value>
} else {
    return <falsy value>
}
Sample input and output is as follows:

run(`
if (var_1 == 2, 0, if (var_2 == 4, 15, 0))
+ if (var_2 == 3, 5, 0)
- if (var_4 == 2, 0, 5)
+ if (var_3 == 3, 5, 0)`, {
    var_1: 1,
    var_2: 4,
    var_3: 3,
    var_4: 5
});


 output : 15
*/