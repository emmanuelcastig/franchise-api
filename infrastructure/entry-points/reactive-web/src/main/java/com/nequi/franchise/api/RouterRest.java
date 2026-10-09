package com.nequi.franchise.api;

import com.nequi.franchise.api.docs.ApiExamples;
import com.nequi.franchise.api.dto.request.CreateProductRequest;
import com.nequi.franchise.api.dto.request.NameRequest;
import com.nequi.franchise.api.dto.request.StockRequest;
import com.nequi.franchise.api.dto.response.BranchResponse;
import com.nequi.franchise.api.dto.response.FranchiseResponse;
import com.nequi.franchise.api.dto.response.ProductResponse;
import com.nequi.franchise.api.dto.response.TopStockProductResponse;
import com.nequi.franchise.api.error.ErrorHandlingFilter;
import com.nequi.franchise.api.handler.BranchHandler;
import com.nequi.franchise.api.handler.FranchiseHandler;
import com.nequi.franchise.api.handler.ProductHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;
    private static final String PROBLEM_JSON = MediaType.APPLICATION_PROBLEM_JSON_VALUE;

    @Bean
    @RouterOperations({
            @RouterOperation(path = ApiPaths.FRANCHISES, method = RequestMethod.POST, produces = JSON,
                    beanClass = FranchiseHandler.class, beanMethod = "createFranchise",
                    operation = @Operation(operationId = "createFranchise", tags = "Franchises",
                            summary = "Create a franchise",
                            description = "Franchise names are unique.",
                            requestBody = @RequestBody(required = true, content = @Content(mediaType = JSON,
                                    schema = @Schema(implementation = NameRequest.class),
                                    examples = @ExampleObject(value = ApiExamples.FRANCHISE_NAME_REQUEST))),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Franchise created",
                                            content = @Content(mediaType = JSON,
                                                    schema = @Schema(implementation = FranchiseResponse.class),
                                                    examples = @ExampleObject(value = ApiExamples.FRANCHISE_RESPONSE))),
                                    @ApiResponse(responseCode = "400", description = "Invalid request",
                                            content = @Content(mediaType = PROBLEM_JSON,
                                                    schema = @Schema(implementation = ProblemDetail.class),
                                                    examples = @ExampleObject(value = ApiExamples.INVALID_REQUEST))),
                                    @ApiResponse(responseCode = "409", description = "Name already taken",
                                            content = @Content(mediaType = PROBLEM_JSON,
                                                    schema = @Schema(implementation = ProblemDetail.class),
                                                    examples = @ExampleObject(value = ApiExamples.FRANCHISE_NAME_CONFLICT))),
                                    @ApiResponse(responseCode = "503", description = "Database unavailable",
                                            content = @Content(mediaType = PROBLEM_JSON,
                                                    schema = @Schema(implementation = ProblemDetail.class),
                                                    examples = @ExampleObject(value = ApiExamples.SERVICE_UNAVAILABLE)))
                            })),
            @RouterOperation(path = ApiPaths.FRANCHISE_NAME, method = RequestMethod.PUT, produces = JSON,
                    beanClass = FranchiseHandler.class, beanMethod = "renameFranchise",
                    operation = @Operation(operationId = "renameFranchise", tags = "Franchises",
                            summary = "Update the name of a franchise",
                            description = "Idempotent: renaming to the current name succeeds.",
                            parameters = @Parameter(in = ParameterIn.PATH, name = ApiPaths.FRANCHISE_ID, required = true,
                                    schema = @Schema(type = "string", format = "uuid")),
                            requestBody = @RequestBody(required = true, content = @Content(mediaType = JSON,
                                    schema = @Schema(implementation = NameRequest.class),
                                    examples = @ExampleObject(value = ApiExamples.FRANCHISE_NAME_REQUEST))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Franchise renamed",
                                            content = @Content(mediaType = JSON,
                                                    schema = @Schema(implementation = FranchiseResponse.class),
                                                    examples = @ExampleObject(value = ApiExamples.FRANCHISE_RESPONSE))),
                                    @ApiResponse(responseCode = "400", description = "Invalid request",
                                            content = @Content(mediaType = PROBLEM_JSON,
                                                    schema = @Schema(implementation = ProblemDetail.class),
                                                    examples = @ExampleObject(value = ApiExamples.INVALID_REQUEST))),
                                    @ApiResponse(responseCode = "404", description = "Franchise not found",
                                            content = @Content(mediaType = PROBLEM_JSON,
                                                    schema = @Schema(implementation = ProblemDetail.class),
                                                    examples = @ExampleObject(value = ApiExamples.FRANCHISE_NOT_FOUND))),
                                    @ApiResponse(responseCode = "409", description = "Name already taken",
                                            content = @Content(mediaType = PROBLEM_JSON,
                                                    schema = @Schema(implementation = ProblemDetail.class),
                                                    examples = @ExampleObject(value = ApiExamples.FRANCHISE_NAME_CONFLICT)))
                            })),
            @RouterOperation(path = ApiPaths.TOP_STOCK_PRODUCTS, method = RequestMethod.GET, produces = JSON,
                    beanClass = FranchiseHandler.class, beanMethod = "getTopStockProducts",
                    operation = @Operation(operationId = "getTopStockProducts", tags = "Franchises",
                            summary = "Get the product with the highest stock of every branch",
                            description = "Branches without products are omitted. Stock ties are broken by product name.",
                            parameters = @Parameter(in = ParameterIn.PATH, name = ApiPaths.FRANCHISE_ID, required = true,
                                    schema = @Schema(type = "string", format = "uuid")),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Top stock product per branch",
                                            content = @Content(mediaType = JSON,
                                                    array = @ArraySchema(schema = @Schema(implementation = TopStockProductResponse.class)),
                                                    examples = @ExampleObject(value = ApiExamples.TOP_STOCK_PRODUCTS_RESPONSE))),
                                    @ApiResponse(responseCode = "404", description = "Franchise not found",
                                            content = @Content(mediaType = PROBLEM_JSON,
                                                    schema = @Schema(implementation = ProblemDetail.class),
                                                    examples = @ExampleObject(value = ApiExamples.FRANCHISE_NOT_FOUND)))
                            })),
            @RouterOperation(path = ApiPaths.BRANCHES, method = RequestMethod.POST, produces = JSON,
                    beanClass = BranchHandler.class, beanMethod = "addBranch",
                    operation = @Operation(operationId = "addBranch", tags = "Branches",
                            summary = "Add a branch to a franchise",
                            description = "Branch names are unique within a franchise.",
                            parameters = @Parameter(in = ParameterIn.PATH, name = ApiPaths.FRANCHISE_ID, required = true,
                                    schema = @Schema(type = "string", format = "uuid")),
                            requestBody = @RequestBody(required = true, content = @Content(mediaType = JSON,
                                    schema = @Schema(implementation = NameRequest.class),
                                    examples = @ExampleObject(value = ApiExamples.BRANCH_NAME_REQUEST))),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Branch created",
                                            content = @Content(mediaType = JSON,
                                                    schema = @Schema(implementation = BranchResponse.class),
                                                    examples = @ExampleObject(value = ApiExamples.BRANCH_RESPONSE))),
                                    @ApiResponse(responseCode = "400", description = "Invalid request",
                                            content = @Content(mediaType = PROBLEM_JSON,
                                                    schema = @Schema(implementation = ProblemDetail.class),
                                                    examples = @ExampleObject(value = ApiExamples.INVALID_REQUEST))),
                                    @ApiResponse(responseCode = "404", description = "Franchise not found",
                                            content = @Content(mediaType = PROBLEM_JSON,
                                                    schema = @Schema(implementation = ProblemDetail.class),
                                                    examples = @ExampleObject(value = ApiExamples.FRANCHISE_NOT_FOUND))),
                                    @ApiResponse(responseCode = "409", description = "Name already taken in the franchise",
                                            content = @Content(mediaType = PROBLEM_JSON,
                                                    schema = @Schema(implementation = ProblemDetail.class),
                                                    examples = @ExampleObject(value = ApiExamples.BRANCH_NAME_CONFLICT)))
                            })),
            @RouterOperation(path = ApiPaths.BRANCH_NAME, method = RequestMethod.PUT, produces = JSON,
                    beanClass = BranchHandler.class, beanMethod = "renameBranch",
                    operation = @Operation(operationId = "renameBranch", tags = "Branches",
                            summary = "Update the name of a branch",
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = ApiPaths.FRANCHISE_ID, required = true,
                                            schema = @Schema(type = "string", format = "uuid")),
                                    @Parameter(in = ParameterIn.PATH, name = ApiPaths.BRANCH_ID, required = true,
                                            schema = @Schema(type = "string", format = "uuid"))
                            },
                            requestBody = @RequestBody(required = true, content = @Content(mediaType = JSON,
                                    schema = @Schema(implementation = NameRequest.class),
                                    examples = @ExampleObject(value = ApiExamples.BRANCH_NAME_REQUEST))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Branch renamed",
                                            content = @Content(mediaType = JSON,
                                                    schema = @Schema(implementation = BranchResponse.class),
                                                    examples = @ExampleObject(value = ApiExamples.BRANCH_RESPONSE))),
                                    @ApiResponse(responseCode = "400", description = "Invalid request",
                                            content = @Content(mediaType = PROBLEM_JSON,
                                                    schema = @Schema(implementation = ProblemDetail.class),
                                                    examples = @ExampleObject(value = ApiExamples.INVALID_REQUEST))),
                                    @ApiResponse(responseCode = "404", description = "Branch not found in the franchise",
                                            content = @Content(mediaType = PROBLEM_JSON,
                                                    schema = @Schema(implementation = ProblemDetail.class),
                                                    examples = @ExampleObject(value = ApiExamples.BRANCH_NOT_FOUND))),
                                    @ApiResponse(responseCode = "409", description = "Name already taken in the franchise",
                                            content = @Content(mediaType = PROBLEM_JSON,
                                                    schema = @Schema(implementation = ProblemDetail.class),
                                                    examples = @ExampleObject(value = ApiExamples.BRANCH_NAME_CONFLICT)))
                            })),
            @RouterOperation(path = ApiPaths.PRODUCTS, method = RequestMethod.POST, produces = JSON,
                    beanClass = ProductHandler.class, beanMethod = "addProduct",
                    operation = @Operation(operationId = "addProduct", tags = "Products",
                            summary = "Add a product to a branch",
                            description = "Product names are unique within a branch and stock must be zero or greater.",
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = ApiPaths.FRANCHISE_ID, required = true,
                                            schema = @Schema(type = "string", format = "uuid")),
                                    @Parameter(in = ParameterIn.PATH, name = ApiPaths.BRANCH_ID, required = true,
                                            schema = @Schema(type = "string", format = "uuid"))
                            },
                            requestBody = @RequestBody(required = true, content = @Content(mediaType = JSON,
                                    schema = @Schema(implementation = CreateProductRequest.class),
                                    examples = @ExampleObject(value = ApiExamples.CREATE_PRODUCT_REQUEST))),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Product created",
                                            content = @Content(mediaType = JSON,
                                                    schema = @Schema(implementation = ProductResponse.class),
                                                    examples = @ExampleObject(value = ApiExamples.PRODUCT_RESPONSE))),
                                    @ApiResponse(responseCode = "400", description = "Invalid request",
                                            content = @Content(mediaType = PROBLEM_JSON,
                                                    schema = @Schema(implementation = ProblemDetail.class),
                                                    examples = @ExampleObject(value = ApiExamples.INVALID_REQUEST))),
                                    @ApiResponse(responseCode = "404", description = "Branch not found in the franchise",
                                            content = @Content(mediaType = PROBLEM_JSON,
                                                    schema = @Schema(implementation = ProblemDetail.class),
                                                    examples = @ExampleObject(value = ApiExamples.BRANCH_NOT_FOUND))),
                                    @ApiResponse(responseCode = "409", description = "Name already taken in the branch",
                                            content = @Content(mediaType = PROBLEM_JSON,
                                                    schema = @Schema(implementation = ProblemDetail.class),
                                                    examples = @ExampleObject(value = ApiExamples.PRODUCT_NAME_CONFLICT)))
                            })),
            @RouterOperation(path = ApiPaths.PRODUCT, method = RequestMethod.DELETE,
                    beanClass = ProductHandler.class, beanMethod = "removeProduct",
                    operation = @Operation(operationId = "removeProduct", tags = "Products",
                            summary = "Remove a product from a branch",
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = ApiPaths.FRANCHISE_ID, required = true,
                                            schema = @Schema(type = "string", format = "uuid")),
                                    @Parameter(in = ParameterIn.PATH, name = ApiPaths.BRANCH_ID, required = true,
                                            schema = @Schema(type = "string", format = "uuid")),
                                    @Parameter(in = ParameterIn.PATH, name = ApiPaths.PRODUCT_ID, required = true,
                                            schema = @Schema(type = "string", format = "uuid"))
                            },
                            responses = {
                                    @ApiResponse(responseCode = "204", description = "Product removed"),
                                    @ApiResponse(responseCode = "404", description = "Product not found in the branch",
                                            content = @Content(mediaType = PROBLEM_JSON,
                                                    schema = @Schema(implementation = ProblemDetail.class),
                                                    examples = @ExampleObject(value = ApiExamples.PRODUCT_NOT_FOUND)))
                            })),
            @RouterOperation(path = ApiPaths.PRODUCT_STOCK, method = RequestMethod.PUT, produces = JSON,
                    beanClass = ProductHandler.class, beanMethod = "updateStock",
                    operation = @Operation(operationId = "updateStock", tags = "Products",
                            summary = "Set the stock of a product",
                            description = "Sets an absolute value, so retries are safe.",
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = ApiPaths.FRANCHISE_ID, required = true,
                                            schema = @Schema(type = "string", format = "uuid")),
                                    @Parameter(in = ParameterIn.PATH, name = ApiPaths.BRANCH_ID, required = true,
                                            schema = @Schema(type = "string", format = "uuid")),
                                    @Parameter(in = ParameterIn.PATH, name = ApiPaths.PRODUCT_ID, required = true,
                                            schema = @Schema(type = "string", format = "uuid"))
                            },
                            requestBody = @RequestBody(required = true, content = @Content(mediaType = JSON,
                                    schema = @Schema(implementation = StockRequest.class),
                                    examples = @ExampleObject(value = ApiExamples.STOCK_REQUEST))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Stock updated",
                                            content = @Content(mediaType = JSON,
                                                    schema = @Schema(implementation = ProductResponse.class),
                                                    examples = @ExampleObject(value = ApiExamples.PRODUCT_RESPONSE))),
                                    @ApiResponse(responseCode = "400", description = "Invalid request",
                                            content = @Content(mediaType = PROBLEM_JSON,
                                                    schema = @Schema(implementation = ProblemDetail.class),
                                                    examples = @ExampleObject(value = ApiExamples.INVALID_REQUEST))),
                                    @ApiResponse(responseCode = "404", description = "Product not found in the branch",
                                            content = @Content(mediaType = PROBLEM_JSON,
                                                    schema = @Schema(implementation = ProblemDetail.class),
                                                    examples = @ExampleObject(value = ApiExamples.PRODUCT_NOT_FOUND)))
                            })),
            @RouterOperation(path = ApiPaths.PRODUCT_NAME, method = RequestMethod.PUT, produces = JSON,
                    beanClass = ProductHandler.class, beanMethod = "renameProduct",
                    operation = @Operation(operationId = "renameProduct", tags = "Products",
                            summary = "Update the name of a product",
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = ApiPaths.FRANCHISE_ID, required = true,
                                            schema = @Schema(type = "string", format = "uuid")),
                                    @Parameter(in = ParameterIn.PATH, name = ApiPaths.BRANCH_ID, required = true,
                                            schema = @Schema(type = "string", format = "uuid")),
                                    @Parameter(in = ParameterIn.PATH, name = ApiPaths.PRODUCT_ID, required = true,
                                            schema = @Schema(type = "string", format = "uuid"))
                            },
                            requestBody = @RequestBody(required = true, content = @Content(mediaType = JSON,
                                    schema = @Schema(implementation = NameRequest.class),
                                    examples = @ExampleObject(value = ApiExamples.PRODUCT_NAME_REQUEST))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Product renamed",
                                            content = @Content(mediaType = JSON,
                                                    schema = @Schema(implementation = ProductResponse.class),
                                                    examples = @ExampleObject(value = ApiExamples.PRODUCT_RESPONSE))),
                                    @ApiResponse(responseCode = "400", description = "Invalid request",
                                            content = @Content(mediaType = PROBLEM_JSON,
                                                    schema = @Schema(implementation = ProblemDetail.class),
                                                    examples = @ExampleObject(value = ApiExamples.INVALID_REQUEST))),
                                    @ApiResponse(responseCode = "404", description = "Product not found in the branch",
                                            content = @Content(mediaType = PROBLEM_JSON,
                                                    schema = @Schema(implementation = ProblemDetail.class),
                                                    examples = @ExampleObject(value = ApiExamples.PRODUCT_NOT_FOUND))),
                                    @ApiResponse(responseCode = "409", description = "Name already taken in the branch",
                                            content = @Content(mediaType = PROBLEM_JSON,
                                                    schema = @Schema(implementation = ProblemDetail.class),
                                                    examples = @ExampleObject(value = ApiExamples.PRODUCT_NAME_CONFLICT)))
                            }))
    })
    public RouterFunction<ServerResponse> franchiseRoutes(FranchiseHandler franchiseHandler,
                                                          BranchHandler branchHandler,
                                                          ProductHandler productHandler,
                                                          ErrorHandlingFilter errorHandlingFilter) {
        return route(POST(ApiPaths.FRANCHISES), franchiseHandler::createFranchise)
                .andRoute(PUT(ApiPaths.FRANCHISE_NAME), franchiseHandler::renameFranchise)
                .andRoute(GET(ApiPaths.TOP_STOCK_PRODUCTS), franchiseHandler::getTopStockProducts)
                .andRoute(POST(ApiPaths.BRANCHES), branchHandler::addBranch)
                .andRoute(PUT(ApiPaths.BRANCH_NAME), branchHandler::renameBranch)
                .andRoute(POST(ApiPaths.PRODUCTS), productHandler::addProduct)
                .andRoute(DELETE(ApiPaths.PRODUCT), productHandler::removeProduct)
                .andRoute(PUT(ApiPaths.PRODUCT_STOCK), productHandler::updateStock)
                .andRoute(PUT(ApiPaths.PRODUCT_NAME), productHandler::renameProduct)
                .filter(errorHandlingFilter);
    }
}
