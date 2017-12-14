package ubiqore.fhir.rest;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import net.bytebuddy.implementation.bind.annotation.Default;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import ubiqore.fhir.model.admin.Project;
//import ubiqore.fhir.security.JwtTokenUtil;
import ubiqore.fhir.transform.OsakiToFHIRMapper;
import ubiqore.fhir.transform.SwftEmisToFHIRMapper01;

import javax.xml.bind.annotation.XmlElement;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by roky on 23/03/17.
 * Step 1 : service libre d'acces
 * Step 2 : protegé via jwt
 * Step 3 : acces en fonction des droits
 */

@RestController
public class ProjectRestService {

    private static  final List<Project> projects;
    private static  final List<String> mapperInstances;

    private static final String osaki$cda$01="osaki:cda:0.1";
    private static final String swft$csv$01="swft:csv:0.1";

    static {
        mapperInstances=new ArrayList<>();
        mapperInstances.add(osaki$cda$01);
        mapperInstances.add(swft$csv$01);
    }

    static {
        projects=new ArrayList<>();
        projects.add(new Project("test","toto","ok"));
        projects.add(new Project("test2","toto2","ok"));
        projects.add(new Project("test3","toto3","ok"));
    }


    @RequestMapping(path = "/admin/projects", method = RequestMethod.GET)
    @CrossOrigin
    public static List<Project> getProjects(){

        System.out.println(SecurityContextHolder.getContext().getAuthentication().getName());
        return projects;
    }

    @RequestMapping(path = "/admin/projects/{name}", method = RequestMethod.GET)
    @CrossOrigin
    public static Project getProject(String name ){


        return projects.stream()
                .filter(project -> name.equalsIgnoreCase(project.getName()))
                .findAny().orElse(null);
    }

    /**
    @GetMapping(value="/person/{id}/",
            params="format=json",
            produces=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Person> getPerson(@PathVariable Integer id){
        Person person = personMapRepository.findPerson(id);
        return ResponseEntity.ok(person);
    }
    @GetMapping(value="/person/{id}/",
            params="format=xml",
            produces=MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<Person> getPersonXML(@PathVariable Integer id){
        return GetPerson(id); // delegate
    }
    */



    private static Bundle getData(String data,String mapperInstance){

        ClinicalDocument cda = null;

            if (mapperInstance.equalsIgnoreCase(osaki$cda$01)) {
                try {
                    cda = CDAUtil.load(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8.name())));
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new DataOsakiCDAException();
                }
                OsakiToFHIRMapper mapper = new OsakiToFHIRMapper(cda);
                Bundle b=mapper.getAll();
                return b;
            }
            else if (mapperInstance.equalsIgnoreCase(swft$csv$01)){

                SwftEmisToFHIRMapper01 mapper=new SwftEmisToFHIRMapper01(data);
                Bundle b=mapper.getAll();
                return b;
            }


     return null;
    }

    @RequestMapping(path = "/transform",  params="format=json",
                    method = RequestMethod.POST ,
                    produces= {MediaType.APPLICATION_JSON_VALUE} )
    @CrossOrigin
    public static String transform(@RequestBody String data,@RequestParam( value="mapperInstance" , required =true, defaultValue = "osaki:cda:0.1") String mapperInstance ){

            System.out.println("------------------"+mapperInstance+"-------------------------");
        if (!mapperInstances.contains(mapperInstance)) throw new MapperInstanceException();

        Bundle b=getData(data,mapperInstance);

           // System.out.println(xml);
            try {

                FhirContext ctx = new FhirContext(FhirVersionEnum.DSTU3);
                String encoded = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(b);
                return encoded;


            } catch (Exception e) {
                e.printStackTrace();
            }

        return null;
    }

    @RequestMapping(path = "/transform",  params="format=xml",
            method = RequestMethod.POST ,
            produces= {MediaType.APPLICATION_XML_VALUE} )
    @CrossOrigin
    public static String transform2(@RequestBody String data , @RequestParam( value="mapperInstance" , required =true, defaultValue = "osaki:cda:0.1") String mapperInstance ){

        // System.out.println(xml);
        if (!mapperInstances.contains(mapperInstance)) throw new MapperInstanceException();

        Bundle b=getData(data,mapperInstance);
        try {

            FhirContext ctx = new FhirContext(FhirVersionEnum.DSTU3);
            String encoded = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(b);
            return encoded;


        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}

