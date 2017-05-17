import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.BinaryConfiguration;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DeploymentMode;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.logger.java.JavaLogger;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.zk.TcpDiscoveryZookeeperIpFinder;
import pojos.Client;
import pojos.Locator;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

class IgnitionConfigurer {
    private static AtomicInteger nodeId = new AtomicInteger(0);

    private static Ignite startServer(IgniteConfiguration cfg) {
        cfg.setCacheConfiguration(createCacheConfig(Client.class));
        cfg.setCacheConfiguration(createCacheConfig(Locator.class));
        return Ignition.start(cfg);
    }

    static Ignite igniteServerZK(final Logger logger, final Integer localPort, String zkConnString) {
        final IgniteConfiguration cfg = getIgniteConfigurationZK("SrvNode" + nodeId.incrementAndGet(),
                false, logger, localPort, zkConnString);
        return startServer(cfg);
    }

    static Ignite igniteServer(final Logger logger, final Integer localPort, final List<String> nodes) {
        final IgniteConfiguration cfg = getIgniteConfiguration("SrvNode" + nodeId.incrementAndGet(),
                false, logger, localPort, nodes);
        return startServer(cfg);
    }

    static Ignite igniteClient(final Logger logger, final List<String> nodes) {
        final IgniteConfiguration cfg = getIgniteConfiguration("CltNode" + nodeId.incrementAndGet(),
                true, logger, null, nodes);
        return Ignition.start(cfg);
    }

    static Ignite igniteClientZK(final Logger logger, final String zkConnString) {
        final IgniteConfiguration cfg = getIgniteConfigurationZK("CltNode" + nodeId.incrementAndGet(),
                true, logger, null, zkConnString);
        return Ignition.start(cfg);
    }

    private static IgniteConfiguration getIgniteConfigurationZK(final String gridClientName,
                                                              final boolean clientMode,
                                                              final Logger logger,
                                                              Integer localPort,
                                                              String zkConnString) {
        final IgniteConfiguration cfg = new IgniteConfiguration();
        final IgniteLogger log = new JavaLogger(logger);
        cfg.setGridLogger(log);
        cfg.setClientMode(clientMode);
        cfg.setGridName(gridClientName);

        cfg.setPeerClassLoadingEnabled(true);
        cfg.setDeploymentMode(DeploymentMode.CONTINUOUS);

        final BinaryConfiguration binaryCfg = new BinaryConfiguration();
        binaryCfg.setCompactFooter(false);
        cfg.setBinaryConfiguration(binaryCfg);

        final TcpDiscoverySpi discoverySpi = new TcpDiscoverySpi();
        discoverySpi.setHeartbeatFrequency(400);
        discoverySpi.setSocketTimeout(1000);
        discoverySpi.setAckTimeout(500);
        discoverySpi.setNetworkTimeout(2000);

        if (!clientMode) {
            discoverySpi.setLocalPort(localPort == null ? 47500 : localPort);
        }
        cfg.setDiscoverySpi(discoverySpi);

        TcpDiscoveryZookeeperIpFinder zkIpFinder = new TcpDiscoveryZookeeperIpFinder() {
            @Override
            public void onSpiContextDestroyed() {
                super.onSpiContextDestroyed();
                logger.info("org.apache.ignite.spi.discovery.tcp.ipfinder.zk.TcpDiscoveryZookeeperIpFinder.onSpiContextDestroyed");
            }
        };
        zkIpFinder.setAllowDuplicateRegistrations(true);
        zkIpFinder.setZkConnectionString(zkConnString);

        discoverySpi.setIpFinder(zkIpFinder);

        return cfg;
    }

    private static IgniteConfiguration getIgniteConfiguration(final String gridClientName,
                                                             final boolean clientMode,
                                                             final Logger logger,
                                                             Integer localPort,
                                                             List<String> addressesList) {
        final IgniteConfiguration cfg = new IgniteConfiguration();
        final IgniteLogger log = new JavaLogger(logger);
        cfg.setGridLogger(log);
        cfg.setClientMode(clientMode);
        cfg.setGridName(gridClientName);

        cfg.setPeerClassLoadingEnabled(true);
        cfg.setDeploymentMode(DeploymentMode.CONTINUOUS);

        final BinaryConfiguration binaryCfg = new BinaryConfiguration();
        binaryCfg.setCompactFooter(false);
        cfg.setBinaryConfiguration(binaryCfg);

        final TcpDiscoverySpi discoverySpi = new TcpDiscoverySpi();
        discoverySpi.setHeartbeatFrequency(400);
        discoverySpi.setSocketTimeout(1000);
        discoverySpi.setAckTimeout(500);
        discoverySpi.setNetworkTimeout(2000);

        if (!clientMode) {
            discoverySpi.setLocalPort(localPort == null ? 47500 : localPort);
        }
        cfg.setDiscoverySpi(discoverySpi);

        final TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder();

        ipFinder.setAddresses(addressesList);
        discoverySpi.setIpFinder(ipFinder);

        return cfg;
    }

    private static <T> CacheConfiguration<String, T> createCacheConfig(Class<? extends T> entity) {
        final CacheConfiguration<String, T> cfg = new CacheConfiguration<>();
        cfg.setName(entity.getName());
        //to avoid too much instances of org.jsr166.ConcurrentHashMap8$Node[]
        cfg.setStartSize(CacheConfiguration.DFLT_START_SIZE / 10);

        // cfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
        // cfg.setAtomicWriteOrderMode(CacheAtomicWriteOrderMode.CLOCK)
        return cfg;
    }

}
