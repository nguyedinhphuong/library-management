package com.project.library.service.impl;

import com.project.library.converter.BorrowRecordMapper;
import com.project.library.converter.StudentMapper;
import com.project.library.dto.request.student.CreateStudentRequest;
import com.project.library.dto.request.student.UpdateStudentRequest;
import com.project.library.dto.request.student.UpdateStudentStatusRequest;
import com.project.library.dto.response.BorrowRecordResponse;
import com.project.library.dto.response.PageResponse;
import com.project.library.dto.response.StudentResponse;
import com.project.library.exception.BusinessException;
import com.project.library.exception.ResourceNotFoundException;
import com.project.library.model.BorrowRecord;
import com.project.library.model.Student;
import com.project.library.repository.StudentRepository;
import com.project.library.repository.criteria.BorrowRecordSearchRepository;
import com.project.library.repository.criteria.StudentSearchRepository;
import com.project.library.service.StudentService;
import com.project.library.utils.BorrowStatus;
import com.project.library.utils.Major;
import com.project.library.utils.StudentCodeGenerator;
import com.project.library.utils.StudentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {
    private final StudentRepository studentRepository;
    private final StudentSearchRepository studentSearchRepository;
    private final BorrowRecordSearchRepository borrowRecordSearchRepository;

    @Override
    public StudentResponse create(CreateStudentRequest request) {

        log.info("Create student request received, fullName = {}, major = {}",
                request.getFullName(), request.getMajor());

        if(studentRepository.existsByEmail(request.getEmail())) throw new BusinessException("Email already exists: "+ request.getEmail());
        if(studentRepository.existsByPhone(request.getPhone())) throw new BusinessException("Phone number already exists: "+ request.getEmail());
        try {
            String studentCode = generateStudentCode(request.getMajor());
            log.debug("Generated student code: {} ", studentCode);
            Student student = StudentMapper.toEntity(request, studentCode);
            Student saved = studentRepository.save(student);
            log.info("Student created successfully, id = {}, studentCode = {}",
                    saved.getId(), saved.getStudentCode());
            return StudentMapper.toResponse(saved);
        }catch (DataIntegrityViolationException e) {
            log.error("Race condition detected when creating student", e);
            throw new BusinessException("Email or phone already exists");
        }
    }


    @Override
    @Transactional(readOnly = true)
    public StudentResponse getById(Long id) {
        log.debug("Fetching student by id = {}", id);
        Student student = getStudentById(id);
        return StudentMapper.toResponse(student);
    }

    @Override
    public StudentResponse update(Long id, UpdateStudentRequest request) {
        log.info("Update student request received, id = {}", id);
        Student student = getStudentById(id);
        if(StringUtils.hasText(request.getEmail()) && !request.getEmail().equals(student.getEmail())){
            if(studentRepository.existsByEmail(request.getEmail())) throw new BusinessException("Email already exists: " + request.getEmail());
        }

        if(StringUtils.hasText(request.getPhone()) && !request.getPhone().equals(student.getPhone())){
            if(studentRepository.existsByPhone(request.getPhone())) throw new BusinessException("Phone already exists: " + request.getEmail());
        }

        try {
            StudentMapper.updateEntity(student,request);
            Student updated = studentRepository.save(student);
            log.info("Student updated successfully, id = {}", updated.getId());
            return StudentMapper.toResponse(updated);
        }catch(DataIntegrityViolationException e){
            log.error("Race condition detected when updating student, id = {}", id, e);
            throw new BusinessException("Email or phone already exists");
        }
    }

    @Override
    public StudentResponse updateStatus(Long id, UpdateStudentStatusRequest request) {
        log.info("Update student status request received, id = {}, status = {}", id, request.getStatus());

        Student student = getStudentById(id);
        // Cannot suspend student is currently borrowing books
        if(request.getStatus() == StudentStatus.SUSPENDED) {
            long currentBorrowing = student.getBorrowRecords()
                    .stream()
                    .filter(r -> r.getStatus() == BorrowStatus.BORROWING)
                    .count();
            if(currentBorrowing > 0) {
                throw new BusinessException("Cannot suspend student is currently borrowing books "+ currentBorrowing+ " book(s). Please wait for return");
            }
        }
        student.setStatus(request.getStatus());
        Student updated = studentRepository.save(student);

        log.info("Student status updated successfully, id = {}, status = {}, reason = {}",
                id, request.getStatus(), request.getReason());

        return StudentMapper.toResponse(updated);
    }

    @Override
    public PageResponse<?> advanceStudents(int pageNo, int pageSize, String sortBy, String... search) {
        log.debug("Advanced search - page: {}, size: {}, criteria count: {}", pageNo, pageSize, search != null ? search.length : 0);
        if(pageSize > 100) {
            pageSize = 100;
            log.warn("Page size extends maxium, set to 100");
        }
        return studentSearchRepository.advanceSearchByCriteria(pageNo, pageSize, sortBy, search);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<?> getStudentBorrowHistory(Long studentId, BorrowStatus status, int pageNo, int pageSize) {

        log.debug("Get student borrow history - studentId: {}, status: {}", studentId, status);

        Student student = studentRepository.findById(studentId).orElseThrow(() -> new BusinessException("Student not found with id: " + studentId));

        if(pageSize > 100) {
            pageSize = 100;
            log.warn("Page size exceeds maximum, set to 100");
        }

        PageResponse<?> pageResponse = borrowRecordSearchRepository.searchBorrowRecords(
                studentId, null, status, null, null, pageNo, pageSize, "borrowDate:desc");

        @SuppressWarnings("unchecked")
        List<BorrowRecord> records = (List<BorrowRecord>) pageResponse.getItem();

        List<BorrowRecordResponse> responses = records.stream()
                .map(BorrowRecordMapper::toResponse)
                .toList();

        log.debug("Found {} borrow records for student {}", responses.size(), studentId);

        return PageResponse.builder()
                .pageNo(pageResponse.getPageNo())
                .pageSize(pageResponse.getPageSize())
                .totalPages(pageResponse.getTotalPages())
                .totalElements(pageResponse.getTotalElements())
                .item(responses)
                .build();

    }

    // others
    private String generateStudentCode(Major major) {
        Long generate = studentRepository.getNextStudentSequence();
        return StudentCodeGenerator.generate(major, generate);
    }

    private Student getStudentById(Long id) {
        return studentRepository.findById(id).orElseThrow(() ->  new ResourceNotFoundException("Student not found"));
    }
}
