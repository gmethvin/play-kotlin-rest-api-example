package v1.post

import javax.persistence.*

/**
 * Data returned from the database
 */
@Entity
@Table(name = "posts")
data class PostData(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) var id: Long?,
    var title: String,
    var body: String
) {
    constructor() : this(null, "", "") // needed for JPA
    constructor(title: String, body: String) : this(null, title, body)
}
