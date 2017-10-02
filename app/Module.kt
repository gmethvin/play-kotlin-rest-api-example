import com.codahale.metrics.ConsoleReporter
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.Slf4jReporter
import com.google.inject.AbstractModule
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import v1.post.PostRepository
import v1.post.JPAPostRepository

import javax.inject.Provider
import java.util.concurrent.TimeUnit

/**
 * This class is a Guice module that tells Guice how to bind several
 * different types. This Guice module is created when the Play
 * application starts.
 *
 * Play will automatically use any class called `Module` that is in
 * the root package. You can create modules in other locations by
 * adding `play.modules.enabled` settings to the `application.conf`
 * configuration file.
 */
class Module : AbstractModule() {

    override fun configure() {
        bind(MetricRegistry::class.java).toProvider(MetricRegistryProvider::class.java).asEagerSingleton()
        bind(PostRepository::class.java).to(JPAPostRepository::class.java).asEagerSingleton()
    }
}

internal class MetricRegistryProvider : Provider<MetricRegistry> {

    private fun consoleReporter() {
        val reporter = ConsoleReporter.forRegistry(registry)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build()
        reporter.start(1, TimeUnit.SECONDS)
    }

    private fun slf4jReporter() {
        val reporter = Slf4jReporter.forRegistry(registry)
                .outputTo(logger)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build()
        reporter.start(1, TimeUnit.MINUTES)
    }

    override fun get(): MetricRegistry {
        return registry
    }

    companion object {
        private val logger = LoggerFactory.getLogger("application.Metrics")

        private val registry = MetricRegistry()
    }
}//consoleReporter();
// slf4jReporter();
