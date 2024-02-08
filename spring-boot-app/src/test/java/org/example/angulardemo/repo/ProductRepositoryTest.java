package org.example.angulardemo.repo;

import org.example.angulardemo.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProductRepositoryTest {

    private static Product givenProduct;

    @BeforeEach
    public void beforeEach() {
        // given a product instance with name 'test', description 'test', and extId 'test'
        givenProduct = new Product();
        givenProduct.setName("test");
        givenProduct.setDescription("test");
        givenProduct.setExtId("test");
    }

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    @Test
    public void whenFindById_thenReturnProduct() {
        // given a product instance created in the beforeEach method that is persisted
        entityManager.persistAndFlush(givenProduct);

        // when
        Product foundEntity = productRepository.findById(givenProduct.getId()).orElse(null);

        // then
        assertThat(foundEntity).isNotNull();
        assertThat(foundEntity.getName()).isEqualTo(givenProduct.getName());
    }

    @Test
    public void whenDelete_thenProductIsRemoved() {
        // given a product instance created in the beforeEach method that is persisted
        entityManager.persistAndFlush(givenProduct);

        // when
        assertThat(productRepository.deleteProductById(givenProduct.getId())).isEqualTo(1);
        entityManager.clear();

        // then
        assertThat(productRepository.deleteProductById(givenProduct.getId())).isEqualTo(0);
    }

    @Test
    public void whenSave_thenProductIsPersisted() {
        // given a product instance created in the beforeEach method

        // when
        Product savedEntity = productRepository.save(givenProduct);

        // then
        Product foundProduct = entityManager.find(Product.class, savedEntity.getId());
        assertThat(foundProduct).isNotNull();
        assertThat(foundProduct.getName()).isEqualTo(givenProduct.getName());
    }

    @Test
    public void whenSave_givenExistingProduct_thenProductIsUpdated() {
        // given a product instance created in the beforeEach method that is persisted
        entityManager.persistAndFlush(givenProduct);

        // when the entity is updated
        var updatedProductName = "updatedtest";
        givenProduct.setName(updatedProductName);
        assertThat(productRepository.updateProductById(givenProduct.getId(), givenProduct.getExtId(), givenProduct.getName(), givenProduct.getDescription())).isEqualTo(1);
        entityManager.clear();

        // then the changes are persisted
        Product foundProduct = entityManager.find(Product.class, givenProduct.getId());
        assertThat(foundProduct.getName()).isEqualTo(updatedProductName);

        // Delete the product and verify that updateProductById now returns 0.
        productRepository.delete(foundProduct);
        assertThat(productRepository.updateProductById(givenProduct.getId(), givenProduct.getExtId(), givenProduct.getName(), givenProduct.getDescription())).isEqualTo(0);
    }

}