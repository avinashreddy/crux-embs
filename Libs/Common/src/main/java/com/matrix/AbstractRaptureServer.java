package com.matrix;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.matrix.common.Config;
import com.matrix.common.ConfigList;
import com.matrix.common.MapConfig;
import com.matrix.common.ServiceLocator;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import rapture.app.RaptureAppService;
import rapture.config.ConfigLoader;
import rapture.kernel.Kernel;

import java.util.concurrent.TimeUnit;

import static com.matrix.Constants.DEFAULT_HOST_NAME;
import static com.matrix.Constants.SERVER_ENV;
import static com.matrix.Constants.SERVER_HOST_NAME;

/**
 * A template method for {@link RaptureServer}s. 
 */
public abstract class AbstractRaptureServer implements RaptureServer {

    protected final Logger log = Logger.getLogger(getClass());

    @Override
    public final void start(String[] args) {
        log.info("*** Starting"+ getServerName() + " ***");
        try {
            beforeStart(args);
            log.info("*** Initializing Kernel ***");
            startKernel(args);
            log.info("*** Kernel Initialized***");

        }catch(Exception e) {
            log.error("Failed to start " + getServerName(), e);
            return;
        }
        log.info("*** Initializing ServiceLocator ***");
        initServiceLocator(args);
        log.info("*** ServiceLocator Initialized***");
        kernelStarted(args);
        log.info("*** "+ getServerName() + " successfully started ***");
        waitAMin(); // For what??
    }

    protected void startKernel(String[] args) {
        Kernel.initBootstrap(ImmutableMap.of("STD", ConfigLoader.getConf().StandardTemplate), this.getClass(), requiresScheduler());
        RaptureAppService.setupApp(getServerName());
    }

    protected boolean requiresScheduler() {
        return false;
    }

    protected void waitAMin() {
        while (true) {
            try {
                TimeUnit.MINUTES.sleep(1);
            } catch (InterruptedException e) {
                log.error("Error while sleeping on main thread", e);
            }
        }
    }

    protected String getServerName() {
        return this.getClass().getSimpleName();
    }

    private void initServiceLocator(String[] args) {
        Config envConfig = new MapConfig(ImmutableMap.of(
                SERVER_ENV, getServerEnv(),
                SERVER_HOST_NAME, getServerHostName()
        ));
        Config serverConfig = new ConfigList(envConfig, getServerConfig());
        ServiceLocator.init(serverConfig);
    }

    protected void kernelStarted(String[] args) {
        setCategoryMembership();
    }

    protected void setCategoryMembership() {
        Kernel.setCategoryMembership(ConfigLoader.getConf().Categories);
    }

    protected Config getServerConfig() {
       return new MapConfig(new HashedMap());
    }


    protected void beforeStart(String[] args) {
        getServerEnv();
    }

    private String getServerEnv() {
        //This can be set as a docker env variable
        String env = System.getenv(SERVER_ENV);
        Preconditions.checkState(StringUtils.isNotBlank(env),
                String.format("%s not set. Try 'docker run -e %s=<ENV-NAME> ...'", SERVER_ENV, SERVER_ENV));
        return env;
    }

    protected String getServerHostName() {
        //This can be set as a docker env variable
        String hostName = System.getenv(SERVER_HOST_NAME);
        if(StringUtils.isBlank(hostName)) {
            hostName = System.getenv("HOST");
        }
        if(StringUtils.isBlank(hostName)) {
            hostName = DEFAULT_HOST_NAME;
        }
        return hostName;
    }

}
