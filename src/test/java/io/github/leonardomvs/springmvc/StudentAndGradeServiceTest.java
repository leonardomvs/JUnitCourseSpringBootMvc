package io.github.leonardomvs.springmvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import io.github.leonardomvs.springmvc.models.CollegeStudent;
import io.github.leonardomvs.springmvc.models.GradebookCollegeStudent;
import io.github.leonardomvs.springmvc.models.HistoryGrade;
import io.github.leonardomvs.springmvc.models.MathGrade;
import io.github.leonardomvs.springmvc.models.ScienceGrade;
import io.github.leonardomvs.springmvc.repository.HistoryGradeDao;
import io.github.leonardomvs.springmvc.repository.MathGradeDao;
import io.github.leonardomvs.springmvc.repository.ScienceGradeDao;
import io.github.leonardomvs.springmvc.repository.StudentDao;
import io.github.leonardomvs.springmvc.service.StudentAndGradeService;

@TestPropertySource("/application-test.properties")
@SpringBootTest
public class StudentAndGradeServiceTest {

	@Autowired
	JdbcTemplate jdbc;
	
	@Autowired
	StudentDao studentDao;
	
	@Autowired
	MathGradeDao mathGradeDao;
	
	@Autowired
	ScienceGradeDao scienceGradeDao;
	
	@Autowired
	HistoryGradeDao historyGradeDao;
	
	@Autowired
	StudentAndGradeService studentService;
	
	@Value("${sql.script.create.student}")
	private String sqlAddStudent;
	
	@Value("${sql.script.create.math.grade}")
	private String sqlAddMathGrade;
	
	@Value("${sql.script.create.science.grade}")
	private String sqlAddScienceGrade;
	
	@Value("${sql.script.create.history.grade}")
	private String sqlAddHistoryGrade;
	
	@Value("${sql.script.delete.student}")
	private String sqlDeleteStudent;
	
	@Value("${sql.script.delete.math.grade}")
	private String sqlDeleteMathGrade;
	
	@Value("${sql.script.delete.science.grade}")
	private String sqlDeleteScienceGrade;
	
	@Value("${sql.script.delete.history.grade}")
	private String sqlDeleteHistoryGrade;
		
	@BeforeEach
	public void setupDatabase() {		
		jdbc.execute(sqlAddStudent);		
		jdbc.execute(sqlAddMathGrade);		
		jdbc.execute(sqlAddScienceGrade);
		jdbc.execute(sqlAddHistoryGrade);		
	}
	
	@Test
	public void createStudentService() {
		
		String email = "chad.darby@luv2code_school.com";
		
		studentService.createStudent("Chad", "Darby", email);
		
		CollegeStudent student = studentDao.findByEmailAddress(email);
		
		assertEquals(email, student.getEmailAddress(), "find by email");
		
	}
			
	@Test
	public void isStudentNullCheck() {
		
		assertTrue(studentService.checkIfStudentIsNotNull(1));
		
		assertFalse(studentService.checkIfStudentIsNotNull(0));
		
	}
	
	@Test
	public void deleteStudentService() {
		
		Optional<CollegeStudent> deletedCollegeStudent = studentDao.findById(1);
		Optional<MathGrade> deletedMathGrade = mathGradeDao.findById(1);
		Optional<HistoryGrade> deletedHistoryGrade = historyGradeDao.findById(1);
		Optional<ScienceGrade> deletedScienceGrade = scienceGradeDao.findById(1);
		
		assertTrue(deletedCollegeStudent.isPresent(), "Return True");
		assertTrue(deletedMathGrade.isPresent());
		assertTrue(deletedHistoryGrade.isPresent());
		assertTrue(deletedScienceGrade.isPresent());
		
		studentService.deleteStudent(1);
		
		deletedCollegeStudent = studentDao.findById(1);
		deletedMathGrade = mathGradeDao.findById(1);
		deletedHistoryGrade = historyGradeDao.findById(1);
		deletedScienceGrade = scienceGradeDao.findById(1);
		
		assertFalse(deletedCollegeStudent.isPresent(), "Return False");
		assertFalse(deletedMathGrade.isPresent());
		assertFalse(deletedHistoryGrade.isPresent());
		assertFalse(deletedScienceGrade.isPresent());
		
	}
	
