import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.io.IOException;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class WebContextListener implements javax.servlet.ServletContextListener {
    private static final Logger logger = Logger.getGlobal();
    static {
        final ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.INFO);
        logger.addHandler(handler);
        logger.setLevel(Level.INFO);
    }

    private IgniteServerNodeRunner isnr;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        final ServletContext ctx = servletContextEvent.getServletContext();
        try {
            isnr = new IgniteServerNodeRunner(IgnitionConfigurer.igniteServer(logger,
                    (Integer) ctx.getAttribute("port"), (List<String>) ctx.getAttribute("hosts")), logger);
//            isnr = new IgniteServerNodeRunner(IgnitionConfigurer.igniteServerZK(logger,
//                    (Integer) ctx.getAttribute("port"), (String) ctx.getAttribute("zkConnString")), logger);
            isnr.run();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "ctx init fail", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        if (isnr != null)
            try {
                isnr.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "ctx destroy fail", e);
            }
    }
}
