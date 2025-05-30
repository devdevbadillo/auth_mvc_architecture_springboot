package com.david.auth_mvc.model.services;

import com.david.auth_mvc.model.business.services.impl.TypeTokenServiceImpl;
import com.david.auth_mvc.model.domain.entity.TypeToken;
import com.david.auth_mvc.model.infrestructure.repository.TypeTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class TypeTokenServiceImplTest {

    @Mock
    private TypeTokenRepository typeTokenRepository;

    @InjectMocks
    private TypeTokenServiceImpl typeTokenService;

    @Test
    void getTypeToken_WithExistingTypeToken_ShouldReturnTypeToken() {
        // Given
        String typeTokenName = "typeToken1";
        TypeToken typeToken = new TypeToken();
        typeToken.setType(typeTokenName);

        when(typeTokenRepository.findByType(typeTokenName)).thenReturn(typeToken);

        // When
        TypeToken result = typeTokenService.getTypeToken(typeTokenName);

        // Then
        assertSame(typeToken, result);
        verify(typeTokenRepository).findByType(typeTokenName);
    }

    @Test
    void getTypeToken_WithNonExistingTypeToken_ShouldReturnNull() {
        // Given
        String typeTokenName = "typeToken1";

        // When
        TypeToken result = typeTokenService.getTypeToken(typeTokenName);

        // Then
        assertSame(null, result);
        verify(typeTokenRepository).findByType(typeTokenName);
    }
}
