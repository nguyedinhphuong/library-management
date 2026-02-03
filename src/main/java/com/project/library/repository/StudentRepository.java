package com.project.library.repository;


import com.project.library.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByStudentCode(String studentCode);
    Optional<Student> findByEmail(String email);
    Optional<Student> findByPhone(String phone);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

    @Query(value = "select nextval('student_code_seq')", nativeQuery = true)
    Long getNextStudentSequence();

    @Query("select s.email from Student s where s.email in :emails")
    List<String> findExistingEmails(@Param("emails") List<String> emails);

    @Query("select s.phone from Student s where s.phone in :phones")
    List<String> findExistingPhones(@Param("phones") List<String> phones);
}
