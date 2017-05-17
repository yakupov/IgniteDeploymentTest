package pojos;

import common.Constants;

public class Locator extends MyMap {
    private static final String CLID = "clid";

    public String getClid() {
        return (String) get(CLID);
    }

    public void setClid(String clid) {
        put(CLID, clid);
    }

    public String getLoc() {
        return (String) get(Constants.LOC);
    }

    public void setLoc(String loc) {
        put(Constants.LOC, loc);
    }
}
