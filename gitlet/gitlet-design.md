# Gitlet Design Document
author: Abhiroop Mathur

## 1. Classes and Data Structures

### Main.java
This class is the entry point of the program. Checks for argument validity and calls command.

### Structure.java
This class represents the gitlet repo and underlying structure of program. This is the class which is home to the gitlet commands.

#### Fields
1. static final File CWD: Working directory of repo.
2. static final File GITLET_FOLDER: Contains metadata for gitlet commands.
3. static final File COMMIT_DIR: Contains metadata for commits.

### Commit.java
This class represents the commit object.

#### Fields
1. private Date _date: Timestamp of commit.
2. private String _msg: Commit message.
3. private Map<String, String> _blobs: Hashmap of _blobs.
4. private String hashcode: Sha1 hashstring for a commit.
5. private String _parent: Parent of commit.

### StagingArea.java
Represents where files are temporary stored for addition or removal.

#### Fields
1. private Object _addedFiles: Data structure storing staged files for addition.
2. private Object _removedFiles Data structure storing staged files for removal.

## 2. Algorithms

### Main.java
1. main(String[] args): This is the entry point of the program. It first checks to make sure that the input array is not empty. Then, it initiates a structure called repo `setupPersistence` to create the /.gitlet for persistance. Lastly, depending on the input argument, different functions are called to perform the operation.
2. check_input(String[] args): Test to see if argument is valid.
### Structure.java
1. init(): Initializes repo.
2. commit(): Commits blobs from staging area to files.
3. add(String name): Adds file to staging area.
4. remove(String name): Removes files from staging area.
5. log(): Returns log of commits.
6. global_log(): Returns log of all commits ever.
7. find(String commit_msg): Find certain commit due to message.
8. status(): Display status of repo.
9. checkout(String name): Takes 3 different styles of arguments, [file name]: Takes the head commit version of file and places it in the work directory, overwriting the file already there.
   [commit id] -- [file name]: Takes the version of commit corresponding to the commit id, and places it in the working directory, replacing file already there.
   [branch name]: Takes all files at the head of a commit and places them in the working directory, overwriting the files already there.
10. branch(String branch): Creates new branch for file system.
11. remove_branch(String branch): Removes branch with the same name as argument.
12. reset(String commit_id): Removes any tracked files that are not in the commit.
13. merge(String branch): Merges the files from different branches into target branch.

### Commit.java
1. get_parent(): Returns the parent of commit.
2. get_date() : Returns timestamps of commits.
3. getBlobs(): Returns hashmap of blobs for commit.
4. get_hashcode(): Returns hash string of the commit.
5. get_msg(): Returns commit message.

### StagingArea.java
1. get_addedFiles(): Returns the data structure containing files staged for addition.
2. getRemovedFiles(): Returns the data structure containing files staged for removal.
3. addToRemoved(String file): Adds file to _removedFiles.
4. add(): Adds file to _addedFiles. 
5. clear(): Clears staging area.

## 3. Persistence
 
###init 
When called, the .gitlet folder is created, as well as files for blobs, commits, staging area, and other important objects to be serialized.

###commit 
Commit object is serialized and stored as byte, as well as the blob it contains.

###add/rm
Blob added or removed from staging area, which serialized.

###branch
Branch object is serialized.

.gitlet/ -->
  * /blobs
  * /commits
  * /branches
  * /Staging
  * /...

## 4. Design Diagram



