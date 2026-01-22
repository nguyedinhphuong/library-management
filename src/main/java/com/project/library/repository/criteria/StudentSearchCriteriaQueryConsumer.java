package com.project.library.repository.criteria;

import com.project.library.model.Student;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;

import java.util.function.Consumer;

public class StudentSearchCriteriaQueryConsumer implements Consumer<SearchCriteria> {

    private CriteriaBuilder builder; // AND/OR,...
    private Predicate predicate;// WHERE
    private Root<Student> root; // from tới bảng nào?

    public StudentSearchCriteriaQueryConsumer(CriteriaBuilder builder, Predicate predicate, Root<Student> root) {
        this.builder = builder;
        this.predicate = predicate;
        this.root = root;
    }

    @Override
    public void accept(SearchCriteria criteria) {
        String key = criteria.getKey();
        String op = criteria.getOperation();
        Object rawValue = criteria.getValue();

        Path<?> path = getPath(root, key); // trỏ tới cột nào
        Object value = convertValue(path, rawValue);
        
        Predicate newPredicate = switch (op) {
            case "=" -> builder.equal(path,value);
            case "!=" -> builder.notEqual(path,value);
            case ">" -> builder.greaterThan(path.as(Comparable.class),(Comparable)value);
            case "<" -> builder.lessThan(path.as(Comparable.class), (Comparable) value);
            case ">=" -> builder.greaterThanOrEqualTo(path.as(Comparable.class), (Comparable) value);
            case "<=" -> builder.lessThanOrEqualTo(path.as(Comparable.class), (Comparable) value);
            case ":" -> {
                if(path.getJavaType() != String.class){
                    yield builder.equal(path, value);
                }
                yield  builder.like(
                        builder.lower(path.as(String.class)),
                        "%"+value.toString().toLowerCase()+"%"
                );
            }
            default -> throw new IllegalStateException("Operation not supported:" + op);
        };
        
        predicate = builder.and(predicate, newPredicate);
    }

    private Object convertValue(Path<?> path, Object value) {
        if(value == null) return null;

        Class<?> type = path.getJavaType();

        if(type.isEnum()){return Enum.valueOf((Class<Enum>)type,value.toString().toUpperCase());}
        if (type == Integer.class) {return Integer.valueOf(value.toString());}
        if (type == Long.class) {return Long.valueOf(value.toString());}
        if (type == Double.class) {return Double.valueOf(value.toString());}
        if (type == Boolean.class) {return Boolean.valueOf(value.toString());}
        return value;
    }

    private Path<?> getPath(Root<Student> root, String property) {
        Path<?> path = root;
        for(String part: property.split("\\.")){
            path = path.get(part);
        }
        return path;
    }
    public Predicate getPredicate(){return predicate;}
}
