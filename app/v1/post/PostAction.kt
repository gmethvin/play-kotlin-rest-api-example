package v1.post

import com.codahale.metrics.Meter
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.MetricRegistry.name
import com.codahale.metrics.Timer
import net.jodah.failsafe.FailsafeException
import play.libs.concurrent.Futures
import play.libs.concurrent.HttpExecutionContext
import play.mvc.Http
import play.mvc.Http.Status.*
import play.mvc.Result
import play.mvc.Results
import java.util.concurrent.CompletableFuture.completedFuture
import java.util.concurrent.CompletionException
import java.util.concurrent.CompletionStage
import java.util.concurrent.TimeUnit
import java.util.function.BiFunction
import javax.inject.Inject
import javax.inject.Singleton

class PostAction @Singleton
@Inject
constructor(metrics: MetricRegistry, private val ec: HttpExecutionContext, private val futures: Futures) : play.mvc.Action.Simple() {
    private val logger = play.Logger.of("application.PostAction")

    private val requestsMeter: Meter
    private val responsesTimer: Timer

    init {
        this.requestsMeter = metrics.meter("requestsMeter")
        this.responsesTimer = metrics.timer(name(PostAction::class.java, "responsesTimer"))
    }

    private val timeoutHtml = """<!DOCTYPE html>
        <html>
          <head>
            <title>Timeout Page</title>
          </head>
          <body>
            <h1>Timeout Page</h1>

            Database timed out, so showing this page instead.
          </body>
        </html>
    """.trimIndent()

    override fun call(ctx: Http.Context): CompletionStage<Result> {
        if (logger.isTraceEnabled()) {
            logger.trace("call: ctx = " + ctx)
        }

        requestsMeter.mark()
        if (ctx.request().accepts("application/json")) {
            val time = responsesTimer.time()
            return futures.timeout(doCall(ctx), 1L, TimeUnit.SECONDS)
                    .exceptionally({ _ -> Results.status(GATEWAY_TIMEOUT, timeoutHtml) })
                    .whenComplete({ _, _ -> time.close() })
        } else {
            return completedFuture(
                    status(NOT_ACCEPTABLE, "We only accept application/json")
            )
        }
    }

    private fun doCall(ctx: Http.Context): CompletionStage<Result> {
        return delegate.call(ctx).handleAsync(BiFunction { result, e ->
            if (e != null) {
                if (e is CompletionException) {
                    val completionException = e.cause
                    if (completionException is FailsafeException) {
                        logger.error("Circuit breaker is open!", completionException)
                        Results.status(SERVICE_UNAVAILABLE, "Service has timed out")
                    } else {
                        logger.error("Direct exception " + e.message, e)
                        internalServerError()
                    }
                } else {
                    logger.error("Unknown exception " + e.message, e)
                    Results.internalServerError()
                }
            } else {
                result
            }
        }, ec.current())
    }
}
