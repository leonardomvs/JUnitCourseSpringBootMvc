package io.github.leonardomvs.springmvc.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import io.github.leonardomvs.springmvc.models.CollegeStudent;

@Repository
public interface StudentDao extends CrudRepository<CollegeStudent, Integer> {

	public CollegeStudent findByEmailAddress(String emailAddress);
	
}
