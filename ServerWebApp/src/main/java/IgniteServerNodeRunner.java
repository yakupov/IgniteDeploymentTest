import common.Constants;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.lang.IgniteBiPredicate;
import pojos.Client;
import pojos.Locator;
import pojos.MyMap;

import javax.cache.Cache;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class IgniteServerNodeRunner implements Closeable {
    private final Ignite ignite;
    private final Logger logger;
    private final IgniteCache<String, Client> clients;
    private final IgniteCache<String, Locator> locs;

    @Deprecated
    public static void main(String[] args) throws Exception {
        final Logger logger = Logger.getGlobal();
        final ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);

        final int localPort = Integer.parseInt(args[0]);
        final List<String> hosts = new ArrayList<>();
        for (int i = 1; i < args.length && args[i] != null; ++i) {
            hosts.add(args[i]);
        }

        try (final IgniteServerNodeRunner isnr =
                     new IgniteServerNodeRunner(IgnitionConfigurer.igniteServer(logger, localPort, hosts), logger)) {
            isnr.run();
            Thread.sleep(60000);
        }
    }

    IgniteServerNodeRunner(Ignite ignite, Logger logger) {
        this.logger = logger;
        this.ignite = ignite;
        this.clients = ignite.getOrCreateCache(Client.class.getName());
        this.locs = ignite.getOrCreateCache(Locator.class.getName());
    }

    void run() {
        ignite.message().localListen(Constants.ENROLL_TOPIC, (UUID uuid, Object o) -> {
            final byte[] u8str = (byte[]) o;
            final String clid = new String(u8str, StandardCharsets.UTF_8);

            final Client client = clients.get(clid);
            if (client == null) {
                logger.severe("No client with id = " + clid);
                return false;
            }

            client.setPayload("" + Math.random());
            clients.put(clid, client);

            ignite.message(ignite.cluster().forServers().forRandom()).send(Constants.UPDLOC_TOPIC, o);

            return true;
        });

        ignite.message().localListen(Constants.UPDLOC_TOPIC, (uuid, o) -> {
            final byte[] u8str = (byte[]) o;
            final String clid = new String(u8str, StandardCharsets.UTF_8);

            final Client client = clients.get(clid);
            if (client == null) {
                logger.severe("No client with id = " + clid);
                return false;
            }

            //logger.info("Running scan for " + o);
            final List<Cache.Entry<String, Locator>> rs = locs.query(new ScanQuery<>(new TestPredicate(client.getPayload()))).getAll();

            if (rs.isEmpty() && client.getPayload() != null) {
                final Locator locator = new Locator();
                locator.setClid(clid);
                locator.setLoc(client.getPayload());
                locs.put(client.getPayload(), locator);
            }

            return true;
        });
    }

    @Override
    public void close() throws IOException {
        ignite.close();
    }

    static class TestPredicate<T extends MyMap> implements IgniteBiPredicate<String, T> {
        private final Object value;

        TestPredicate(Object value) {
            this.value = value;
        }

        @Override
        public boolean apply(String key, T entity) {
            return Objects.equals(entity.get(Constants.LOC), value);
        }
    }

}
