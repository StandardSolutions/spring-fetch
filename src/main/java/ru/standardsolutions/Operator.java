package ru.standardsolutions;

import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import ru.standardsolutions.request.FilterRequest;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Набор поддерживаемых операторов и создания предиката.
 */
@Slf4j
public enum Operator {

    /**
     * Оператор сравнения.
     */
    EQUAL(":") {
        @SuppressWarnings({"rawtypes"})
        public <T> Predicate createPredicate(Root<T> root, CriteriaBuilder cb, FilterRequest filter) {
            Path<?> fieldPath = getFieldPath(root, filter.getField());
            Comparable comparableValue = Operator.castToComparable(fieldPath.getJavaType(), filter.getValue());
            return cb.equal(fieldPath, comparableValue);
        }
    },

    /**
     * Оператор не равно.
     */
    NOT_EQUAL("!:") {
        @SuppressWarnings({"rawtypes"})
        public <T> Predicate createPredicate(Root<T> root, CriteriaBuilder cb, FilterRequest filter) {
            Path<?> fieldPath = getFieldPath(root, filter.getField());
            Comparable comparableValue = Operator.castToComparable(fieldPath.getJavaType(), filter.getValue());
            return cb.notEqual(fieldPath, comparableValue);
        }
    },

    /**
     * Больше.
     */
    GREATER(">") {
        @SuppressWarnings({"rawtypes", "unchecked"})
        public <T> Predicate createPredicate(Root<T> root, CriteriaBuilder cb, FilterRequest filter) {
            Path<?> fieldPath = getFieldPath(root, filter.getField());
            Comparable comparableValue = Operator.castToComparable(fieldPath.getJavaType(), filter.getValue());
            return cb.greaterThan((Expression<Comparable>) fieldPath, comparableValue);
        }
    },

    /**
     * Больше или равно.
     */
    GREATER_OR_EQUAL(">:") {
        @SuppressWarnings({"rawtypes", "unchecked"})
        public <T> Predicate createPredicate(Root<T> root, CriteriaBuilder cb, FilterRequest filter) {
            Path<?> fieldPath = getFieldPath(root, filter.getField());
            Comparable comparableValue = Operator.castToComparable(fieldPath.getJavaType(), filter.getValue());
            return cb.greaterThanOrEqualTo((Expression<Comparable>) fieldPath, comparableValue);
        }
    },

    /**
     * Меньше.
     */
    LESS("<") {
        @SuppressWarnings({"rawtypes", "unchecked"})
        public <T> Predicate createPredicate(Root<T> root, CriteriaBuilder cb, FilterRequest filter) {
            Path<?> fieldPath = getFieldPath(root, filter.getField());
            Comparable comparableValue = Operator.castToComparable(fieldPath.getJavaType(), filter.getValue());
            return cb.lessThan((Expression<Comparable>) fieldPath, comparableValue);
        }
    },

    /**
     * Меньше или равно.
     */
    LESS_OR_EQUAL("<:") {
        @SuppressWarnings({"rawtypes", "unchecked"})
        public <T> Predicate createPredicate(Root<T> root, CriteriaBuilder cb, FilterRequest filter) {
            Path<?> fieldPath = getFieldPath(root, filter.getField());
            Comparable comparableValue = Operator.castToComparable(fieldPath.getJavaType(), filter.getValue());
            return cb.lessThanOrEqualTo((Expression<Comparable>) fieldPath, comparableValue);
        }
    },

    /**
     * Оператор поиска по строке.
     */
    LIKE("like") {
        public <T> Predicate createPredicate(Root<T> root, CriteriaBuilder cb, FilterRequest filter) {
            Path<?> fieldPath = getFieldPath(root, filter.getField());
            return cb.like(fieldPath.as(String.class), filter.getValue());
        }
    },

    /**
     * Оператор поиска по строке нечуствительный к регистру.
     */
    ILIKE("ilike") {
        public <T> Predicate createPredicate(Root<T> root, CriteriaBuilder cb, FilterRequest filter) {
            Path<?> fieldPath = getFieldPath(root, filter.getField());
            return cb.like(cb.lower(fieldPath.as(String.class)), filter.getValue().toLowerCase());
        }
    },

    /**
     * Оператор вхождения в список значений.
     */
    IN("in") {
        public <T> Predicate createPredicate(Root<T> root, CriteriaBuilder cb, FilterRequest filter) {
            Path<?> fieldPath = getFieldPath(root, filter.getField());
            String[] rawValues = filter.getValue().split(",");
            List<? extends Comparable<?>> valueList = Arrays.stream(rawValues)
                    .map(val -> castToComparable(fieldPath.getJavaType(), val.trim()))
                    .toList();
            return fieldPath.in(valueList);
        }
    },

    /**
     * Оператор отрицания вхождения в список значений.
     */
    NOT_IN("not in") {
        public <T> Predicate createPredicate(Root<T> root, CriteriaBuilder cb, FilterRequest filter) {
            Path<?> fieldPath = getFieldPath(root, filter.getField());
            String[] rawValues = filter.getValue().split(",");
            List<? extends Comparable<?>> valueList = Arrays.stream(rawValues)
                    .map(val -> castToComparable(fieldPath.getJavaType(), val.trim()))
                    .toList();
            return fieldPath.in(valueList).not();
        }
    },

    /**
     * Логическое И.
     */
    AND("AND") {
        public <T> Predicate createPredicate(Root<T> root, CriteriaBuilder cb, FilterRequest filter) {
            return null;
        }
    },

    /**
     * Логическое ИЛИ.
     */
    OR("OR") {
        public <T> Predicate createPredicate(Root<T> root, CriteriaBuilder cb, FilterRequest filter) {
            return null;
        }
    };

    private static final DateTimeFormatter[] LOCAL_DATE_TIME_FORMATTERS = new DateTimeFormatter[]{
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
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
        for (int i = 0; i < parts.length; i++) {
            if (i == 0 && parts.length > 1) {
                path = root.join(parts[i], JoinType.LEFT);
            } else {
                path = path.get(parts[i]);
            }
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
            for (DateTimeFormatter formatter : LOCAL_DATE_TIME_FORMATTERS) {
                try {
                    return LocalDateTime.parse(value, formatter);
                } catch (RuntimeException e) {
                    // Пробуем следующий форматер
                }
            }
            throw new DateTimeParseException("Не удалось распарсить дату: " + value, value, 0);
        } else if (fieldType == String.class) {
            return value;
        } else if (fieldType == UUID.class) {
            return UUID.fromString(value);
        }
        throw new IllegalArgumentException("Неподдерживаемый тип данных для операции сравнения: " + fieldType);
    }
}