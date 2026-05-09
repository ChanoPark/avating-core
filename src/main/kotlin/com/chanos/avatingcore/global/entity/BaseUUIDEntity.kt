package com.chanos.avatingcore.global.entity

import jakarta.persistence.Column
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PostLoad
import jakarta.persistence.PostPersist
import jakarta.persistence.Transient
import org.springframework.data.domain.Persistable
import java.util.UUID

@MappedSuperclass
abstract class BaseUUIDEntity : BaseEntity(), Persistable<UUID> {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getEntityId")
    val id: UUID = UUID.randomUUID()

    @Transient
    private var _isNew = true

    override fun getId(): UUID = id

    override fun isNew(): Boolean = _isNew

    @PostPersist
    @PostLoad
    protected fun markNotNew() {
        _isNew = false
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BaseUUIDEntity) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
