# Gitlet

Lite version of fully-functional version-control system implemented in Java 17.

Source Code/Skeleton Code: UC Berkeley's CS61B: Data Structures  https://sp21.datastructur.es/materials/proj/proj2/proj2

To Run:
1. Clone repository
2. In terminal, run java gitlet.Main init
   This will initialize an empty reposirty in the current directory.
3. Play around with these following commands which mimic the behavior of the full-fledged version of Git:
   add: java gitlet.Main add [file name]
   commit: java gitlet.Main commit [message]
   remove: java gitlet.Main rm [file name]
   log: java gitlet.Main log
   global-log: java gitlet.Main global-log
   find: java gitlet.Main find [commit message]
   status: java gitlet.Main status
   checkout: java gitlet.Main checkout -- [file name]
             java gitlet.Main checkout [commit id] -- [file name]
             java gitlet.Main checkout [branch name]
   branch: java gitlet.Main branch [branch name]
   remove branch: java gitlet.Main rm-branch [branch name]
   reset: java gitlet.Main reset [commit id]
   merge: java gitlet.Main merge [branch name]
   
