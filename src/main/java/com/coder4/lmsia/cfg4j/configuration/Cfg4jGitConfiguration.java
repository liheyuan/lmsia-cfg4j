package com.coder4.lmsia.cfg4j.configuration;

import com.coder4.lmsia.cfg4j.Cfg4jValueProcessor;
import org.cfg4j.provider.ConfigurationProvider;
import org.cfg4j.provider.ConfigurationProviderBuilder;
import org.cfg4j.source.ConfigurationSource;
import org.cfg4j.source.context.environment.Environment;
import org.cfg4j.source.context.environment.ImmutableEnvironment;
import org.cfg4j.source.context.filesprovider.ConfigFilesProvider;
import org.cfg4j.source.git.GitConfigurationSourceBuilder;
import org.cfg4j.source.reload.ReloadStrategy;
import org.cfg4j.source.reload.strategy.PeriodicalReloadStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author coder4
 */
@Configuration
@ConditionalOnProperty("msName")
public class Cfg4jGitConfiguration {

    @Value("${msName}")
    private String msName;

    // May Change this
    private static String CONFIG_GIT_HOST = "10.1.64.72";

    // May Change this
    private static String CONFIG_GIT_REPO = "http://" + CONFIG_GIT_HOST + ":9002/lmsia-config.git";

    // May Change this
    private static String branch = "master";

    private static int RELOAD_SECS = 60;

    @Bean
    public ConfigurationProvider configurationProvider() {
        ConfigFilesProvider configFilesProvider = () -> Arrays.asList(Paths.get(msName + "/config.properties"));
        ConfigurationSource source = new GitConfigurationSourceBuilder()
                .withRepositoryURI(CONFIG_GIT_REPO)
                .withConfigFilesProvider(configFilesProvider)
                .build();

        Environment environment = new ImmutableEnvironment(branch);

        ReloadStrategy reloadStrategy = new PeriodicalReloadStrategy(RELOAD_SECS, TimeUnit.SECONDS);

        return new ConfigurationProviderBuilder()
                .withConfigurationSource(source)
                .withEnvironment(environment)
                .withReloadStrategy(reloadStrategy)
                .build();
    }

    @Bean
    public Cfg4jValueProcessor createCfg4jValueProcessor() {
        return new Cfg4jValueProcessor();
    }

}