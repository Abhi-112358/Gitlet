package gitlet;
import java.io.Serializable;

import java.util.Date;

import java.util.TreeMap;

/** Commit class for Gitlet.
 *  @author Abhiroop Mathur
 */
public class Commit implements Serializable {

    public Commit(String msg, TreeMap<String, String> blobs, String parent) {
        _msg = msg;
        _blobs = blobs;
        hashcode = Utils.sha1(Utils.serialize(this));
        _parent = parent;
        _parent2 = null;

        if (_parent == null) {
            _date = new Date(0);
        } else {
            _date = new Date();
        }
    }

    public String getCommitMsg() {
        return _msg;
    }

    public String getParent() {
        return _parent;
    }

    public Date getDate() {
        return _date;
    }

    public TreeMap<String, String> getBlobs() {
        return _blobs;
    }

    public String getHashcode() {
        return hashcode;
    }

    public void setParent2(String newparent) {
        _parent2 = newparent;
    }

    public String getParent2() {
        return _parent2;
    }

    /** Date. */
    private Date _date;

    /** Commit message. */
    private String _msg;

    /** blobs. */
    private TreeMap<String, String> _blobs;

    /** Sha-1. */
    private String hashcode;

    /** Parent. */
    private String _parent;

    /** Parent. 2 */
    private String _parent2;
}
