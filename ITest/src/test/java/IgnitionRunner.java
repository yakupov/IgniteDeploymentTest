import common.Constants;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteMessaging;
import org.apache.ignite.cache.query.ScanQuery;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.Ignore;
import org.junit.Test;
import pojos.Client;
import pojos.Locator;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;

public class IgnitionRunner {
    private static final Logger logger = Logger.getGlobal();

    static {
        final ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
    }

    @Ignore
    @Test
    public void testJetty() throws Exception {
        final List<Integer> localPorts = Arrays.asList(11115, 11116, 11117);
        final List<String> nodes = localPorts.stream().map(p -> "127.0.0.1:" + p).collect(Collectors.toList());

        final List<Server> jettys = new ArrayList<>(localPorts.size());
        for (Integer localPort : localPorts) {
            jettys.add(jetty(localPort, nodes, "../ServerWebApp/target/deploy"));
        }

        Thread.sleep(2000);

        try (Ignite ignite = IgnitionConfigurer.igniteClient(logger, nodes)) {
            final IgniteCache<String, Client> clients = ignite.getOrCreateCache(Client.class.getName());
            final IgniteCache<String, Locator> locs = ignite.getOrCreateCache(Locator.class.getName());

            for (int i = 0; i < 50; ++i) {
                final Client client = new Client();
                client.setId("" + i);
                clients.put(client.getId(), client);

                final IgniteMessaging imsg = ignite.message(ignite.cluster().forServers().forRandom());
                imsg.send(Constants.UPDLOC_TOPIC, client.getId().getBytes(StandardCharsets.UTF_8));
            }

            for (int i = 0; i < 50; ++i) {
                final ScanQuery<String, Locator> query = new ScanQuery<>(new LocatorBiPredicate("" + i));
                while (locs.query(query).getAll().size() == 0) {
                    Thread.sleep(100);
                }
            }

            for (int i = 0; i < 50; ++i) {
                final String clid = "" + i;
                final IgniteMessaging imsg = ignite.message(ignite.cluster().forServers().forRandom());
                imsg.send(Constants.ENROLL_TOPIC, clid.getBytes(StandardCharsets.UTF_8));
            }

            Thread.sleep(20000);
        } finally {
            for (Server server : jettys) {
                server.stop();
            }
        }
    }

    private static Server jetty(int port, List<String> nodes, String dir) throws Exception {
        final Server server = new Server(port + 10);

        final File expolodedDir = new File(dir);
        final WebAppContext ctx = new WebAppContext(expolodedDir.getAbsolutePath(), "/app");
        ctx.setAttribute("port", port);
        ctx.setAttribute("hosts", nodes);

        server.setHandler(ctx);
        server.start();
        return server;
    }
}
