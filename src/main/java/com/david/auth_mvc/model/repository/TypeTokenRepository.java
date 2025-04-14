package com.david.auth_mvc.model.repository;

import com.david.auth_mvc.model.domain.entity.TypeToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeTokenRepository extends CrudRepository<TypeToken, Long> {
    TypeToken findByType(String type);
}
