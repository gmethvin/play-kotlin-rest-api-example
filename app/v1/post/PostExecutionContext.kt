package v1.post

import akka.actor.ActorSystem
import play.libs.concurrent.CustomExecutionContext

import javax.inject.Inject

/**
 * Custom execution context wired to "post.repository" thread pool
 */
class PostExecutionContext @Inject
constructor(actorSystem: ActorSystem) : CustomExecutionContext(actorSystem, "post.repository")
