package ubiqore.fhir.transform;

import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.exceptions.FHIRException;
import org.openhealthtools.mdht.uml.cda.*;
import org.openhealthtools.mdht.uml.hl7.datatypes.*;
import org.openhealthtools.mdht.uml.hl7.vocab.ActClassObservation;
import org.openhealthtools.mdht.uml.hl7.vocab.TimingEvent;
import ubiqore.fhir.transform.osaki.CDARule01;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by roky on 20/11/17.
 */
public class OsakiToFHIRMapper {
    private ClinicalDocument cda=null;
    private Identifier id=null;
    private Patient patient=null;

    public OsakiToFHIRMapper(ClinicalDocument cda){
        this.cda=cda;
    }


    public Bundle getAll(){
        this.getIdentity();
        Patient pat=this.getPatient();
        Bundle returnB= new Bundle();
        returnB.setType(Bundle.BundleType.COLLECTION);
        returnB.addEntry().setResource(pat);

        Bundle cs=this.getConditions();
        for (Bundle.BundleEntryComponent bb:cs.getEntry()){
            returnB.addEntry(bb);
        }
        Bundle mss=this.getMedicalStatements();
        for (Bundle.BundleEntryComponent bb:mss.getEntry()){
            returnB.addEntry(bb);
        }
        return returnB;
    }
    public void getIdentity(){
       Identifier id=null;
        RecordTarget rt=cda.getRecordTargets().get(0);
        List<II> l=rt.getPatientRole().getIds();
        System.out.println("nb ids = "+l.size());
        for (II idy:l){
           if (idy.getRoot()!=null){
               if (idy.getRoot().equalsIgnoreCase("2.16.724.4.16.1.100.2.1"))
               {
                   id=new Identifier();
                   id.setSystem("urn:oid:2.16.724.4.16.1.100.2.1");
                   id.setValue(idy.getExtension());
                   this.id=id;
                   return;
               }
           }
        }

    }
    public Bundle getConditions(){
        if (this.id==null)this.getIdentity();
        if (this.patient==null) this.getPatient();
        Bundle conditions=new Bundle();
        conditions.setType(Bundle.BundleType.COLLECTION);
        StructuredBody sb=cda.getComponent().getStructuredBody();
        for (Component3 c:sb.getComponents()){
            II ti=c.getTemplateIds().get(0);
            if (ti.getRoot().equalsIgnoreCase("2.16.724.4.50.1.7")){
                Section section=c.getSection();
                for (Entry entry:section.getEntries()){

                    Condition condition=new Condition();
                    Reference ref=new Reference();
                 try {  ref.setDisplay(this.patient.getName().get(0).getGiven().toString()+ " "+this.patient.getName().get(0).getFamily());}catch (Exception e ){}//not mandatory but better to put name for patient ref.

                    ref.setReference("Patient/"+this.id.getValue());
                    //ref.setIdentifier(this.id);
                    condition.setSubject(ref);
                    condition.setClinicalStatus(Condition.ConditionClinicalStatus.ACTIVE);
                    CodeableConcept concept=new CodeableConcept();
                    Coding code=new Coding();
                    try {
                        for (ANY value:entry.getObservation().getValues()) {
                            boolean valid=true;
                            CD cd = (CD) value;


                                code.setCode(cd.getCode());
                                if (code.getCode()==null || code.getCode().isEmpty() || code.getCode().trim().equalsIgnoreCase(""))valid=false;
                                code.setSystem("urn:oid:" + cd.getCodeSystem());
                                code.setDisplay(cd.getDisplayName());


                            IVL_TS ets = entry.getObservation().getEffectiveTime();
                            String etime = ets.getValue();
                            Date et = new GregorianCalendar(Integer.parseInt(etime.substring(0, 4))
                                    , Integer.parseInt(etime.substring(4, 6)) - 1
                                    , Integer.parseInt(etime.substring(6, 8))
                            ).getTime();
                            condition.setAssertedDate(et);
                            concept.addCoding(code);
                            condition.setCode(concept);

                            if (valid)conditions.addEntry(new Bundle.BundleEntryComponent().setResource(condition));
                        }
                    }catch (Exception e){e.printStackTrace();}

                }
            }
        }
        return conditions;
    }
    public Patient getPatient(){

        if (this.id==null)this.getIdentity();
        this.patient=new Patient();

        //add identifier
        List<Identifier> l=new ArrayList<Identifier>();
        l.add(this.id);
        this.patient.setId(this.id.getValue());
        this.patient.setIdentifier(l);
        RecordTarget rt=cda.getRecordTargets().get(0);
        PatientRole pt = rt.getPatientRole();
        HumanName hn = this.patient.addName();
        StringBuilder fn = new StringBuilder();

        for (PN pn:pt.getPatient().getNames()){
            if(pn.getFamilies() != null && !pn.getFamilies().isEmpty()) {
                for(ENXP family: pn.getFamilies()) {
                    fn.append(family.getText()+" ");
                }

            }
            if(pn.getGivens() != null && !pn.getGivens().isEmpty()) {
                for(ENXP givens: pn.getGivens()) {
                    hn.addGiven(givens.getText());
                }

            }
        }

        hn.setFamily(fn.toString().trim());

        TS bt=pt.getPatient().getBirthTime();
        String b = bt.getValue();
        System.out.println("mois"+b.substring(4,6));
        System.out.println(b);
        Date bd = new GregorianCalendar(Integer.parseInt(b.substring(0,4))
                , Integer.parseInt(b.substring(4,6))-1
                , Integer.parseInt(b.substring(6,8))
        ).getTime();
        System.out.println(bd.toString());
        this.patient.setBirthDate(bd);


        CE  ce=pt.getPatient().getAdministrativeGenderCode();

        switch (ce.getCode()) {
            case "F" :
                this.patient.setGender(Enumerations.AdministrativeGender.FEMALE);
                break;
            case "M" :
                this.patient.setGender(Enumerations.AdministrativeGender.MALE);
                break;
            default  :
                this.patient.setGender(Enumerations.AdministrativeGender.UNKNOWN);
        }

        //address
        for (AD ad:pt.getAddrs()){
            Address address=this.patient.addAddress();
            // Line

            StringBuilder aline = new StringBuilder();
            // streetAddressLine -> line
            if(ad.getStreetAddressLines() != null && !ad.getStreetAddressLines().isEmpty()) {
                for(ADXP adxp : ad.getStreetAddressLines()){
                    if(adxp != null && !adxp.isSetNullFlavor()) {
                        address.addLine(adxp.getText());
                    }
                }
            }

            if(ad.getStreetNameTypes() != null && !ad.getStreetNameTypes().isEmpty()) {
                for(ADXP adxp : ad.getStreetNameTypes()){
                    if(adxp != null && !adxp.isSetNullFlavor()) {
                        aline.append(adxp.getText()+" ");
                    }
                }
            }
            if(ad.getStreetNames() != null && !ad.getStreetNames().isEmpty()) {
                for(ADXP adxp : ad.getStreetNames()){
                    if(adxp != null && !adxp.isSetNullFlavor()) {
                        aline.append(adxp.getText()+", ");
                    }
                }
            }

            if(ad.getHouseNumbers() != null && !ad.getHouseNumbers().isEmpty()) {
                for(ADXP adxp : ad.getHouseNumbers()){
                    if(adxp != null && !adxp.isSetNullFlavor()) {
                        aline.append(adxp.getText()+" ");
                    }
                }
            }
            if (aline.length()>0){
                address.addLine(aline.toString().trim());
            }
            // deliveryAddressLine -> line
            if(ad.getDeliveryAddressLines() != null && !ad.getDeliveryAddressLines().isEmpty()) {
                for(ADXP adxp : ad.getDeliveryAddressLines()) {
                    if(adxp != null && !adxp.isSetNullFlavor()) {
                        address.addLine(adxp.getText());
                    }
                }
            }

            // city -> city
            if(ad.getCities() != null && !ad.getCities().isEmpty()) {
                for(ADXP adxp : ad.getCities()) {
                    // Asserting that at most one city information exists
                    if(adxp != null && !adxp.isSetNullFlavor()) {
                        address.setCity(adxp.getText());
                    }
                }
            }

            // county -> district
            if(ad.getCounties() != null && !ad.getCounties().isEmpty()) {
                for(ADXP adxp : ad.getCounties()) {
                    // Asserting that at most one county information exists
                    if(adxp != null && !adxp.isSetNullFlavor()) {
                        address.setDistrict(adxp.getText());
                    }
                }

            }

            // country -> country
            if( ad.getCountries() != null && !ad.getCountries().isEmpty()) {
                for(ADXP adxp : ad.getCountries()) {
                    if(adxp != null && !adxp.isSetNullFlavor()) {
                        address.setCountry(adxp.getText());
                    }
                }

            }

            // state -> state
            if(ad.getStates() != null && !ad.getStates().isEmpty()) {
                for(ADXP adxp : ad.getStates()) {
                    if(adxp != null && !adxp.isSetNullFlavor()) {
                        address.setState(adxp.getText());
                    }
                }
            }

            // postalCode -> postalCode
            if(ad.getPostalCodes() != null && !ad.getPostalCodes().isEmpty()) {
                for(ADXP adxp : ad.getPostalCodes()) {
                    if(adxp != null && !adxp.isSetNullFlavor()) {
                        address.setPostalCode(adxp.getText());
                    }
                }
            }



        }

        for (TEL tel:pt.getTelecoms()){
            if (tel.getValue().startsWith("tel:"))
              this.patient.addTelecom()
                          .setSystem(ContactPoint.ContactPointSystem.PHONE)
                          .setValue(tel.getValue().replaceFirst("tel:",""));
        }
        return this.patient;
    }

