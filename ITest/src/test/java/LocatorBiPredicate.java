import org.apache.ignite.lang.IgniteBiPredicate;
import pojos.Locator;

public class LocatorBiPredicate implements IgniteBiPredicate<String, Locator> {
    private final String clid;

    public LocatorBiPredicate(String clid) {
        this.clid = clid;
    }

    @Override
    public boolean apply(String s, Locator locator) {
        return clid.equals(locator.getClid());
    }
}
