package ca.gbc.productservice;

import ca.gbc.productservice.dto.ProductRequest;
import ca.gbc.productservice.dto.ProductResponse;
import ca.gbc.productservice.model.Product;
import ca.gbc.productservice.repository.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
//import static

@SpringBootTest
@AutoConfigureMockMvc
class ProductServiceApplicationTests extends AbstractContainerBaseTest{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    private ProductRequest getProductRequest(){
        return ProductRequest.builder()
                .name("Apple iPad 2023")
                .description("Apple iPad version 2023")
                .price(BigDecimal.valueOf(1200))
                .build();
    }

    private List<Product> getProductList(){

        List<Product> products = new ArrayList<>();
        UUID uuid = UUID.randomUUID();

        Product product = Product.builder()
                .Id(uuid.toString())
                .name("Apple iPad 2023")
                .description("Apple iPad version 2023")
                .price(BigDecimal.valueOf(1200))
                .build();

        products.add(product);

        return products;

    }

    private String convertObjectToString(List<ProductResponse> productList) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(productList);
    }

    private List<ProductResponse> convertStringToObject(String jsonString) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        return mapper.readValue(jsonString, new TypeReference<List<ProductResponse>>(){});
    }

    @Test
    void createProduct() throws Exception {

        ProductRequest productRequest = getProductRequest();
        String productRequestJsonString = objectMapper.writeValueAsString(productRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/product")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productRequestJsonString))
                .andExpect(MockMvcResultMatchers.status().isCreated());

        //Assertions
        Assertions.assertTrue(productRepository.findAll().size() > 0);

        Query query = new Query();
        query.addCriteria(Criteria.where("name").is("Apple iPad 2023"));
        List<Product> product = mongoTemplate.find(query, Product.class);
        Assertions.assertTrue(product.size() > 0);
    }

    /****
        //BDD - Behaviour Driven Development
            // Given - Setup
            // When - Action
            // Then - Verify
     ******/
    @Test
    void getAllProducts() throws Exception{

        //Given
        productRepository.saveAll(getProductList());

        //When
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.
                get("/api/product")
                .accept(MediaType.APPLICATION_JSON));

        //Then
        response.andExpect(MockMvcResultMatchers.status().isOk());
        response.andDo(MockMvcResultHandlers.print());

        MvcResult result = response.andReturn();
        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode jsonNodes = new ObjectMapper().readTree(jsonResponse);

        int actualSize = jsonNodes.size();
        int exceptedSize = getProductList().size();

        assertEquals(exceptedSize, actualSize);
    }

    @Test
    void updateProducts() throws Exception{

        //Given
        Product savedProduct = Product.builder()
                .Id(UUID.randomUUID().toString())
                .name("Widget")
                .description("Widget original price")
                .price(BigDecimal.valueOf(100))
                .build();

        //Saved product with original price
        productRepository.save(savedProduct);

        //prepare update product and productRequest
        savedProduct.setPrice(BigDecimal.valueOf(200));
        String productRequestString = objectMapper.writeValueAsString(savedProduct);

        //When
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders
                .put("/api/product" + savedProduct.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(productRequestString));
        //Then
        response.andExpect(MockMvcResultMatchers.status().isNoContent());
        response.andDo(MockMvcResultHandlers.print());

        Query query = new Query();
        query.addCriteria(Criteria.where("Id").is(savedProduct.getId()));
        Product storedProduct = mongoTemplate.findOne(query, Product.class);

        assertEquals(savedProduct.getPrice(), storedProduct.getPrice());

    }

    @Test
    void deleteProduct() throws Exception{

        //Given
        Product savedProduct = Product.builder()
                .Id(UUID.randomUUID().toString())
                .name("Jave Microservice Programming")
                .description("Course Textbook - Java Microservice Programming")
                .price(BigDecimal.valueOf(200))
                .build();

        productRepository.save(savedProduct);

        //When
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders
                .delete("/api/product" + savedProduct.getId().toString())
                .contentType(MediaType.APPLICATION_JSON));

        //Then
        response.andExpect(MockMvcResultMatchers.status().isNoContent());
        response.andDo(MockMvcResultHandlers.print());

        Query query = new Query();
        query.addCriteria(Criteria.where("Id").is(savedProduct.getId()));
        Long productCount = mongoTemplate.count(query, Product.class);

        assertEquals(0, productCount);
    }

}
