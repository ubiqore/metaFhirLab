package ubiqore.fhir.rest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "wrong xml CDA file")
public class DataOsakiCDAException extends RuntimeException {

}
