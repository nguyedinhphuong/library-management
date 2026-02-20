package com.spring.code.demo.repository.Criteria;


import com.spring.code.demo.model.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Getter;
import lombok.Setter;
import java.util.function.Consumer;

@Getter
@Setter
public class UserSearchCriteriaQueryConsumer implements Consumer<SearchCriteria> {

    private CriteriaBuilder builder;
    private Predicate predicate;
    private final Root<User> root;

    public UserSearchCriteriaQueryConsumer(CriteriaBuilder builder, Predicate predicate, Root<User> root) {
        this.builder = builder;
        this.predicate = predicate;
        this.root = root;
    }

    @Override
    public void accept(SearchCriteria param) {
        String key = param.getKey();
        String op = param.getOperation();
        Object value = param.getValue();

        Path<?> path = getPath(root, key);
        Predicate newPredicate = switch (op) {
            case "="  -> builder.equal(path, value);
            case ">"  -> builder.greaterThan(path.as(Comparable.class), (Comparable) value);
            case "<"  -> builder.lessThan(path.as(Comparable.class), (Comparable) value);
            case ">=" -> builder.greaterThanOrEqualTo(path.as(Comparable.class), (Comparable) value);
            case "<=" -> builder.lessThanOrEqualTo(path.as(Comparable.class), (Comparable) value);
            case ":"  -> builder.like(builder.lower(path.as(String.class)), "%" + value.toString().toLowerCase() + "%");
            case "!=" -> builder.notEqual(path, value);
            default -> throw new IllegalArgumentException("Operation not supported: " + op);
        };
        predicate = builder.and(predicate, newPredicate);
//        if (param.getOperation().equals(">")) {
//            predicate = builder.and(predicate, builder.greaterThanOrEqualTo(root.get(param.getKey()), param.getValue().toString()));
//        } else if (param.getOperation().equals("<")) {
//            predicate = builder.and(predicate, builder.lessThanOrEqualTo(root.get(param.getKey()), param.getValue().toString()));
//        } else if (param.getOperation().equals("=")) {
//            if (root.get(param.getKey()).getJavaType() == String.class) {
//                // nếu là string sd like
//                predicate = builder.and(predicate, builder.like(root.get(param.getKey()), "%" + param.getValue().toString() + "%"));
//            } else {
//                predicate = builder.and(predicate, builder.equal(root.get(param.getKey()), param.getValue().toString()));
//            }
//        }
    }

    private Path<?> getPath(Root<User> root, String property) {
        String[] parts = property.split("\\.");
        Path<?> path = root;
        for (String part : parts) {
            path = path.get(part);
        }
        return path;
    }
//
//    @Override
//    public void accept(SearchCriteria param) {
//        var path = root.get(param.getKey());
//        Class<?> type = path.getJavaType();
//        Object value = castValue(type, param.getValue().toString());
//        if (param.getOperation().equals(">")) {
//            predicate = builder.and(predicate, builder.greaterThanOrEqualTo(path.as(Comparable.class), (Comparable) value));
//        } else if (param.getOperation().equals("<")) {
//            predicate = builder.and(predicate, builder.lessThanOrEqualTo(path.as(Comparable.class), (Comparable) value));
//        } else if (param.getOperation().equals(":")) {
//            if (type == String.class) {
//                predicate = builder.and(predicate, builder.like(path.as(String.class), "%" + value + "%"));
//            } else {
//                predicate = builder.and(predicate, builder.equal(path, value));
//            }
//        }
//    }
//
//    private Object castValue(Class<?> type, String value) {
//        if (type == Integer.class) return Integer.valueOf(value);
//        if (type == Long.class) return Long.valueOf(value);
//        if (type == Boolean.class) return Boolean.valueOf(value);
//        return value;
//    }
}
