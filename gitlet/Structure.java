package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.Set;


/** Main Structure class for Gitlet.
 *  @author Abhiroop Mathur
 */

public class Structure implements Serializable {

    /** CWD. */
    static final File CWD = new File(System.getProperty("user.dir"));
    /** .gitlet. */
    static final File GITLET_FOLDER = Utils.join(CWD, ".gitlet");
    /** Commit directory. */
    static final File COMMIT_DIR = Utils.join(GITLET_FOLDER, "commits");
    /** Head. */
    private String _head;


    /** Initialization for a repo. */
    public void init() {
        _head = "master";
        File cwd = new File(System.getProperty("user.dir"));

        if (GITLET_FOLDER.exists()) {
            System.out.println("Gitlet version-control system already "
                    + "exists in the current directory.");
            return;
        }
        GITLET_FOLDER.mkdir();

        COMMIT_DIR.mkdir();

        new File(".gitlet/blobs").mkdir();
        new File(".gitlet/staging").mkdir();
        File branches = new File(".gitlet/branch");
        branches.mkdir();

        File master = Utils.join(branches, "master.txt");
        File head = Utils.join(branches, "HEAD.txt");


        Commit initial = new Commit("initial commit", new
                TreeMap<String, String>(), null);

        Utils.writeContents(master, initial.getHashcode());

        Utils.writeContents(head, _head);

        Utils.writeObject(Utils.join(COMMIT_DIR,
            initial.getHashcode() + ".txt"), initial);

        StagingArea stage = new StagingArea();
        File area = new File(".gitlet/staging/stage.txt");
        try {
            area.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Utils.writeObject(area, stage);



    }

    public void commits(String msg) {
        _head = Utils.readContentsAsString(new File(".gitlet/branch/HEAD.txt"));
        if (msg.equals("")) {
            System.out.println("Please enter a commit message.");
            return;
        }
        File area = new File(".gitlet/staging", "stage.txt");

        StagingArea stage = Utils.readObject(area, StagingArea.class);
        TreeMap staged = stage.getAddedFiles();
        if (staged.size() == 0) {
            String[] msgList = msg.split(" ");
            if (!msgList[0].equals("Merged")
                && stage.getRemovedFiles().size() == 0) {
                System.out.println("No changes "
                        + "added to the commit.");
                return;
            }
        }

        TreeMap parentBlobs =
            getMostRecentCommit().getBlobs();
        parentBlobs.putAll(staged);
        Set removed = stage.getRemovedFiles().keySet();

        for (Object i : removed) {
            parentBlobs.remove((String) i);

        }
        Commit curr = new Commit(msg, parentBlobs,
            getMostRecentCommit().getHashcode());


        Utils.writeContents(new File(".gitlet/branch",
            _head + ".txt"), curr.getHashcode());
        Utils.writeObject(new File(COMMIT_DIR,
            curr.getHashcode() + ".txt"), curr);



        stage.clearArea();

        Utils.writeObject(new File(".gitlet/staging/stage.txt"), stage);

    }

    public void add(String file) {
        String name = file;
        File toAdd = new File(CWD.getPath(), file);
        if (!toAdd.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        File area = new File(".gitlet/staging/", "stage.txt");

        StagingArea stage = Utils.readObject(area, StagingArea.class);
        String blobHash = Utils.sha1(Utils.readContents(toAdd));

        if (stage.getRemovedFiles().containsKey(file)) {
            if (stage.getRemovedFiles().get(file) != null) {
                if (stage.getRemovedFiles().get(file).equals(blobHash)) {
                    stage.getRemovedFiles().remove(file);
                    serializeStage(stage);
                    return;
                }

            } else {
                stage.getRemovedFiles().remove(file);
                serializeStage(stage);
                return;
            }
        }

        Commit curr = getMostRecentCommit();

        if (curr.getBlobs().containsKey(file)) {
            if (curr.getBlobs().get(file).equals(blobHash)) {
                return;
            }

        }
        stage.add(toAdd, name);

        Utils.writeContents(new File(".gitlet/blobs/" + blobHash + ".txt"),
            Utils.readContentsAsString(toAdd));



        Utils.writeObject(new File(".gitlet/staging/stage.txt"), stage);
    }


    public void remove(String fileName) {
        StagingArea stage = getStage();
        TreeMap staged = stage.getAddedFiles();
        Commit curr = getMostRecentCommit();


        if (!staged.containsKey(fileName)
            && !curr.getBlobs().containsKey(fileName)) {
            System.out.println("No reason to remove the file.");
            return;
        }



        File remove = new File(CWD, fileName);

        if (!remove.exists()) {
            remove = null;
        }


        if (!stage.getAddedFiles().containsKey(fileName)) {
            stage.addToRemoved(remove, fileName);
        }

        staged.remove(fileName);

        if (getMostRecentCommit().getBlobs().containsKey(fileName)) {
            Utils.restrictedDelete(new File(CWD, fileName));
        }


        Utils.writeObject(new File(".gitlet/staging/stage.txt"), stage);

    }

    public void log() {
        Commit curr = getMostRecentCommit();
        while (curr != null) {
            String ln1 = "===";
            String ln2 = "commit" + " " + curr.getHashcode();
            String ln3 = "Date:" + " " + formatDate(curr.getDate().toString());
            String ln4 = curr.getCommitMsg();
            System.out.println(ln1);
            System.out.println(ln2);
            System.out.println(ln3);
            System.out.println(ln4);
            System.out.println("");

            if (curr.getParent() == null) {
                return;
            }
            curr = Utils.readObject(new
                    File(".gitlet/commits",
                    curr.getParent() + ".txt"), Commit.class);
        }

    }

    public void globalLog() {
        List<String> commitHash = Utils.plainFilenamesIn(COMMIT_DIR);
        for (String hash: commitHash) {
            Commit curr = Utils.readObject(new
                    File(COMMIT_DIR, hash), Commit.class);
            String ln1 = "===";
            String ln2 = "commit" + " " + curr.getHashcode();
            String ln3 = "Date:" + " " + formatDate(curr.getDate().toString());
            String ln4 = curr.getCommitMsg();
            System.out.println(ln1);
            System.out.println(ln2);
            System.out.println(ln3);
            System.out.println(ln4);
            System.out.println("");
        }
    }

    public void find(String commitMsg) {
        boolean found = false;
        List<String> commits = Utils.plainFilenamesIn(COMMIT_DIR);
        for (String hash : commits) {
            Commit curr = Utils.readObject(new
                  File(COMMIT_DIR, hash), Commit.class);

            if (curr.getCommitMsg().equals(commitMsg)) {
                System.out.println(curr.getHashcode());
                found = true;
            }
        }

        if (!found) {
            System.out.println("Found no commit with that message.");

        }
    }

    public void status() {
        _head = Utils.readContentsAsString(new
        File(".gitlet/branch", "HEAD.txt"));
        System.out.println("=== Branches ===");
        System.out.println("*" + _head);
        List<String> branches = Utils.plainFilenamesIn(".gitlet/branch");
        for (String branchTxt : branches) {
            if (branchTxt.equals("HEAD.txt")
                || branchTxt.equals(_head + ".txt")
                || branchTxt.equals("null.txt")) {
                continue;
            }
            String[] branch = branchTxt.split("\\.");
            System.out.println(branch[0]);
        }
        System.out.println("");
        System.out.println("=== Staged Files ===");
        File area = new File(".gitlet/staging/", "stage.txt");
        StagingArea stage = Utils.readObject(area, StagingArea.class);

        Set files = stage.getAddedFiles().keySet();
        for (Object file : files) {
            System.out.println((String) file);
        }
        System.out.println("");
        System.out.println("=== Removed Files ===");
        Set removed = stage.getRemovedFiles().keySet();
        for (Object rmFile : removed) {
            System.out.println((String) rmFile);
        }
        System.out.println("");
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println("");
        System.out.println("=== Untracked Files ===");
    }

    public void checkout(String[] name) {
        if (name.length == 2) {
            checkout3(name);
            return;
        }

        if (name[1].equals("--")) {
            checkout1(name);
            return;
        }
        if (name.length > 2) {
            if (name[2].equals("--")) {
                checkout2(name);
                return;
            } else {
                System.out.println("Incorrect operands.");
            }
        }
    }

    public void checkout1(String[] name) {
        Commit head = getMostRecentCommit();

        TreeMap<String, String> blobs = head.getBlobs();
        if (!blobs.containsKey(name[2])) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        String blobHash = blobs.get(name[2]);
        if ((new File(CWD, name[2]).exists())) {
            Utils.restrictedDelete(CWD.getPath() + "/" + name[2]);
        }

        File replace = new File(
                ".gitlet/blobs/" + blobHash + ".txt");

        String contents = Utils.readContentsAsString(replace);
        File newFile = new File(CWD.getPath(), name[2]);
        Utils.writeContents(newFile, contents);


    }

    public void checkout2(String[] name) {
        String hash = null;
        List<String> commits = Utils.plainFilenamesIn(COMMIT_DIR);
        for (String code: commits) {
            if (code.contains(name[1])) {
                hash = code;
                break;
            }
        }
        if (hash == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit checkOut = Utils.readObject(new
                File(COMMIT_DIR, hash), Commit.class);
        TreeMap<String, String> blobs = checkOut.getBlobs();
        if (!blobs.containsKey(name[3])) {
            System.out.println("File does not exist in that commit.");
            return;
        }

        String blobHash = blobs.get(name[3]);
        if ((new File(CWD, name[3]).exists())) {
            Utils.restrictedDelete(CWD + "/" + name[3]);
        }
        File replace = new File(".gitlet/blobs/" + blobHash + ".txt");
        String contents = Utils.readContentsAsString(replace);
        File newFile = new File(CWD, name[3]);
        Utils.writeContents(newFile, contents);
    }

    public void checkout3(String[] name) {
        List<String> branches = Utils.plainFilenamesIn(
                new File(".gitlet/branch"));
        if (!branches.contains(name[1] + ".txt")) {
            System.out.println("No such branch exists.");
            return;
        }
        if (Utils.readContentsAsString(new File(
                ".gitlet/branch/HEAD.txt")).equals(name[1])) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        String commitHash = Utils.readContentsAsString(new
                File(".gitlet/branch", name[1] + ".txt"));
        Commit branchHead = Utils.readObject(new
                File(COMMIT_DIR, commitHash + ".txt"), Commit.class);
        if (getUntrackedFiles().size() > 0) {

            for (String file : getUntrackedFiles()) {
                File check = new File(CWD, file);

                if (branchHead.getBlobs().containsKey(file)) {
                    if (!branchHead.getBlobs().get(file).equals
                            (Utils.sha1(Utils.readContents(check)))) {
                        System.out.println("There is an untracked "
                                + "file in the way; "
                                + "delete it, or add and commit it first.");
                        return;
                    }
                }
            }

        }
        Set<String> branchFiles = branchHead.getBlobs().keySet();
        Set<String> trackedFiles =
                getMostRecentCommit().getBlobs().keySet();

        for (String tracked: trackedFiles) {

            if (!branchHead.getBlobs().containsKey(tracked)) {

                remove(tracked);
            }
        }

        Utils.writeContents(new File(
                ".gitlet/branch", "HEAD.txt"), name[1]);

        reset(commitHash);


    }
    public void branch(String branch) throws IOException {
        _head = Utils.readContentsAsString(new File(
        ".gitlet/branch", "HEAD.txt"));
        List<String> branches = Utils.plainFilenamesIn(new
            File(".gitlet/branch"));
        if (branches.contains(branch + ".txt")) {
            System.out.println("A branch with that name already exists.");
            return;

        }



        File newBranch = Utils.join(".gitlet/branch", branch + ".txt");
        newBranch.createNewFile();
        Utils.writeContents(newBranch, Utils.readContents(new
            File(".gitlet/branch", _head + ".txt")));
    }

    public void removeBranch(String branch) {
        List<String> branches = Utils.plainFilenamesIn(new
            File(".gitlet/branch"));
        if (!branches.contains(branch + ".txt")) {
            System.out.println("A branch with that name does not exist.");
            return;
        }

        if (Utils.readContentsAsString(new File(
            ".gitlet/branch/HEAD.txt")).equals(branch)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }

        File remove = new File(".gitlet/branch", branch + ".txt");

        remove.delete();


    }

    public void reset(String commitId) {
        _head = Utils.readContentsAsString(new
         File(".gitlet/branch", "HEAD.txt"));
        List<String> commits = Utils.plainFilenamesIn(COMMIT_DIR);
        if (!commits.contains(commitId + ".txt")) {
            System.out.println("No commit with that id exists.");
            return;
        }


        if (getUntrackedFiles().size() > 0) {
            Commit given = Utils.readObject(new File(COMMIT_DIR,
                commitId + ".txt"), Commit.class);
            for (String file : getUntrackedFiles()) {
                File check = new File(CWD, file);

                if (given.getBlobs().containsKey(file)) {
                    if (!given.getBlobs().get(file).equals(
                            Utils.sha1(Utils.readContents(check)))) {

                        System.out.println("There is an untracked "
                                + "file in the way; "
                                + "delete it, or add and commit it first.");
                        return;
                    }
                }
            }

        }


        StagingArea stage = getStage();

        Commit thisOne = Utils.readObject(new File(
                COMMIT_DIR, commitId + ".txt"), Commit.class);

        Set<String> files = thisOne.getBlobs().keySet();

        for (String file : files) {
            String[] input = {"", commitId, "--", file};
            checkout(input);
        }

        Set<String> trackedFiles = getMostRecentCommit().
                getBlobs().keySet();
        for (String tracked: trackedFiles) {

            if (!thisOne.getBlobs().containsKey(tracked)) {

                remove(tracked);
            }
        }
        Utils.writeContents(new File(
                ".gitlet/branch", _head + ".txt"), commitId);
        stage.clearArea();

        serializeStage(stage);
    }

    public void merge(String branch) {
        boolean conflict = false;
        if (mergeError(branch)) {
            return;
        }
        StagingArea stage = getStage();
        _head = Utils.readContentsAsString(new File(
            ".gitlet/branch/HEAD.txt"));
        List<String> branches = Utils.plainFilenamesIn(new File(
                ".gitlet/branch"));
        Commit curr = getMostRecentCommit();
        String spechash = Utils.readContentsAsString(new File(
                ".gitlet/branch", branch + ".txt"));
        Commit specified = Utils.readObject(new
                File(COMMIT_DIR, spechash + ".txt"), Commit.class);
        Commit splitpoint = getCommit(getSplitPnt(
                curr.getHashcode(), spechash));
        Set<String> splitFiles = splitpoint.getBlobs().keySet();
        Set<String> givenFiles = specified.getBlobs().keySet();
        if (mergeBody(splitFiles, givenFiles, curr,
                specified, spechash, splitpoint, stage)) {
            conflict = true;
        }
        endOfMerge(conflict, stage, spechash, _head, branch);
    }
    public boolean mergeBody(Set<String> splitFiles, Set<String> givenFiles,
                             Commit curr, Commit specified, String spechash,
                             Commit splitpoint, StagingArea stage) {
        boolean conflict = false;
        for (String file : splitFiles) {
            if (curr.getBlobs().containsKey(file)
                    && specified.getBlobs().containsKey(file)) {
                if (splitpoint.getBlobs().get(file).equals(
                        curr.getBlobs().get(file))
                        & !splitpoint.getBlobs().get(file).equals(
                        specified.getBlobs().get(file))) {
                    String[] input = {"", getSplitPnt(curr.getHashcode(),
                            spechash), "--", file};
                    checkout(input);
                    add(file);
                }
                if ((!splitpoint.getBlobs().get(file).equals
                        (curr.getBlobs().get(file))
                        & !splitpoint.getBlobs().get(file).equals
                        (specified.getBlobs().get(file))
                        & !curr.getBlobs().get(file).equals
                        (specified.getBlobs().get(file)))) {
                    mergeConflict(file, curr, specified);
                    conflict = true;
                }
            }
            if (curr.getBlobs().containsKey(file)
                    && !specified.getBlobs().containsKey(file)) {
                if (splitpoint.getBlobs().get(file).equals(
                        curr.getBlobs().get(file))) {
                    remove(file);
                    stage.getRemovedFiles().remove(file);
                }
                if (!splitpoint.getBlobs().get(file).equals(
                        curr.getBlobs().get(file))) {
                    mergeconflict2(file, curr);
                    conflict = true;
                }
            }
        }
        for (String file : givenFiles) {
            if (!splitpoint.getBlobs().containsKey(file)
                    && !curr.getBlobs().containsKey(file)) {
                String[] input = {"", spechash, "--", file};
                checkout(input);
                add(file);
            }
            if (!splitpoint.getBlobs().containsKey(file)
                    && curr.getBlobs().containsKey(file)
                    && !curr.getBlobs().get(file).equals
                    (specified.getBlobs().get(file))) {
                mergeConflict(file, curr, specified);
                conflict = true;
            }
        }
        return conflict;
    }
    public void endOfMerge(Boolean conflict, StagingArea stage,
                           String spechash, String head, String branch) {
        commits("Merged " + branch + " into " + head + ".");
        Commit merge = getMostRecentCommit();
        merge.setParent2(spechash);
        byte[] mergeContents = Utils.serialize(merge);
        Utils.writeContents(new
                        File(COMMIT_DIR, getMostRecentCommit().
                        getHashcode() + ".txt"),
                mergeContents);
        serializeStage(stage);
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }
    public boolean mergeError(String branch) {
        StagingArea stage = getStage();
        _head = Utils.readContentsAsString(new File(
                ".gitlet/branch/HEAD.txt"));
        if (stage.getAddedFiles().size() > 0
                || stage.getRemovedFiles().size() > 0) {
            System.out.println("You have uncommitted changes.");
            return true;
        }
        for (String file : getUntrackedFiles()) {
            if (getMostRecentCommit().getBlobs().containsKey(file)) {
                File compare = new File(CWD, file);
                if (!getMostRecentCommit().
                        getBlobs().get(file).equals(
                                Utils.sha1(Utils.readContents(compare)))) {
                    System.out.println("You have uncommitted changes.");
                    return true;
                }
            }
        }
        List<String> branches = Utils.plainFilenamesIn(new File(
                ".gitlet/branch"));
        Commit curr = getMostRecentCommit();
        if (!branches.contains(branch + ".txt")) {
            System.out.println("A branch with that name does not exist.");
            return true;
        }
        String spechash = Utils.readContentsAsString(new File(
                ".gitlet/branch", branch + ".txt"));

        if (Utils.readContentsAsString(new File(
                ".gitlet/branch/HEAD.txt")).equals(branch)) {
            System.out.println("Cannot merge a branch with itself.");
            return true;
        }
        if (getUntrackedFiles().size() > 0) {
            System.out.println("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
            return true;
        }
        if (getSplitPnt(curr.getHashcode(), spechash).equals(
                Utils.readContentsAsString(new File(
                        ".gitlet/branch", branch + ".txt")))) {
            System.out.println("Given branch is an "
                    + "ancestor of the current branch.");
            return true;
        }
        if (getSplitPnt(curr.getHashcode(), spechash).equals(
                Utils.readContentsAsString(new File(
                        ".gitlet/branch", _head + ".txt")))) {
            checkout(new String[] {"", branch});
            System.out.println("Current branch fast-forwarded.");
            return true;
        }
        return false;
    }
    public void mergeConflict(String file, Commit curr, Commit specified) {
        File conflicted = new File(CWD, file);
        String conflictedCurrContent =
                Utils.readContentsAsString(new File
                (".gitlet/blobs", curr.getBlobs().get(file)
                                + ".txt"));
        String conflictedGivenContent =
                Utils.readContentsAsString(new File
                (".gitlet/blobs",
                                specified.getBlobs().get(file) + ".txt"));
        String replaced = "<<<<<<< HEAD"
                + System.lineSeparator()
                + conflictedCurrContent
                + "=======" + System.lineSeparator()
                + conflictedGivenContent
                + ">>>>>>>" + System.lineSeparator();

        Utils.writeContents(conflicted, replaced);
        add(file);
    }

    public void mergeconflict2(String file, Commit curr) {

        File conflicted = new File(CWD, file);
        String conflictedCurrContent =
                Utils.readContentsAsString(new File
                (".gitlet/blobs",
                                curr.getBlobs().get(file) + ".txt"));

        String replaced = "<<<<<<< HEAD" + System.lineSeparator()
                + conflictedCurrContent
                + "=======" + System.lineSeparator()
                + ">>>>>>>" + System.lineSeparator();

        Utils.writeContents(conflicted, replaced);
        add(file);
    }

    public static String getSplitPnt
    (String currCommit, String branchCommit) {
        ArrayList<String> currCommits = new ArrayList<>();
        ArrayList<String> branchCommits = new ArrayList<>();
        Date compare = new Date(0);
        String comparehash = null;
        while (getCommit(currCommit).getParent() != null) {
            currCommits.add(currCommit);
            if (getCommit(currCommit).getParent2() != null) {
                compare = getCommit(getSplitPnt(
                        getCommit(currCommit).getParent2(),
                        branchCommit)).getDate();
                comparehash = getSplitPnt(getCommit(
                        currCommit).getParent2(), branchCommit);
            }
            if (getCommit(currCommit).getParent() == null) {
                break;
            }
            currCommit = getCommit(currCommit).getParent();
        }
        Date compare2 = new Date(0);
        String compare2hash = null;
        while (getCommit(branchCommit).getParent() != null) {
            branchCommits.add(branchCommit);
            if (getCommit(currCommit).getParent2() != null) {
                compare2 = getCommit(getSplitPnt(currCommit,
                        getCommit(branchCommit).getParent2())).getDate();
                compare2hash = getSplitPnt(currCommit,
                        getCommit(branchCommit).getParent2());
            }
            branchCommit = getCommit(branchCommit).getParent();
        }
        String compare3hash = null;
        Date compare3 = new Date(0);
        for (String commit : currCommits) {
            if (branchCommits.contains(commit)) {

                compare3 = getCommit(commit).getDate();
                compare3hash = commit;
                break;
            }
        }
        if (compare3hash == null
                && compare2hash == null
                && comparehash == null) {
            return currCommit;
        }
        if (compare.compareTo(compare2) > 0) {
            if (compare.compareTo(compare3) > 0) {
                return comparehash;
            } else {
                return compare3hash;
            }
        } else {
            if ((compare2).compareTo(compare3) > 0) {
                return compare2hash;
            } else {
                return compare3hash;
            }
        }
    }

    public static Commit getMostRecentCommit() {
        File head = new File(".gitlet/branch", "HEAD.txt");
        String branch = Utils.readContentsAsString(head);

        String currHash = Utils.readContentsAsString(new
                File(".gitlet/branch", branch + ".txt"));
        File commit = new File(".gitlet/commits", currHash + ".txt");
        Commit curr = Utils.readObject(commit, Commit.class);

        return curr;
    }


    public static String formatDate(String date) {

        String[] dateParts = date.split(" ");

        return dateParts[0] + " " + dateParts[1]
                + " " + dateParts[2] + " "
                + dateParts[3] + " " + dateParts[5] + " -0800";
    }

    public static ArrayList<String> getUntrackedFiles() {
        List<String> allFiles = Utils.plainFilenamesIn(CWD);

        ArrayList<String> files = new ArrayList<String>();


        for (String file : allFiles) {
            if (file.equals("--") || file.equals(".gitignore")
                    || file.equals("Makefile") || file.equals("proj3.iml")) {
                continue;
            }

            if (getStage().getAddedFiles().containsKey(file)
                    || getStage().getRemovedFiles().containsKey(file)
                    || getMostRecentCommit().getBlobs().containsKey(file)) {
                continue;
            }
            files.add(file);
        }
        return files;
    }

    public static StagingArea getStage() {
        File area = new File(".gitlet/staging/", "stage.txt");
        StagingArea stage = Utils.readObject(area, StagingArea.class);
        return stage;
    }

    public static void serializeStage(StagingArea stage) {
        Utils.writeObject(new File(".gitlet/staging/stage.txt"), stage);
    }

    public static Commit getCommit(String hash) {
        return Utils.readObject(new
                File(COMMIT_DIR, hash + ".txt"), Commit.class);
    }

}
