package i.design.handlers

import com.github.openEdgn.logger4k.getLogger
import i.design.handlers.exceptions.ApplicationException
import i.design.handlers.result.Result
import i.design.handlers.result.ResultUtils
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.DispatcherServlet
import org.springframework.web.servlet.NoHandlerFoundException
import javax.annotation.PostConstruct
import javax.annotation.Resource

@Configuration
@RestControllerAdvice
class ExceptionHandler {
    @Resource
    private lateinit var dispatcherServlet: DispatcherServlet

    @PostConstruct
    private fun configureDispatcherServlet() {
        dispatcherServlet.setThrowExceptionIfNoHandlerFound(true)
    }

    private val logger = getLogger()

    @ResponseBody
    @ExceptionHandler(value = [Exception::class])
    @ResponseStatus
    fun exceptionHandler(e: Exception): Result {
        logger.errorThrowable("发送服务器内部错误", e)
        return ResultUtils.Fail.unknown("服务器内部错误")
    }

    @ExceptionHandler(
        NoHandlerFoundException::class
    )
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    fun notFoundExceptionHandler(e: NoHandlerFoundException): Result {
        return ResultUtils.Fail.notFound(e.requestURL)
    }

    @ResponseBody
    @ExceptionHandler(value = [ApplicationException::class])
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    fun formatCheckExceptionHandler(e: ApplicationException): Result {
        logger.warn("触发错误：{} ", e.message)
        return Result(e.errorId, e.errorMessage)
    }

    @ResponseBody
    @ExceptionHandler(value = [HttpMessageNotReadableException::class])
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    fun postFormatError(e: Exception): Result {
        logger.errorThrowable("内容不合法", e)
        return ResultUtils.Fail.badRequest("内容不合法！")
    }

    @ResponseBody
    @ExceptionHandler(value = [MethodArgumentNotValidException::class])
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    fun formatCheckExceptionHandler(e: MethodArgumentNotValidException): Result {
        logger.warn("客户端请求错误：{} ", e.message)
        val builder = StringBuilder()
        e.bindingResult.allErrors.forEach {
            val param = if (it is FieldError) {
                it.field
            } else {
                ""
            }
            builder.append("字段 $param ").append(it.defaultMessage ?: "格式化异常")
        }
        return ResultUtils.Fail.badRequest(builder.toString())
    }

}