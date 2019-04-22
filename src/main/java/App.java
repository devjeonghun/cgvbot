import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Config;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Enumeration;
import java.util.Properties;

public class App {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] argu) {
        LOGGER.info("START");
        Config.authInit(Config.SYSTEM_ROOT + "/config.properties");
    }
}
