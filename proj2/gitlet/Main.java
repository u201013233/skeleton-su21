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
            case "global-log":
                validArgs(args, 1);
                Repository.checkIfInitialized();
                Repository.globalLog();
                break;
            case "find":
                validArgs(args, 2);
                Repository.checkIfInitialized();
                Repository.find(args[1]);
                break;

            case "status":
                validArgs(args, 1);
                Repository.checkIfInitialized();
                Repository.status();
                break;

            case "branch":
                validArgs(args, 2);
                Repository.checkIfInitialized();
                Repository.branch(args[1]);
                break;

            case "checkout":
                Repository.checkIfInitialized();
                Repository repository = new Repository();
                switch (args.length) {
                    case 3:
                        if (!args[1].equals("--")) {
                            System.out.println("Incorrect operands.");
                            System.exit(0);
                        }
                        // file 丢弃file
                        repository.checkout(args[2]);
                        break;
                    case 4:
                        // /* * checkout [commit id] -- [file name] */
                        if (!args[2].equals("--")) {
                            System.out.println("Incorrect operands.");
                            System.exit(0);
                        }
                        repository.checkout(args[1], args[3]);
                        break;
                    case 2:
                        repository.checkoutBranch(args[1]);
                        break;
                    default:
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                        break;
                }
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }

    private static void validArgs(String[] args, int num) {
        if (args.length != num) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
}
