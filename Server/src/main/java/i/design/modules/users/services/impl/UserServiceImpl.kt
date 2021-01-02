package i.design.modules.users.services.impl

import com.github.openEdgn.logger4k.getLogger
import com.github.open_edgn.security4k.hash.sha384Sum
import i.design.handlers.exceptions.ApplicationExceptions
import i.design.modules.users.models.entities.UserDetailsEntity
import i.design.modules.users.models.entities.UserEntity
import i.design.modules.users.models.rest.UserModel
import i.design.modules.users.repositories.UserRepository
import i.design.modules.users.services.IUserService
import i.design.utils.LocalDateTimeUtils
import i.design.utils.SnowFlake
import i.design.utils.rest.RestStatus
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import javax.annotation.Resource

@Service
class UserServiceImpl : IUserService {
    @Resource
    private lateinit var snowFlake: SnowFlake

    private val logger = getLogger()

    @Resource
    private lateinit var userRepository: UserRepository
    override fun selectAll(index: Int, length: Int): List<UserModel> {
        val toList = userRepository.findAll(PageRequest.of(index, length)).map {
            entity2Model(it)
        }.toList()
        return toList
    }

    fun entity2Model(entity: UserEntity): UserModel {
        return entity.let {
            UserModel(
                it.id, it.email,
                it.userDetail.sex,
                it.userDetail.name,
                it.userDetail.imageUrl,
                admin = it.admin,
                canLogin = it.canLogin,
                regiserDate = LocalDateTimeUtils.format(it.registerDate)
            )
        }
    }

    override fun selectOneById(id: Long): UserModel {
        val data = userRepository.findById(id)
        if (data.isEmpty) {
            throw ApplicationExceptions.badRequest("无id 为 $id 的用户。")
        }
        val get = data.get()
        return entity2Model(get)
    }

    override fun insert(user: UserModel): RestStatus<Long> {
        val userDetail = UserDetailsEntity()
        userDetail.name = user.name
        userDetail.sex = user.sex
        userDetail.imageUrl = user.image
        return UserEntity(
            id = snowFlake.nextId(),
            email = user.email,
            password = user.password.sha384Sum(),
            admin = user.admin,
            canLogin = user.canLogin,
            registerDate = LocalDateTime.now(),
            lastLoginDate = LocalDateTime.now(),
            userDetail = userDetail
        ).run { RestStatus(true, userRepository.save(this).id) }
    }

    override fun update(id: Long, user: UserModel): RestStatus<Long> {
        val findById = userRepository.findById(id)
        if (findById.isEmpty) {
            throw ApplicationExceptions.badRequest("无此 ID");
        }
        val get = findById.get()
        get.email = user.email
        get.admin = user.admin
        if (user.password.isNotEmpty()) {
            get.password = user.password.sha384Sum()
        }
        get.canLogin = user.canLogin
        get.userDetail.name = user.name
        get.userDetail.sex = user.sex
        get.userDetail.imageUrl = user.image
        return get.run { RestStatus(true, userRepository.save(this).id) }
    }

    override fun delete(id: Long): RestStatus<Long> {
        userRepository.deleteById(id)
        return RestStatus(true, id)
    }

    override fun length(): RestStatus<Long> {
        return RestStatus(true, userRepository.count())
    }
}