package ubiqore.fhir.transform.osaki;

import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.dstu3.model.Timing;
import org.hl7.fhir.dstu3.model.codesystems.DaysOfWeek;
import org.hl7.fhir.dstu3.model.codesystems.DaysOfWeekEnumFactory;
import org.testng.collections.Lists;

import java.lang.reflect.Array;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by roky on 30/11/17.
 */
public class CDARule01 {

    private void treatRight(Timing.TimingRepeatComponent ttc, String rightPart){
        // regles
        rightPart= rightPart.replaceAll("_","").trim();
        StringTokenizer st=new StringTokenizer(rightPart,"-");
        int nbDays= st.countTokens();
        if (nbDays==1)this.getOneday(ttc,st.nextToken());
        else if (nbDays >1 && nbDays <=3)this.getThreeLettersdays(ttc,rightPart);
        else if (nbDays >3 )this.getOneLetterdays(ttc,rightPart);
    }

    //lunes, martes, miércoles, jueves, viernes, sábado, domingo.
    void getOneday (Timing.TimingRepeatComponent ttc , String oneDay){

        List<Timing.DayOfWeek> myl= Lists.newArrayList();
        if (oneDay.equalsIgnoreCase("lunes"))ttc.addDayOfWeek(Timing.DayOfWeek.MON);
        if (oneDay.equalsIgnoreCase("martes"))ttc.addDayOfWeek(Timing.DayOfWeek.TUE);
        if (oneDay.equalsIgnoreCase("miércoles"))ttc.addDayOfWeek(Timing.DayOfWeek.WED);
        if (oneDay.equalsIgnoreCase("jueves"))ttc.addDayOfWeek(Timing.DayOfWeek.THU);
        if (oneDay.equalsIgnoreCase("viernes"))ttc.addDayOfWeek(Timing.DayOfWeek.FRI);
        if (oneDay.equalsIgnoreCase("sábado"))ttc.addDayOfWeek(Timing.DayOfWeek.SAT);
        if (oneDay.equalsIgnoreCase("domingo"))ttc.addDayOfWeek(Timing.DayOfWeek.SUN);
        // lunes, martes, miércoles, jueves, viernes, sábado, domingo.

    }

   // lunes, martes, miércoles, jueves, viernes, sábado, domingo.
    void  getThreeLettersdays (Timing.TimingRepeatComponent ttc , String rightPart){
        StringTokenizer st=new StringTokenizer(rightPart,"-");
        while (st.hasMoreTokens()){
            String oneDay=st.nextToken();
            if (oneDay.equalsIgnoreCase("lun"))ttc.addDayOfWeek(Timing.DayOfWeek.MON);
            if (oneDay.equalsIgnoreCase("mar"))ttc.addDayOfWeek(Timing.DayOfWeek.TUE);
            if (oneDay.equalsIgnoreCase("mié"))ttc.addDayOfWeek(Timing.DayOfWeek.WED);
            if (oneDay.equalsIgnoreCase("jue"))ttc.addDayOfWeek(Timing.DayOfWeek.THU);
            if (oneDay.equalsIgnoreCase("vie"))ttc.addDayOfWeek(Timing.DayOfWeek.FRI);
            if (oneDay.equalsIgnoreCase("sáb"))ttc.addDayOfWeek(Timing.DayOfWeek.SAT);
            if (oneDay.equalsIgnoreCase("dom"))ttc.addDayOfWeek(Timing.DayOfWeek.SUN);
        }

    }

    // lunes, martes, miércoles, jueves, viernes, sábado, domingo.
    void  getOneLetterdays (Timing.TimingRepeatComponent ttc , String rightPart){
        StringTokenizer st=new StringTokenizer(rightPart,"-");
        while (st.hasMoreTokens()){
            String oneDay=st.nextToken();
            if (oneDay.equalsIgnoreCase("l"))ttc.addDayOfWeek(Timing.DayOfWeek.MON);
            if (oneDay.equalsIgnoreCase("m"))ttc.addDayOfWeek(Timing.DayOfWeek.TUE);
            if (oneDay.equalsIgnoreCase("x"))ttc.addDayOfWeek(Timing.DayOfWeek.WED);
            if (oneDay.equalsIgnoreCase("j"))ttc.addDayOfWeek(Timing.DayOfWeek.THU);
            if (oneDay.equalsIgnoreCase("v"))ttc.addDayOfWeek(Timing.DayOfWeek.FRI);
            if (oneDay.equalsIgnoreCase("s"))ttc.addDayOfWeek(Timing.DayOfWeek.SAT);
            if (oneDay.equalsIgnoreCase("d"))ttc.addDayOfWeek(Timing.DayOfWeek.SUN);
        }

    }

