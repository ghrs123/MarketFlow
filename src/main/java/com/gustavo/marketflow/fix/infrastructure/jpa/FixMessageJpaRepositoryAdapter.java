package com.gustavo.marketflow.fix.infrastructure.jpa;

import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import com.gustavo.marketflow.fix.domain.FixMessage;
import com.gustavo.marketflow.fix.domain.FixMessageRepository;
import com.gustavo.marketflow.shared.exception.FixMessageAlreadyExistsException;

/**
 * PostgreSQL adapter for the FIX message persistence port.
 */
@Repository
public class FixMessageJpaRepositoryAdapter implements FixMessageRepository {

    private final SpringDataFixMessageJpaRepository springDataFixMessageJpaRepository;

    public FixMessageJpaRepositoryAdapter(SpringDataFixMessageJpaRepository springDataFixMessageJpaRepository) {
        this.springDataFixMessageJpaRepository = springDataFixMessageJpaRepository;
    }

    @Override
    public FixMessage save(FixMessage fixMessage) {
        try {
            return springDataFixMessageJpaRepository.saveAndFlush(FixMessageEntity.from(fixMessage)).toDomain();
        } catch (DataIntegrityViolationException ex) {
            throw new FixMessageAlreadyExistsException(fixMessage.orderId());
        }
    }

    @Override
    public Optional<FixMessage> findByOrderId(UUID orderId) {
        return springDataFixMessageJpaRepository.findByOrderId(orderId)
                .map(FixMessageEntity::toDomain);
    }

    @Override
    public boolean existsByOrderId(UUID orderId) {
        return springDataFixMessageJpaRepository.existsByOrderId(orderId);
    }
}
