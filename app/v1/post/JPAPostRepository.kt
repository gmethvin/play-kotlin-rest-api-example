package v1.post

import net.jodah.failsafe.CircuitBreaker
import net.jodah.failsafe.Failsafe
import play.db.jpa.JPAApi
import java.sql.SQLException
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture.supplyAsync
import java.util.concurrent.CompletionStage
import java.util.function.Supplier
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton
import javax.persistence.EntityManager

/**
 * A repository that provides a non-blocking API with a custom execution context
 * and circuit breaker.
 */
@Singleton
class JPAPostRepository @Inject
constructor(private val jpaApi: JPAApi, private val ec: PostExecutionContext) : PostRepository {
    private val circuitBreaker = CircuitBreaker().withFailureThreshold(1).withSuccessThreshold(3)

    override fun list(): CompletionStage<Stream<PostData>> {
        return wrap({ select(it) })
    }

    override fun create(postData: PostData): CompletionStage<PostData> {
        return wrap({ insert(it, postData) })
    }

    override fun get(id: Long?): CompletionStage<Optional<PostData>> {
        return wrap({ em ->
            Failsafe.with<Optional<PostData>>(circuitBreaker)
                    .get(Callable { lookup(em, id) })
        })
    }

    override fun update(id: Long?, postData: PostData): CompletionStage<Optional<PostData>> {
        return wrap({ em ->
            Failsafe.with<Optional<PostData>>(circuitBreaker)
                    .get(Callable { modify(em, id, postData) })
        })
    }

    private fun <T> wrap(function: (EntityManager) -> T): CompletionStage<T> {
        return supplyAsync(Supplier {
            jpaApi.withTransaction(function)
        }, ec)
    }

    @Throws(SQLException::class)
    private fun lookup(em: EntityManager, id: Long?): Optional<PostData> {
        throw SQLException("Call this to cause the circuit breaker to trip")
        //return Optional.ofNullable(em.find(PostData.class, id));
    }

    private fun select(em: EntityManager): Stream<PostData> {
        val query = em.createQuery("SELECT p FROM PostData p", PostData::class.java)
        return query.getResultList().stream()
    }

    @Throws(InterruptedException::class)
    private fun modify(em: EntityManager, id: Long?, postData: PostData): Optional<PostData> {
        val data = em.find(PostData::class.java, id)
        if (data != null) {
            data.title = postData.title
            data.body = postData.body
        }
        Thread.sleep(10000L)
        return Optional.ofNullable(data)
    }

    private fun insert(em: EntityManager, postData: PostData): PostData {
        return em.merge(postData)
    }
}
