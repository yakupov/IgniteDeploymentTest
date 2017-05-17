How to execute:
1. mvn package in the root;
2. execute JUnit test IgnitionRunner#testJetty (it's in the ITest module).

The result is:
```
SEVERE: Failed to process message [senderId=98138e20-8fe4-4750-8281-a92b2067fdcb, messageType=class o.a.i.i.processors.cache.query.GridCacheQueryRequest]
java.lang.ClassCastException: LocatorBiPredicate cannot be cast to org.apache.ignite.lang.IgniteBiPredicate
    at org.apache.ignite.internal.processors.cache.query.GridCacheQueryRequest.finishUnmarshal(GridCacheQueryRequest.java:324)
    at org.apache.ignite.internal.processors.cache.GridCacheIoManager.unmarshall(GridCacheIoManager.java:1298)
    at org.apache.ignite.internal.processors.cache.GridCacheIoManager.onMessage0(GridCacheIoManager.java:364)
    at org.apache.ignite.internal.processors.cache.GridCacheIoManager.handleMessage(GridCacheIoManager.java:293)
    at org.apache.ignite.internal.processors.cache.GridCacheIoManager.access$000(GridCacheIoManager.java:95)
    at org.apache.ignite.internal.processors.cache.GridCacheIoManager$1.onMessage(GridCacheIoManager.java:238)
    at org.apache.ignite.internal.managers.communication.GridIoManager.invokeListener(GridIoManager.java:1222)
    at org.apache.ignite.internal.managers.communication.GridIoManager.processRegularMessage0(GridIoManager.java:850)
    at org.apache.ignite.internal.managers.communication.GridIoManager.access$2100(GridIoManager.java:108)
    at org.apache.ignite.internal.managers.communication.GridIoManager$7.run(GridIoManager.java:790)
    at org.apache.ignite.internal.util.StripedExecutor$Stripe.run(StripedExecutor.java:428)
    at java.lang.Thread.run(Thread.java:745)
```
where
```
import org.apache.ignite.lang.IgniteBiPredicate;
public class LocatorBiPredicate implements IgniteBiPredicate<String, Locator> {...}
```