	@Sql("/insertData.sql")
	@Test
	public void getGradebookService() {
		
		Iterable<CollegeStudent> iterableCollegeStudents = studentService.getGradeBook();
		
		List<CollegeStudent> collegeStudents = new ArrayList<>();
		
		for(CollegeStudent collegeStudent:iterableCollegeStudents) {
			collegeStudents.add(collegeStudent);
		}
		
		assertEquals(5, collegeStudents.size());
		
	}
	
	@Test
	public void createGradeService() {
		
		// Create the grade
		assertTrue(studentService.createGrade(80.50, 1, "math"));
		assertTrue(studentService.createGrade(80.50, 1, "science"));
		assertTrue(studentService.createGrade(80.50, 1, "history"));
		
		// Get all grades with studentId
		Iterable<MathGrade> mathGrades = mathGradeDao.findGradeByStudentId(1);
		Iterable<ScienceGrade> scienceGrades = scienceGradeDao.findGradeByStudentId(1);
		Iterable<HistoryGrade> historyGrades = historyGradeDao.findGradeByStudentId(1);
		
		// Verify there is grades
		assertTrue(((Collection<MathGrade>) mathGrades).size() == 2, 
				"Student has math grades");
		
		assertTrue(((Collection<ScienceGrade>) scienceGrades).size() == 2, 
				"Student has science grades");
		
		assertTrue(((Collection<HistoryGrade>) historyGrades).size() == 2, 
				"Student has history grades");
		
	}
	
	@Test
	public void createGradeServiceReturnFalse() {
		
		assertFalse(studentService.createGrade(105, 1, "math"));		
		assertFalse(studentService.createGrade(-5, 1, "math"));
		assertFalse(studentService.createGrade(80.50, 2, "math"));
		assertFalse(studentService.createGrade(80.50, 1, "literature"));
		
	}
	
	@Test
	public void deleteGradeService() {
		
		int expectedStudentId = 1;
		
		assertEquals(expectedStudentId, studentService.deleteGrade(1, "math"), 
				"Returns the student id after delete");
		
		assertEquals(expectedStudentId, studentService.deleteGrade(1, "science"), 
				"Returns the student id after delete");
		
		assertEquals(expectedStudentId, studentService.deleteGrade(1, "history"), 
				"Returns the student id after delete");
	}
	
	@Test
	public void deleteGradeServiceReturnsStudentIdOfZero() {
		
		int expectedStudentId = 0;
		
		assertEquals(expectedStudentId, studentService.deleteGrade(0, "science"),
				"No student should have 0 id");
		
		assertEquals(expectedStudentId, studentService.deleteGrade(1, "literature"),
				"No student should have a literature class");
		
	}
	
	@Test
	public void studentInformation() {
		
		GradebookCollegeStudent gradebookCollegeStudent = studentService.studentInformation(1);
		
		assertNotNull(gradebookCollegeStudent);
		
		assertEquals(1, gradebookCollegeStudent.getId());
		assertEquals("Eric", gradebookCollegeStudent.getFirstname());
		assertEquals("Roby", gradebookCollegeStudent.getLastname());
		assertEquals("eric.roby@luv2code_school.com", gradebookCollegeStudent.getEmailAddress());
		
		assertTrue(gradebookCollegeStudent.getStudentGrades().getMathGradeResults().size() == 1);
		assertTrue(gradebookCollegeStudent.getStudentGrades().getHistoryGradeResults().size() == 1);
		assertTrue(gradebookCollegeStudent.getStudentGrades().getScienceGradeResults().size() == 1);
		
	}
	
	@Test
	public void studentInformationServiceReturnNull() {
		
		GradebookCollegeStudent gradebookCollegeStudent = studentService.studentInformation(0);
		
		assertNull(gradebookCollegeStudent);
		
	}
	
	@AfterEach
	public void setupAfterTransaction() {
		jdbc.execute(sqlDeleteStudent);		
		jdbc.execute(sqlDeleteMathGrade);
		jdbc.execute(sqlDeleteScienceGrade);
		jdbc.execute(sqlDeleteHistoryGrade);				
	}
	
}
