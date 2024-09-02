package gitlet;


import java.util.Objects;
import java.util.ResourceBundle;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                validNumArgs(args, 1);
                Repository.initCommand();
                break;
            case "add":
                validNumArgs(args, 2);
                Repository.addCommand(args[1]);
                break;
            case "commit":
                validNumArgs(args, 2);
                Repository.commitCommand(args[1]);
                break;
            case "rm":
                validNumArgs(args, 2);
                Repository.removeCommand(args[1]);
                break;
            case "log":
                validNumArgs(args,1);
                Repository.logCommand();
                break;
            case "global-log":
                validNumArgs(args, 1);
                Repository.globalLogCommand();
                break;
            case "find":
                validNumArgs(args, 2);
                Repository.findCommand(args[1]);
                break;
            case "status":
                validNumArgs(args, 1);
                Repository.statusCommand();
                break;
            case "branch":
                validNumArgs(args, 2);
                Repository.branchCommand(args[1]);
                break;
            case "rm-branch":
                validNumArgs(args, 2);
                Repository.rmBranchCommand(args[1]);
                break;
            case "checkout":
                checkoutCases(args);
                break;
            case "reset":
                validNumArgs(args, 2);
                Repository.resetCommand(args[1]);
                break;
            case "merge":
                validNumArgs(args, 2);
                Repository.mergeCommand(args[1]);
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }

    public static void validNumArgs(String[] args, int n) {
        if (args.length != n) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    private static void checkoutCases(String[] args) {
        if (args.length == 3) {
            if (Objects.equals(args[1], "--")) {
                Repository.checkoutCommand1(args[2]);
            } else {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
        } else if (args.length == 4) {
            if (Objects.equals(args[2], "--")) {
                Repository.checkoutCommand2(args[1], args[3]);
            } else {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
        } else if (args.length == 2) {
            Repository.checkoutCommand3(args[1]);
        } else {
            System.out.println("No command with that name exists.");
            System.exit(0);
        }
    }
}
