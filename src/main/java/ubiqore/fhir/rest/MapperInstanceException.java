package ubiqore.fhir.rest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "MapperInstance is wrong")
public class MapperInstanceException extends RuntimeException {

}
