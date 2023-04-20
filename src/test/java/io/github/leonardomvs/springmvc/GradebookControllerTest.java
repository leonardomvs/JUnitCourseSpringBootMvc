package io.github.leonardomvs.springmvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.ModelAndView;

import io.github.leonardomvs.springmvc.models.CollegeStudent;
import io.github.leonardomvs.springmvc.models.GradebookCollegeStudent;
import io.github.leonardomvs.springmvc.models.MathGrade;
import io.github.leonardomvs.springmvc.repository.MathGradeDao;
import io.github.leonardomvs.springmvc.repository.StudentDao;
import io.github.leonardomvs.springmvc.service.StudentAndGradeService;

@TestPropertySource("/application-test.properties")
@AutoConfigureMockMvc
@SpringBootTest
public class GradebookControllerTest {

	private static MockHttpServletRequest request;
	
	@Autowired
	JdbcTemplate jdbc;
	
	@Autowired
	MockMvc mockMvc;
	
	@Autowired
	StudentDao studentDao;

	@Autowired
	MathGradeDao mathGradeDao;
	
	@Autowired
	StudentAndGradeService studentService;
	
	@Mock
	StudentAndGradeService studentCreateServiceMock;
	
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
	
	@BeforeAll
	public static void setup() {
		request = new MockHttpServletRequest();
		request.setParameter("firstname", "Chad");
		request.setParameter("lastname", "Darby");
		request.setParameter("emailAddress", "chad.darby@luv2code_school.com");
	}
	
	@BeforeEach
	public void beforeEach() {
		jdbc.execute(sqlAddStudent);		
		jdbc.execute(sqlAddMathGrade);		
		jdbc.execute(sqlAddScienceGrade);
		jdbc.execute(sqlAddHistoryGrade);
	}
	
	@Test
	void getStudentsHttpRequest() throws Exception {
		
		CollegeStudent studentOne = new GradebookCollegeStudent("Eric", "Roby", 
				"eric_roby@luv2code_school.com");
		
		CollegeStudent studentTwo = new GradebookCollegeStudent("Chad", "Darby", 
				"chad_darby@luv2code_school.com");
		
		List<CollegeStudent> collegeStudentList = List.of(studentOne, studentTwo);
		
		when(studentCreateServiceMock.getGradeBook()).thenReturn(collegeStudentList);
		
		assertIterableEquals(collegeStudentList, studentCreateServiceMock.getGradeBook());
		
		MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/"))
				.andExpect(status().isOk()).andReturn();
		
		ModelAndView mav = mvcResult.getModelAndView();
		
		ModelAndViewAssert.assertViewName(mav, "index");
		
	}
	
	@Test
	void createStudentHttpRequest() throws Exception {
		
		CollegeStudent studentOne = new CollegeStudent("Eric", "Roby", "eric_roby@luv2code_school.com");
		
		List<CollegeStudent> collegeStudentList = List.of(studentOne);
		
		when(studentCreateServiceMock.getGradeBook()).thenReturn(collegeStudentList);
		
		assertIterableEquals(collegeStudentList, studentCreateServiceMock.getGradeBook());
		
		MvcResult mvcResult = this.mockMvc.perform(post("/")
				.contentType(MediaType.APPLICATION_JSON)
				.param("firstname", request.getParameterValues("firstname"))
				.param("lastname", request.getParameterValues("lastname"))
				.param("emailAddress", request.getParameterValues("emailAddress")))
				.andExpect(status().isOk()).andReturn();
		
		ModelAndView mav = mvcResult.getModelAndView();
		
		ModelAndViewAssert.assertViewName(mav, "index");
		
		CollegeStudent verifyStudent = studentDao.findByEmailAddress("chad.darby@luv2code_school.com");
		
		assertNotNull(verifyStudent, "Student should be found");
		
	}
	
	@Test
	void deleteStudentHttpRequest() throws Exception {
		
		assertTrue(studentDao.findById(1).isPresent());
		
		MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
				.get("/delete/student/{id}", 1))
				.andExpect(status().isOk())
				.andReturn();
		
		ModelAndView mav = mvcResult.getModelAndView();
		
		ModelAndViewAssert.assertViewName(mav, "index");
		
