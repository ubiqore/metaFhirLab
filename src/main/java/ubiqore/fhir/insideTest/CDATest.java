package ubiqore.fhir.insideTest;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import org.hl7.fhir.dstu3.model.*;
import org.openhealthtools.mdht.uml.cda.*;
import org.openhealthtools.mdht.uml.cda.impl.ClinicalDocumentImpl;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import ubiqore.fhir.transform.OsakiToFHIRMapper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by roky on 20/11/17.
 */
public class CDATest {

    public static void main (String ... args){
        FileInputStream fis = null;
        try {
            fis = new FileInputStream("src/test/resources/osaki.xml");
            ClinicalDocument cda = CDAUtil.load(fis);
            OsakiToFHIRMapper mapper=new OsakiToFHIRMapper(cda);
            Bundle b=mapper.getMedicalStatements();
            FhirContext ctx = new FhirContext(FhirVersionEnum.DSTU3);
            String encoded = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(b);
            System.out.println(encoded);

//            org.hl7.fhir.dstu3.model.Patient patient=mapper.getPatient();
//            Bundle b=mapper.getConditions();
//            b.addEntry().setResource(patient);
//
//            String encoded = ctx.newJsonParser().encodeResourceToString(patient);
//            System.out.println(encoded);
//            encoded = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(b);
//            System.out.println(encoded);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
