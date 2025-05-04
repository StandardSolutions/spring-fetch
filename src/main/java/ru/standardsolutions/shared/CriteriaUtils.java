package ru.standardsolutions.shared;


import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.Bindable;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CriteriaUtils {

    public static Path<?> getFieldPath(Root<?> root, String fullFieldName) {
        String[] fields = fullFieldName.split("\\.");
        Path<?> path = root;
        for (String field : fields) {
            if (isAssociation(path, field)) {
                path = ((From<?, ?>) path).join(field, JoinType.LEFT);
            } else {
                path = path.get(field);
            }
        }
        return path;
    }

    public static Comparable<?> castToComparable(Class<?> fieldType, String value) {
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
        } else if (fieldType == UUID.class) {
            return UUID.fromString(value);
        }
        throw new IllegalArgumentException("Неподдерживаемый тип данных для операции сравнения: " + fieldType);
    }

    private static boolean isAssociation(Path<?> path, String field) {
        Bindable<?> bindable = path.getModel();
        // Проверяем, является ли Bindable ManagedType (EntityType, EmbeddableType, PluralAttribute)
        if (bindable instanceof ManagedType<?> managedType) {
            Attribute<?, ?> attr = managedType.getAttribute(field);
            // Для SingularAttribute проверяем isAssociation()
            if (attr instanceof SingularAttribute<?, ?> singularAttr) {
                return singularAttr.isAssociation();
            }
            // Для PluralAttribute (коллекций) считаем ассоциацией
            return attr instanceof PluralAttribute<?, ?, ?>;
        }
        // Если Bindable не ManagedType (например, Basic Type), поле не ассоциация
        return false;
    }
}