package org.example.angulardemo.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.angulardemo.entity.Product;
import org.example.angulardemo.repo.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.StreamSupport;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Test
    public void testGetAllProducts() throws Exception {

        // Add some products to the repository
        // Product 1.
        Product newProduct1 = new Product();
        newProduct1.setExtId("extId1");
        newProduct1.setName("name1");
        newProduct1 = productRepository.save(newProduct1);
        // Product 2.
        Product newProduct2 = new Product();
        newProduct2.setExtId("extId2");
        newProduct2.setName("name2");
        newProduct2 = productRepository.save(newProduct2);

        // Test the getProduct endpoint.
        var mvcResult = mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andReturn();

        // Test if the returned products map on the saved products.
        var returnedProducts = new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<List<Product>>() {
                });

        assertTrue(returnedProducts.contains(newProduct1));
        assertTrue(returnedProducts.contains(newProduct2));
    }

    @Test
    public void testAddProduct() throws Exception {

        var validProduct = new Product();
        validProduct.setName("valid name");
        validProduct.setExtId("valid extId");

        // 1. Happy flow. Test the addProduct endpoint.
        var jsonResponse = mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(validProduct)))
                .andExpect(status().isCreated())
                .andReturn();

        // Deserialize the json response to a Product object.
        var savedProduct = new ObjectMapper().readValue(jsonResponse.getResponse().getContentAsString(), Product.class);

        // Test if the product is indeed saved in the repository.
        assertTrue(StreamSupport.stream(productRepository.findAll().spliterator(), false)
                .anyMatch(product -> product.equals(savedProduct)));

        // 2. Adding an incomplete product should return a 400.
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(new Product())))
                .andExpect(status().isBadRequest());

        // 3. Adding a product with a non-null id should return a 400.
        validProduct.setId(1L);
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(validProduct)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDeleteProduct() throws Exception {

        // Add a product to the repository
        Product newProduct = new Product();
        newProduct.setExtId("extId");
        newProduct.setName("name");
        newProduct = productRepository.save(newProduct);

        // 1. Happy flow: test the deleteProduct endpoint.
        mockMvc.perform(delete("/products/" + newProduct.getId()))
                .andExpect(status().isNoContent());
        // Test if the product is indeed deleted from the repository.
        assertTrue(productRepository.findById(newProduct.getId()).isEmpty());

        // 2. Deleting a non-existing product should return a 404.
        mockMvc.perform(delete("/products/" + newProduct.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateProduct() throws Exception {

        // 1. Happy flow
        // Add a product to the repository
        Product newProduct = new Product();
        newProduct.setName("Old name");
        newProduct.setExtId("extId");
        newProduct = productRepository.save(newProduct);
        // Update the product name.
        newProduct.setName("New name");
        // Test the updateProduct endpoint with the updated product.
        mockMvc.perform(put("/products/" + newProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(newProduct)))
                .andExpect(status().isOk());
        // Test if the product is indeed updated in the repository.
        assertEquals(newProduct.getName(), productRepository.findById(newProduct.getId())
                .map(Product::getName).orElse("TEST FAILED"));

        // 2. Updating a non-existing product should return a 404.
        mockMvc.perform(put("/products/" + newProduct.getId() + 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(newProduct)))
                .andExpect(status().isNotFound());

        // 3. Putting an invalid (i.e. non-null fields null) update should return a 400.
        newProduct.setName(null);
        mockMvc.perform(put("/products/" + newProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(newProduct)))
                .andExpect(status().isBadRequest());
    }

    @AfterEach
    public void cleanup() {
        productRepository.deleteAll();
    }
}