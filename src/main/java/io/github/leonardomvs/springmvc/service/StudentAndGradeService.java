package io.github.leonardomvs.springmvc.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import io.github.leonardomvs.springmvc.models.CollegeStudent;
import io.github.leonardomvs.springmvc.models.Grade;
import io.github.leonardomvs.springmvc.models.GradebookCollegeStudent;
import io.github.leonardomvs.springmvc.models.HistoryGrade;
import io.github.leonardomvs.springmvc.models.MathGrade;
import io.github.leonardomvs.springmvc.models.ScienceGrade;
import io.github.leonardomvs.springmvc.models.StudentGrades;
import io.github.leonardomvs.springmvc.repository.HistoryGradeDao;
import io.github.leonardomvs.springmvc.repository.MathGradeDao;
import io.github.leonardomvs.springmvc.repository.ScienceGradeDao;
import io.github.leonardomvs.springmvc.repository.StudentDao;

@Service
@Transactional
public class StudentAndGradeService {

	@Autowired
	StudentDao studentDao;
	
	@Autowired
	MathGradeDao mathGradeDao;
	
	@Autowired
	HistoryGradeDao historyGradeDao;
	
	@Autowired
	ScienceGradeDao scienceGradeDao;
			
	@Autowired
	@Qualifier("mathGrades")
	private MathGrade mathGrade;
	
	@Autowired
	@Qualifier("scienceGrades")
	private ScienceGrade scienceGrade;
	
	@Autowired
	@Qualifier("historyGrades")
	private HistoryGrade historyGrade;
	
	@Autowired
	private StudentGrades studentGrades;
	
	public void createStudent(String firstName, String lastName, String emailAddress) {
		CollegeStudent student = new CollegeStudent(firstName, lastName, emailAddress);
		student.setId(0);
		studentDao.save(student);
	}

	public boolean checkIfStudentIsNotNull(int id) {
		Optional<CollegeStudent> student = studentDao.findById(id);
		if(student.isPresent()) {
			return true;
		} else {
			return false;
		}
	}

	public void deleteStudent(int id) {
		if(checkIfStudentIsNotNull(id)) {
			studentDao.deleteById(id);
			
			mathGradeDao.deleteByStudentId(id);
			historyGradeDao.deleteByStudentId(id);
			scienceGradeDao.deleteByStudentId(id);
			
		}			
	}

	public Iterable<CollegeStudent> getGradeBook() {
		return studentDao.findAll();
	}

	public boolean createGrade(double grade, int studentId, String gradeType) {
		
		if(!checkIfStudentIsNotNull(studentId)) { return false; }
		
		if(grade < 0 || grade > 100) { return false; }
		
		if(gradeType.equals("math")) {
			mathGrade.setId(0);
			mathGrade.setGrade(grade);
			mathGrade.setStudentId(studentId);
			mathGradeDao.save(mathGrade);
			return true;
		}
		if(gradeType.equals("science")) {
			scienceGrade.setId(0);
			scienceGrade.setGrade(grade);
			scienceGrade.setStudentId(studentId);
			scienceGradeDao.save(scienceGrade);
			return true;
		}
		if(gradeType.equals("history")) {
			historyGrade.setId(0);
			historyGrade.setGrade(grade);
			historyGrade.setStudentId(studentId);
			historyGradeDao.save(historyGrade);
			return true;
		}		
		
		return false;
	}

	public Integer deleteGrade(int id, String gradeType) {
		int studentId = 0;
		if(gradeType.equals("math")) {
			Optional<MathGrade> grade = mathGradeDao.findById(id);
			if(grade.isPresent()) {
				studentId = grade.get().getStudentId();
				mathGradeDao.deleteById(id);
			}			
		}
		if(gradeType.equals("science")) {
			Optional<ScienceGrade> grade = scienceGradeDao.findById(id);
			if(grade.isPresent()) {
				studentId = grade.get().getStudentId();
				scienceGradeDao.deleteById(id);
			}
		}
		if(gradeType.equals("history")) {
			Optional<HistoryGrade> grade = historyGradeDao.findById(id);
			if(grade.isPresent()) {
				studentId = grade.get().getStudentId();
				historyGradeDao.deleteById(id);
			}
		}		
		return studentId;
	}

	public GradebookCollegeStudent studentInformation(int studentId) {
		
		Optional<CollegeStudent> student = studentDao.findById(studentId);
		
		if(!student.isPresent()) { return null; }
		
		Iterable<MathGrade> mathGrades = mathGradeDao.findGradeByStudentId(studentId);
		Iterable<ScienceGrade> scienceGrades = scienceGradeDao.findGradeByStudentId(studentId);
		Iterable<HistoryGrade> historyGrades = historyGradeDao.findGradeByStudentId(studentId);
		
		List<Grade> mathGradesList = new ArrayList<>();
		mathGrades.forEach(mathGradesList::add);
		
		List<Grade> scienceGradesList = new ArrayList<>();
		scienceGrades.forEach(scienceGradesList::add);
		
		List<Grade> historyGradesList = new ArrayList<>();
		historyGrades.forEach(historyGradesList::add);
		
		studentGrades.setMathGradeResults(mathGradesList);
		studentGrades.setScienceGradeResults(scienceGradesList);
		studentGrades.setHistoryGradeResults(historyGradesList);
		
		GradebookCollegeStudent gradebookCollegeStudent = new GradebookCollegeStudent(student.get().getId(), 
				student.get().getFirstname(), 
				student.get().getLastname(),
				student.get().getEmailAddress(),
				studentGrades);
		
		return gradebookCollegeStudent;
	}
	
	public void configureStudentInformationModel(int studentId, Model m) {
		
		GradebookCollegeStudent studentEntity = studentInformation(studentId);
		
		m.addAttribute("student", studentEntity);
		
		addAverageToModel(m, studentEntity, studentEntity.getStudentGrades().getMathGradeResults(), "mathAverage");
		addAverageToModel(m, studentEntity, studentEntity.getStudentGrades().getScienceGradeResults(), "scienceAverage");
		addAverageToModel(m, studentEntity, studentEntity.getStudentGrades().getHistoryGradeResults(), "historyAverage");
		
	}

	private void addAverageToModel(Model m, GradebookCollegeStudent studentEntity, List<Grade> listOfGradeResults, String attributeName) {
		
		if(listOfGradeResults == null || listOfGradeResults.isEmpty()) {
			m.addAttribute(attributeName, "N/A");
			return; 
		}
		
		double average = studentEntity.getStudentGrades().findGradePointAverage(listOfGradeResults);
		
		m.addAttribute(attributeName, average);
		
	}
	
}