		assertFalse(studentDao.findById(1).isPresent());
		
	}
	
	@Test
	void deleteStudentHttpRequestErrorPage() throws Exception {
		
		MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
				.get("/delete/student/{id}", 0))
				.andExpect(status().isOk())
				.andReturn();
		
		ModelAndView mav = mvcResult.getModelAndView();
		
		ModelAndViewAssert.assertViewName(mav, "error");
		
	}
	
	@Test
	void studentInformationHttpRequest() throws Exception {
		
		assertTrue(studentDao.findById(1).isPresent());
		
		MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
				.get("/studentInformation/{id}", 1))
				.andExpect(status().isOk())
				.andReturn();
		
		ModelAndView mav = mvcResult.getModelAndView();
		
		ModelAndViewAssert.assertViewName(mav, "studentInformation");
		
	}
	
	@Test
	void studentInformationHttpStudentDoesNotExistRequest() throws Exception {
		
		assertFalse(studentDao.findById(0).isPresent());
		
		MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
				.get("/studentInformation/{id}", 0))
				.andExpect(status().isOk())
				.andReturn();
		
		ModelAndView mav = mvcResult.getModelAndView();
		
		ModelAndViewAssert.assertViewName(mav, "error");
		
	}
	
	@Test
	void createValidGradeHttpRequest() throws Exception {
		
		assertTrue(studentDao.findById(1).isPresent());
		
		GradebookCollegeStudent student = studentService.studentInformation(1);
		
		assertEquals(1, student.getStudentGrades().getMathGradeResults().size());
		
		MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
				.post("/grades")
				.contentType(MediaType.APPLICATION_JSON)
				.param("grade", "85.00")
				.param("gradeType", "math")
				.param("studentId", "1"))
				.andExpect(status().isOk())
				.andReturn();
		
		ModelAndView mav = mvcResult.getModelAndView();
		
		ModelAndViewAssert.assertViewName(mav, "studentInformation");
		
		student = studentService.studentInformation(1);
		
		assertEquals(2, student.getStudentGrades().getMathGradeResults().size());
		
	}
	
	@Test
	void createAValidGradeHttpRequestStudentDoesNotExistEmptyResponse() throws Exception {
		
		MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
				.post("/grades")
				.contentType(MediaType.APPLICATION_JSON)
				.param("grade", "85.00")
				.param("gradeType", "math")
				.param("studentId", "0"))
				.andExpect(status().isOk())
				.andReturn();
		
		ModelAndView mav = mvcResult.getModelAndView();
		
		ModelAndViewAssert.assertViewName(mav, "error");
		
	}
	
	@Test
	void createANonValidGradeHttpRequestGradeTypeDoesNotExistEmptyResponse() throws Exception {
		
		MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
				.post("/grades")
				.contentType(MediaType.APPLICATION_JSON)
				.param("grade", "85.00")
				.param("gradeType", "literature")
				.param("studentId", "1"))
				.andExpect(status().isOk())
				.andReturn();

		ModelAndView mav = mvcResult.getModelAndView();
		
		ModelAndViewAssert.assertViewName(mav, "error");
		
	}
	
	@Test
	void deleteAValidGradeHttpRequest() throws Exception {
		
		Optional<MathGrade> mathGrade = mathGradeDao.findById(1);
		
		assertTrue(mathGrade.isPresent());
		
		MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
				.get("/grades/{id}/{gradeType}", 1, "math"))
				.andExpect(status().isOk())
				.andReturn();

		ModelAndView mav = mvcResult.getModelAndView();
		
		ModelAndViewAssert.assertViewName(mav, "studentInformation");
		
		mathGrade = mathGradeDao.findById(1);
		
		assertFalse(mathGrade.isPresent());
		
	}
	
	@Test
	void deleteAValidGradeHttpRequestStudentIdDoesNotExistEmptyResponse() throws Exception {
		
		Optional<MathGrade> mathGrade = mathGradeDao.findById(0);
		
		assertFalse(mathGrade.isPresent());
		
		MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
				.get("/grades/{id}/{gradeType}", 0, "math"))
				.andExpect(status().isOk())
				.andReturn();
		
		ModelAndView mav = mvcResult.getModelAndView();
		
		ModelAndViewAssert.assertViewName(mav, "error");
		
	}

	@Test
	void deleteANonValidGradeHttpRequest() throws Exception {
				
		MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
				.get("/grades/{id}/{gradeType}", 1, "literature"))
				.andExpect(status().isOk())
				.andReturn();
		
		ModelAndView mav = mvcResult.getModelAndView();
		
		ModelAndViewAssert.assertViewName(mav, "error");
		
	}
	@AfterEach
	public void setupAfterTransaction() {
		jdbc.execute(sqlDeleteStudent);		
		jdbc.execute(sqlDeleteMathGrade);
		jdbc.execute(sqlDeleteScienceGrade);
		jdbc.execute(sqlDeleteHistoryGrade);
	}
	
}
