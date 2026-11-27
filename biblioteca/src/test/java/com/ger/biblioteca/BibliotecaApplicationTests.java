package com.ger.biblioteca;

import com.ger.biblioteca.biblioteca.BibliotecaApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = BibliotecaApplication.class)
@ActiveProfiles("test")
class BibliotecaApplicationTests {

	@Test
	void contextLoads() {
	}

}
