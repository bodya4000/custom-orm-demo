package org.orm.project.entity.record;

public record EntryKey<T>(Class<T> type, Long id) {
}
