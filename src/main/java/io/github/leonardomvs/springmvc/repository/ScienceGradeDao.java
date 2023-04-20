package io.github.leonardomvs.springmvc.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import io.github.leonardomvs.springmvc.models.ScienceGrade;

@Repository
public interface ScienceGradeDao extends CrudRepository<ScienceGrade, Integer> {

	public Iterable<ScienceGrade> findGradeByStudentId(int studentId);

	public void deleteByStudentId(int studentId);
	
}
