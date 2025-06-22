package com.example.rawsource.services;

import com.example.rawsource.entities.Product;
import com.example.rawsource.entities.User;
import com.example.rawsource.entities.dto.product.AddProductDto;
import com.example.rawsource.entities.dto.product.ProductDto;
import com.example.rawsource.entities.dto.product.UpdateProductDto;
import com.example.rawsource.exceptions.ForbiddenException;
import com.example.rawsource.exceptions.ResourceNotFoundException;
import com.example.rawsource.repositories.ProductRepository;
import com.example.rawsource.repositories.UserRepository;
import com.example.rawsource.utils.SecureLogger;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public ProductDto createProduct(AddProductDto productInfo) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User provider = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", username));

        SecureLogger.info("Current user authorities retrieved - User ID: {}", provider.getId());

        if (!provider.getRole().name().equals("PROVIDER")) {
            throw new ForbiddenException("Only providers can create products");
        }

        Product product = new Product();
        product.setName(productInfo.getName());
        product.setDescription(productInfo.getDescription());
        product.setPrice(productInfo.getPrice());
        product.setImage(productInfo.getImage());
        product.setProvider(provider);

        Product savedProduct = productRepository.save(product);
        return convertToDto(savedProduct);
    }

    public ProductDto updateProduct(String id, UpdateProductDto productInfo) {
        UUID productId = UUID.fromString(id);
        Product existingProduct = productRepository.findProductById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User provider = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", username));

        if (!existingProduct.getProvider().getId().equals(provider.getId())) {
            throw new ForbiddenException("You are not allowed to update this product");
        }

        existingProduct.setName(productInfo.getName());
        existingProduct.setDescription(productInfo.getDescription());
        existingProduct.setPrice(productInfo.getPrice());
        existingProduct.setImage(productInfo.getImage());

        Product updatedProduct = productRepository.save(existingProduct);
        return convertToDto(updatedProduct);
    }

    public void deleteProduct(String id) {
        UUID productId = UUID.fromString(id);
        Product existingProduct = productRepository.findProductById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User provider = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", username));

        if (!existingProduct.getProvider().getId().equals(provider.getId())) {
            throw new ForbiddenException("You are not allowed to delete this product");
        }

        productRepository.delete(existingProduct);
    }

    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ProductDto getProductById(String id) {
        UUID productId = UUID.fromString(id);
        Product product = productRepository.findProductById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        return convertToDto(product);
    }

    public List<ProductDto> getProductsByProvider(UUID providerId) {
        SecureLogger.info("Searching products for provider - Provider ID: {}", providerId);

        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider", "id", providerId));

        List<Product> products = productRepository.findByProvider(provider);
        SecureLogger.info("Number of products found: {}", products.size());

        return products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ProductDto convertToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setImage(product.getImage());
        dto.setProviderId(product.getProvider().getId());
        dto.setProviderName(product.getProvider().getName());
        return dto;
    }
}
