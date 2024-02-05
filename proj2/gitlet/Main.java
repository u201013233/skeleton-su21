package gitlet;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 *
 * @author TODO
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                validArgs(args, 1);
                Repository.init();
                break;
            case "add":
                validArgs(args, 2);
                Repository.checkIfInitialized();
                Repository.add(args[1]);
                break;
            case "commit":
                validArgs(args, 2);
                Repository.checkIfInitialized();
                Repository.commit(args[1]);
                break;
            case "rm":
                validArgs(args, 2);
                Repository.checkIfInitialized();
                Repository.rm(args[1]);
                break;
            case "log":
                validArgs(args, 1);
                Repository.checkIfInitialized();
                Repository.log();
                break;
        }
    }

    private static void validArgs(String[] args, int num) {
        if (args.length != num) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
}
