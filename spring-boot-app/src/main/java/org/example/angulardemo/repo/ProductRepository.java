package org.example.angulardemo.repo;

import jakarta.transaction.Transactional;
import org.example.angulardemo.entity.Product;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface ProductRepository extends CrudRepository<Product, Long> {

    @Transactional
    @Modifying
    @Query("delete from Product p where p.id = ?1")
    int deleteProductById(Long id);

    @Transactional
    @Modifying
    @Query("update Product p set p.extId = ?2, p.name = ?3, p.description = ?4 where p.id = ?1")
    int updateProductById(Long id, String extId, String name, String description);


}