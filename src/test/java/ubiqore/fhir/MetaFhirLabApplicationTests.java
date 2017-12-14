package ubiqore.fhir;

import com.beust.jcommander.internal.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.testng.Assert;
import ubiqore.fhir.model.security.AuthorityName;
import ubiqore.fhir.model.security.User;
//import ubiqore.fhir.persist.AppPersistanceManager;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MetaFhirLabApplicationTests {

	@Value("${app.init.dir}")
	private String path;

//	@Autowired
//	public AppPersistanceManager persistence;

	//@Test
	public void contextLoads() {
	//	System.out.println(persistence.getDir());
		User u1= new User();
		u1.setUsername("bob");
		u1.setPassword("alig1410");
		u1.setEmail("erico.ere@zeze.fr");
		u1.setEnabled(true);

		List<String> l=Lists.newArrayList();
		l.add(AuthorityName.ROLE_USER);
		l.add(AuthorityName.ROLE_ADMIN);
		u1.setAuthorities(l);
		try {
	//		Assert.assertEquals(0, persistence.createUser(u1));
	//		Assert.assertEquals(1, persistence.createUser(u1));

		}catch (Exception e){e.printStackTrace();}
	//	System.err.println(persistence.getUserByUsername("sadou").getPassword());
	//	persistence.outPutGraph();
	}

}
