package de.haevn.worksuite.common;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Meta-annotation combining {@link RestController}, {@link RequestMapping}, and default OpenAPI responses.
 *
 * <p>Example usage:
 * <pre>{@code
 * @RestApiController("/api/v1/orders")
 * public class OrderController {
 *
 *     @GetMapping("/{id}")
 *     public OrderDTO getOrder(@PathVariable UUID id) {
 *         return orderService.findById(id);
 *     }
 * }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RestController
@RequestMapping
@ApiResponses({@ApiResponse(responseCode = "500", description = "Internal server error")})
public @interface RestApiController {

    /**
     * The primary mapping paths for the controller.
     *
     * @return path mapping expressions
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "path") String[] value() default {};

    /**
     * Alias for {@link #value()}.
     *
     * @return path mapping expressions
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "path") String[] path() default {};
}