    public Bundle getMedicalStatements(){
        if (this.id==null)this.getIdentity();
        if (this.patient==null)this.getPatient();

        Bundle mss=new Bundle();
        mss.setType(Bundle.BundleType.COLLECTION);
        StructuredBody sb=cda.getComponent().getStructuredBody();
        for (Component3 c:sb.getComponents()){
            II ti=c.getTemplateIds().get(0);
            if (ti.getRoot().equalsIgnoreCase("2.16.724.4.50.1.9")){
                Section section=c.getSection();
                for (Entry entry:section.getEntries()){
                    System.out.println("one entry");
                    MedicationStatement ms=new MedicationStatement();

                    mss.addEntry().setResource(ms);
                    ms.setTaken(MedicationStatement.MedicationStatementTaken.UNK);
                    ms.setStatus(MedicationStatement.MedicationStatementStatus.ENTEREDINERROR);
                    Reference ref=new Reference();

                    ref.setReference("Patient/"+this.id.getValue());
                    try {
                        ref.setDisplay(this.patient.getName().get(0).getGiven().toString() + " " + this.patient.getName().get(0).getFamily());
                    }catch (Exception e){} //not mandatory .. could crash...
                    ms.setSubject(ref);
                    SubstanceAdministration sa=entry.getSubstanceAdministration();
                    for (II ids : sa.getIds()) {
                        Identifier id = ms.addIdentifier();
                        id.setSystem("urn:oid:"+ids.getRoot());
                        id.setValue(ids.getExtension());
                    }
                    ManufacturedProduct mfp=sa.getConsumable().getManufacturedProduct();
                    CodeableConcept produit=new CodeableConcept();
                    Coding produitCode=produit.addCoding();
                    produitCode.setSystem("urn:oid:"+mfp.getManufacturedMaterial().getCode().getCodeSystem());
                    produitCode.setDisplay(mfp.getManufacturedMaterial().getCode().getDisplayName());
                    Dosage dosage=ms.addDosage();
                    ms.setMedication(produit);


                    // doseQuantity ??
                    try { IVL_PQ dq=sa.getDoseQuantity();
                        SimpleQuantity sq=new SimpleQuantity();
                        sq.setValue(dq.getValue());
                        sq.setUnit(dq.getUnit());
                        dosage.setDose(sq);
                    } catch (Exception e){}

                    try {
                        CE ce = sa.getRouteCode();
                        CodeableConcept route=new CodeableConcept();
                        Coding routeCoding=route.addCoding();
                        routeCoding.setDisplay(ce.getDisplayName());
                        routeCoding.setSystem("urn:oid:"+ce.getCodeSystem());
                        routeCoding.setCode(ce.getCode());

                        dosage.setRoute(route);
                    }catch (Exception e ){}




                    for (SXCM_TS t:sa.getEffectiveTimes()){
                        if (t instanceof PIVL_TS){ //unité
                            PIVL_TS pt=(PIVL_TS)t;
                            String unit=pt.getPeriod().getUnit();
                            dosage.setText((dosage.getText()!=null) ? dosage.getText()+" " : "" + "original unit text:"+unit);
                            System.out.println(pt.getPeriod().getUnit());
                            //al_acostarse au coucher (mapping : HS https://www.hl7.org/fhir/valueset-event-timing.html )
                            CDARule01 defaultRule=new CDARule01();
                            Timing timing=defaultRule.getTimingOrNullFromCDAPerioUniyString(unit);
                            if (timing!=null)dosage.setTiming(timing);


                            //

//                            CodeableConcept c=new CodeableConcept();
//                            c.setCoding(Timing.EventTiming.)
//                            timing.setCode()
                        }
                        else if (t instanceof IVL_TS){
                            IVL_TS it=(IVL_TS)t;
                            Date low=this.getJavaDate(it.getLow().getValue());
                            Date high=this.getJavaDate(it.getHigh().getValue());
                            Period period=new Period();
                            period.setStart(low);
                            period.setEnd(high);
                            ms.setEffective(period);
                            Date today=new Date();
                            if (low.after(today)){
                                ms.setStatus(MedicationStatement.MedicationStatementStatus.ENTEREDINERROR);
                            }else {
                                if (high.before(today)){
                                    ms.setStatus(MedicationStatement.MedicationStatementStatus.COMPLETED);
                                }else ms.setStatus(MedicationStatement.MedicationStatementStatus.ACTIVE);
                            }


                        }
                 //       System.out.println(t.toString());

                    }

                    }
                }
            }

        return mss;
    }


    private Date getJavaDate(String etime){
        return  new GregorianCalendar(Integer.parseInt(etime.substring(0, 4))
                , Integer.parseInt(etime.substring(4, 6)) - 1
                , Integer.parseInt(etime.substring(6, 8))
        ).getTime();
    }
}
