package gitlet;


import java.io.Serializable;

import java.io.File;

import java.util.TreeMap;

/** Staging Area class for Gitlet.
 *  @author Abhiroop Mathur
 */

public class StagingArea implements Serializable {

    /**Staged files. */
    private TreeMap<String, String> _addedFiles;

    /** To be removed. */
    private TreeMap<String, String> _removedFiles;

    public StagingArea() {
        _addedFiles = new TreeMap<String, String>();
        _removedFiles = new TreeMap<String, String>();
    }
    public TreeMap getAddedFiles() {
        return _addedFiles;
    }

    public TreeMap getRemovedFiles() {
        return _removedFiles;
    }

    public void addToRemoved(File file, String name) {
        if (file == null) {
            _removedFiles.put(name, null);
        } else {
            _removedFiles.put(name, Utils.sha1(Utils.readContents(file)));
        }
    }

    public void add(File file, String name) {
        _addedFiles.put(name, Utils.sha1(Utils.readContents(file)));
    }

    public void clearArea() {
        _addedFiles.clear();
        _removedFiles.clear();
    }
}
