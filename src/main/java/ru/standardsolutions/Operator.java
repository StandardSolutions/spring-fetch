package ru.standardsolutions;

import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import ru.standardsolutions.request.FilterRequest;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public enum Operator {

    EQUAL(":") {
        public <T> Predicate createPredicate(Root<T> root, CriteriaBuilder cb, FilterRequest filter) {
            Path<?> fieldPath = getFieldPath(root, filter.getField());
            return cb.equal(fieldPath, filter.getValue());
        }
    },

    LIKE("like") {
        public <T> Predicate createPredicate(Root<T> root, CriteriaBuilder cb, FilterRequest filter) {
            Path<?> fieldPath = getFieldPath(root, filter.getField());
            return cb.like(fieldPath.as(String.class), filter.getValue());
        }
    },

    GREATER_OR_EQUAL(">:") {
        @SuppressWarnings({"rawtypes", "unchecked"})
        public <T> Predicate createPredicate(Root<T> root, CriteriaBuilder cb, FilterRequest filter) {
            Path<?> fieldPath = getFieldPath(root, filter.getField());
            Comparable comparableValue = Operator.castToComparable(fieldPath.getJavaType(), filter.getValue());
            return cb.greaterThanOrEqualTo((Expression<Comparable>) fieldPath, comparableValue);
        }
    },

    LESS_OR_EQUAL("<:") {
        @SuppressWarnings({"rawtypes", "unchecked"})
        public <T> Predicate createPredicate(Root<T> root, CriteriaBuilder cb, FilterRequest filter) {
            Path<?> fieldPath = getFieldPath(root, filter.getField());
            Comparable comparableValue = Operator.castToComparable(fieldPath.getJavaType(), filter.getValue());
            return cb.lessThanOrEqualTo((Expression<Comparable>) fieldPath, comparableValue);
        }
    },

    AND("AND") {
        public <T> Predicate createPredicate(Root<T> root, CriteriaBuilder cb, FilterRequest filter) {
            return null;
        }
    },

    OR("OR") {
        public <T> Predicate createPredicate(Root<T> root, CriteriaBuilder cb, FilterRequest filter) {
            return null;
        }
    };

    private final String strValue;

    Operator(String strValue) {
        this.strValue = strValue;
    }

    public abstract <T> Predicate createPredicate(Root<T> root, CriteriaBuilder cb, FilterRequest request);

    public static Operator fromString(String strValue) {
        for (Operator op : Operator.values()) {
            if (strValue.equalsIgnoreCase(op.strValue)) {
                return op;
            }
        }
        throw new IllegalArgumentException("Неподдерживаемый оператор: " + strValue);
    }

    private static Path<?> getFieldPath(Root<?> root, String fieldName) {
        String[] parts = fieldName.split("\\.");
        Path<?> path = root;
        for (String part : parts) {
            path = path.get(part);
        }
        return path;
    }

    private static Comparable<?> castToComparable(Class<?> fieldType, String value) {
        if (fieldType == BigDecimal.class) {
            return new BigDecimal(value);
        } else if (fieldType == BigInteger.class) {
            return new BigInteger(value);
        } else if (fieldType == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (fieldType == Double.class) {
            return Double.parseDouble(value);
        } else if (fieldType == Float.class) {
            return Float.parseFloat(value);
        } else if (fieldType == Integer.class) {
            return Integer.parseInt(value);
        } else if (fieldType == Long.class) {
            return Long.parseLong(value);
        } else if (fieldType == Short.class) {
            return Short.parseShort(value);
        } else if (fieldType == LocalDate.class) {
            return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } else if (fieldType == LocalDateTime.class) {
            return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } else if (fieldType == String.class) {
            return value;
        }
        throw new IllegalArgumentException("Неподдерживаемый тип данных для операции сравнения: " + fieldType);
    }
}