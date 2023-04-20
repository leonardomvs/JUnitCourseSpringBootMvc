package io.github.leonardomvs.springmvc.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import io.github.leonardomvs.springmvc.models.MathGrade;

@Repository
public interface MathGradeDao extends CrudRepository<MathGrade, Integer> {

	public Iterable<MathGrade> findGradeByStudentId(int studentId);

	public void deleteByStudentId(int studentId);
	
}
