package ru.standardsolutions;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import ru.standardsolutions.request.FilterRequest;
import ru.standardsolutions.shared.CriteriaUtils;

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
        public <T> Predicate createPredicate(Root<T> root, CriteriaBuilder cb, FilterRequest filter) {
            Path<?> fieldPath = CriteriaUtils.getFieldPath(root, filter.getField());
            if (fieldPath.getJavaType() == UUID.class) {
                return cb.equal(fieldPath, UUID.fromString(filter.getValue()));
            }
            return cb.equal(fieldPath, filter.getValue());
        }
    },

    /**
     * Оператор не равно.
     */
    NOT_EQUAL("!:") {
        public <T> Predicate createPredicate(Root<T> root, CriteriaBuilder cb, FilterRequest filter) {
            Path<?> fieldPath = CriteriaUtils.getFieldPath(root, filter.getField());
            if (fieldPath.getJavaType() == UUID.class) {
                return cb.notEqual(fieldPath, UUID.fromString(filter.getValue()));
            }
            return cb.notEqual(fieldPath, filter.getValue());
        }
    },

    /**
     * Больше.
     */
    GREATER(">") {
        @SuppressWarnings({"rawtypes", "unchecked"})
        public <T> Predicate createPredicate(Root<T> root, CriteriaBuilder cb, FilterRequest filter) {
            Path<?> fieldPath = CriteriaUtils.getFieldPath(root, filter.getField());
            Comparable comparableValue = CriteriaUtils.castToComparable(fieldPath.getJavaType(), filter.getValue());
            return cb.greaterThan((Expression<Comparable>) fieldPath, comparableValue);
        }
    },

    /**
     * Больше или равно.
     */
    GREATER_OR_EQUAL(">:") {
        @SuppressWarnings({"rawtypes", "unchecked"})
        public <T> Predicate createPredicate(Root<T> root, CriteriaBuilder cb, FilterRequest filter) {
            Path<?> fieldPath = CriteriaUtils.getFieldPath(root, filter.getField());
            Comparable comparableValue = CriteriaUtils.castToComparable(fieldPath.getJavaType(), filter.getValue());
            return cb.greaterThanOrEqualTo((Expression<Comparable>) fieldPath, comparableValue);
        }
    },

    /**
     * Меньше.
     */
    LESS("<") {
        @SuppressWarnings({"rawtypes", "unchecked"})
        public <T> Predicate createPredicate(Root<T> root, CriteriaBuilder cb, FilterRequest filter) {
            Path<?> fieldPath = CriteriaUtils.getFieldPath(root, filter.getField());
            Comparable comparableValue = CriteriaUtils.castToComparable(fieldPath.getJavaType(), filter.getValue());
            return cb.lessThan((Expression<Comparable>) fieldPath, comparableValue);
        }
    },

    /**
     * Меньше или равно.
     */
    LESS_OR_EQUAL("<:") {
        @SuppressWarnings({"rawtypes", "unchecked"})
        public <T> Predicate createPredicate(Root<T> root, CriteriaBuilder cb, FilterRequest filter) {
            Path<?> fieldPath = CriteriaUtils.getFieldPath(root, filter.getField());
            Comparable comparableValue = CriteriaUtils.castToComparable(fieldPath.getJavaType(), filter.getValue());
            return cb.lessThanOrEqualTo((Expression<Comparable>) fieldPath, comparableValue);
        }
    },

    /**
     * Оператор поиска по строке.
     */
    LIKE("like") {
        public <T> Predicate createPredicate(Root<T> root, CriteriaBuilder cb, FilterRequest filter) {
            Path<?> fieldPath = CriteriaUtils.getFieldPath(root, filter.getField());
            return cb.like(fieldPath.as(String.class), filter.getValue());
        }
    },

    /**
     * Оператор поиска по строке нечуствительный к регистру.
     */
    ILIKE("ilike") {
        public <T> Predicate createPredicate(Root<T> root, CriteriaBuilder cb, FilterRequest filter) {
            Path<?> fieldPath = CriteriaUtils.getFieldPath(root, filter.getField());
            return cb.like(cb.lower(fieldPath.as(String.class)), filter.getValue().toLowerCase());
        }
    },

    /**
     * Оператор вхождения в список значений.
     */
    IN("in") {
        public <T> Predicate createPredicate(Root<T> root, CriteriaBuilder cb, FilterRequest filter) {
            Path<?> fieldPath = CriteriaUtils.getFieldPath(root, filter.getField());
            String[] rawValues = filter.getValue().split(",");
            List<? extends Comparable<?>> valueList = Arrays.stream(rawValues)
                    .map(val -> CriteriaUtils.castToComparable(fieldPath.getJavaType(), val.trim()))
                    .toList();
            return fieldPath.in(valueList);
        }
    },

    /**
     * Оператор отрицания вхождения в список значений.
     */
    NOT_IN("not in") {
        public <T> Predicate createPredicate(Root<T> root, CriteriaBuilder cb, FilterRequest filter) {
            Path<?> fieldPath = CriteriaUtils.getFieldPath(root, filter.getField());
            String[] rawValues = filter.getValue().split(",");
            List<? extends Comparable<?>> valueList = Arrays.stream(rawValues)
                    .map(val -> CriteriaUtils.castToComparable(fieldPath.getJavaType(), val.trim()))
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
}