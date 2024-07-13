package Parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Parser {
    final private ArrayList<String> tokens;
    public Parser(ArrayList<String> tokens) {
        this.tokens = tokens;
    }

    public List<String> getFunctionTokens(String func_name, List<String> tokens) {
        boolean InFunc = false, InDesireFunc = false;
        String prevToken, thisToken;
        int begin = 0, end = 0;
        for (int k = 1; k < tokens.size() - 1; k++) {
            prevToken = tokens.get(k - 1);
            thisToken = tokens.get(k);
            if (prevToken.equals("#") && thisToken.equals("F_BEGIN")) {
                InFunc = true;
                begin = k - 1;
            } else if (prevToken.equals("#") && thisToken.equals("F_END")) {
                InFunc = false;
                if (InDesireFunc) {
                    end = k + 1;
                    break;
                }
            } else if (InFunc && prevToken.equals("F_NAME") && thisToken.equals(func_name)) {
                InDesireFunc = true;
            }
        }
        return tokens.subList(begin, end + 1);
    }

    int countOfVars(List<String> tokens) {
        int count = 0;
        String thisToken, nextToken;
        boolean InVars = false;
        for (int k = 0; k < tokens.size() - 1; k++) {
            thisToken = tokens.get(k);
            nextToken = tokens.get(k + 1);
            if (thisToken.equals("#") && (nextToken.equals("F_VARS_BEGIN") || nextToken.equals("F_CONSTANTS_BEGIN")))
            {
                InVars = true;
            } else if (thisToken.equals("#") && nextToken.equals("F_VARS_END") || nextToken.equals("F_CONSTANTS_END")) {
                InVars = false;
            } else if (nextToken.equals("@") && InVars) {
                count++;
            }
            }
        return count;
    }

    public Variable[] createVarsList(String func_name) {
        List<String> tokens = getFunctionTokens(func_name, this.tokens);
        boolean InVars = false;
        String thisToken, nextToken;
        int count = countOfVars(tokens), index = 0;
        Variable[] variables = new Variable[count];
        for (int k = 0; k < tokens.size() - 1; k++) {
            thisToken = tokens.get(k);
            nextToken = tokens.get(k + 1);
            if (thisToken.equals("#") && (nextToken.equals("F_VARS_BEGIN") || nextToken.equals("F_CONSTANTS_BEGIN")))
            {
                InVars = true;
            } else if (thisToken.equals("#") && (nextToken.equals("F_VARS_END") || nextToken.equals("F_CONSTANTS_END"))) {
                InVars = false;
            } else if (nextToken.equals("@") && InVars) {
                variables[index] = new Variable(thisToken, tokens.get(k + 2));
                index++;
            }
        }
        return variables;
    }

    int countOfArgs(List<String> tokens) {
        int count = 0;
        String thisToken, nextToken;
        boolean InArgs = false;
        for (int k = 0; k < tokens.size() - 1; k++) {
            thisToken = tokens.get(k);
            nextToken = tokens.get(k + 1);
            if (thisToken.equals("#") && nextToken.equals("F_ARGS_BEGIN"))
            {
                InArgs = true;
            } else if (thisToken.equals("#") && nextToken.equals("F_ARGS_END")) {
                return count;
            } else if (nextToken.equals("@") && InArgs) {
                count++;
            }
        }
        return count;
    }

    Variable[] createArgsList(String func_name) {
        List<String> tokens = getFunctionTokens(func_name, this.tokens);
        int count = countOfArgs(tokens), index = 0;
        String thisToken, nextToken;
        boolean InArgs = false;
        Variable[] arguments = new Variable[count];
        for (int k = 0; k < tokens.size() - 1; k++) {
            thisToken = tokens.get(k);
            nextToken = tokens.get(k + 1);
            if (thisToken.equals("#") && nextToken.equals("F_ARGS_BEGIN"))
            {
                InArgs = true;
            } else if (thisToken.equals("#") && nextToken.equals("F_ARGS_END")) {
                return arguments;
            } else if (nextToken.equals("@") && InArgs) {
                arguments[index] = new Variable(thisToken, tokens.get(k + 2));
                index++;
            }
        }
        return arguments;
    }

    int countOfInstruction(List<String> tokens) {
        boolean InBody = false;
        int count = 0;
        for (int k = 1; k < tokens.size(); k++) {
            if (tokens.get(k - 1).equals("#") && tokens.get(k).equals("F_BODY_BEGIN")) {
                InBody = true;
            } else if (tokens.get(k - 1).equals("#") && tokens.get(k).equals("F_BODY_END")) {
                return count;
            } else if (InBody && tokens.get(k - 1).equals("#")) {
                count++;
            }
        }
        return count;
    }

    InstructionType getInstructionType(String type) {
        return switch (type) {
            case "+" -> InstructionType.ADD;
            case "-" -> InstructionType.SUB;
            case "*" -> InstructionType.MUL;
            case "/" -> InstructionType.DIV;
            case "=" -> InstructionType.ASSIGN;
            default -> InstructionType.CALL;
        };
    }

    Either<String, Integer>[] toEither(ArrayList<String> array) {
        int size = array.size() - array.stream().filter(s -> s.equals("#")).toArray().length, index = 0;
        Either<String, Integer>[] any = new Either[size];
        for (int k = 1; k < size; k++) {
            if (array.get(k - 1).equals("#")) {
                any[index] = Either.right(Integer.parseInt(array.get(k)));
                index++;
            } else {
                any[index] = Either.left(array.get(k));
            }
        }
        return any;
    }

    Instruction[] createInstructionList(String func_name) {
        List<String> tokens = getFunctionTokens(func_name, this.tokens);
        ArrayList<String> args = new ArrayList<>();
        InstructionType instructionType = null;
        int index = 0, count = countOfInstruction(tokens);
        Instruction[] instructions = new Instruction[count];
        boolean InBody = false, InInstruction = false;
        String instructionName = null;
        for (int k = 1; k < tokens.size(); k++) {
            if (tokens.get(k - 1).equals("#") && tokens.get(k).equals("F_BODY_BEGIN")) {
                InBody = true;
            } else if (tokens.get(k - 1).equals("#") && tokens.get(k).equals("F_BODY_END")) {
                return instructions;
            } else if (InBody && tokens.get(k - 1).equals("#") && !InInstruction) {
                InInstruction = true;
                instructionName = tokens.get(k);
                instructionType = getInstructionType(instructionName);
            } else if (InInstruction && tokens.get(k).equals(";")) {
                instructions[index] = new Instruction(instructionType,
                        toEither(args),
                        Optional.of(instructionName));
                InInstruction = false;
            } else if (InInstruction) {
                args.add(tokens.get(k));
            }
        }
        return instructions;
    }
}
