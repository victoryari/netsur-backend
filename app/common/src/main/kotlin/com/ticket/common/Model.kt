import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

@Serializable
data class Ticket(
    val id: String,
    val title: String,
    val description: String?,
    val status: TicketStatus,
    val priority: TicketPriority,
    val category: String?,
    val createdBy: String,
    val createdAt: Long,
    val updatedAt: Long,
    var assignedTo: String?,
    var comments: List<Comment> = emptyList(),
    var attachments: List<Attachment> = emptyList()
) {
    enum class TicketStatus : String {
        case OPEN, IN_PROGRESS, RESOLVED, CLOSED
    }

    enum class TicketPriority : String {
        case LOW, MEDIUM, HIGH, URGENT
    }
}

@Serializable
data class Comment(
    val id: String,
    val text: String,
    val author: String,
    val createdAt: Long
)

@Serializable
data class Attachment(
    val id: String,
    val url: String,
    val name: String,
    val size: Long,
    val mimeType: String
)

@Serializable
data class User(
    val id: String,
    val email: String,
    val displayName: String?,
    val photoUrl: String?,
    val role: UserRole,
    val createdAt: Long
)

enum class UserRole : String {
    case ADMIN, AGENT, USER
}