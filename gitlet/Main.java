package gitlet;

import java.io.File;
import java.io.IOException;


/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Abhiroop Mathur
 */
public class Main {

    /** CWD. */
    private static File cwd = new File(System.getProperty("user.dir"));
    /** gitlet. */
    private static File gitlet = new File(cwd, ".gitlet");
    /** structure. */
    private static Structure repo = new Structure();
    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
        } else {
            switch (args[0]) {
            default: { } case "init": {
                repo.init();
                break;
            } case "add": {
                repo.add(args[1]);
                break;
            } case "commit": {
                if (args.length == 1) {
                    repo.commits("");
                    break;
                }
                repo.commits(args[1]);
                break;
            } case "rm": {
                repo.remove(args[1]);
                break;
            } case "log": {
                repo.log();
                break;
            } case "global-log": {
                repo.globalLog();
                break;
            } case "find": {
                repo.find(args[1]);
                break;
            } case "status": {
                if (gitlet.exists()) {
                    repo.status();
                } else {
                    System.out.println("Not in an "
                            + "initialized Gitlet directory.");
                }
                break;
            } case "checkout": {
                repo.checkout(args);
                break;
            } case "branch": {
                repo.branch(args[1]);
                break;
            } case "rm-branch": {
                repo.removeBranch(args[1]);
                break;
            } case "reset": {
                repo.reset(args[1]);
                break;
            } case "merge": {
                repo.merge(args[1]);
                break;
            } case "glorp": {
                System.out.println("No command with that name exists.");
            }
            }
        }
        System.exit(0);
    }
}
