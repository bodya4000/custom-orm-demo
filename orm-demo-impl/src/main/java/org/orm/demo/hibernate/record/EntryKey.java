package org.orm.demo.hibernate.record;

public record EntryKey<T>(Class<T> type, Long id) {
}
