package ubiqore.fhir.transform;

import com.univocity.parsers.common.processor.RowListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;
import org.hl7.fhir.dstu3.model.*;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;

import javax.swing.text.html.HTMLDocument;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created by roky on 01/12/17.
 *
 * http://www.hl7.org.uk/version3group/oids.asp READ  OID
 *
 *
 *
 */
public class SwftEmisToFHIRMapper01 {

    public static void main(String... args ){

        byte[] encoded = new byte[0];
        try {

            URL resource = SwftEmisToFHIRMapper01.class.getResource("/static/patientA.csv");

            encoded = Files.readAllBytes(Paths.get(resource.toURI()));
            String data=new String(encoded, StandardCharsets.UTF_8);
            SwftEmisToFHIRMapper01 mapper = new SwftEmisToFHIRMapper01(data);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }
    private ClinicalDocument cda=null;
    private Identifier id=null;

    String data=null;


    public SwftEmisToFHIRMapper01(String data){
        this.data=data;

        CsvParserSettings settings = new CsvParserSettings();
        settings.getFormat().setLineSeparator("\n");
        CsvParser parser = new CsvParser(settings);

        // parses all rows in one go.
        try {
            List<String[]> allRows = parser.parseAll(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8.name())));
            System.out.println("nb lines"+ allRows.size());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    private List<String[]>  getRowsForSpecific(String ... fields){
        // A RowListProcessor stores each parsed row in a List.
        RowListProcessor rowProcessor = new RowListProcessor();


        // Let's consider the first parsed row as the headers of each column in the file.

        CsvParserSettings settings = new CsvParserSettings();
        settings.setHeaderExtractionEnabled(true);
        settings.setProcessor(rowProcessor);
        settings.setHeaderExtractionEnabled(true);

        settings.getFormat().setLineSeparator("\n");
        settings.selectFields(fields);
        CsvParser parser = new CsvParser(settings);
        try {
            parser.parse(new ByteArrayInputStream(this.data.getBytes(StandardCharsets.UTF_8.name())));
            String[] headers = rowProcessor.getHeaders();
            List<String[]> rows = rowProcessor.getRows();
            System.out.println("nb lines"+ rows.size());
            return rows;


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;

    }
    Patient patient;

    java.util.Date transformDate(String ddate){
       // 01-Jan-1970
       // 01-Jan.-1970
       // ddate=ddate.toLowerCase();
        // DateTimeFormatter f = new DateTimeFormatterBuilder().parseCaseInsensitive().append(existing_formatter).toFormatter();
        DateFormat formatter2 = new SimpleDateFormat("d-MMM-yyyy", Locale.ENGLISH);

        DateFormat formatter3 = new SimpleDateFormat("d-MMM.-yyyy",Locale.ENGLISH   );
        Date date = null;
        try {
            date = formatter2.parse(ddate);
        } catch (ParseException e) {
            try { date = formatter3.parse(ddate);}catch (Exception e2 ){}
        }
        System.out.println(ddate + " ==>"+date);
        return date;
    }

    Bundle conditions;

    void createConditions(){

        conditions=new Bundle();
        // PMH:D	PMH:C	PMH:CC	PMH:P	PMH:A	PMH:PS	PMH:PED
        List<String[]> rows =this.getRowsForSpecific("PMH:D","PMH:C","PMH:CC","PMH:P","PMH:A","PMH:PS","PMH:PED");
        Iterator<String[]> rowIt=rows.iterator();
        while (rowIt.hasNext()){
            String[] row=rowIt.next();
            if (row[0]!=null){
                Condition condition=new Condition();
                condition.setSubject(null); //todo
                condition.setAssertedDate(this.transformDate(row[0]));
                CodeableConcept cc=new CodeableConcept();
                Coding coding= cc.addCoding();
                coding.setDisplay(row[1].trim());
                coding.setCode(row[2].trim());
                coding.setSystem("urn:oid:READ:OID:TO:BE:SET");
                condition.setCode(cc);
                if (row[3]!=null){
                    String stat=row[3].trim();
                    if (stat.equalsIgnoreCase("Past Problem")){
                        condition.setClinicalStatus(Condition.ConditionClinicalStatus.INACTIVE);
                    }
                    else if (stat.equalsIgnoreCase("Active Problem")){
                        condition.setClinicalStatus(Condition.ConditionClinicalStatus.ACTIVE);
                    }

                }
                condition.addNote().setText(row[4]);
                if (row[5]!=null) {
                    CodeableConcept cc2=new CodeableConcept();
                    cc2.addCoding().setDisplay(row[5]);
                    condition.setSeverity(cc2);
                }
                if (row[6]!=null){
                    DateTimeType abatement=new DateTimeType();
                    abatement.setValue(this.transformDate(row[6]));
                    condition.setAbatement(abatement);
                }


                conditions.addEntry().setResource(condition);
            }

        }
    }

    void createPatient(){

            patient=new Patient();

            List<String[]> rows =this.getRowsForSpecific("P:G","P:U");
            String[] myrow=rows.iterator().next(); //gender + organization name

            patient.setId("NONE");

            String myGender=myrow[0].trim().toLowerCase();

            switch (myGender){
                case "female" :
                    this.patient.setGender(Enumerations.AdministrativeGender.FEMALE);
                    break;
                case "male" :
                    this.patient.setGender(Enumerations.AdministrativeGender.MALE);
                    break;
                default  :
                    this.patient.setGender(Enumerations.AdministrativeGender.UNKNOWN);
            }

    }
    public void createVitalSigns(){
        // A RowListProcessor stores each parsed row in a List.
        RowListProcessor rowProcessor = new RowListProcessor();


        // Let's consider the first parsed row as the headers of each column in the file.

        CsvParserSettings settings = new CsvParserSettings();
        settings.setHeaderExtractionEnabled(true);
        settings.setProcessor(rowProcessor);
        settings.setHeaderExtractionEnabled(true);

        settings.getFormat().setLineSeparator("\n");
        settings.selectFields("VS:CC","VS:CT","VS:AT","VS:D","VS:V","VS:UM","VS:DNR");
        CsvParser parser = new CsvParser(settings);

        // let's parse with these settings and print the parsed rows.
        try {
            parser.parse(new ByteArrayInputStream(this.data.getBytes(StandardCharsets.UTF_8.name())));
            String[] headers = rowProcessor.getHeaders();
            List<String[]> rows = rowProcessor.getRows();
            System.out.println("nb lines"+ rows.size());
            int cpt=0;
            for (String[] row:rows){
                if(row[0]!=null)  System.out.println(cpt +""+ row[0] +row[1]);
                cpt++;
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    //
    public Bundle getAll(){
        //this.getIdentity();
        this.createPatient();
        this.createConditions();

        Bundle returnB= new Bundle();

        returnB.setType(Bundle.BundleType.COLLECTION);
        returnB.addEntry().setResource(this.patient);


        for (Bundle.BundleEntryComponent bb:this.conditions.getEntry()){
            returnB.addEntry(bb);
        }

        return returnB;
    }
}
