package i.design.modules.question.repositories

import i.design.modules.question.models.entities.QuestionEntity
import i.design.modules.users.models.entities.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface QuestionRepository : JpaRepository<QuestionEntity, Long> {
    fun findAllByCreateUserId(userId: Long): List<QuestionEntity>
    fun findAllByCreateUser(userEntity: UserEntity):List<QuestionEntity>
    fun findTopByCreateUserIdAndId(userId: Long, id: Long): Optional<QuestionEntity>
    fun countByCreateUserId(userId: Long): Long
}