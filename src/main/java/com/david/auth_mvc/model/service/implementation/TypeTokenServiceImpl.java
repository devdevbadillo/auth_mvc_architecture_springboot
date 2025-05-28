package com.david.auth_mvc.model.service.implementation;

import com.david.auth_mvc.model.service.interfaces.ITypeTokenService;
import com.david.auth_mvc.model.domain.entity.TypeToken;
import com.david.auth_mvc.model.infrestructure.repository.TypeTokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class TypeTokenServiceImpl implements ITypeTokenService {

    private final TypeTokenRepository typeTokenRepository;

    @Override
    public TypeToken getTypeToken(String typeToken) {
        return this.typeTokenRepository.findByType(typeToken);
    }
}