    public Timing getTimingOrNullFromCDAPerioUniyString(String unit){
        Timing timing=new Timing();
        unit=unit.trim();

        boolean treatRight=false;
        String rightPart=null;

        if (unit.contains("/")){
            // format a_quel(s)_moment(s)_/_quels_jours?
            StringTokenizer st=new StringTokenizer(unit,"/");
            String leftPart=st.nextToken();
            rightPart=st.nextToken();
            unit=leftPart;
            if (rightPart.trim().length()>0)treatRight=true;
        }


        if (unit.equalsIgnoreCase("al_acostarse")) {

            Timing.TimingRepeatComponent trc = new Timing.TimingRepeatComponent();
            trc.addWhen(Timing.EventTiming.HS);
            timing.setRepeat(trc);
            if (treatRight)this.treatRight(trc,rightPart);
            return timing;
        }
        else if (unit.equalsIgnoreCase("en_desayuno,_comida_y_acostarse")) {

            Timing.TimingRepeatComponent trc = new Timing.TimingRepeatComponent();
            trc.addWhen(Timing.EventTiming.CM);
            trc.addWhen(Timing.EventTiming.CD);
            trc.addWhen(Timing.EventTiming.HS);
            timing.setRepeat(trc);
            if (treatRight)this.treatRight(trc,rightPart);
            return timing;
        }

        else if (unit.equalsIgnoreCase("en_desayuno")) {

            Timing.TimingRepeatComponent trc = new Timing.TimingRepeatComponent();
            trc.addWhen(Timing.EventTiming.CM);
            timing.setRepeat(trc);
            if (treatRight)this.treatRight(trc,rightPart);
            return timing;
        }

        else if (unit.equalsIgnoreCase("en_merienda")) {
//            merienda = goûter https://en.wikipedia.org/wiki/Merienda
//            Timing.TimingRepeatComponent trc = new Timing.TimingRepeatComponent();
//            trc.addWhen(Timing.EventTiming.C_);
//            timing.setRepeat(trc);
//            if (treatRight)this.treatRight(trc,rightPart);
//            return timing;
        }
        else if (unit.equalsIgnoreCase("en_comida")) {

            Timing.TimingRepeatComponent trc = new Timing.TimingRepeatComponent();
            trc.addWhen(Timing.EventTiming.CD);
            timing.setRepeat(trc);
            return timing;
        }
        else if (unit.equalsIgnoreCase("en_cena")) {

            Timing.TimingRepeatComponent trc = new Timing.TimingRepeatComponent();
            trc.addWhen(Timing.EventTiming.CV);
            timing.setRepeat(trc);
            if (treatRight)this.treatRight(trc,rightPart);
            return timing;
        }
        else if (unit.equalsIgnoreCase("en_desayuno_y_comida")) {

            Timing.TimingRepeatComponent trc = new Timing.TimingRepeatComponent();
            trc.addWhen(Timing.EventTiming.CM);
            trc.addWhen(Timing.EventTiming.CD);
            timing.setRepeat(trc);
            if (treatRight)this.treatRight(trc,rightPart);
            return timing;
        }
        else if (unit.equalsIgnoreCase("en_desayuno_y_cena")) {

            Timing.TimingRepeatComponent trc = new Timing.TimingRepeatComponent();
            trc.addWhen(Timing.EventTiming.CM);
            trc.addWhen(Timing.EventTiming.CV);
            timing.setRepeat(trc);
            if (treatRight)this.treatRight(trc,rightPart);
            return timing;
        }
        else if (unit.equalsIgnoreCase("en_comida_y_cena")) {

            Timing.TimingRepeatComponent trc = new Timing.TimingRepeatComponent();
            trc.addWhen(Timing.EventTiming.CV);
            trc.addWhen(Timing.EventTiming.CD);
            timing.setRepeat(trc);
            if (treatRight)this.treatRight(trc,rightPart);
            return timing;
        }
        else if (unit.equalsIgnoreCase("en desayuno,_comida_y_cena")) {

            Timing.TimingRepeatComponent trc = new Timing.TimingRepeatComponent();
            trc.addWhen(Timing.EventTiming.CM);
            trc.addWhen(Timing.EventTiming.CD);
            trc.addWhen(Timing.EventTiming.CV);
            timing.setRepeat(trc);
            if (treatRight)this.treatRight(trc,rightPart);
            return timing;
        }
        else if (unit.equalsIgnoreCase("cada_semana")) {

            Timing.TimingRepeatComponent trc = new Timing.TimingRepeatComponent();
            trc.setFrequency(1);
            trc.setPeriod(1);
            trc.setPeriodUnit(Timing.UnitsOfTime.WK);
            timing.setRepeat(trc);
            if (treatRight)this.treatRight(trc,rightPart);
            return timing;
        }

        else if (unit.equalsIgnoreCase("cada_quince_días")) {

            Timing.TimingRepeatComponent trc = new Timing.TimingRepeatComponent();
            trc.setFrequency(1);
            trc.setPeriod(2);
            trc.setPeriodUnit(Timing.UnitsOfTime.WK);
            timing.setRepeat(trc);
            if (treatRight)this.treatRight(trc,rightPart);
            return timing;
        }


        else if (unit.equalsIgnoreCase("cada_mes")) {

            Timing.TimingRepeatComponent trc = new Timing.TimingRepeatComponent();
            trc.setFrequency(1);
            trc.setPeriod(1);
            trc.setPeriodUnit(Timing.UnitsOfTime.MO);
            timing.setRepeat(trc);
            if (treatRight)this.treatRight(trc,rightPart);
            return timing;
        }

        else if (unit.equalsIgnoreCase("cada_dos_meses")) {

            Timing.TimingRepeatComponent trc = new Timing.TimingRepeatComponent();
            trc.setFrequency(1);
            trc.setPeriod(2);
            trc.setPeriodUnit(Timing.UnitsOfTime.MO);
            timing.setRepeat(trc);
            if (treatRight)this.treatRight(trc,rightPart);
            return timing;
        }
        else if (unit.equalsIgnoreCase("cada_tres_meses")) {

            Timing.TimingRepeatComponent trc = new Timing.TimingRepeatComponent();
            trc.setFrequency(1);
            trc.setPeriod(3);
            trc.setPeriodUnit(Timing.UnitsOfTime.MO);
            timing.setRepeat(trc);
            return timing;
        }
        else if (unit.equalsIgnoreCase("Semestral")) {

            Timing.TimingRepeatComponent trc = new Timing.TimingRepeatComponent();
            trc.setFrequency(1);
            trc.setPeriod(6);
            trc.setPeriodUnit(Timing.UnitsOfTime.MO);
            timing.setRepeat(trc);
            if (treatRight)this.treatRight(trc,rightPart);
            return timing;
        }
        else if (unit.startsWith("cada_") && unit.endsWith("_horas")){
                Timing.TimingRepeatComponent trc=new Timing.TimingRepeatComponent();
                String hours=StringUtils.substringBetween(unit, "cada_", "_horas");
                trc.setFrequency(1);

                int hoursInt=new Integer(hours).intValue();
                if (hoursInt !=24 && hoursInt !=48 && hoursInt !=72){
                    trc.setPeriod(hoursInt);

                    trc.setPeriodUnit(Timing.UnitsOfTime.H);
                }else {
                    trc.setPeriodUnit(Timing.UnitsOfTime.D);
                    if (hoursInt==24)trc.setPeriod(1);
                    else if (hoursInt==48)trc.setPeriod(2);
                    else if (hoursInt==72)trc.setPeriod(3);

                }

                 timing.setRepeat(trc);
                 if (treatRight)this.treatRight(trc,rightPart);
                 return timing;



        }
        else if (unit.startsWith("cada_") && unit.endsWith("_días")){
            Timing.TimingRepeatComponent trc=new Timing.TimingRepeatComponent();
            String hours=StringUtils.substringBetween(unit, "cada_", "_días");
            trc.setFrequency(1);
            int hoursInt=new Integer(hours).intValue();
            trc.setPeriod(hoursInt);
            trc.setPeriodUnit(Timing.UnitsOfTime.H);
            timing.setRepeat(trc);
            if (treatRight)this.treatRight(trc,rightPart);
            return timing;
        }

        else if (unit.startsWith("cada_") && unit.endsWith("_semanas")){
            Timing.TimingRepeatComponent trc=new Timing.TimingRepeatComponent();
            String hours=StringUtils.substringBetween(unit, "cada_", "_semanas");
            trc.setFrequency(1);
            int hoursInt=new Integer(hours).intValue();
            trc.setPeriod(hoursInt);
            trc.setPeriodUnit(Timing.UnitsOfTime.WK);
            timing.setRepeat(trc);
            if (treatRight)this.treatRight(trc,rightPart);
            return timing;
        }

        else if (unit.startsWith("cada_") && unit.endsWith("_meses")){
            Timing.TimingRepeatComponent trc=new Timing.TimingRepeatComponent();
            String hours=StringUtils.substringBetween(unit, "cada_", "_meses");
            trc.setFrequency(1);
            int hoursInt=new Integer(hours).intValue();
            trc.setPeriod(hoursInt);
            trc.setPeriodUnit(Timing.UnitsOfTime.MO);
            timing.setRepeat(trc);
            if (treatRight)this.treatRight(trc,rightPart);
            return timing;
        }

        else if  (unit.equalsIgnoreCase("en_desayuno,_comida,_merienda_y_cena")){

            Timing.TimingRepeatComponent trc=new Timing.TimingRepeatComponent();
            trc.addWhen(Timing.EventTiming.C);
            timing.setRepeat(trc);
            if (treatRight)this.treatRight(trc,rightPart);
            return timing;
        }
        else if (unit.contains("-")){
            // left part are different depending time/period of the day)
            // exemple A : 1 - 0 - 3 - 0 - 0 COMP   exemple 1
            // exemple B : 1 (08:00) - 1 (12:00) - 1 (19:00) - 1 (22:00) ML

           // determiner exemple A: si apres



            Timing.TimingRepeatComponent trc=new Timing.TimingRepeatComponent();
            StringTokenizer st=new StringTokenizer(unit,"-");
            //int count=st.countTokens();
            while (st.hasMoreTokens()){
                String next=st.nextToken().trim();
                try {
                    int number= new Integer(next).intValue();


                }catch(Exception e){}
            }
            timing.setRepeat(trc);
            if (treatRight)this.treatRight(trc,rightPart);
            return timing;
        }

        return null;

    }
}
