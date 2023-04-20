package io.github.leonardomvs.springmvc.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import io.github.leonardomvs.springmvc.models.HistoryGrade;

@Repository
public interface HistoryGradeDao extends CrudRepository<HistoryGrade, Integer> {

	public Iterable<HistoryGrade> findGradeByStudentId(int studentId);

	public void deleteByStudentId(int studentId);
	
}
